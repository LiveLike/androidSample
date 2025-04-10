package com.android.tf1samples.customWidgets


import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.tf1samples.R
import com.android.tf1samples.databinding.CustomNumberPredictionItemBinding
import com.bumptech.glide.Glide
import com.livelike.engagementsdk.OptionsItem
import com.livelike.engagementsdk.core.data.models.NumberPredictionVotes

class PredictionListAdapter(
    val list: ArrayList<OptionsItem>
): RecyclerView.Adapter<PredictionListAdapter.PredictionListItemViewHolder>() {

    var predictionMap: HashMap<String, Int> = HashMap()
    var isFollowUp = false
    private var editTextsEnabled = true

    fun setEditTextsEnabled(enabled: Boolean) {
        this.editTextsEnabled = enabled
        notifyDataSetChanged() // Refresh all items to apply the change
    }

    fun getPredictedScore(): HashMap<String, Int> {
        return predictionMap
    }

    fun setFollowUpState(followUp: Boolean) {
        this.isFollowUp = followUp
        notifyDataSetChanged() // Refresh all items to apply the follow-up state
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PredictionListItemViewHolder {
        val itemBinding = CustomNumberPredictionItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PredictionListItemViewHolder(
            itemBinding
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: PredictionListItemViewHolder, position: Int) {
        val item = list[position]
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(
                holder.itemBinding.playerImage
            )

        if (item.number != null) {
            holder.itemBinding.playerScore.setText(item.number.toString())
            // Store initial value in predictionMap
            predictionMap[item.id] = item.number ?: 0
        } else {
            holder.itemBinding.playerScore.hint = "-"
        }

        // Set the EditText's enabled state
        holder.itemBinding.playerScore.isEnabled = editTextsEnabled

        // Apply highlighting for follow-up if needed
        if (isFollowUp && item.correctNumber != null && item.number != null) {
            // Check if the prediction matches the correct answer
            val isCorrect = item.number == item.correctNumber

            if (isCorrect) {
                // Correct prediction - green highlight
                holder.itemBinding.playerScore.setTextColor(Color.parseColor("#46B946"))
                holder.itemBinding.playerScore.setBackgroundResource(R.drawable.edittext_correct_border)
            } else {
                // Incorrect prediction - red highlight
                holder.itemBinding.playerScore.setTextColor(Color.parseColor("#F44336"))
                holder.itemBinding.playerScore.setBackgroundResource(R.drawable.edittext_incorrect_border)
            }
        } else {
            holder.itemBinding.playerScore.setTextColor(Color.BLACK) // Default text color // Default background
            holder.itemBinding.playerScore.setBackgroundResource(R.drawable.edittext_default_border)
        }

        // Set up TextWatcher to update predictionMap when user types
        holder.itemBinding.playerScore.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                val inputText = s.toString()
                if (inputText.isNotEmpty()) {
                    try {
                        val score = inputText.toInt()
                        predictionMap[item.id ?: ""] = score
                    } catch (e: NumberFormatException) {
                        // Handle case where input is not a valid integer
                        // Optionally reset to default or previous value
                    }
                } else {
                    // Handle empty input
                    predictionMap[item.id ?: ""] = 0
                }
            }
        })

        holder.itemBinding.playerImage.visibility = View.VISIBLE
        holder.itemBinding.playerName.text = item.description
    }

    fun setInteractedData(interactedList: List<NumberPredictionVotes>) {
        for (interactedItem in interactedList) {
            // Find matching item in our list by ID
            val matchingItem = list.find { it.id == interactedItem.optionId }
            matchingItem?.let { item ->
                item.number = interactedItem.number
                predictionMap[item.id ?: ""] = interactedItem.number
                Log.d("CustomPredictionWidget", "Updated item ${item.id} with number ${interactedItem.number}")
            }
        }

        // Notify adapter that data has changed
        notifyDataSetChanged()
    }

    class PredictionListItemViewHolder(var itemBinding: CustomNumberPredictionItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)
}