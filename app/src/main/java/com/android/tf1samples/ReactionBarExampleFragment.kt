package com.android.tf1samples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.tf1samples.composableWrappers.ReactionBarComposable
import com.android.tf1samples.databinding.ReactionBarExampleFragmentBinding

class ReactionBarExampleFragment : Fragment() {

    private var _binding: ReactionBarExampleFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ReactionBarExampleFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val application = requireActivity().application as Application

        // this renders the reaction bar composable wrapper
        binding.composeView.setContent {
            ReactionBarComposable(
                sdk = application.sdk,
                targetGroupId = "135f341f-9daf-461c-8c02-239f76aaf85f",
                reactionSpaceId = "cba07b97-0c39-4b9c-827b-41fad1225ab7"
            )
        }

        binding.buttonClickMe.setOnClickListener {
            findNavController().navigate(R.id.action_ReactionFragment_to_ReactionPickerFragment)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}