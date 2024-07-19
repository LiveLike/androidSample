package com.android.tf1samples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.livelike.engagementsdk.ContentSession

class FifthFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val application = requireActivity().application as Application
        val contentSession = application.sdk.createContentSession(
            programId = "5f0f0a74-3798-47ed-9246-93e48230857b", // pass on your program Id
            connectToDefaultChatRoom = false
        ) as ContentSession

        return ComposeView(requireContext()).apply {
            setContent {
                CustomImageQuiz(
                    sdk = application.sdk,
                    contentSession = contentSession,
                    widgetId = "e9945806-c374-4f79-bfbd-f346d3a4820d", //pass widget Id
                    widgetKind = "image-quiz" //pass widget kind
                )
            }
        }
    }
}
