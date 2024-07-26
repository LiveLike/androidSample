package com.android.tf1samples.reactions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.tf1samples.databinding.FragmentReactionPickerBinding
import com.android.tf1samples.databinding.ReactionsPopupViewBinding
import com.livelike.common.profile
import com.livelike.engagementsdk.EngagementSDK
import com.livelike.engagementsdk.chat.chatreaction.ReactionPack
import com.livelike.engagementsdk.chat.data.remote.LiveLikePagination
import com.livelike.engagementsdk.createReactionSession
import com.livelike.engagementsdk.publicapis.ErrorDelegate
import com.livelike.engagementsdk.publicapis.LiveLikeUserApi
import com.livelike.engagementsdk.reaction.LiveLikeReactionSession
import com.livelike.engagementsdk.reaction.models.TargetUserReactionCount
import com.livelike.engagementsdk.reaction.models.UserReaction
import com.livelike.engagementsdk.reaction.models.UserReactionCount
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@SuppressLint("ViewConstructor")
class ReactionPickerView(
    context: Context,
    private val sdk: EngagementSDK,
    private val targetGroupId: String?=null, //either targetGroupId or reactionSpaceId should be passed
    private val reactionSpaceId: String?=null
): ConstraintLayout(context)  {

    private lateinit var binding: FragmentReactionPickerBinding
    private var session: LiveLikeReactionSession? = null
    private var reactionPackList: List<ReactionPack>? = null
    private var currentReactionPack: ReactionPack? = null

    private var reactionPickerAdapter: ReactionPickerAdapter? = null
    private lateinit var reactionPopupWindow: PopupWindow
    private val reactionPopupAdapter = ReactionAdapter().apply {
        onPopupClose = {
            session?.let { currentReactionPack?.let { it1 -> getUserReactionCount(it, it1) } }
            reactionPopupWindow.dismiss()
        }
    }

    private var currentUser: LiveLikeUserApi? = null
    private val uiScope = MainScope()

    init {
        setupView()
    }

    private fun setupView() {
        binding = FragmentReactionPickerBinding.inflate(LayoutInflater.from(context), this, true)
        reactionPickerAdapter = ReactionPickerAdapter(::openReactionPopup)
        binding.rcylReactionsPicker.adapter = reactionPickerAdapter

        if(session==null) {
            createReactionSession()
        }
        createReactionPopup()

        sdk.profile().profileStream.subscribe(this) { liveLikeUserApi ->
            currentUser = liveLikeUserApi
            reactionPopupAdapter.userId = liveLikeUserApi?.userId

            session?.let { reactionSession ->
                reactionPopupAdapter.session = reactionSession
                fetchReactionPacks(reactionSession)
            }
            observerReactionFlows()
        }
    }


    private fun createReactionSession(){
        if (targetGroupId == null && reactionSpaceId == null) {
            Log.e("ReactionBar", "Cannot create reaction session: both targetGroupId and reactionSpaceId are null")
            return
        }

        session =
            sdk.createReactionSession(
                reactionSpaceId,
                targetGroupId,
                errorDelegate = object : ErrorDelegate() {
                    override fun onError(error: String) {
                        Log.d("ReactionBar","reaction session error-${error}")
                    }
                }
            )
    }



    private fun observerReactionFlows() {
        uiScope.launch {
            launch {
                session?.addUserReactionFlow?.collect { reaction ->
                    reactionPickerAdapter?.let { adapter ->
                        handleUserReaction(adapter, reaction, isAdd = true)
                    }
                }
            }
            launch {
                session?.removeUserReactionFlow?.collect { reaction ->
                    reactionPickerAdapter?.let { adapter ->
                        handleUserReaction(adapter, reaction, isAdd = false)
                    }
                }
            }
        }
    }


    private fun handleUserReaction(
        adapter: ReactionPickerAdapter,
        reaction: UserReaction,
        isAdd: Boolean
    ) {
       val index = adapter.userReactionCountList.indexOfFirst { it.reactionId == reaction.reactionId }
        val userReactionCount = if (index > -1) adapter.userReactionCountList[index] else null
        val count = userReactionCount?.count ?: 0
        val newCount = if (isAdd) count + 1 else count - 1

        if (userReactionCount != null) {
            adapter.userReactionCountList[index] = userReactionCount.copy(
                selfReactedUserReactionId = if (reaction.reactedById == currentUser?.userId && isAdd) reaction.id else null,
                count = newCount.coerceAtLeast(0)
            )
        } else if (isAdd) {
            adapter.userReactionCountList.add(
                UserReactionCount(
                    reactionId = reaction.reactionId,
                    count = 1,
                    selfReactedUserReactionId = if (reaction.reactedById == currentUser?.userId) reaction.id else null
                )
            )
        }

        setReactionsTotalItemCount()
        adapter.notifyDataSetChanged()
    }


    private fun fetchReactionPacks(reactionSession: LiveLikeReactionSession) {
        reactionSession.getReactionPacks { result, error ->
            result?.let { list ->
                reactionPackList = list
                reactionPackList?.let {
                    currentReactionPack = it[0]
                    setReactionPack(currentReactionPack)
                }
            }
            error?.let {
                Log.d("ReactionPicker", it)
            }
        }
    }

    private fun setReactionPack(reactionPack: ReactionPack?) {
        reactionPack?.let { pack ->
            reactionPopupAdapter.reactionPackId = pack.name
            reactionPopupAdapter.list = ArrayList(pack.emojis)
            reactionPopupAdapter.notifyDataSetChanged()

            reactionPickerAdapter?.list = ArrayList(pack.emojis)
            session?.let { reactionSession ->
                getUserReactionCount(reactionSession, pack)
                getUserReactions(reactionSession)
            }
        }
    }

    /*fetches user reaction list*/
    private fun getUserReactions(reactionSession: LiveLikeReactionSession) {
        reactionSession.getUserReactions(
            LiveLikePagination.FIRST, reactionById = currentUser?.userId,
            liveLikeCallback = { result, error ->
                result?.let {
                    reactionPopupAdapter.userReactionList = ArrayList(it)
                    reactionPopupAdapter.notifyDataSetChanged()

                    reactionPickerAdapter?.userReactionList = ArrayList(it)
                }
                error?.let {
                    Log.d("ReactionPicker","get-user-reaction error-${it}")
                }
            })
    }

    /*fetches reaction count for the user reaction list*/
    private fun getUserReactionCount(
        reactionSession: LiveLikeReactionSession,
        reactionPack: ReactionPack
    ) {
        reactionSession.getUserReactionsCount(
            listOf(reactionPack.name),
            LiveLikePagination.FIRST
        ) { result, error ->
            result?.let { list ->
                val targetUserReactionCount = list.find {
                    it.targetId == reactionPack.name
                }
                reactionPopupAdapter.userReactionCountList = ArrayList(
                    targetUserReactionCount?.reactions ?: emptyList<UserReactionCount>()
                )
                reactionPopupAdapter.notifyDataSetChanged()
                targetUserReactionCount?.let {
                    updateReactionCountList(it)

                    //updates the count in last item
                    val lastIndex = reactionPickerAdapter?.userReactionCountList?.size?.minus(1)
                    if (lastIndex != null && lastIndex >= 0) {
                        reactionPickerAdapter?.notifyItemChanged(lastIndex)
                    }
                }
            }
            error?.let {
                Log.d("ReactionPicker","get-user-reaction-count error-${it}")
            }
        }
    }

    /*updates reaction picker adapter list*/
    private fun updateReactionCountList(targetUserReactionCount: TargetUserReactionCount) {
        reactionPickerAdapter?.userReactionCountList = ArrayList(
            targetUserReactionCount.reactions
        )
        setReactionsTotalItemCount()
    }

    /*gets the total user reaction count and set it to adapter*/
    private fun setReactionsTotalItemCount() {
        val totalSum = reactionPickerAdapter?.userReactionCountList?.sumOf { it.count } ?: 0
        reactionPickerAdapter?.setTotalCount(totalSum)
    }


    /*creates reaction popup view*/
    private fun createReactionPopup() {
        val reactionPopupViewBinding = ReactionsPopupViewBinding.inflate(LayoutInflater.from(context))
        reactionPopupWindow = PopupWindow(
            reactionPopupViewBinding.root,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = false
            elevation = 10f
            setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }
        reactionPopupWindow.setOnDismissListener {
            reactionPickerAdapter?.closePopup()
        }
        reactionPopupWindow.setBackgroundDrawable(null)
        reactionPopupViewBinding.rcylReactionsPopup.adapter = reactionPopupAdapter
    }


       /*opens reaction popup view*/
    private fun openReactionPopup(view: View) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        if (!::reactionPopupWindow.isInitialized) {
            Log.e("ReactionPopup", "Popup window not initialized")
            return
        }

        reactionPopupWindow.contentView.measure(
            MeasureSpec.UNSPECIFIED,
            MeasureSpec.UNSPECIFIED
        )
        val popupHeight = reactionPopupWindow.contentView.measuredHeight

        reactionPopupWindow.showAtLocation(
            view,
            Gravity.NO_GRAVITY,
            location[0],
            location[1] - popupHeight // This aligns the bottom of the popup with the bottom of the button
        )
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        uiScope.cancel()

        // Unsubscribe from the profile stream
        sdk.profile().profileStream.unsubscribe(this)
        session = null
        currentUser = null
        reactionPackList = null
        currentReactionPack = null
        reactionPickerAdapter = null

        reactionPopupAdapter.onPopupClose = null
        reactionPopupAdapter.session = null
        reactionPopupAdapter.userId = null

        if (::reactionPopupWindow.isInitialized) {
            reactionPopupWindow.dismiss()
        }
    }

}