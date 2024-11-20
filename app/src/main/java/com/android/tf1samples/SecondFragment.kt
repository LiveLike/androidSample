package com.android.tf1samples

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import com.android.tf1samples.customWidgets.CustomImageQuiz
import com.android.tf1samples.customWidgets.CustomPollWidget
import com.android.tf1samples.customWidgets.CustomTextAskComposable
import com.android.tf1samples.customWidgets.CustomTextAskWidget
import com.android.tf1samples.databinding.FragmentSecondBinding
import com.livelike.engagementsdk.ContentSession
import com.livelike.engagementsdk.fetchWidgetDetails
import com.livelike.engagementsdk.widget.LiveLikeWidgetViewFactory
import com.livelike.engagementsdk.widget.widgetModel.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    //private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
//    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

       /* _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root*/

        val application = requireActivity().application as Application
        val contentSession = application.sdk.createContentSession(
            programId = "5f0f0a74-3798-47ed-9246-93e48230857b", // pass on your program Id
            connectToDefaultChatRoom = false
        ) as ContentSession

        return ComposeView(requireContext()).apply {
            setContent {
                CustomTextAskComposable(
                    sdk = application.sdk,
                    contentSession = contentSession,
                    widgetId = "6dedbbe1-4d91-423b-80d3-591b214223a9", //pass widget Id
                    widgetKind = "text-ask" //pass widget kind
                )
            }
        }

    }

    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_ThirdFragment)
        }

//        binding.widgetView.showTimer = false
//        binding.widgetView.enableDefaultWidgetTransition = false
        binding.widgetView.widgetViewFactory = object : LiveLikeWidgetViewFactory {
            override fun createAlertWidgetView(alertWidgetModel: AlertWidgetModel): View? {
                return null
            }

            override fun createCheerMeterView(cheerMeterWidgetModel: CheerMeterWidgetmodel): View? {
                return null
            }

            override fun createImageSliderWidgetView(imageSliderWidgetModel: ImageSliderWidgetModel): View? {
                return null
            }

            override fun createNumberPredictionFollowupWidgetView(
                followUpWidgetViewModel: NumberPredictionFollowUpWidgetModel,
                isImage: Boolean
            ): View? {
                return null
            }

            override fun createNumberPredictionWidgetView(
                numberPredictionWidgetModel: NumberPredictionWidgetModel,
                isImage: Boolean
            ): View? {
                return null
            }

            override fun createPollWidgetView(
                pollWidgetModel: PollWidgetModel,
                isImage: Boolean
            ): View? {
                return activity?.let {
                    CustomPollWidget(it).apply {
                        this.pollWidgetModel = pollWidgetModel
                    }
                }
            }

            override fun createPredictionFollowupWidgetView(
                followUpWidgetViewModel: FollowUpWidgetViewModel,
                isImage: Boolean
            ): View? {
                return null
            }

            override fun createPredictionWidgetView(
                predictionViewModel: PredictionWidgetViewModel,
                isImage: Boolean
            ): View? {
                return null
            }

            override fun createQuizWidgetView(
                quizWidgetModel: QuizWidgetModel,
                isImage: Boolean
            ): View? {
                return null
            }

            override fun createSocialEmbedWidgetView(socialEmbedWidgetModel: SocialEmbedWidgetModel): View? {
                return null
            }

            override fun createTextAskWidgetView(askWidgetViewModel: TextAskWidgetModel): View? {
                return activity?.let {
                    CustomTextAskWidget(it).apply {
                        this.askWidgetModel = askWidgetViewModel
                    }
                }
            }

            override fun createVideoAlertWidgetView(videoAlertWidgetModel: VideoAlertWidgetModel): View? {
                return null
            }

        }

        //loadTextPoll()
        loadTextAskWidget()
    }

    private fun loadTextPoll() {
        (activity?.application as Application).sdk.fetchWidgetDetails("2d7f63cb-0ff0-4f0a-b3cf-81760d48be33",
            "text-poll") { result, error ->
            result?.let {
                binding.widgetView.displayWidget(
                    (activity?.application as Application).sdk,
                    result, showWithInteractionData = true
                )
            }
            error?.let {
                Toast.makeText(activity?.applicationContext, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun loadTextAskWidget() {
        (activity?.application as Application).sdk.fetchWidgetDetails("6dedbbe1-4d91-423b-80d3-591b214223a9",
            "text-ask"){result, error ->
            result?.let {
                binding.widgetView.displayWidget(
                    (activity?.application as Application).sdk,
                    result, showWithInteractionData = true
                )
            }
            error?.let {
                Toast.makeText(activity?.applicationContext, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }*/
}