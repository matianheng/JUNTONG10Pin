package com.hndl.ui.systembar

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
import android.view.View
import com.android.internal.widget.LockPatternUtils
import com.android.systemui.plugins.OverlayPlugin
import com.android.systemui.plugins.annotations.Requires

@Requires(target = OverlayPlugin::class, version = OverlayPlugin.VERSION)
class CustomSystemOverlayPlugin : OverlayPlugin {
    private lateinit var sysuiContext: Context
    private lateinit var pluginContext: Context
    private var statusBar: View? = null
    private var navBar: View? = null

    override fun onCreate(sysuiContext: Context, pluginContext: Context) {
        this.sysuiContext = sysuiContext
        this.pluginContext = pluginContext
    }

    override fun setup(statusBar: View?, navBar: View?) {
        this.statusBar = statusBar
        this.navBar = navBar
        this.statusBar?.visibility = View.GONE
        this.navBar?.visibility = View.GONE

        //移除锁屏 statusBar GONE后进入锁屏会异常
        LockPatternUtils(pluginContext).setLockScreenDisabled(true, UserHandle.myUserId())
        kotlin.runCatching {
            Settings.Global.putString(sysuiContext.contentResolver, Settings.Global.POLICY_CONTROL, "immersive.full=*")
        }
    }

    override fun onDestroy() {
        this.statusBar?.visibility = View.VISIBLE
        this.navBar?.visibility = View.VISIBLE
    }
}