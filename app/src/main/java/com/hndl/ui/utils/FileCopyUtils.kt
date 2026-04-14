package com.hndl.ui.utils

import android.content.Context
import android.util.Base64
import android.widget.Toast
import com.hndl.ui.adblib.AdbConnection
import com.hndl.ui.adblib.AdbCrypto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.net.Socket

public fun copyBootAnimation(context: Context, name: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val bootanimation = File(context.cacheDir, name).absolutePath
            copyFileFromAssets(context,name, bootanimation)

            val socket = Socket("127.0.0.1", 5555)
            val crypto = AdbCrypto.generateAdbKeyPair { data -> Base64.encodeToString(data, Base64.NO_WRAP) }
            val connection = AdbConnection.create(socket, crypto)
            connection.connect()
            connection.open("remount:")
            connection.open("shell:cp $bootanimation /system/media/bootanimation.zip")
            connection.open("shell:chmod 777 /system/media/bootanimation.zip")
            connection.open("shell:chown root:root /system/media/bootanimation.zip")
            connection.open("shell:chown sync")
            CoroutineScope(Dispatchers.Main).launch {
                Toast
                    .makeText(context, "修改完成", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun copyFileFromAssets(context: Context,assetsFilePath: String, destFilePath: String) {
    context.assets
        .open(assetsFilePath)
        .use { ins ->
            File(destFilePath)
                .outputStream()
                .use { ous ->
                    ins.copyTo(ous)
                }
        }
}
