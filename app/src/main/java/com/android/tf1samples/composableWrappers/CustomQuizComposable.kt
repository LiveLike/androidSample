package com.android.tf1samples.composableWrappers

import android.view.View
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.tf1samples.customWidgets.CustomImageQuizWidget
import com.livelike.engagementsdk.ContentSession
import com.livelike.engagementsdk.EngagementSDK
import com.livelike.engagementsdk.fetchWidgetDetails
import com.livelike.engagementsdk.widget.widgetModel.QuizWidgetModel


@Composable
fun CustomImageQuiz(
    sdk: EngagementSDK, // pass the sdk instance
    contentSession: ContentSession, //pass content session instance after creating it with program Id
    widgetId: String,
    widgetKind: String
) {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black)
    ){
        Surface(
            modifier = Modifier.fillMaxSize().padding(top=40.dp),
            color = Color.Black
        ) {
            val context = LocalContext.current
            val rootView = remember { mutableStateOf<View?>(null) }

            LaunchedEffect(Unit) {
                sdk.fetchWidgetDetails(
                    widgetId, widgetKind
                ) { result, error ->
                    result?.let {
                        val viewModel = contentSession.getWidgetModelFromLiveLikeWidget(it) as QuizWidgetModel
                        val quizView = CustomImageQuizWidget(context).apply {
                            this.quizWidgetModel = viewModel
                        }
                        rootView.value = quizView
                    }
                    error?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            rootView.value?.let { view ->
                AndroidView(factory = { view })
            }
        }
    }

}

