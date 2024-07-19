package com.android.tf1samples

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.tf1samples.databinding.CustomQuizWidgetBinding
import com.livelike.engagementsdk.LiveLikeWidget
import com.livelike.engagementsdk.widget.viewModel.QuizViewModel
import com.livelike.engagementsdk.widget.viewModel.WidgetStates
import com.livelike.engagementsdk.widget.widgetModel.QuizWidgetModel
import com.livelike.utils.parseISODateTime
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CustomImageQuizWidget : ConstraintLayout {
    var quizWidgetModel: QuizWidgetModel? = null
    private lateinit var binding: CustomQuizWidgetBinding
    private val uiScope = MainScope()
    private var adapter: QuizListAdapter? = null

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
        binding = CustomQuizWidgetBinding.inflate(LayoutInflater.from(context))
        binding.root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        addView(binding.root)

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        quizWidgetModel?.widgetData?.let { liveLikeWidget ->

           resourceObserver(liveLikeWidget)

                adapter?.let {quizAdapter->
                    (quizWidgetModel as QuizViewModel).widgetStateFlow.value?.let { loadQuizStateFlow(quizAdapter) }

                    //click listener
                    binding.btnLock.setOnClickListener {
                        quizAdapter.getSelectedOption()?.let { item ->
                            if (quizAdapter.optionIdCount.isEmpty()) {
                                quizWidgetModel?.lockInAnswer(item.id)
                                renderResultState()
                            }
                        }
                    }

                    quizAdapter.quizListener = object : QuizListAdapter.QuizListener {
                        override fun onSelectOption(id: String) {
                            enableLockButton()
                        }
                    }

                    //get interaction history
                    getInteractionHistory(liveLikeWidget)
                }
        }
    }

    //render quiz widget
    private fun resourceObserver(liveLikeWidget: LiveLikeWidget){
        binding.txtTitle.text = liveLikeWidget.question
        val attribute = liveLikeWidget.widgetAttributes?.find { it.key == "custom-attribute" }
        binding.quizTitle.text = attribute?.value ?: "QUIZ"

        //disables interactivity
        liveLikeWidget.interactiveUntil?.parseISODateTime()?.let {
            val epochTimeMs = it.toInstant().toEpochMilli()
            if (System.currentTimeMillis() > epochTimeMs) {
                disableLockButton()
                binding.btnLock.text = context.getString(R.string.quiz_expired)
            }
        }

        disableLockButton()
        liveLikeWidget.choices?.let { list ->
            binding.rcylQuizList.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)

            adapter =
                QuizListAdapter(
                    ArrayList(list.map { item -> item })
                )
            binding.rcylQuizList.adapter = adapter
        }
        adapter?.interactiveUntil = liveLikeWidget.interactiveUntil
        (quizWidgetModel as QuizViewModel).widgetStateFlow.value = WidgetStates.READY
    }


    //load states
    private fun loadQuizStateFlow(adapter:QuizListAdapter){
        uiScope.launch {
            (quizWidgetModel as QuizViewModel).widgetStateFlow.collect{ widgetStates->
                when (widgetStates) {
                    WidgetStates.READY -> {
                        disableLockButton()
                        (quizWidgetModel as QuizViewModel).widgetStateFlow.value = WidgetStates.INTERACTING
                    }

                    WidgetStates.INTERACTING -> {
                        enableLockButton()
                    }

                    WidgetStates.RESULTS, WidgetStates.FINISHED ->{
                        quizWidgetModel?.voteResults?.subscribe(this.hashCode()) { result ->
                            val op =
                                result?.choices?.find { option -> option.id == adapter.getSelectedOption()?.id }

                            result?.choices?.let { options ->
                                for (itemOption in options) {
                                    adapter.optionIdCount[itemOption.id] = itemOption.answerCount ?: 0
                                }
                                uiScope.launch {
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                    else -> {

                    }
                }
            }
        }
    }


    //load previous interaction
    private fun getInteractionHistory(liveLikeWidget: LiveLikeWidget){
        if (quizWidgetModel?.getUserInteraction() == null) {
            quizWidgetModel?.loadInteractionHistory { result, error ->
                result?.let {
                    if(it.isNotEmpty()){
                        //this is to set options right & wrong
                        for (itemOption in liveLikeWidget.choices!!) {
                            adapter?.optionIdCount?.set(itemOption.id,
                                itemOption.answerCount ?: 0
                            )
                        }

                        for (element in result) {
                            adapter?.restoreSelectedPosition(element.choiceId) //restores user interaction
                            uiScope.launch {
                                renderResultState()
                                adapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
                error?.let {
                  Log.d("quiz interaction",error)
                }
            }
        }
    }

    private fun renderResultState(){
        disableLockButton()
        adapter?.selectionLockedFlow?.value = true
        (quizWidgetModel as QuizViewModel).widgetStateFlow.value = WidgetStates.RESULTS
    }


    private fun enableLockButton() {
        binding.btnLock.apply {
            isEnabled = true
        }
    }

    private fun disableLockButton() {
        binding.btnLock.apply {
            isEnabled = false
        }
    }
}
