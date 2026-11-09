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


## 2026-11-09 · 主页读取 / 主页分析 / 自动灵魂匹配

- 将原主页分析入口拆分为并排的“主页读取”和“主页分析”两个按键，放在“判断分析 / 读取信息”上方。
- “主页读取”只读取并保存当前 Soul / 牵手主页可见文字；“主页分析”分析最近一次读取结果。
- 在回复风格编辑页增加“我的主页分析”，可读取、分析并保存本人的主页资料。
- 增加自动灵魂匹配开关、0–100 匹配阈值、匹配识别次数统计。匹配依据为双方明确展示的兴趣和话题，匹配度仅作启发式参考。
- 达到阈值后尝试返回上一页并读取聊天，将推荐开场白填入空白聊天输入框。自动发送默认关闭；用户主动开启后，仅在草稿完全匹配且唯一识别到发送按钮时尝试点击发送。
- 所有主页内容和匹配记录保存在本机 ProfileStore。

## Kotlin compile fixes (2026-11-09)
- Added the missing `ReplyFiller` import used when filling the recommended opener after returning to chat.
- Converted the `Throwable` supplied by `runCatching` into an `Exception` before passing it to diagnostic and safe-error APIs, whose signatures require `Exception`.
- Braced `$platform` interpolation before Chinese suffixes (`主页资料`, `主页`) so Kotlin does not parse the suffix as part of an unresolved identifier.
