package com.android.tf1samples

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.tf1samples.databinding.FragmentReactionBinding
import com.livelike.common.profile
import com.livelike.engagementsdk.EngagementSDK
import com.livelike.engagementsdk.chat.chatreaction.ReactionPack
import com.livelike.engagementsdk.chat.data.remote.LiveLikePagination
import com.livelike.engagementsdk.createReactionSession
import com.livelike.engagementsdk.publicapis.ErrorDelegate
import com.livelike.engagementsdk.publicapis.LiveLikeUserApi
import com.livelike.engagementsdk.reaction.LiveLikeReactionSession
import com.livelike.engagementsdk.reaction.models.UserReactionCount
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@SuppressLint("ViewConstructor")
class ReactionBarView(
    context: Context,
    private val sdk: EngagementSDK,
    private val targetGroupId: String,
    private val reactionSpaceId: String
) : ConstraintLayout(context) {


    private lateinit var binding: FragmentReactionBinding
    private var session: LiveLikeReactionSession? = null
    private var reactionPackList: List<ReactionPack>? = null
    private val adapter = ReactionAdapter()
    private var currentUser: LiveLikeUserApi? = null
    private val uiScope = MainScope()

    init {
        setupView()
    }

    private fun setupView() {
        binding = FragmentReactionBinding.inflate(LayoutInflater.from(context), this, true)
        binding.rcylReactions.adapter = adapter
        if(session==null) {
            createReactionSession()
        }
        setupReactions()
    }


     private fun createReactionSession(){
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


    @SuppressLint("NotifyDataSetChanged")
    private fun setupReactions() {
        sdk.profile().profileStream.subscribe(this) { liveLikeUserApi ->
            currentUser = liveLikeUserApi
            adapter.userId = liveLikeUserApi?.userId
            session?.let { reactionSession ->
                adapter.session = reactionSession
                reactionSession.getReactionPacks { result, error ->
                    result?.let { list ->
                        reactionPackList = list
                        reactionPackList?.let {
                            setReactionPack(it[0])
                        }
                    }
                    error?.let {
                        Log.d("ReactionBar","reaction session error-${it}")
                    }
                }
            }
            //reaction flow
            setupReactionFlows()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setReactionPack(reactionPack: ReactionPack) {
        adapter.reactionPackId = reactionPack.name
        adapter.list = ArrayList(reactionPack.emojis)
        adapter.notifyDataSetChanged()
        session?.let { reactionSession ->
            getUserReactionCount(reactionSession, reactionPack)
            getUserReactions(reactionSession)
        }
    }

    private fun getUserReactions(reactionSession: LiveLikeReactionSession) {
        reactionSession.getUserReactions(
            LiveLikePagination.FIRST, reactionById = currentUser?.userId,
            liveLikeCallback = { result, error ->
                result?.let {
                    adapter.userReactionList = ArrayList(it)
                    adapter.notifyDataSetChanged()
                }
                error?.let {
                    Log.d("ReactionBar","get-user-reaction error-${it}")
                }
            })
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
                adapter.userReactionCountList = ArrayList(
                    targetUserReactionCount?.reactions ?: emptyList<UserReactionCount>()
                )
                adapter.notifyDataSetChanged()
            }
            error?.let {
                Log.d("ReactionBar","user-reaction-count error-${it}")
            }
        }
    }



    private fun setupReactionFlows() {
        uiScope.launch {
            launch {
                session?.addUserReactionFlow?.collect { reaction ->
                    val userReactionCount =
                        adapter.userReactionCountList.find { it.reactionId == reaction.reactionId }
                    val index =
                        adapter.userReactionCountList.indexOfFirst { it.reactionId == reaction.reactionId }

                    if (userReactionCount != null && index > -1) {
                        var count = userReactionCount.count ?: 0
                        count += 1
                        if (reaction.reactedById == currentUser?.userId) {
                            adapter.userReactionCountList[index] =
                                userReactionCount.copy(
                                    selfReactedUserReactionId = reaction.id,
                                    count = count
                                )
                        } else {
                            adapter.userReactionCountList[index] =
                                userReactionCount.copy(
                                    count = count
                                )
                        }
                    } else {
                        adapter.userReactionCountList.add(
                            UserReactionCount(
                                reactionId = reaction.reactionId,
                                count = 1,
                                selfReactedUserReactionId = when (reaction.reactedById == currentUser?.userId) {
                                    true -> reaction.id
                                    else -> null
                                }
                            )
                        )
                    }
                    adapter.notifyDataSetChanged()
                }
            }
            launch {
                session?.removeUserReactionFlow?.collect { reaction ->
                    val index =
                        adapter.userReactionCountList.indexOfFirst { it.reactionId == reaction.reactionId }
                    if (index > -1) {
                        val userReactionCount = adapter.userReactionCountList[index]
                        var count = userReactionCount.count ?: 0
                        count -= 1
                        if (reaction.reactedById == currentUser?.userId) {
                            adapter.userReactionCountList[index] =
                                userReactionCount.copy(
                                    selfReactedUserReactionId = null,
                                    count = when (count >= 0) {
                                        true -> count
                                        else -> 0
                                    }
                                )
                        } else {
                            adapter.userReactionCountList[index] =
                                userReactionCount.copy(
                                    count = when (count >= 0) {
                                        true -> count
                                        else -> 0
                                    }
                                )
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        uiScope.cancel()
        sdk.profile().profileStream.unsubscribe(this)
        session = null
        currentUser = null
        reactionPackList = null
    }
}