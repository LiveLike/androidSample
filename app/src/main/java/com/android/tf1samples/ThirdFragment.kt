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
            contentSession = application.sdk.createContentSession(programId = "5f0f0a74-3798-47ed-9246-93e48230857b", connectToDefaultChatRoom = false) as ContentSession //create content session with programId

            loadImageQuiz(widgetId = "21248032-017d-4101-86c7-3a4693025113", widgetKind = "image-quiz")  //pass your own widgetId and widgetKind
        }


        private fun loadImageQuiz(widgetId:String,widgetKind:String) {
            (activity?.application as Application).sdk.fetchWidgetDetails(widgetId,
                widgetKind){result, error ->
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

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }