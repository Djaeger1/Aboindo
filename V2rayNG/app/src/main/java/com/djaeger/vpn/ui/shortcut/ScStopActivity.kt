package com.djaeger.vpn.ui.shortcut

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.djaeger.vpn.core.CoreServiceManager
import com.djaeger.vpn.core.LauncherManager
import com.djaeger.vpn.ui.base.BaseComponentActivity

class ScStopActivity : BaseComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun ScreenContent() {
        LaunchedEffect(Unit) {
            moveTaskToBack(true)
            if (CoreServiceManager.isRunning()) {
                LauncherManager.stopService(this@ScStopActivity)
            }
            finish()
        }
    }
}
