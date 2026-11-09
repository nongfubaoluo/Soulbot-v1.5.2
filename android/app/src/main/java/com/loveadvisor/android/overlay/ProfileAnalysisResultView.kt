package com.loveadvisor.android.overlay

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import com.loveadvisor.android.input.ClipboardController
import com.loveadvisor.android.ui.ViewStyles

/** Homepage analysis with per-opening-line copy and safe input actions. */
class ProfileAnalysisResultView(
    context:Context,
    result:String,
    platform:String="主页",
    source:String="",
    private val onBack:()->Unit={},
    private val onInput:(String)->Unit={}
):LinearLayout(context),PanelViewportPage {
    override val preferredViewportHeightDp=540
    init {
        orientation=VERTICAL
        val palette=PanelPalette(context)
        addView(palette.text("主页分析 · $platform",17f,bold=true))
        addView(palette.text("主页资料已读取；以下为本次识别资料与 AI 分析结果。",11f,palette.muted),LayoutParams(-1,-2).apply { topMargin=dp(6);bottomMargin=dp(8) })
        addView(palette.actionButton("返回悬浮窗主页",heightDp=38,action=onBack),LayoutParams(-1,-2).apply { bottomMargin=dp(8) })

        val scroll=ScrollView(context).apply { isFillViewport=false }
        val content=LinearLayout(context).apply {
            orientation=VERTICAL
            setPadding(dp(12),dp(10),dp(12),dp(12))
            background=ViewStyles.shape(context,palette.card,16f,palette.border)
        }
        if(source.isNotBlank()) {
            content.addView(palette.text("主页原始资料",11f,palette.blue,true))
            content.addView(palette.text(source,13f).apply { setTextIsSelectable(true) },LayoutParams(-1,-2).apply { topMargin=dp(6);bottomMargin=dp(12) })
        }
        content.addView(palette.text("AI 分析结果",11f,palette.blue,true),LayoutParams(-1,-2).apply { bottomMargin=dp(6) })
        val sectionStart=result.indexOf("5条自然不冒犯的私聊开场白").let { if(it>=0) it else result.indexOf("私聊开场白") }
        val sectionText=if(sectionStart>=0) result.substring(sectionStart) else ""
        val matches=Regex("(?m)^\\s*(\\d+)[.、]\\s*").findAll(sectionText).toList()
        if(matches.isEmpty()) {
            content.addView(palette.text(result,13f).apply { setTextIsSelectable(true) })
        } else {
            val intro=result.substring(0,sectionStart).trim()
            if(intro.isNotBlank()) content.addView(palette.text(intro,13f).apply { setTextIsSelectable(true) },LayoutParams(-1,-2).apply { bottomMargin=dp(10) })
            content.addView(palette.text(sectionText.substring(0,matches.first().range.first).trim(),12f,palette.blue,true),LayoutParams(-1,-2).apply { bottomMargin=dp(6) })
            matches.forEachIndexed { index,match ->
                val end=if(index+1<matches.size) matches[index+1].range.first else sectionText.length
                val reply=sectionText.substring(match.range.first,end).trim()
                val card=LinearLayout(context).apply {
                    orientation=VERTICAL
                    setPadding(dp(10),dp(8),dp(10),dp(8))
                    background=ViewStyles.shape(context,palette.selected,14f,palette.border)
                }
                card.addView(palette.text(reply,14f).apply { setTextIsSelectable(true) })
                val actions=LinearLayout(context).apply { orientation=HORIZONTAL;gravity=Gravity.CENTER_VERTICAL }
                actions.addView(palette.button("复制",false) { ClipboardController.copy(context,reply) },LayoutParams(0,dp(34),1f).apply { rightMargin=dp(6);topMargin=dp(6) })
                actions.addView(palette.button("输入",true) { onInput(reply) },LayoutParams(0,dp(34),1f).apply { topMargin=dp(6) })
                card.addView(actions)
                content.addView(card,LayoutParams(-1,-2).apply { bottomMargin=dp(8) })
            }
        }
        scroll.addView(content,LayoutParams(-1,-2))
        addView(scroll,LayoutParams(-1,0,1f))
        val copyText=buildString { if(source.isNotBlank()) append("主页原始资料\n$source\n\n");append("AI 分析结果\n$result") }
        addView(palette.button("复制全部内容",true) { ClipboardController.copy(context,copyText) },LayoutParams(-1,dp(42)).apply { topMargin=dp(8) })
    }
    private fun dp(value:Int)=ViewStyles.dp(context,value.toFloat())
}
