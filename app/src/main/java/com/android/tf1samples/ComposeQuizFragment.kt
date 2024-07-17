package com.android.tf1samples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.livelike.engagementsdk.ContentSession

class ComposeQuizFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val application = requireActivity().application as Application
        val contentSession = application.sdk.createContentSession(
            programId = "5f0f0a74-3798-47ed-9246-93e48230857b",
            connectToDefaultChatRoom = false
        ) as ContentSession

        return ComposeView(requireContext()).apply {
            setContent {
                CustomImageQuiz(
                    sdk = application.sdk,
                    contentSession = contentSession,
                    widgetId = "21248032-017d-4101-86c7-3a4693025113",
                    widgetKind = "image-quiz"
                )
            }
        }
    }
}
