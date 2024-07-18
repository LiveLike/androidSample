package com.android.tf1samples

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.tf1samples.databinding.ReactionPickerItemBinding
import com.android.tf1samples.databinding.ReactionPickerPlaceholderBinding
import com.bumptech.glide.Glide
import com.livelike.engagementsdk.chat.chatreaction.Reaction
import com.livelike.engagementsdk.reaction.LiveLikeReactionSession
import com.livelike.ui.reactions.R


import kotlin.reflect.KFunction1

class ReactionPickerAdapter(private val reactionPlaceHolderClickListener: KFunction1<View, Unit>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(
) {
    var reactionPackId: String? = null
    var list = arrayListOf<Reaction>()
    var userReactionCountList =
        arrayListOf<com.livelike.engagementsdk.reaction.models.UserReactionCount>()
    var userReactionList = arrayListOf<com.livelike.engagementsdk.reaction.models.UserReaction>()
    var session: LiveLikeReactionSession? = null
    var userId: String? = null
    private var totalCount = 0

    fun setTotalCount(totalCount: Int) {
        this.totalCount = totalCount
        notifyDataSetChanged()
    }


    inner class ReactionPickerViewHolder(var itemPickerBinding: ReactionPickerItemBinding) : RecyclerView.ViewHolder(itemPickerBinding.root)

    inner class ReactionPickerPlaceHolder(val pickerPlaceholderBinding: ReactionPickerPlaceholderBinding) :
        RecyclerView.ViewHolder(pickerPlaceholderBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
           VIEW_TYPE_PLACEHOLDER -> {
               ReactionPickerPlaceHolder(
                   ReactionPickerPlaceholderBinding.inflate(
                       LayoutInflater.from(parent.context),
                       parent,
                       false
                   )
               )
            }

            VIEW_TYPE_REACTION -> {
                ReactionPickerViewHolder(
                    ReactionPickerItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_PLACEHOLDER
            else -> VIEW_TYPE_REACTION
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ReactionPickerPlaceHolder -> {
                //holder.pickerPlaceholderBinding.placeholderImage.setImageResource(R.drawable.ic_add_reaction)
                holder.itemView.setOnClickListener(reactionPlaceHolderClickListener)
            }

            is ReactionPickerViewHolder -> {
                val reactionPosition = position - 1
                val reaction = userReactionCountList[reactionPosition]
                val userReaction = list.find { it.id == reaction.reactionId }
                holder.itemPickerBinding.txtReactionCount.visibility = View.GONE
                userReaction?.let { reac ->
                    Glide.with(holder.itemView).load(reac.file).into(holder.itemPickerBinding.imgReaction)
                }
                if (reactionPosition == userReactionCountList.size - 1) {
                    holder.itemPickerBinding.txtReactionCount.visibility = View.VISIBLE
                    holder.itemPickerBinding.txtReactionCount.text = totalCount.toString()
                }
            }
        }
    }

    override fun getItemCount(): Int = userReactionCountList.size + 1



    companion object {
        private const val VIEW_TYPE_PLACEHOLDER = 1
        private const val VIEW_TYPE_REACTION = 2
    }
}

