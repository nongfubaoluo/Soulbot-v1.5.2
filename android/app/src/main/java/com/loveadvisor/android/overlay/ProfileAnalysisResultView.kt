package com.loveadvisor.android.overlay

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.loveadvisor.android.input.ClipboardController
import com.loveadvisor.android.ui.ViewStyles

/** Scrollable profile-analysis result hosted inside the existing floating panel. */
class ProfileAnalysisResultView(context:Context,result:String,platform:String="主页",source:String=""):LinearLayout(context),PanelViewportPage {
    override val preferredViewportHeightDp=540
    init {
        orientation=VERTICAL
        val palette=PanelPalette(context)
        addView(palette.text("主页分析 · $platform",17f,bold=true))
        addView(palette.text("已通过无障碍读取当前主页可见文字；分析结果已保存。",11f,palette.muted),LayoutParams(-1,-2).apply { topMargin=dp(6);bottomMargin=dp(12) })
        val scroll=android.widget.ScrollView(context).apply { isFillViewport=false }
        val messages=LinearLayout(context).apply { orientation=VERTICAL }
        fun messageBubble(title:String,text:String,background:Int,titleColor:Int) = LinearLayout(context).apply {
            orientation=VERTICAL;setPadding(dp(12),dp(10),dp(12),dp(10));this.background=android.graphics.drawable.GradientDrawable().apply { cornerRadius=dp(16).toFloat();setColor(background) }
            addView(palette.text(title,11f,titleColor,true));addView(palette.text(text,13f).apply { setTextIsSelectable(true);gravity=Gravity.TOP },LayoutParams(-1,-2).apply { topMargin=dp(5) })
        }
        if(source.isNotBlank()) messages.addView(messageBubble("主页资料 · 无障碍读取",source,palette.selected,palette.blue),LayoutParams(-1,-2).apply { bottomMargin=dp(10);leftMargin=dp(24) })
        messages.addView(messageBubble("AI 分析",result,palette.card,palette.blue),LayoutParams(-1,-2));scroll.addView(messages,LayoutParams(-1,-2))
        addView(scroll,LayoutParams(-1,0,1f))
        addView(palette.button("复制全部分析结果",true) { ClipboardController.copy(context,result) },LayoutParams(-1,dp(42)).apply { topMargin=dp(10) })
    }
    private fun dp(value:Int)=ViewStyles.dp(context,value.toFloat())
}
