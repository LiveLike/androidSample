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
        private lateinit var contentSession: ContentSession

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

            binding.buttonFirst.setOnClickListener {
                findNavController().navigate(R.id.action_ThirdFragment_to_ForthFragment)
            }

            val application = activity?.application as Application
            contentSession = application.sdk.createContentSession(programId = "65ca0a62-ffa1-4f09-9e16-47c64d8f9d32", connectToDefaultChatRoom = false) as ContentSession

            loadImageQuiz()
   //         loadTextPoll()
    //        loadTextAskWidget()
        }

        fun loadTextAskWidget() {
            (activity?.application as Application).sdk.fetchWidgetDetails("bba359f5-a288-4fd2-b222-fad298dd3391",
                "text-ask"){result, error ->
                result?.let {
                    val viewModel = contentSession.getWidgetModelFromLiveLikeWidget(it) as TextAskWidgetModel
                    val askView = CustomTextAskWidget(requireActivity()).apply {
                        this.askWidgetModel = viewModel
                    }
                    binding.root.addView(askView)
                }
                error?.let {
                    Toast.makeText(activity?.applicationContext, it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun loadTextPoll() {
            (activity?.application as Application).sdk.fetchWidgetDetails("2d7f63cb-0ff0-4f0a-b3cf-81760d48be33",
                "text-poll"){result, error ->
                result?.let {
                    val viewModel = contentSession.getWidgetModelFromLiveLikeWidget(it) as PollWidgetModel
                    val pollView = CustomPollWidget(requireActivity()).apply {
                        this.pollWidgetModel = viewModel
                    }
                    binding.rootView.addView(pollView)
                }
                error?.let {
                    Toast.makeText(activity?.applicationContext, it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun loadImageQuiz() {
            (activity?.application as Application).sdk.fetchWidgetDetails("350e01ca-5c45-4d1c-a0ad-2b75424caab7",
                "image-quiz"){result, error ->
                result?.let {
                    val viewModel = contentSession.getWidgetModelFromLiveLikeWidget(it) as QuizWidgetModel
                    val pollView = CustomQuizWidget(requireActivity()).apply {
                        this.quizWidgetModel = viewModel
                    }
                    binding.rootView.addView(pollView)
                }
                error?.let {
                    Toast.makeText(activity?.applicationContext, it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun loadImageSlider() {
            (activity?.application as Application).sdk.fetchWidgetDetails("2d7f63cb-0ff0-4f0a-b3cf-81760d48be33",
                "emoji-slider"){result, error ->
                result?.let {
                    val viewModel = contentSession.getWidgetModelFromLiveLikeWidget(it) as PollWidgetModel
                    val pollView = CustomPollWidget(requireActivity()).apply {
                        this.pollWidgetModel = viewModel
                    }
                    binding.rootView.addView(pollView)
                }
                error?.let {
                    Toast.makeText(activity?.applicationContext, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }