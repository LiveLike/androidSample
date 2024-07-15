package com.android.tf1samples

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.tf1samples.databinding.QuizListItemBinding
import com.bumptech.glide.Glide
import com.livelike.engagementsdk.OptionsItem
import com.livelike.utils.parseISODateTime
import kotlinx.coroutines.flow.MutableStateFlow

//quiz adapter
class QuizListAdapter(
    private val list: ArrayList<OptionsItem>
): RecyclerView.Adapter<QuizListAdapter.QuizItemViewHolder>(){

    private var selectedIndex = -1
    val optionIdCount: HashMap<String, Int> = hashMapOf()
    var quizListener: QuizListener? = null
    var interactiveUntil: String? = null

    val selectionLockedFlow = MutableStateFlow<Boolean>(false)

    interface QuizListener {
        fun onSelectOption(id: String)
    }

    fun getSelectedOption(): OptionsItem? = when (selectedIndex > -1) {
        true -> list[selectedIndex]
        else -> null
    }


    class QuizItemViewHolder(var itemBinding: QuizListItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizItemViewHolder {
        val itemBinding =
            QuizListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return QuizItemViewHolder(itemBinding)
    }

    override fun getItemCount(): Int = list.size


    override fun onBindViewHolder(holder: QuizItemViewHolder, position: Int) {
        val item = list[position]

        if (optionIdCount.containsKey(item.id)) {
            if (selectedIndex > -1) {
                holder.itemBinding.imageBar.visibility = View.VISIBLE
                holder.itemBinding.imagePercentage.visibility = View.VISIBLE
            }

            val total = optionIdCount.values.reduce { acc, i -> acc + i }
            val percent = when (total > 0) {
                true -> (optionIdCount[item.id]!!.toFloat() / total.toFloat()) * 100
                else -> 0F
            }
            holder.itemBinding.imagePercentage.text = "${percent.toInt()}%"
            holder.itemBinding.imageBar.progress = percent.toInt()
        }

        holder.itemBinding.imageText.text = item.description

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.itemBinding.quizImgView)


        //when item selected
        if (selectedIndex == position) {
            holder.itemBinding.imageText.setTextColor(
                ContextCompat.getColor(
                holder.itemView.context,R.color.white))
            holder.itemBinding.quizChildLayout.setBackgroundResource(R.drawable.quiz_answer_selected_background)

        } else {
            holder.itemBinding.imageText.setTextColor(
                ContextCompat.getColor(
                holder.itemView.context,R.color.black))
            holder.itemBinding.quizChildLayout.setBackgroundResource(R.drawable.quiz_answer_default_background)

        }

        //change background based on correct and wrong
        optionIdCount[item.id]?.let {
            if(selectedIndex > -1){
                /*if (selectedIndex == position && item.isCorrect == false){
                    holder.itemBinding.quizChildLayout.setBackgroundResource(R.drawable.quiz_answer_wrong_background)
                    holder.itemBinding.imageText.setTextColor(ContextCompat.getColor(
                        holder.itemView.context,R.color.livelike_quiz_wrong))
                    holder.itemBinding.imagePercentage.setTextColor(ContextCompat.getColor(
                        holder.itemView.context,R.color.livelike_quiz_wrong))

                    holder.itemBinding.imageBar.progressDrawable =
                        ContextCompat.getDrawable(holder.itemView.context, R.drawable.progress_wrong_background)
                }


                if (item.isCorrect == true) {
                    holder.itemBinding.quizChildLayout.setBackgroundResource(R.drawable.quiz_anwer_correct_background)
                    holder.itemBinding.imageText.setTextColor(ContextCompat.getColor(
                        holder.itemView.context,R.color.livelike_quiz_correct))
                    holder.itemBinding.imagePercentage.setTextColor(ContextCompat.getColor(
                        holder.itemView.context,R.color.livelike_quiz_correct))
                    holder.itemBinding.imageBar.progressDrawable =
                        ContextCompat.getDrawable(holder.itemView.context, R.drawable.progress_correct_background)

                }*/

                if(item.isCorrect == true){
                    holder.itemBinding.quizChildLayout.setBackgroundResource(R.drawable.quiz_anwer_correct_background)
                    holder.itemBinding.imageText.setTextColor(
                        ContextCompat.getColor(
                        holder.itemView.context,R.color.livelike_quiz_correct))
                    holder.itemBinding.imagePercentage.setTextColor(
                        ContextCompat.getColor(
                        holder.itemView.context,R.color.livelike_quiz_correct))
                    holder.itemBinding.imageBar.progressDrawable =
                        ContextCompat.getDrawable(holder.itemView.context, R.drawable.progress_correct_background)
                }else{
                    holder.itemBinding.quizChildLayout.setBackgroundResource(R.drawable.quiz_answer_wrong_background)
                    holder.itemBinding.imageText.setTextColor(
                        ContextCompat.getColor(
                        holder.itemView.context,R.color.livelike_quiz_wrong))
                    holder.itemBinding.imagePercentage.setTextColor(
                        ContextCompat.getColor(
                        holder.itemView.context,R.color.livelike_quiz_wrong))

                    holder.itemBinding.imageBar.progressDrawable =
                        ContextCompat.getDrawable(holder.itemView.context, R.drawable.progress_wrong_background)
                }
            }

        }

        holder.itemBinding.quizChildLayout.setOnClickListener {
            //checks expiry interactive until
            val interactiveUntil = interactiveUntil?.parseISODateTime()
            if (interactiveUntil != null) {
                val epochTimeMs = interactiveUntil.toInstant().toEpochMilli()
                if (System.currentTimeMillis() > epochTimeMs) {
                    return@setOnClickListener
                }
            }else if(selectionLockedFlow.value){
                return@setOnClickListener
            }

            selectedIndex = holder.adapterPosition
            //change background for selected
            holder.itemBinding.imageText.setTextColor(
                ContextCompat.getColor(
                holder.itemView.context,R.color.white))
            holder.itemBinding.quizChildLayout.setBackgroundResource(R.drawable.quiz_answer_selected_background)

            quizListener?.onSelectOption(item.id)
        }

    }

    fun restoreSelectedPosition(optionId: String?) {
        optionId?.let { id ->
            selectedIndex = list.indexOfFirst { it.id == id }
            /*if(selectedIndex >  -1){
                val option = list[selectedIndex]
                Log.d("interacted restore",option.answerCount.toString())
                optionIdCount[id] = option.answerCount ?: 0
                Log.d("interaction options",optionIdCount.toString())
            }*/
        }
    }
}