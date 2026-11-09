package com.loveadvisor.android.overlay

import android.content.Context
import android.widget.LinearLayout
import com.loveadvisor.android.input.ClipboardController
import com.loveadvisor.android.ui.ViewStyles

/** Homepage source and analysis presented together in one consistent panel card. */
class ProfileAnalysisResultView(context:Context,result:String,platform:String="主页",source:String=""):LinearLayout(context),PanelViewportPage {
    override val preferredViewportHeightDp=540
    init {
        orientation=VERTICAL
        val palette=PanelPalette(context)
        addView(palette.text("主页分析 · $platform",17f,bold=true))
        addView(
            palette.text("主页资料已读取；以下为本次识别资料与 AI 分析结果。",11f,palette.muted),
            LayoutParams(-1,-2).apply { topMargin=dp(6);bottomMargin=dp(10) }
        )

        val scroll=android.widget.ScrollView(context).apply { isFillViewport=false }
        val content=LinearLayout(context).apply {
            orientation=VERTICAL
            setPadding(dp(14),dp(12),dp(14),dp(14))
            background=ViewStyles.shape(context,palette.card,16f,palette.border)
        }
        if(source.isNotBlank()) {
            content.addView(palette.text("主页原始资料",11f,palette.blue,true))
            content.addView(
                palette.text(source,13f).apply { setTextIsSelectable(true) },
                LayoutParams(-1,-2).apply { topMargin=dp(6);bottomMargin=dp(14) }
            )
        }
        content.addView(palette.text("AI 分析结果",11f,palette.blue,true))
        content.addView(
            palette.text(result,13f).apply { setTextIsSelectable(true) },
            LayoutParams(-1,-2).apply { topMargin=dp(6) }
        )
        scroll.addView(content,LayoutParams(-1,-2))
        addView(scroll,LayoutParams(-1,0,1f))

        val copyText=buildString {
            if(source.isNotBlank()) append("主页原始资料\n$source\n\n")
            append("AI 分析结果\n$result")
        }
        addView(
            palette.button("复制全部内容",true) { ClipboardController.copy(context,copyText) },
            LayoutParams(-1,dp(42)).apply { topMargin=dp(10) }
        )
    }
    private fun dp(value:Int)=ViewStyles.dp(context,value.toFloat())
}
