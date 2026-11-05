package com.loveadvisor.core.capture

/**
 * Platform-specific visual reading rules for chat layouts that do not expose a
 * stable, public AccessibilityNode tree. These rules are deliberately used as
 * visual guidance only; they do not assume private resource IDs or app internals.
 */
object VisualChatPlatformGuidance {
    fun forPackage(packageName: String): String = when (packageName) {
        ChatApps.SOUL -> soul
        ChatApps.QIANSHOU -> qianshou
        ChatApps.QQ -> qq
        ChatApps.DOUYIN -> douyin
        ChatApps.WECHAT -> wechat
        else -> error("Unsupported chat application")
    }

    private const val soul = """
Soul私聊界面识别补充规则：顶部聊天栏中联系人昵称是会话标题；右侧可能有“关注”等操作按钮，不能当成消息。典型聊天布局中，右侧青绿色/蓝绿色气泡及右侧头像属于ME，左侧浅灰色气泡及左侧头像属于OTHER；部分消息旁有小圆点或气泡下方有时间，这些状态点和时间不是消息。必须以气泡、头像与屏幕左右位置的组合判断说话人，不可只凭颜色。底部“文明聊天，友善交友～”一类输入提示、语音/表情/加号按钮，以及输入框上方的功能提示均不是消息。把可见图片、动态表情和表情包分别识别为IMAGE/STICKER，并只描述画面里确实看得到的内容。顶部资料、关注状态、推荐提示、通话邀请等均非聊天正文。
"""

    private const val qianshou = """
牵手聊天界面识别补充规则：顶部联系人头像和昵称组成会话标题；标题下方的个人简介、慢热/兴趣等资料文字，以及“见面最重要”“喝咖啡”“看场电影”“散步”等快捷话题按钮不是消息。典型布局中，左侧头像旁的白色/浅色气泡是OTHER，右侧头像旁的粉红/玫红气泡是ME；但必须以气泡与头像的左右几何关系为主，颜色只能辅助，不能仅凭颜色判断。时间通常居中显示在消息组之间，不是消息。底部“查看对方是否已读”一类会员/回执卡片不是对话内容；底部语音、输入框、表情和加号属于输入工具，不是消息。图片、贴纸、动图要按实际缩略图分别识别为IMAGE/STICKER；不要把贴纸上的文字按钮、时间、系统回执或资料信息误读成聊天发言。
"""

    private const val qq = "QQ聊天窗口识别补充规则：确认当前是私聊或群聊消息列表，忽略联系人资料页、频道、空间动态、搜索和系统弹窗。消息双方以气泡和头像的左右位置组合判断，不凭气泡颜色单独判断。输入框、工具栏、语音/表情/加号及系统提示不是消息。"
    private const val douyin = "抖音聊天窗口识别补充规则：仅接受私信聊天会话；视频流、评论区、直播间、作者主页、私信列表和资料页均不是聊天窗口。忽略视频推荐卡、关注按钮、在线状态和系统操作提示。消息双方按气泡与头像的左右位置组合判断。"
    private const val wechat = "微信聊天窗口识别补充规则：仅接受微信真实单聊或群聊消息列表；小程序、网页、朋友圈、公众号文章、联系人资料页和搜索页不属于聊天。忽略顶部标题栏、时间分隔符、系统提示、输入草稿和底部工具栏。"
}
