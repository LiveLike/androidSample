package com.android.tf1samples

import android.os.Handler
import android.os.Looper
import com.livelike.engagementsdk.EngagementSDK
import com.livelike.engagementsdk.core.AccessTokenDelegate
import com.livelike.engagementsdk.publicapis.ErrorDelegate

class Application : android.app.Application() {
    lateinit var sdk: EngagementSDK
    override fun onCreate() {
        super.onCreate()
        initSDK()
    }

    fun initSDK() {
        if (this::sdk.isInitialized)
            sdk.close()
        if (this::sdk.isInitialized)
            println("LiveLikeApplication.initSDK::: $sdk")
        sdk = EngagementSDK(
            "mOBYul18quffrBDuq2IACKtVuLbUzXIPye5S3bq5",
            applicationContext,
            object : ErrorDelegate() {
                override fun onError(error: String) {
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            initSDK()
                        },
                        1000
                    )
                }
            },
            accessTokenDelegate = object : AccessTokenDelegate {
                override fun getAccessToken(): String? {
                    return getSharedPreferences(
                        "Livelike_profile_sharedprefs",
                        MODE_PRIVATE
                    ).getString(
                        "Livelike_profile",
                        null
                    )
                }

                override fun storeAccessToken(accessToken: String?) {
                    println("accessToken = [${accessToken}]")
                    getSharedPreferences("Livelike_profile_sharedprefs", MODE_PRIVATE).edit()
                        .putString(
                            "Livelike_profile", accessToken
                        ).apply()
                }
            },
        )
        println("LiveLikeApplication.initSDK: $sdk")
    }
}
