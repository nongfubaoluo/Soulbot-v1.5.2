package com.loveadvisor.android.capture

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import android.app.AlertDialog
import com.loveadvisor.android.AppGraph
import com.loveadvisor.core.api.ProviderPresets
import com.loveadvisor.core.api.Cancellation
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors
import kotlin.math.max

/** Picks a user-provided Soul/Tantan profile screenshot and sends it to the configured vision model. */
class ProfileImagePickerActivity:Activity() {
    private val worker=Executors.newSingleThreadExecutor()
    private var platform="SOUL"
    private var finished=false

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        AlertDialog.Builder(this)
            .setTitle("选择主页平台")
            .setItems(arrayOf("Soul 主页截图","牵手主页截图")) { _,which ->
                platform=if(which==0)"SOUL" else "TANTAN"
                pickImage()
            }
            .setOnCancelListener { finish() }
            .show()
    }

    private fun pickImage() {
        val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type="image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try { startActivityForResult(intent,REQUEST_IMAGE) }
        catch(_:Exception) { Toast.makeText(this,"无法打开图片选择器",Toast.LENGTH_SHORT).show();finish() }
    }

    @Deprecated("Deprecated by Android API; kept for compatibility with the app's minSdk")
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode!=REQUEST_IMAGE)return
        if(resultCode!=RESULT_OK || data?.data==null) { finish();return }
        val uri=data.data!!
        try { contentResolver.takePersistableUriPermission(uri,data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch(_:Exception) {}
        Toast.makeText(this,"正在分析主页截图…",Toast.LENGTH_SHORT).show()
        worker.execute {
            try {
                val graph=AppGraph.get(this)
                val config=graph.settings.load()
                val profile=config.vision.copy(apiKey=config.vision.apiKey.ifBlank {
                    if(ProviderPresets.ownsOpenRouter(config.vision))ProviderPresets.openRouterKey(config.profile,config.vision) else ""
                })
                val image=encodeImage(uri)
                val result=graph.vision.analyzeProfile(profile,image,platform,Cancellation())
                runOnUiThread {
                    if(!finished) {
                        ChatCaptureService.instance?.showProfileAnalysisResult(result)
                            ?: Toast.makeText(this,"分析完成，请重新连接悬浮服务后查看结果",Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            } catch(error:Exception) {
                runOnUiThread {
                    if(!finished) {
                        Toast.makeText(this,error.message?.take(160) ?: "主页分析失败，请检查视觉模型配置",Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun encodeImage(uri:Uri):String {
        val source=contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            ?: error("无法读取所选图片")
        val scale=minOf(1f,1600f/max(source.width,source.height).toFloat())
        val resized=if(scale<1f)Bitmap.createScaledBitmap(source,(source.width*scale).toInt().coerceAtLeast(1),(source.height*scale).toInt().coerceAtLeast(1),true) else source
        val bytes=ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG,82,bytes)
        if(resized!==source)resized.recycle()
        source.recycle()
        require(bytes.size()<=8_500_000) { "图片仍然过大，请先裁剪或压缩截图" }
        return "data:image/jpeg;base64,"+Base64.encodeToString(bytes.toByteArray(),Base64.NO_WRAP)
    }

    override fun onDestroy() { finished=true;worker.shutdownNow();super.onDestroy() }
    companion object { private const val REQUEST_IMAGE=7134 }
}
