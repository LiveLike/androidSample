package com.android.tf1samples

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.tf1samples.databinding.FragmentReactionPickerBinding
import com.android.tf1samples.databinding.ReactionsPopupViewBinding
import com.livelike.common.profile
import com.livelike.engagementsdk.chat.chatreaction.ReactionPack
import com.livelike.engagementsdk.chat.data.remote.LiveLikePagination
import com.livelike.engagementsdk.createReactionSession
import com.livelike.engagementsdk.publicapis.ErrorDelegate
import com.livelike.engagementsdk.publicapis.LiveLikeUserApi
import com.livelike.engagementsdk.reaction.LiveLikeReactionSession
import com.livelike.engagementsdk.reaction.models.UserReactionCount
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ReactionPickerFragment:Fragment() {

    private var session: LiveLikeReactionSession? = null
    private var reactionSpaceId: String? = null
    private var targetGroupId: String? = null
    private var reactionPackList: List<ReactionPack>? = null
    private var currentReactionPack:ReactionPack?=null

    private var reactionPickerAdapter: ReactionPickerAdapter? = null
    private lateinit var reactionPopupWindow: PopupWindow
    private val reactionPopupAdapter = ReactionAdapter().apply {
        onPopupClose = {
            session?.let { currentReactionPack?.let { it1 -> getUserReactionCount(it, it1) } }
            reactionPopupWindow.dismiss()
        }
    }  //used for popup

    private var currentUser: LiveLikeUserApi? = null
    private lateinit var binding: FragmentReactionPickerBinding
    private val uiScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reactionSpaceId = "cba07b97-0c39-4b9c-827b-41fad1225ab7"// pass your own reaction space id
        targetGroupId = "135f341f-9daf-461c-8c02-239f76aaf85f" // pass the target group id
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReactionPickerBinding.inflate(inflater, container, false)
        return binding.root
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLast.setOnClickListener {
            findNavController().navigate(R.id.action_ReactionPickerFragment_to_FifthFragment)
        }

        reactionPickerAdapter = ReactionPickerAdapter(::openReactionPopup)
        binding.rcylReactionsPicker.adapter = reactionPickerAdapter

        /* create reaction session */
        createReactionSession()

        /* create reaction picker popup */
        createReactionPopup()

        (activity?.application as Application).sdk.profile().profileStream.subscribe(this) { liveLikeUserApi ->
            currentUser = liveLikeUserApi
            reactionPopupAdapter.userId = liveLikeUserApi?.userId

            session?.let { reactionSession ->
                reactionPopupAdapter.session = reactionSession
                reactionSession.getReactionPacks { result, error ->
                    result?.let { list ->
                        reactionPackList = list
                        reactionPackList?.let {
                            currentReactionPack = it[0]
                            setReactionPack(currentReactionPack)
                        }

                    }
                    error?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
          /* observer reaction flows*/
            observerReactionFlows()
        }
    }


    private fun createReactionSession() {
        session = (activity?.application as Application).sdk.createReactionSession(
            reactionSpaceId,
            targetGroupId,
            errorDelegate = object : ErrorDelegate() {
                override fun onError(error: String) {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }


    private fun createReactionPopup(){
        val reactionPopupViewBinding = ReactionsPopupViewBinding.inflate(LayoutInflater.from(context))
        reactionPopupWindow = PopupWindow(
            reactionPopupViewBinding.root,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,  // Changed from MATCH_PARENT to WRAP_CONTENT
            true
        ).apply {
            isOutsideTouchable = false
            elevation = 10f  // Add elevation for a shadow effect
            setBackgroundDrawable(ColorDrawable(Color.WHITE))  // Set a background
        }
        reactionPopupWindow.setBackgroundDrawable(null)
        reactionPopupViewBinding.rcylReactionsPopup.adapter = reactionPopupAdapter

    }


    private fun observerReactionFlows(){
        uiScope.launch {
            launch {
                session?.addUserReactionFlow?.collect { reaction ->
                    val userReactionCount =
                        reactionPickerAdapter?.userReactionCountList?.find { it.reactionId == reaction.reactionId }
                    val index =
                        reactionPickerAdapter?.userReactionCountList?.indexOfFirst { it.reactionId == reaction.reactionId }

                }
            }
            launch {
                session?.removeUserReactionFlow?.collect { reaction ->

                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setReactionPack(reactionPack: ReactionPack?) {
        reactionPack?.let{ pack->
            reactionPopupAdapter.reactionPackId = pack.name
            reactionPopupAdapter.list = ArrayList(pack.emojis)
            reactionPopupAdapter.notifyDataSetChanged()

            reactionPickerAdapter?.list = ArrayList(pack.emojis)
            session?.let { reactionSession ->
                getUserReactionCount(reactionSession, pack)

                reactionSession.getUserReactions(
                    LiveLikePagination.FIRST, reactionById = currentUser?.userId,
                    liveLikeCallback = { result, error ->
                        result?.let {

                            //pop up
                            reactionPopupAdapter.userReactionList = ArrayList(it)
                            reactionPopupAdapter.notifyDataSetChanged()

                            //reaction picker
                            reactionPickerAdapter?.userReactionList = ArrayList(it)
                        }
                        error?.let {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }

    }

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
                    targetUserReactionCount?.reactions
                        ?: emptyList<UserReactionCount>()
                )
                reactionPopupAdapter.notifyDataSetChanged()

                //this is for reaction picker
                reactionPickerAdapter?.userReactionCountList = ArrayList(
                    targetUserReactionCount?.reactions
                        ?: emptyList<UserReactionCount>()
                )
                val totalSum = reactionPickerAdapter?.userReactionCountList?.sumOf { it.count } ?: 0
                reactionPickerAdapter?.setTotalCount(totalSum)
                reactionPickerAdapter?.notifyDataSetChanged()
            }
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun openReactionPopup(view: View) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        // Ensure the popup window is initialized
        if (!::reactionPopupWindow.isInitialized) {
            Log.e("ReactionPopup", "Popup window not initialized")
            return
        }
        // Show the popup
        reactionPopupWindow.showAtLocation(
            view,
            Gravity.NO_GRAVITY,
            location[0],
            location[1] - dpToPx(32f)
        )
    }

    private fun dpToPx(dp: Float): Int {
        val scale = requireContext().resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}