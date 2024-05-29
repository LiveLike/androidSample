        package com.android.tf1samples

        import android.os.Bundle
        import androidx.fragment.app.Fragment
        import android.view.LayoutInflater
        import android.view.View
        import android.view.ViewGroup
        import android.widget.Toast
        import androidx.navigation.fragment.findNavController
        import com.android.tf1samples.databinding.FragmentFirstBinding
        import com.livelike.engagementsdk.fetchWidgetDetails


        /**
         * A simple [Fragment] subclass as the default destination in the navigation.
         */
        class FirstFragment : Fragment() {

            private var _binding: FragmentFirstBinding? = null

            // This property is only valid between onCreateView and
            // onDestroyView.
            private val binding get() = _binding!!

            override fun onCreateView(
                inflater: LayoutInflater, container: ViewGroup?,
                savedInstanceState: Bundle?
            ): View? {

                _binding = FragmentFirstBinding.inflate(inflater, container, false)
                return binding.root

            }

            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                super.onViewCreated(view, savedInstanceState)

                binding.buttonFirst.setOnClickListener {
                    findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                }
                loadTextPoll()
        //        loadTextAskWidget()
            }

            private fun loadTextPoll() {
                val application = activity?.application
                    (application as Application).sdk.fetchWidgetDetails("2d7f63cb-0ff0-4f0a-b3cf-81760d48be33",
                    "text-poll") { result, error ->
                        result?.let {
                            binding.widgetView.displayWidget(
                                application.sdk,
                                result, showWithInteractionData = true
                            )
                        }
                        error?.let {
                            Toast.makeText(activity?.applicationContext, it, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            }

            fun loadTextAskWidget() {
                val application = activity?.application
                (application as Application).sdk.fetchWidgetDetails("151359d2-de10-4e14-aae1-85edc32f50bc",
                    "text-ask"){ result, error ->
                    result?.let {
                        binding.widgetView.displayWidget(
                            application.sdk,
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
            }
        }