package com.android.tf1samples.reactions

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

import com.livelike.engagementsdk.EngagementSDK

@Composable
fun ReactionPickerComposable(
    sdk: EngagementSDK, // pass the sdk instance
    targetGroupId: String?=null, //either targetGroupId or reactionSpaceId should be passed
    reactionSpaceId: String?=null,
    modifier: Modifier = Modifier
){
    AndroidView(
        modifier = modifier.padding(top = 50.dp),
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