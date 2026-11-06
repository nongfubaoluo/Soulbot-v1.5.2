# V1.5.2 主页分析无障碍版改动

- Soul (`cn.soulapp.android`) 与牵手 (`com.tantan.x`) 主页分析改为用户主动点击悬浮窗入口后，通过 Android AccessibilityService 读取当前窗口可访问的文本与 contentDescription。
- 不再打开相册、不再要求主页截图，也不再调用视觉模型做主页识别。
- 将识别到的主页文字作为用户消息样式展示，并将模型分析作为 AI 消息样式展示在原悬浮窗页面。
- 使用当前已配置的对话模型（`SettingsData.profile`）生成主页摘要、兴趣话题、沟通切入点、误判提醒和 5 条开场白。
- 识别原文、平台、模型分析结果和时间写入 `profiles` 本地文档，最多保留最近 100 条；旧版资料文件兼容缺失字段。
- 本功能只在用户主动点击主页分析时读取当前前台 Soul/牵手页面，不在后台自动扫描。

## 注意

无障碍只能读取目标应用实际暴露给 Accessibility API 的文本节点。若某些资料仅绘制在图片/Canvas/WebView 且未暴露文本节点，应用不会猜测或伪造该内容；此时会提示可读取内容不足。

本次环境无法下载 Gradle 8.9（`services.gradle.org` DNS/网络不可达），因此没有声称 APK 编译通过。请在可用 Android Studio/Gradle 环境执行 `:app:assembleDebug` 并进行 Soul、牵手真机验证。
