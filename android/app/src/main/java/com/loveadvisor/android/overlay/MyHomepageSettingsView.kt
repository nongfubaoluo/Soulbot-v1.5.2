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
    private val sourceInput=field("本人主页原始资料（可手动校正）",true)
    private val parametersInput=field("识别参数：个人简介、兴趣、话题、互动偏好等（可编辑）",true)
    private val summary=TextView(context)
    private var ownPlatform="Soul/牵手"
    init {
        orientation=VERTICAL
        addView(palette.text("我的主页分析",19f,bold=true),row(8))
        addView(palette.text("先打开 Soul 或牵手的本人主页，再点击读取并分析。识别结果会保存到本机；下方可以手动修改识别到的主页参数。",12f,palette.muted),row(10))
        autoMatch.text="启用自动灵魂匹配"
        autoMatch.isChecked=graph.settings.load().autoSoulMatchEnabled
        autoMatch.setTextColor(palette.ink);addView(autoMatch,row(4))
        addView(palette.text("分析对方主页时，与这里保存的个人资料对比兴趣、话题和沟通偏好。",11f,palette.muted),row(6))
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
        addView(palette.text("自动发送默认关闭。开启后仍需满足安全校验；无法确认聊天输入框或发送按钮时只填入草稿。",11f,palette.muted),row(10))
        summary.setTextColor(palette.ink);summary.textSize=12f;summary.setTextIsSelectable(true)
        addView(summary,row(6))
        val own=graph.profiles.load().selfHomepage
        if(own!=null) {
            ownPlatform=own.platform
            sourceInput.setText(own.visibleText)
            parametersInput.setText(own.analysis)
        }
        addView(palette.text("本人主页原始资料",12f,palette.ink,true),row(4));addView(sourceInput,row(8))
        addView(palette.text("识别参数（可直接修改）",12f,palette.ink,true),row(4));addView(parametersInput,row(8))
        addView(palette.actionButton("读取并分析我的主页",true) { onReadAndSave() },row(8))
        addView(palette.actionButton("保存修改后的主页参数",heightDp=42) { saveProfileEdits() },row(8))
        addView(palette.actionButton("保存匹配设置",heightDp=42) { saveSettings() },row(8))
        addView(palette.actionButton("返回回复风格",heightDp=42) { onBack() },row(0))
        refreshSummary()
    }
    /** Reload the profile after background analysis has persisted it. */
    fun reloadSavedProfile() {
        val own=graph.profiles.load().selfHomepage
        if(own!=null) {
            ownPlatform=own.platform
            sourceInput.setText(own.visibleText)
            parametersInput.setText(own.analysis)
        }
        refreshSummary()
    }
    private fun refreshSummary() {
        val data=graph.profiles.load();val own=data.selfHomepage
        summary.text=buildString {
            append("识别/匹配记录：${data.matchRecords.size} 次\n")
            if(own==null) append("我的主页资料：尚未保存")
            else {
                append("我的主页资料：已保存（${own.platform}）\n")
                append("保存时间：${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",java.util.Locale.getDefault()).format(java.util.Date(own.createdAt))}")
            }
        }
    }
    private fun saveProfileEdits() {
        val source=sourceInput.text.toString().trim()
        val parameters=parametersInput.text.toString().trim()
        if(source.isBlank() && parameters.isBlank()) { sourceInput.error="请先读取主页或填写主页资料";return }
        graph.profiles.saveSelfHomepage(ownPlatform,source,parameters)
        refreshSummary()
        ChatCaptureService.instance?.notifyUser("我的主页参数已修改并保存")
    }
    private fun saveSettings() {
        val value=threshold.text.toString().toIntOrNull()
        if(value==null || value !in 0..100) { threshold.error="请输入 0 到 100 之间的整数";return }
        graph.settings.update { it.copy(autoSoulMatchEnabled=autoMatch.isChecked,soulMatchThreshold=value,soulMatchAutoSend=autoSend.isChecked) }
        ChatCaptureService.instance?.notifyUser("自动灵魂匹配设置已保存")
        refreshSummary()
    }
    private fun field(hintValue:String,multiline:Boolean)=EditText(context).apply {
        hint=hintValue;setTextColor(palette.ink);setHintTextColor(palette.muted);textSize=13f
        setPadding(dp(12),dp(10),dp(12),dp(10));background=ViewStyles.shape(context,palette.card,12f,palette.border)
        inputType=InputType.TYPE_CLASS_TEXT or if(multiline)InputType.TYPE_TEXT_FLAG_MULTI_LINE else InputType.TYPE_CLASS_TEXT
        imeOptions=android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI
        if(multiline) { minLines=3;maxLines=7;gravity=android.view.Gravity.TOP } else setSingleLine(true)
    }
    private fun row(bottom:Int)=LayoutParams(-1,-2).apply { bottomMargin=dp(bottom) }
    private fun dp(value:Int)=ViewStyles.dp(context,value.toFloat())
}
