package com.android.tf1samples.customWidgets

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.tf1samples.R
import com.android.tf1samples.databinding.CustomTextAskBinding
import com.android.tf1samples.databinding.WidgetAskMeAnythingBinding
import com.bumptech.glide.Glide
import com.livelike.engagementsdk.LiveLikeWidget
import com.livelike.engagementsdk.widget.widgetModel.TextAskWidgetModel
import com.livelike.utils.LiveLikeException
import com.livelike.utils.parseISODateTime

class CustomTextAskWidget : ConstraintLayout {
    var askWidgetModel: TextAskWidgetModel? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }


    private lateinit var binding: WidgetAskMeAnythingBinding
    private fun init() {
        binding = WidgetAskMeAnythingBinding.inflate(LayoutInflater.from(context))
        binding.root.setLayoutParams(
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        addView(binding.root)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        askWidgetModel?.widgetData?.let { liveLikeWidget ->
            binding.titleView.text = liveLikeWidget.title
            binding.bodyText.text = liveLikeWidget.prompt
            binding.confirmationMessageTv.text = liveLikeWidget.confirmationMessage
            binding.confirmationMessageTv.visibility = View.GONE

            binding.userInputEdt.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(arg0: Editable) {
                    if (binding.userInputEdt.isEnabled) enableSendBtn() // send button is enabled
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val text: String = binding.userInputEdt.text.toString()
                    if (text.startsWith(" ")) {
                        binding.userInputEdt.setText(text.trim { it <= ' ' })
                    } else {
                        binding.textCount.text = (250 - s.length).toString()
                    }
                    binding.textCount.visibility = View.VISIBLE
                }
            })

            binding.userInputEdt.setOnTouchListener { v, _ -> // Disallow the touch request for parent scroll on touch of child view
                if (binding.userInputEdt.text.toString().isNotEmpty()) {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
                false
            }

            binding.sendBtn.setOnClickListener {
                if (binding.userInputEdt.text.toString().trim().isNotEmpty()) {
                    renderAlreadySubmittedState()
                    askWidgetModel?.submitReply(binding.userInputEdt.text.toString().trim()) { result, error ->
                        result?.let { println(result) }
                        error?.let { println(LiveLikeException(it)) }
                    }
                    hideKeyboard()
                }
            }

            //disables interactivity
            liveLikeWidget.interactiveUntil?.parseISODateTime()?.let {
                val epochTimeMs = it.toInstant().toEpochMilli()
                if (System.currentTimeMillis() > epochTimeMs) {
                    disableSendBtn()
                    disableUserInput()
                    binding.sendBtn.text = context.getString(R.string.ama_expired)
                }
            }
            // by default send button will be disabled
            disableSendBtn()
            setImeOptionDoneInKeyboard()
            getInteractionHistory(liveLikeWidget)
            wouldInflateSponsorUi(liveLikeWidget)

        }
    }

    fun enableSendBtn() {
        val isReady: Boolean = binding.userInputEdt.text.toString().isNotEmpty()
        binding.sendBtn.isEnabled = isReady
    }

    private fun disableSendBtn() {
        binding.sendBtn.isEnabled = false
    }


    private fun disableUserInput() {
        binding.userInputEdt.isFocusableInTouchMode = false
        binding.userInputEdt.isCursorVisible = false
        binding.userInputEdt.clearFocus()
    }

    private fun renderAlreadySubmittedState(){
        disableUserInput()// user input edit text disbaled
        disableSendBtn() // send button disbaled
        binding.sendBtn.text = context.getString(R.string.ama_submitted)
        binding.confirmationMessageTv.visibility = VISIBLE
    }

    /** changes the return key as done in keyboard */
    private fun setImeOptionDoneInKeyboard() {
        binding.userInputEdt.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.userInputEdt.setRawInputType(InputType.TYPE_CLASS_TEXT)
    }

    private fun hideKeyboard() {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(
            binding.userInputEdt.windowToken,
            0
        )
    }

    private fun wouldInflateSponsorUi(widgetData:LiveLikeWidget) {
        widgetData.sponsors?.let {
            if (it.isNotEmpty()) {
                val sponsor = it[0]
                binding.sponsorContainer.visibility =View.VISIBLE
                Glide.with(context).load(sponsor.logoUrl).into(binding.sponsorIv)
            }
        }
    }

    //load previous interaction
    private fun getInteractionHistory(liveLikeWidget: LiveLikeWidget){
        if(askWidgetModel?.getUserInteraction() != null){
            askWidgetModel?.getUserInteraction()?.let {
                renderAlreadySubmittedState()
                binding.userInputEdt.isEnabled = false
                binding.userInputEdt.setText(it.text)
            }
        }else{
            askWidgetModel?.loadInteractionHistory { result, error ->
                result?.let {
                    if(it.isNotEmpty()){
                        renderAlreadySubmittedState()
                        binding.userInputEdt.isEnabled = false
                        for (element in result) {
                            binding.userInputEdt.setText(element.text)
                        }

                    }
                }
            }
        }
        }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }
}