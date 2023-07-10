package com.android.tf1samples

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.android.tf1samples.databinding.FragmentFirstBinding
import com.livelike.engagementsdk.LiveLikeWidget
import com.livelike.engagementsdk.fetchWidgetDetails
import com.livelike.engagementsdk.publicapis.LiveLikeCallback
import com.livelike.engagementsdk.widget.viewModel.WidgetStates

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
        val application = activity?.application
        (application as Application).sdk.fetchWidgetDetails("a93edd55-44d0-4c17-a309-2281f4e0ac74",
            "text-poll",
            object : LiveLikeCallback<LiveLikeWidget>() {
                override fun onResponse(result: LiveLikeWidget?, error: String?) {
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
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}