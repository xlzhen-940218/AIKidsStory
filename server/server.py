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


# 初始化 DeepSeek 客户端
try:
    api_key = sys.argv[1] if len(sys.argv) > 1 else os.getenv("DEEPSEEK_API_KEY")

    if not api_key:
        print("错误: 未提供 DeepSeek API Key。请通过命令行参数或 DEEPSEEK_API_KEY 环境变量提供。")
        client = None
    else:
        client = OpenAI(
            api_key=api_key,
            base_url="https://api.deepseek.com/v1"
        )
except Exception as e:
    print(f"初始化 OpenAI 客户端失败: {e}")
    client = None

# 使用的模型名称
MODEL_NAME = "deepseek-chat"

# =================================================================
# 2. 动态系统级提示生成 (System Prompt Generation)
# =================================================================

# [修改] 核心系统指令。应用户要求，移除几乎所有限制，只保留年龄和安全要求。
BASE_STORY_SYSTEM_PROMPT = """
You are an expert children's story writer, specializing in creating narratives for children aged 4-8.
Your stories must adhere to the following strict rules:
1. **Safety and Positivity**: The story must be entirely positive, optimistic, safe, and absolutely age-appropriate for young children.
2. **Format**: Output only the story itself, without any opening remarks (like "Okay, here is a story") or concluding remarks (like "Hope you enjoyed it").
"""


def get_story_system_prompt(language_name):
    """
    返回包含语言要求的系统提示。
    """
    language_instruction = f"\n\n**CRITICAL RULE**: You MUST write the entire story in {language_name}."
    return BASE_STORY_SYSTEM_PROMPT + language_instruction


# [修改] 用于生成随机主题的系统提示。简化要求，只要求一个“富有想象力的点子”。
def get_theme_system_prompt(language_name):
    """
    返回用于生成随机主题的系统提示，要求主题使用指定语言。
    """
    return f"""
    You are a creative children's story theme generator. Generate an interesting, positive, and *wildly imaginative* story idea suitable for children aged 4-8.
    The theme must be a single sentence.

    **CRITICAL RULE**: The output theme MUST be written in {language_name}.
    Output only the theme content (a single sentence) directly, without any additional modifiers or explanations.
    """


# =================================================================
# 3. 辅助函数：生成随机主题 (多语言支持)
# =================================================================

def generate_random_theme(target_language_code, target_language_name):
    """使用 DeepSeek API 生成一个指定语言的随机儿童故事主题。"""
    print(f"用户未提供要求，正在生成 {target_language_name} 随机主题...")

    user_prompt = f"Please generate a brand new, imaginative children's story theme. The theme MUST be in {target_language_name}."

    try:
        messages = [
            {"role": "system", "content": get_theme_system_prompt(target_language_name)},
            {"role": "user", "content": user_prompt},
        ]

        # [建议] 调整参数以获得最大创意：提高温度，添加 top_p 和 presence_penalty
        response = client.chat.completions.create(
            model="deepseek-chat",
            messages=messages,
            temperature=1.0,  # 提高温度以增加随机性
            top_p=0.95,       # 配合 high-temp 使用
            presence_penalty=0.8, # [修改] 进一步提高，鼓励模型引入新概念
            frequency_penalty=0.2, # [新增] 轻微惩罚重复词汇
            max_tokens=500
        )

        random_theme = response.choices[0].message.content.strip()
        return random_theme

    except Exception as e:
        print(f"生成随机主题失败: {e}")
        # 失败时返回一个硬编码的默认主题
        if target_language_code == 'zh':
            return "一只勇敢的小狐狸帮助朋友找到了丢失的玩具。"
        else:
            return "A brave little fox who helps a friend find a lost toy."


# =================================================================
# 4. API 路由
# =================================================================

@app.route('/generate_story', methods=['POST'])
def generate_story():
    """
    接收 POST 请求，生成多语言儿童故事。
    """
    if not client:
        return jsonify({"error": "API 客户端未正确初始化，请检查 API Key。"}), 500

    try:
        data = request.get_json(silent=True)
        if not (data and isinstance(data, dict)):
            data = {}

        user_request = data.get('user_request', '').strip()
        raw_language_code = data.get('language', 'zh-CN')

        lang_code, lang_name = normalize_language_code(raw_language_code)

        print(f"请求语言：{lang_name} ({lang_code})")

        if not user_request:
            theme = generate_random_theme(lang_code, lang_name)
            theme_source = "AI Generated Theme"
            final_user_prompt = (
                f"Please create a story for children aged 4-8. "
                f"The theme/request is: '{theme}'. "
                f"Strictly adhere to all rules in the system prompt, including the language rule."
            )
        else:
            theme = user_request
            theme_source = "User Specified"
            final_user_prompt = (
                f"Please create a story for children aged 4-8. "
                f"The user-provided story request or theme is: '{user_request}'. "
                f"Strictly adhere to all rules in the system prompt, including the language rule."
            )

        print(f"最终故事主题/要求：{theme}")

        story_system_prompt = get_story_system_prompt(lang_name)

        messages = [
            {"role": "system", "content": story_system_prompt},
            {"role": "user", "content": final_user_prompt},
        ]

        # [建议] 调整故事生成参数，增加多样性
        response = client.chat.completions.create(
            model=MODEL_NAME,
            messages=messages,
            temperature=0.95,      # [修改] 保持较高的温度
            top_p=0.95,            # 使用 nucleus sampling
            presence_penalty=0.7,  # [修改] 进一步提高，鼓励引入新话题
            frequency_penalty=0.1, # [新增] 轻微惩罚重复词汇
            max_tokens=1500        # [建议] 适当增加最大 token，因为 800 词可能超过 1000 token
        )

        story_content = response.choices[0].message.content.strip()

        return jsonify({
            "story": story_content,
            "theme_used": theme,
            "theme_source": theme_source,
            "language_code": lang_code,
            "language_name": lang_name,
            "model": MODEL_NAME,
            "success": True
        }), 200

    except Exception as e:
        print(f"生成故事时发生错误: {e}")
        return jsonify({"error": f"服务器内部错误或 API 调用失败: {str(e)}"}), 500


# =================================================================
# 5. 启动服务器
# =================================================================

if __name__ == '__main__':
    if client:
        print(f"故事生成服务器正在使用模型: {MODEL_NAME}")
        print(f"支持的语言: {', '.join(LANGUAGES.values())}")
        print(
            "您可以通过 POST 请求到 /generate_story 路径，并在 JSON 体中传入 'user_request' (可选) 和 'language' (可选，默认为 zh-CN) 参数。")
    else:
        print("服务器启动，但 API 客户端未初始化。请检查 API Key。")

    serve(app, host='0.0.0.0', port=18080)

