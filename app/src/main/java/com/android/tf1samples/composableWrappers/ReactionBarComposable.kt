package com.android.tf1samples.composableWrappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.android.tf1samples.ReactionBarView
import com.livelike.engagementsdk.EngagementSDK

@Composable
fun ReactionBarComposable(
    sdk: EngagementSDK, // pass the sdk instance
    targetGroupId: String,
    reactionSpaceId: String,
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
            ).apply {
                // You can set up any additional properties or listeners here if needed
            }
        },
        update = { view ->
            // This block is called whenever the composable is recomposed
            // You can update the view here if any of the parameters change
            view.apply {
                // For example, if you need to update the targetGroupId or reactionSpaceId:
                // updateIds(targetGroupId, reactionSpaceId)
            }
        }
    )

}