import os
import json
import sys
from functools import wraps
import time

from flask import Flask, request, jsonify
from openai import OpenAI
from waitress import serve

# =================================================================
# 1. 配置和初始化
# =================================================================

app = Flask(__name__)

# 定义支持的语言及其对应的 ISO 639-1 代码和全称
LANGUAGES = {
    "zh": "Simplified Chinese",
    "en": "English",
    "fr": "French",
    "de": "German",
    "it": "Italian",
    "ja": "Japanese",
    "es": "Spanish",
    "pt": "Portuguese",
}


# 辅助函数：标准化语言代码
def normalize_language_code(code):
    """从 zh-CN, en-US 等格式中提取基础的 ISO 639-1 代码，并检查是否支持。"""
    if not code:
        return 'zh', 'Simplified Chinese'  # 默认中文

    # 提取前两个字母作为基础代码
    base_code = code.lower().split('-')[0]

    if base_code in LANGUAGES:
        return base_code, LANGUAGES[base_code]

    # 如果不支持，则回退到默认语言
    return 'zh', 'Simplified Chinese'


# 初始化 DeepSeek 客户端。DeepSeek 兼容 OpenAI API 格式。
try:
    # 尝试从命令行参数获取 API Key，如果没有则尝试从环境变量获取
    api_key = sys.argv[1] if len(sys.argv) > 1 else os.getenv("DEEPSEEK_API_KEY")

    if not api_key:
        print("错误: 未提供 DeepSeek API Key。请通过命令行参数或 DEEPSEEK_API_KEY 环境变量提供。")
        client = None
    else:
        client = OpenAI(
            api_key=api_key,
            base_url="https://api.deepseek.com/v1"  # DeepSeek API 的 base URL
        )
except Exception as e:
    print(f"初始化 OpenAI 客户端失败: {e}")
    client = None

# 使用的模型名称
MODEL_NAME = "deepseek-chat"

# =================================================================
# 2. 动态系统级提示生成 (System Prompt Generation)
# =================================================================

# 核心的儿童故事创作系统指令 (保持英文以保证指令的准确性，但会要求模型以指定语言输出)
BASE_STORY_SYSTEM_PROMPT = """
You are an expert children's story writer, specializing in creating narratives for children aged 4-8.
Your stories must adhere to the following strict rules:
1. **Safety and Positivity**: The story must be entirely positive, optimistic, safe, and absolutely must not contain any violence, fear, adult themes, pornography, politics, religion, negative emotions (like excessive sadness, despair), discrimination, or any content unsuitable for children.
2. **Themes**: Stories should focus on friendship, kindness, courage, exploration, learning new things, solving simple problems, or the beauty of nature.
3. **Vividness & Narrative Arc (NEW)**: The story must be lively and descriptive, using engaging action verbs and sensory details. It must follow a clear story structure: **Introduction** (setting and character), **Rising Action** (a simple problem/challenge), **Climax** (solving the problem), and **Resolution** (a happy ending and lesson learned). Include simple, age-appropriate dialogue to make characters engaging.
4. **Language**: Use simple, clear, and lively language, easy for children to understand.
5. **Length**: The story should be between 500 and 800 words (medium length).
6. **Format**: Output only the story itself, without any opening remarks (like "Okay, here is a story") or concluding remarks (like "Hope you enjoyed it").
"""


def get_story_system_prompt(language_name):
    """
    返回包含语言要求的系统提示。
    """
    # 在基础提示后追加语言输出要求
    language_instruction = f"\n\n**CRITICAL RULE**: You MUST write the entire story in {language_name}."
    return BASE_STORY_SYSTEM_PROMPT + language_instruction


# 用于生成随机主题的系统提示
def get_theme_system_prompt(language_name):
    """
    返回用于生成随机主题的系统提示，要求主题使用指定语言。
    """
    return f"""
    You are a creative children's story theme generator. Generate an interesting, positive story theme or requirement suitable for children aged 4-8.
    The theme must be a single sentence, focusing on positive, imaginative elements and a clear **action or challenge**.
    Examples in English: "The adventure of a small turtle who wants to fly to the moon by building a balloon," or "A story about a talking rainbow candy who must convince the sun not to melt his friends."

    **CRITICAL RULE**: The output theme MUST be written in {language_name}.
    Output only the theme content directly, without any additional modifiers or explanations.
    """


# =================================================================
# 3. 辅助函数：生成随机主题 (多语言支持)
# =================================================================

