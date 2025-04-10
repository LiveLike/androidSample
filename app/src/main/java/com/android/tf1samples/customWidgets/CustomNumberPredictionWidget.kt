package com.android.tf1samples.customWidgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.tf1samples.databinding.CustomNumberPredictionWidgetBinding
import com.livelike.engagementsdk.core.data.models.NumberPredictionVotes
import com.livelike.engagementsdk.widget.widgetModel.NumberPredictionFollowUpWidgetModel
import com.livelike.engagementsdk.widget.widgetModel.NumberPredictionWidgetModel
import kotlinx.coroutines.MainScope

class CustomNumberPredictionWidget: ConstraintLayout  {

    var numberPredictionWidgetModel: NumberPredictionWidgetModel? = null
    var followUpWidgetViewModel: NumberPredictionFollowUpWidgetModel? = null
    private lateinit var binding: CustomNumberPredictionWidgetBinding
    private val uiScope = MainScope()
    private var adapter: PredictionListAdapter? = null
    var isFollowUp = false

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        binding = CustomNumberPredictionWidgetBinding.inflate(LayoutInflater.from(context))
        binding.root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        addView(binding.root)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        var widgetData = numberPredictionWidgetModel?.widgetData
        if (isFollowUp) {
            disableInteractions()
            widgetData = followUpWidgetViewModel?.widgetData
        }else {

            enableInteractions()
        }

        widgetData?.let { liveLikeWidget ->
            liveLikeWidget.options?.let { option ->
                adapter =
                    PredictionListAdapter(
                        ArrayList(option.map { item -> item })
                    )
                binding.rcylPredictionList.adapter = adapter
                binding.txt.text = liveLikeWidget.question
                if(!isFollowUp) {
                    setOnClickListeners()
                }
                getInteractionHistory(adapter!!)

                if (adapter != null){
                    if (isFollowUp) {
                        val votedList = followUpWidgetViewModel?.getPredictionVotes()
                        for (op in votedList ?: emptyList()) {
                            adapter!!.predictionMap[op.optionId ?: ""] = op.number
                        }
                        adapter!!.isFollowUp = true
                        adapter!!.setFollowUpState(true)
                        disableInteractions()
                        verifyPredictedAnswer()
                    } else {
                        binding.resultTv.visibility = GONE
                        adapter!!.setFollowUpState(false)
                    }
            }
            }

        }
    }


    //get user interacted data from load history api
    private fun getInteractionHistory(adapter: PredictionListAdapter) {
        if(isFollowUp){
            followUpWidgetViewModel?.loadInteractionHistory { result, error ->
                result?.let {
                    if (it.isNotEmpty()) {
                        val interaction = it[0]
                        disableInteractions()
                        Log.d(
                            "CustomPredictionWidget",
                            "CustomNoPredictionWidget.historyonResponse>>${interaction.votes}"
                        )
                        interaction.votes?.let { scores ->
                            adapter.setInteractedData(scores)
                        }
                    }
                }
            }
        }else {
            numberPredictionWidgetModel?.loadInteractionHistory { result, error ->
                result?.let {
                    if (it.isNotEmpty()) {
                        val interaction = it[0]
                        disableInteractions()
                        Log.d(
                            "CustomPredictionWidget",
                            "CustomNoPredictionWidget.historyonResponse>>${interaction.votes}"
                        )
                        interaction.votes?.let { scores ->
                            adapter.setInteractedData(scores)
                        }
                    }
                }
            }
        }
    }


    private fun setOnClickListeners() {
        binding.submitBtn.setOnClickListener {
            if (!isFollowUp) {
                adapter?.let{ adap->
                    val optionList = submitVoteRequest(adap)
                    numberPredictionWidgetModel?.lockInVote(optionList).apply {
                        Toast.makeText(context, "score submitted", Toast.LENGTH_SHORT).show()
                        disableInteractions()
                    }
                }

            }
        }
    }


    private fun submitVoteRequest(adapter: PredictionListAdapter): List<NumberPredictionVotes> {
        val optionList = mutableListOf<NumberPredictionVotes>()
        val maps = adapter.getPredictedScore()
        if (maps.isNullOrEmpty()) {
            val options = numberPredictionWidgetModel?.widgetData?.options
            for (item in options!!) {
                optionList.add(
                    NumberPredictionVotes(
                        optionId = item?.id,
                        number = item.number ?: 0
                    )
                )
            }
        } else {
            for (item in maps) {
                optionList.add(
                    NumberPredictionVotes(
                        optionId = item.key,
                        number = item.value
                    )
                )
            }
        }
        return optionList
    }


    private fun verifyPredictedAnswer() {
        @Suppress("UnusedDeclaration")
        var isCorrect = false
        followUpWidgetViewModel?.widgetData?.options?.let { option ->
            val votedList = followUpWidgetViewModel?.getPredictionVotes()
            if (option.size == votedList?.size) {
                for (i in votedList.indices) {
                    val votedOption = votedList[i]

                    val op = option.find { it?.id == votedOption.optionId }
                    isCorrect = op != null && votedOption.number == op.correctNumber
                }
            }
        }
    }

    private fun enableLockButton() {
        binding.submitBtn.apply {
            isEnabled = true
        }
    }

    private fun disableLockButton() {
        binding.submitBtn.apply {
            isEnabled = false
        }
    }

    private fun disableInteractions() {
        // Disable the submit button
        disableLockButton()

        // Disable EditTexts via adapter
        // Disable EditTexts via adapter
        adapter?.setEditTextsEnabled(false)
    }

    private fun enableInteractions() {
        // Enable the submit button
        enableLockButton()

        // Enable EditTexts via adapter
        adapter?.setEditTextsEnabled(true)
    }
}