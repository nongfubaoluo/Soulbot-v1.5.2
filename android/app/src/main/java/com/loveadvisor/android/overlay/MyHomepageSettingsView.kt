package com.loveadvisor.android.overlay

import android.content.Context
import android.text.InputType
import android.widget.*
import com.loveadvisor.android.AppGraph
import com.loveadvisor.android.capture.ChatCaptureService
import com.loveadvisor.android.ui.ViewStyles

/** Personal profile storage and opt-in Soul-match controls inside the floating panel. */
class MyHomepageSettingsView(
    context:Context,
    private val graph:AppGraph,
    private val onReadAndSave:()->Unit,
    private val onBack:()->Unit
):LinearLayout(context),PanelViewportPage {
    override val preferredViewportHeightDp=540
    private val palette=PanelPalette(context)
    private val autoMatch=CheckBox(context)
    private val autoSend=CheckBox(context)
    private val threshold=EditText(context)
    private val summary=TextView(context)
    init {
        orientation=VERTICAL
        addView(palette.text("我的主页分析",19f,bold=true),row(8))
        addView(palette.text("先打开 Soul 或牵手的本人主页，再点击下方按钮。资料会保存在本机，用于之后与对方主页进行兴趣和话题匹配。",12f,palette.muted),row(12))
        autoMatch.text="启用自动灵魂匹配"
        autoMatch.isChecked=graph.settings.load().autoSoulMatchEnabled
        autoMatch.setTextColor(palette.ink);addView(autoMatch,row(4))
        addView(palette.text("匹配度达到设定值后，自动返回聊天页面并填入推荐开场白。",11f,palette.muted),row(6))
        addView(palette.text("匹配度阈值（0–100）",12f,palette.ink,true),row(4))
        threshold.apply {
            inputType=InputType.TYPE_CLASS_NUMBER
            setSingleLine(true);setText(graph.settings.load().soulMatchThreshold.toString())
            setTextColor(palette.ink);setHintTextColor(palette.muted);textSize=14f
            setPadding(dp(12),dp(10),dp(12),dp(10));background=ViewStyles.shape(context,palette.card,12f,palette.border)
        }
        addView(threshold,row(8))
        autoSend.text="达到阈值后，自动点击发送（谨慎开启）"
        autoSend.isChecked=graph.settings.load().soulMatchAutoSend
        autoSend.setTextColor(palette.ink);addView(autoSend,row(6))
        addView(palette.text("自动发送默认关闭。开启后，仅在输入框为空或内容已是推荐消息、且无障碍服务唯一识别到‘发送’按钮时才会尝试发送；否则只填入草稿。",11f,palette.muted),row(12))
        summary.setTextColor(palette.ink);summary.textSize=12f;summary.setTextIsSelectable(true)
        addView(summary,row(10));refreshSummary()
        addView(palette.actionButton("读取并分析我的主页，保存个人资料",true) { onReadAndSave() },row(8))
        addView(palette.actionButton("保存匹配设置",heightDp=42) { saveSettings() },row(8))
        addView(palette.actionButton("返回回复风格",heightDp=42) { onBack() },row(0))
    }
    private fun refreshSummary() {
        val data=graph.profiles.load();val own=data.selfHomepage
        summary.text=buildString {
            append("识别次数：${data.matchRecords.size}\n")
            if(own==null) append("我的主页资料：尚未保存")
            else {
                append("我的主页资料：已保存（${own.platform}）\n")
                append("保存时间：${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",java.util.Locale.getDefault()).format(java.util.Date(own.createdAt))}\n")
                append("资料摘要：${own.analysis.take(420)}")
            }
        }
    }
    private fun saveSettings() {
        val value=threshold.text.toString().toIntOrNull()
        if(value==null || value !in 0..100) { threshold.error="请输入 0 到 100 之间的整数";return }
        graph.settings.update { it.copy(autoSoulMatchEnabled=autoMatch.isChecked,soulMatchThreshold=value,soulMatchAutoSend=autoSend.isChecked) }
        ChatCaptureService.instance?.notifyUser("自动灵魂匹配设置已保存")
        refreshSummary()
    }
    private fun row(bottom:Int)=LayoutParams(-1,-2).apply { bottomMargin=dp(bottom) }
    private fun dp(value:Int)=ViewStyles.dp(context,value.toFloat())
}
