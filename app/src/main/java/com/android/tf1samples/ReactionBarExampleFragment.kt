package com.android.tf1samples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.android.tf1samples.composableWrappers.ReactionBarComposable

class ReactionBarExampleFragment: Fragment()  {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val application = requireActivity().application as Application
        return ComposeView(requireContext()).apply {
            setContent {
                ReactionBarComposable(
                    sdk = application.sdk,
                    targetGroupId = "135f341f-9daf-461c-8c02-239f76aaf85f",
                    reactionSpaceId = "cba07b97-0c39-4b9c-827b-41fad1225ab7"
                )
            }
        }
    }
}