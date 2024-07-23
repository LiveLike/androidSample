package com.android.tf1samples.composableWrappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

import com.android.tf1samples.ReactionPickerView
import com.livelike.engagementsdk.EngagementSDK

@Composable
fun ReactionPickerComposable(
    sdk: EngagementSDK, // pass the sdk instance
    targetGroupId: String,
    reactionSpaceId: String,
    modifier: Modifier = Modifier
){
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            ReactionPickerView(
                context = ctx,
                sdk = sdk,
                targetGroupId = targetGroupId,
                reactionSpaceId = reactionSpaceId
            )
        }
    )
}