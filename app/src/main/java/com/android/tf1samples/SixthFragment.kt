package com.android.tf1samples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.tf1samples.customWidgets.CustomNumberPredictionWidget
import com.android.tf1samples.databinding.FragmentSixBinding
import com.livelike.engagementsdk.ContentSession
import com.livelike.engagementsdk.fetchWidgetDetails
import com.livelike.engagementsdk.widget.widgetModel.NumberPredictionFollowUpWidgetModel
import com.livelike.engagementsdk.widget.widgetModel.NumberPredictionWidgetModel


class SixthFragment: Fragment()  {

    private var _binding: FragmentSixBinding? = null
    private lateinit var contentSession: ContentSession
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSixBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSix.setOnClickListener {
            //findNavController().navigate(R.id.action_ThirdFragment_to_ForthFragment)
        }

        val application = activity?.application as Application
        contentSession = application.sdk.createContentSession(programId = "5f0f0a74-3798-47ed-9246-93e48230857b", connectToDefaultChatRoom = false) as ContentSession //create content session with programId
        loadNumberImagePrediction(widgetId = "9ec14c86-54bf-496e-87dd-d8842d6faa10", widgetKind = "image-number-prediction")

    }


    private fun loadNumberImagePrediction(widgetId:String,widgetKind:String) {
        (activity?.application as Application).sdk.fetchWidgetDetails(widgetId,
            widgetKind){result, error ->
            result?.let {
                val viewModel = contentSession.getWidgetModelFromLiveLikeWidget(it) as NumberPredictionWidgetModel
                val isFollowUp = result.followUps?.getOrNull(0)?.status == "published"

                val predictionView = CustomNumberPredictionWidget(requireActivity()).apply {
                    this.numberPredictionWidgetModel = viewModel
                    this.isFollowUp = isFollowUp // Pass the isFollowUp to check ifFollowUp is published
                    if(isFollowUp) {
                        val followUpWidgetViewModel = contentSession.getWidgetModelFromLiveLikeWidget(it) as NumberPredictionFollowUpWidgetModel
                        this.followUpWidgetViewModel = followUpWidgetViewModel
                    }
                }
                binding.rootView.addView(predictionView)


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