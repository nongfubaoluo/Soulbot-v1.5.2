package com.loveadvisor.android.overlay

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.loveadvisor.android.input.ClipboardController
import com.loveadvisor.android.ui.ViewStyles

/** Scrollable profile-analysis result hosted inside the existing floating panel. */
class ProfileAnalysisResultView(context:Context,result:String):LinearLayout(context),PanelViewportPage {
    override val preferredViewportHeightDp=540
    init {
        orientation=VERTICAL
        val palette=PanelPalette(context)
        addView(palette.text("主页分析结果",17f,bold=true))
        addView(palette.text("以下内容仅根据截图可见信息生成；不确定的资料不会当作事实。",11f,palette.muted),LayoutParams(-1,-2).apply { topMargin=dp(6);bottomMargin=dp(12) })
        val scroll=android.widget.ScrollView(context).apply { isFillViewport=false }
        val body=palette.text(result,13f).apply { setTextIsSelectable(true);gravity=Gravity.TOP }
        scroll.addView(body,LayoutParams(-1,-2))
        addView(scroll,LayoutParams(-1,0,1f))
        addView(palette.button("复制全部分析结果",true) { ClipboardController.copy(context,result) },LayoutParams(-1,dp(42)).apply { topMargin=dp(10) })
    }
    private fun dp(value:Int)=ViewStyles.dp(context,value.toFloat())
}
