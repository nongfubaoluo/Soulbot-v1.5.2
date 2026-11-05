package com.loveadvisor.core.capture

import org.junit.Assert.assertTrue
import org.junit.Test

class VisualChatPlatformGuidanceTest {
    @Test fun soulGuidanceDescribesItsBubbleAndComposerCues() {
        val prompt = VisualChatPlatformGuidance.forPackage(ChatApps.SOUL)
        assertTrue(prompt.contains("青绿色"))
        assertTrue(prompt.contains("左右位置"))
        assertTrue(prompt.contains("文明聊天"))
        assertTrue(prompt.contains("关注"))
    }

    @Test fun qianshouGuidanceExcludesProfileShortcutsAndReadReceipt() {
        val prompt = VisualChatPlatformGuidance.forPackage(ChatApps.QIANSHOU)
        assertTrue(prompt.contains("粉红/玫红"))
        assertTrue(prompt.contains("查看对方是否已读"))
        assertTrue(prompt.contains("喝咖啡"))
        assertTrue(prompt.contains("气泡与头像的左右几何关系"))
    }

    @Test fun everySupportedPlatformHasVisualGuidance() {
        listOf(ChatApps.WECHAT, ChatApps.QQ, ChatApps.DOUYIN, ChatApps.SOUL, ChatApps.QIANSHOU)
            .forEach { assertTrue("Missing visual guidance for $it", VisualChatPlatformGuidance.forPackage(it).isNotBlank()) }
    }
}
