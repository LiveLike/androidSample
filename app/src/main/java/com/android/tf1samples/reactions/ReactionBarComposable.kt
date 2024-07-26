package com.android.tf1samples.reactions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.livelike.engagementsdk.EngagementSDK

@Composable
fun ReactionBarComposable(
    sdk: EngagementSDK, // pass the sdk instance
    targetGroupId: String?=null,  //either targetGroupId or reactionSpaceId should be passed
    reactionSpaceId: String?=null,
    modifier: Modifier = Modifier
){
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            ReactionBarView(
                context = ctx,
                sdk = sdk,
                targetGroupId = targetGroupId,
                reactionSpaceId = reactionSpaceId
            )
        }
    )

}