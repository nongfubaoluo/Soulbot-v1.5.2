package com.loveadvisor.core.capture

/** Supported foreground chat applications. Platform-specific parsing may fall back to local OCR. */
object ChatApps {
    const val WECHAT="com.tencent.mm"
    const val QQ="com.tencent.mobileqq"
    const val DOUYIN="com.ss.android.ugc.aweme"
    const val SOUL="cn.soulapp.android"
    const val QIANSHOU="com.tantan.x"

    val packages=setOf(WECHAT,QQ,DOUYIN,SOUL,QIANSHOU)
    fun supported(packageName:String?)=packageName in packages
    fun label(packageName:String?)=when(packageName) {
        WECHAT -> "微信"
        QQ -> "QQ"
        DOUYIN -> "抖音"
        SOUL -> "Soul"
        QIANSHOU -> "牵手"
        else -> "聊天"
    }
}
