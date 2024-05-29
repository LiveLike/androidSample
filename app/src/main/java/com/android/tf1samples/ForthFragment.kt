package com.android.tf1samples

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.android.tf1samples.databinding.FragmentForthBinding
import com.android.tf1samples.databinding.FragmentSecondBinding
import com.livelike.engagementsdk.LiveLikeWidget
import com.livelike.engagementsdk.fetchWidgetDetails
import com.livelike.engagementsdk.publicapis.LiveLikeCallback
import com.livelike.engagementsdk.widget.LiveLikeWidgetViewFactory
import com.livelike.engagementsdk.widget.widgetModel.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ForthFragment : Fragment() {

    private var _binding: FragmentForthBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentForthBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_ThirdFragment)
        }

        binding.widgetView.showTimer = false
//        loadTextPoll()
        loadSlider()
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

    private fun loadSlider() {
        (activity?.application as Application).sdk.fetchWidgetDetails("b046a70b-460c-4a2a-a26b-9985461916c7",
            "emoji-slider") { result, error ->
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
        (activity?.application as Application).sdk.fetchWidgetDetails("151359d2-de10-4e14-aae1-85edc32f50bc",
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
    }
}