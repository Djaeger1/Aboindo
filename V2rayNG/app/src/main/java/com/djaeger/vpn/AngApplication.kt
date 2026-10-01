package com.djaeger.vpn

import android.app.Application
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.Configuration
import androidx.work.WorkManager
import com.djaeger.vpn.AppConfig.ANG_PACKAGE
import com.djaeger.vpn.dto.entities.SubscriptionItem
import com.djaeger.vpn.handler.AngConfigManager
import com.djaeger.vpn.handler.AppLocaleManager
import com.djaeger.vpn.handler.MmkvManager
import com.djaeger.vpn.handler.SettingsManager
import com.djaeger.vpn.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.djaeger.vpn.ui.compose.ThemeManager

class AngApplication : Application() {
    companion object {
        lateinit var application: AngApplication
    }

    /**
     * Attaches the base context to the application.
     * @param base The base context.
     */
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base?.let(ContextCompat::getContextForLanguage))
        application = this
    }

    private val workManagerConfiguration: Configuration = Configuration.Builder()
        .setDefaultProcessName("${ANG_PACKAGE}:bg")
        .build()

    /**
     * Initializes the application.
     */
    override fun onCreate() {
        super.onCreate()

        MmkvManager.initialize(this)

        AppLocaleManager.initialize(this)

        // Initialize WorkManager with the custom configuration
        WorkManager.initialize(this, workManagerConfiguration)

        // Ensure critical preference defaults are present in MMKV early
        SettingsManager.initApp(this)

        // Initialize theme state from MMKV
        ThemeManager.refresh()

        // DJAEGER: preload subscription per negara & protokol saat pertama install
        preloadDjaegerSubscriptions()
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun preloadDjaegerSubscriptions() {
        if (MmkvManager.decodeSubsList().isNotEmpty()) return
        val host = "https://hermes-v2.schatzrasta.workers.dev"
        val countries = listOf(
            "SG" to "Singapura",
            "JP" to "Jepang",
            "US" to "Amerika",
            "DE" to "Jerman",
            "NL" to "Belanda",
        )
        val protocols = listOf("vless" to "VLESS", "trojan" to "Trojan")
        for ((code, name) in countries) {
            for ((proto, protoLabel) in protocols) {
                val remarks = "DJAEGER $name $protoLabel"
                val url = "$host/hermes?country=$code&protocol=$proto"
                MmkvManager.encodeSubscription(Utils.getUuid(), SubscriptionItem(remarks = remarks, url = url))
            }
        }
        // Tarik config langsung di background agar list server terisi
        applicationScope.launch {
            try {
                for (sub in MmkvManager.decodeSubscriptions()) {
                    AngConfigManager.updateConfigViaSub(sub)
                }
            } catch (_: Exception) { }
        }
    }
}
