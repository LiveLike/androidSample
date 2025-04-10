package com.android.tf1samples

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.tf1samples.customWidgets.CustomImageQuizWidget
import com.android.tf1samples.customWidgets.CustomNumberPredictionWidget
import com.android.tf1samples.databinding.FragmentSixBinding
import com.android.tf1samples.databinding.FragmentThirdBinding
import com.google.gson.Gson
import com.livelike.engagementsdk.ContentSession
import com.livelike.engagementsdk.fetchWidgetDetails
import com.livelike.engagementsdk.widget.widgetModel.NumberPredictionWidgetModel
import com.livelike.engagementsdk.widget.widgetModel.QuizWidgetModel

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
        loadNumberImagePrediction(widgetId = "8dd6ee64-0ca8-427f-9117-95dab6b53ecc", widgetKind = "image-number-prediction")

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