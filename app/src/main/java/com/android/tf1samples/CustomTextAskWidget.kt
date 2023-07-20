package com.android.tf1samples

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.tf1samples.databinding.CustomTextAskBinding
import com.livelike.engagementsdk.widget.widgetModel.TextAskWidgetModel

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


    private lateinit var binding: CustomTextAskBinding
    private fun init() {
        binding = CustomTextAskBinding.inflate(LayoutInflater.from(context))
        binding.root.setLayoutParams(
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        addView(binding.root)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        askWidgetModel?.widgetData?.let { liveLikeWidget ->
            binding.titleView.text = liveLikeWidget.title
            binding.bodyText.text = liveLikeWidget.prompt
            binding.confirmationMessageTv.text = liveLikeWidget.confirmationMessage
            binding.confirmationMessageTv.visibility = View.GONE

            binding.inputTxt.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(arg0: Editable) {
                    if (binding.inputTxt.isEnabled) enableSendBtn() // send button is enabled
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            })

            binding.sendBtn.setOnClickListener {
                if (binding.inputTxt.text.toString().trim().isNotEmpty()) {
                    disableUserInput()// user input edit text disbaled
                    disableSendBtn() // send button disbaled
                    askWidgetModel?.submitReply(binding.inputTxt.text.toString().trim())
                    hideKeyboard()
                    binding.confirmationMessageTv.visibility = ConstraintLayout.VISIBLE
                }
            }

        }
    }

    fun enableSendBtn() {
        val isReady: Boolean = binding.inputTxt.text.toString().isNotEmpty()
        binding.sendBtn.isEnabled = isReady
    }

    private fun disableSendBtn() {
        binding.sendBtn.isEnabled = false
    }


    private fun disableUserInput() {
        binding.inputTxt.isFocusableInTouchMode = false
        binding.inputTxt.isCursorVisible = false
        binding.inputTxt.clearFocus()
    }

    private fun hideKeyboard() {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(
            binding.inputTxt.windowToken,
            0
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }
}