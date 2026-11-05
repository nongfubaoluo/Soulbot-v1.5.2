package com.loveadvisor.android.advisor

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.loveadvisor.android.ui.BaseActivity
import com.loveadvisor.android.ui.ViewStyles
import com.loveadvisor.core.api.PromptMessage

/** Independent, opt-in profile analysis and opener generation. Existing chat flow is untouched. */
class SocialAssistantActivity : BaseActivity() {
    private lateinit var profile: EditText
    private lateinit var goal: EditText
    private lateinit var style: EditText
    private lateinit var output: TextView
    private lateinit var status: TextView
    private var busy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        page("主页分析与开场白", "把对方主动展示的主页文字粘贴到这里；确认后再发送给你配置的大模型。")
        val privacy = ViewStyles.card(this)
        privacy.addView(ViewStyles.text(this, "隐私提示", 17f, bold = true))
        privacy.addView(ViewStyles.text(this, "只分析你有权查看、并由你主动提供的资料。发送前请移除手机号、住址等不必要的个人信息。不会自动读取或上传社交平台主页。", 13f, ViewStyles.MUTED))
        body.addView(privacy)

        profile = ViewStyles.field(this, "粘贴主页资料：昵称、自我介绍、兴趣、公开动态等", multiline = true)
        profile.minLines = 5
        goal = ViewStyles.field(this, "你的目标：自然认识、寻找共同话题、认真交友…", "自然认识，找到共同兴趣")
        style = ViewStyles.field(this, "开场白风格：自然、幽默、真诚、温柔…", "自然、真诚，不油腻")
        body.addView(ViewStyles.text(this, "对方主页资料", 16f, bold = true)); body.addView(profile)
        body.addView(ViewStyles.text(this, "交流目标", 16f, bold = true)); body.addView(goal)
        body.addView(ViewStyles.text(this, "表达风格", 16f, bold = true)); body.addView(style)
        body.addView(ViewStyles.button(this, "分析主页 · 提取兴趣与切入点", true) { runPrompt(false) })
        body.addView(ViewStyles.button(this, "生成 6 条私聊开场白") { runPrompt(true) })
        status = ViewStyles.text(this, "", 13f, ViewStyles.MUTED)
        output = ViewStyles.text(this, "分析结果会显示在这里。", 15f)
        val result = ViewStyles.card(this)
        result.addView(ViewStyles.text(this, "AI 分析结果", 18f, bold = true))
        result.addView(status); result.addView(output)
        body.addView(result)
        body.addView(ViewStyles.button(this, "复制结果") {
            val value = output.text.toString()
            if (value.isBlank() || value == "分析结果会显示在这里。") toast("目前没有可复制的结果")
            else {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("对话军师 AI 建议", value))
                toast("已复制")
            }
        })
    }

    private fun runPrompt(openers: Boolean) {
        if (busy) return
        val source = profile.text.toString().trim()
        if (source.length < 5) { toast("请先粘贴一些主页资料"); return }
        val config = graph.settings.load()
        val api = config.profile
        if (api.apiKey.isBlank() || api.selectedModelId.isNullOrBlank()) {
            toast("请先到模型与连接设置中配置 API Key 并选择模型"); return
        }
        val objective = goal.text.toString().trim().ifBlank { "自然认识" }
        val tone = style.text.toString().trim().ifBlank { "自然、真诚" }
        val system = if (openers) {
            "你是尊重边界的社交沟通助手。根据用户提供的公开主页资料，生成6条自然、具体、容易接续的首次私聊开场白。每条都要指出对应的共同兴趣或资料依据；避免套路、夸大、操控、性暗示和虚构共同经历。不要把兴趣或资料推断成确定的人格。最后给出最推荐的一条及理由。用简体中文。"
        } else {
            "你是谨慎的社交资料分析助手。只根据用户提供的资料分析：1.明确写出的兴趣与事实；2.可用于开启对话的共同话题；3.适合询问的开放式问题；4.资料中无法判断的内容。区分事实与推测，不判断对方真实心理或感情意愿，不给出成功率，不编造信息。用简体中文，结构清晰。"
        }
        val user = "主页资料：\n$source\n\n我的交流目标：$objective\n表达风格：$tone\n" + if (openers) "请生成6条不同角度的开场白，每条简洁并说明切入点。" else "请给出主页分析和3个最值得尝试的话题。"
        busy = true
        status.text = "正在请求 ${api.selectedModelId}…"
        output.text = ""
        work(if (openers) "profile_openers" else "profile_analysis", { cancel ->
            graph.chat.complete(api, listOf(PromptMessage("system", system), PromptMessage("user", user)), cancel, 1400)
        }, { answer ->
            busy = false
            status.text = "完成 · ${api.selectedModelId}"
            output.text = answer
        }, { error -> busy = false; status.text = "请求失败"; output.text = error })
    }
}
