package com.android.tf1samples.reactions

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.tf1samples.R
import com.android.tf1samples.databinding.ReactionPackChildBinding
import com.bumptech.glide.Glide
import com.livelike.engagementsdk.chat.chatreaction.Reaction
import com.livelike.engagementsdk.reaction.LiveLikeReactionSession
import java.util.Locale


class ReactionAdapter : RecyclerView.Adapter<ReactionAdapter.ReactionViewHolder>() {
    var reactionPackId: String? = null
    var list = arrayListOf<Reaction>()
    var userReactionCountList =
        arrayListOf<com.livelike.engagementsdk.reaction.models.UserReactionCount>()
    var userReactionList = arrayListOf<com.livelike.engagementsdk.reaction.models.UserReaction>()
    var session: LiveLikeReactionSession? = null
    var userId: String? = null

    var onPopupClose: (() -> Unit)? = null

    inner class ReactionViewHolder(var itemBinding: ReactionPackChildBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionViewHolder {
        val itemBinding = ReactionPackChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReactionViewHolder(itemBinding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ReactionViewHolder, position: Int) {
        val reaction = list[position]
        Glide.with(holder.itemView).load(reaction.file).into(holder.itemBinding.imgReaction)
        val userReactionCount = userReactionCountList.find { it.reactionId == reaction.id }
        val userReaction = userReactionList.find { it.reactionId == reaction.id }
        holder.itemBinding.txtReactionCount.text = formatCount(userReactionCount?.count ?: 0)
        if (userReaction?.reactedById == userId) {
            holder.itemBinding.reactionItemLayout.setBackgroundResource(R.drawable.reaction_background_corner_selected)
        } else {
            holder.itemBinding.reactionItemLayout.setBackgroundResource(R.drawable.reaction_background_with_corner)
        }

        holder.itemView.setOnClickListener {
            session?.let { reactionSession ->
                if (userReaction != null) {
                    userReaction.id.let { userReactionId ->
                        reactionSession.removeUserReaction(
                            userReactionId
                        ) { result, error ->
                            result?.let {
                                userReactionList.removeAll { it.id == userReaction.id }
                                notifyDataSetChanged()
                                onPopupClose?.invoke()
                            }
                            error?.let {
                                Toast.makeText(
                                    holder.itemView.context,
                                    it,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    reactionSession.addUserReaction(
                        targetId = reactionPackId!!,
                        reactionId = reaction.id,
                        liveLikeCallback = { result, error ->
                            result?.let {
                                userReactionList.add(it)
                                notifyDataSetChanged()
                                onPopupClose?.invoke()
                            }
                            error?.let {
                                Toast.makeText(holder.itemView.context, it, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        })
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size

    //this is for larger counts
    private fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", count / 1_000_000.0)
            count >= 1_000 ->  String.format(Locale.getDefault(), "%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }
}