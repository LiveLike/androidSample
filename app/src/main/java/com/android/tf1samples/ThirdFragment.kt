package com.android.tf1samples

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.android.tf1samples.databinding.FragmentSecondBinding
import com.android.tf1samples.databinding.FragmentThirdBinding
import com.livelike.common.LiveLikeKotlin
import com.livelike.engagementsdk.*
import com.livelike.engagementsdk.publicapis.LiveLikeCallback
import com.livelike.engagementsdk.widget.LiveLikeWidgetViewFactory
import com.livelike.engagementsdk.widget.widgetModel.*
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ThirdFragment : Fragment() {

    private var _binding: FragmentThirdBinding? = null
    lateinit var contentSession: ContentSession

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentThirdBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val application = activity?.application as Application
        contentSession = application.sdk.createContentSession(
            programId = "086a57ea-e082-4cd6-a52c-48ab2bbd4ca4",
            timecodeGetter = object : LiveLikeKotlin.TimecodeGetterCore {
                override fun getTimecode(): EpochTime {
                    return EpochTime(0)
                }

            },
            isPlatformLocalContentImageUrl = { false },
            uiDispatcher = Dispatchers.Default
        ) as ContentSession

        loadTextPoll()

        loadTextAskWidget()
    }

    fun loadTextAskWidget() {
        (activity?.application as Application).sdk.fetchWidgetDetails("151359d2-de10-4e14-aae1-85edc32f50bc",
            "text-ask",
            object : LiveLikeCallback<LiveLikeWidget>() {
                override fun onResponse(result: LiveLikeWidget?, error: String?) {
                    result?.let {
                        val viewModel = contentSession.getWidgetModelFromLiveLikeWidget(it) as TextAskWidgetModel
                        val askView = CustomTextAskWidget(activity!!).apply {
                            this.askWidgetModel = viewModel
                        }
                        binding.root.addView(askView)
                    }
                    error?.let {
                        Toast.makeText(activity?.applicationContext, it, Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    fun loadTextPoll() {
        (activity?.application as Application).sdk.fetchWidgetDetails("a93edd55-44d0-4c17-a309-2281f4e0ac74",
            "text-poll",
            object : LiveLikeCallback<LiveLikeWidget>() {
                override fun onResponse(result: LiveLikeWidget?, error: String?) {
                    result?.let {
                        val viewModel = contentSession.getWidgetModelFromLiveLikeWidget(it) as PollWidgetModel
                        val pollView = CustomPollWidget(activity!!).apply {
                            this.pollWidgetModel = viewModel
                        }
                        binding.root.addView(pollView)
                    }
                    error?.let {
                        Toast.makeText(activity?.applicationContext, it, Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}