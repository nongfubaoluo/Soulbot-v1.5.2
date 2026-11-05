package com.loveadvisor.core.capture

import org.junit.Assert.*
import org.junit.Test

class ChatAppsTest {
    @Test fun supportsExistingAndNewChatPlatforms() {
        listOf(
            ChatApps.WECHAT, ChatApps.QQ, ChatApps.DOUYIN,
            ChatApps.SOUL, ChatApps.QIANSHOU
        ).forEach { assertTrue("Expected supported package: $it", ChatApps.supported(it)) }
    }

    @Test fun resolvesNewPlatformNames() {
        assertEquals("Soul", ChatApps.label(ChatApps.SOUL))
        assertEquals("牵手", ChatApps.label(ChatApps.QIANSHOU))
    }

    @Test fun rejectsUnrelatedApplications() {
        assertFalse(ChatApps.supported("com.example.unrelated"))
        assertFalse(ChatApps.supported(null))
    }
}