def generate_random_theme(target_language_code, target_language_name):
    """使用 DeepSeek API 生成一个指定语言的随机儿童故事主题。"""
    print(f"用户未提供要求，正在生成 {target_language_name} 随机主题...")

    # 构造请求 API 的用户提示，要求生成指定语言的主题
    user_prompt = f"Please generate a brand new, imaginative children's story theme. The theme MUST be in {target_language_name}."

    try:
        messages = [
            {"role": "system", "content": get_theme_system_prompt(target_language_name)},
            {"role": "user", "content": user_prompt},
        ]

        response = client.chat.completions.create(
            model="deepseek-chat",
            messages=messages,
            temperature=0.9,  # 提高温度以增加主题的随机性
            max_tokens=500
        )

        random_theme = response.choices[0].message.content.strip()
        return random_theme

    except Exception as e:
        print(f"生成随机主题失败: {e}")
        # 失败时返回一个硬编码的默认主题（此处为英文，因为失败可能是API或连接问题，不保证模型能返回正确的语言）
        return "A brave little fox who helps a friend find a lost toy."


# =================================================================
# 4. API 路由
# =================================================================

@app.route('/generate_story', methods=['POST'])
def generate_story():
    """
    接收 POST 请求，生成多语言儿童故事。
    请求体应为 JSON 格式，包含可选的 'user_request' 和 'language' 字段。
    """
    if not client:
        return jsonify({"error": "API 客户端未正确初始化，请检查 API Key。"}), 500

    try:
        # 1. 解析用户请求和语言参数
        data = request.get_json(silent=True)
        if not (data and isinstance(data, dict)):
            data = {}

        user_request = data.get('user_request', '').strip()
        raw_language_code = data.get('language', 'zh-CN')  # 默认中文

        # 规范化语言代码并获取语言名称
        lang_code, lang_name = normalize_language_code(raw_language_code)

        print(f"请求语言：{lang_name} ({lang_code})")

        # 2. 确定最终的故事主题 (Final Prompt)
        if not user_request:
            # 如果用户请求为空，则调用函数生成一个随机主题 (目标语言)
            theme = generate_random_theme(lang_code, lang_name)
            theme_source = "AI Generated Theme"
            # 最终的用户提示，要求模型以目标语言创作故事
            final_user_prompt = (
                f"Please create a story for children aged 4-8. "
                f"The theme/request is: '{theme}'. "
                f"Strictly adhere to all rules in the system prompt, including the language rule."
            )
        else:
            # 使用用户提供的请求，并明确要求故事输出为目标语言
            theme = user_request
            theme_source = "User Specified"
            final_user_prompt = (
                f"Please create a story for children aged 4-8. "
                f"The user-provided story request or theme is: '{user_request}'. "
                f"Strictly adhere to all rules in the system prompt, including the language rule."
            )

        print(f"最终故事主题/要求：{theme}")

        # 3. 构造发送给 DeepSeek API 的消息列表
        # 动态获取包含语言要求的系统提示
        story_system_prompt = get_story_system_prompt(lang_name)

        messages = [
            {"role": "system", "content": story_system_prompt},
            {"role": "user", "content": final_user_prompt},
        ]

        # 4. 调用 DeepSeek API 生成故事
        response = client.chat.completions.create(
            model=MODEL_NAME,
            messages=messages,
            temperature=0.9,
            max_tokens=1000
        )

        # 5. 提取生成的故事内容
        story_content = response.choices[0].message.content.strip()

        # 6. 返回结果
        return jsonify({
            "story": story_content,
            "theme_used": theme,  # 返回最终使用的主题
            "theme_source": theme_source,
            "language_code": lang_code,
            "language_name": lang_name,
            "model": MODEL_NAME,
            "success": True
        }), 200

    except Exception as e:
        # 处理 API 调用或其他错误
        print(f"生成故事时发生错误: {e}")
        return jsonify({"error": f"服务器内部错误或 API 调用失败: {str(e)}"}), 500


# =================================================================
# 5. 启动服务器
# =================================================================

if __name__ == '__main__':
    # 仅在非生产环境下打印此消息
    if client:
        print(f"故事生成服务器正在使用模型: {MODEL_NAME}")
        print(f"支持的语言: {', '.join(LANGUAGES.values())}")
        print(
            "您可以通过 POST 请求到 /generate_story 路径，并在 JSON 体中传入 'user_request' (可选) 和 'language' (可选，默认为 zh-CN) 参数。")
    else:
        print("服务器启动，但 API 客户端未初始化。请检查 API Key。")

    serve(app, host='0.0.0.0', port=18080)
