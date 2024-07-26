package com.android.tf1samples.reactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.tf1samples.databinding.ReactionPickerItemBinding
import com.android.tf1samples.databinding.ReactionPickerPlaceholderBinding
import com.bumptech.glide.Glide
import com.livelike.engagementsdk.chat.chatreaction.Reaction
import com.livelike.engagementsdk.reaction.models.UserReactionCount
import com.livelike.engagementsdk.reaction.models.UserReaction


import kotlin.reflect.KFunction1

class ReactionPickerAdapter(private val reactionPlaceHolderClickListener: KFunction1<View, Unit>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(
) {
    var list = arrayListOf<Reaction>()
    var userReactionCountList =
        arrayListOf<UserReactionCount>()
    var userReactionList = arrayListOf<UserReaction>()
    private var totalCount = 0
    private var isPopupOpen = false


    fun setTotalCount(totalCount: Int) {
        this.totalCount = totalCount
    }


    inner class ReactionPickerViewHolder(var itemPickerBinding: ReactionPickerItemBinding) : RecyclerView.ViewHolder(itemPickerBinding.root)

    inner class ReactionPickerPlaceHolder(private val pickerPlaceholderBinding: ReactionPickerPlaceholderBinding) :
        RecyclerView.ViewHolder(pickerPlaceholderBinding.root){

        fun updateState(isSelected: Boolean) {
            pickerPlaceholderBinding.placeholderImage.isSelected = isSelected
        }
        }

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
                holder.updateState(isPopupOpen)
                holder.itemView.setOnClickListener { view ->
                    isPopupOpen = true
                    holder.updateState(true)
                    reactionPlaceHolderClickListener(view)
                }
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
                    holder.itemPickerBinding.txtReactionCount.text = formatCount(totalCount)
                }
            }
        }
    }

    override fun getItemCount(): Int = userReactionCountList.size + 1


    //this is for larger counts
    private fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }

    fun closePopup() {
        isPopupOpen = false
        notifyItemChanged(0)  // Update the placeholder item
    }



    companion object {
        private const val VIEW_TYPE_PLACEHOLDER = 1
        private const val VIEW_TYPE_REACTION = 2
    }
}

