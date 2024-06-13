package com.android.tf1samples

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.tf1samples.databinding.FragmentReactionBinding
import com.google.android.material.tabs.TabLayout
import com.livelike.common.profile
import com.livelike.engagementsdk.chat.chatreaction.ReactionPack
import com.livelike.engagementsdk.chat.data.remote.LiveLikePagination
import com.livelike.engagementsdk.createReactionSession
import com.livelike.engagementsdk.publicapis.ErrorDelegate
import com.livelike.engagementsdk.publicapis.LiveLikeUserApi
import com.livelike.engagementsdk.reaction.LiveLikeReactionSession
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ReactionFragment : Fragment() {

    private var session: LiveLikeReactionSession? = null
    private var reactionSpaceId: String? = null
    private var targetGroupId: String? = null
    private var reactionPackList: List<ReactionPack>? = null
    private val adapter = ReactionAdapter()
    private var currentUser: LiveLikeUserApi? = null
    private lateinit var binding: FragmentReactionBinding
    private val uiScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reactionSpaceId = "cba07b97-0c39-4b9c-827b-41fad1225ab7"//it.getString(ARG_PARAM1)
        targetGroupId = "135f341f-9daf-461c-8c02-239f76aaf85f"

//        arguments?.let {
//            reactionSpaceId = "cba07b97-0c39-4b9c-827b-41fad1225ab7"//it.getString(ARG_PARAM1)
//            targetGroupId = "135f341f-9daf-461c-8c02-239f76aaf85f" //it.getString(ARG_PARAM2)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rcylReactions.adapter = adapter
        session =
            (activity?.application as Application).sdk.createReactionSession(
                reactionSpaceId,
                targetGroupId,
                errorDelegate = object : ErrorDelegate() {
                    override fun onError(error: String) {
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                }
            )

        (activity?.application as Application).sdk.profile().profileStream.subscribe(this) { liveLikeUserApi ->
            currentUser = liveLikeUserApi
            adapter.userId = liveLikeUserApi?.userId
            session?.let { reactionSession ->
                adapter.session = reactionSession
                reactionSession.getReactionPacks { result, error ->
                    result?.let { list ->
                        for (it in list) {
                            binding.reactionTabs.addTab(
                                binding.reactionTabs.newTab().setText(it.name).setTag(it)
                            )
                        }
                        reactionPackList = list
                        binding.reactionTabs.getTabAt(0)?.let { tab ->
                            setReactionPack((tab.tag as ReactionPack))
                        }
                        binding.reactionTabs.addOnTabSelectedListener(object :
                            TabLayout.OnTabSelectedListener {
                            override fun onTabSelected(tab: TabLayout.Tab?) {
                                tab?.let { tab1 ->
                                    setReactionPack((tab1.tag as ReactionPack))
                                }
                            }

                            override fun onTabUnselected(tab: TabLayout.Tab?) {

                            }

                            override fun onTabReselected(tab: TabLayout.Tab?) {

                            }

                        })

                    }
                    error?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
                                com.livelike.engagementsdk.reaction.models.UserReactionCount(
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
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setReactionPack(reactionPack: ReactionPack) {
        adapter.reactionPackId = reactionPack.name
        adapter.list = ArrayList(reactionPack.emojis)
        adapter.notifyDataSetChanged()
        session?.let { reactionSession ->
            binding.reactionView.setSession(reactionSession, null)
            binding.reactionView.setTargetId(reactionPack.name)
            reactionSession.getUserReactionsCount(
                listOf(reactionPack.name),
                LiveLikePagination.FIRST
            ) { result, error ->
                result?.let { list ->
                    val targetUserReactionCount = list.find {
                        it.targetId == reactionPack.name
                    }
                    adapter.userReactionCountList = ArrayList(
                        targetUserReactionCount?.reactions
                            ?: emptyList<com.livelike.engagementsdk.reaction.models.UserReactionCount>()
                    )
                    adapter.notifyDataSetChanged()
                }
                error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
            reactionSession.getUserReactions(
                LiveLikePagination.FIRST, reactionById = currentUser?.userId,
                liveLikeCallback = { result, error ->
                    result?.let {
                        adapter.userReactionList = ArrayList(it)
                        adapter.notifyDataSetChanged()
                    }
                    error?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(reactionSpaceId: String, targetGroupId: String) =
            ReactionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, reactionSpaceId)
                    putString(ARG_PARAM2, targetGroupId)
                }
            }
    }
}