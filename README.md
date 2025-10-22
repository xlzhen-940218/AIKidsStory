# AIKidsStory: AI儿童故事生成器 📚🤖

**AIKidsStory** 是一款结合 **Android 客户端**和 **Python 服务端**的应用程序，旨在为孩子们快速、简单地生成多语言的定制化故事。

-----

## ✨ 主要功能

  * **一键生成故事**：用户只需点击按钮，即可由 AI 随机生成一个全新的儿童故事主题和内容。
  * **多语言支持**：可根据用户设备默认语言生成相应国家/地区语言的故事。
  * **故事语音播放**：支持将生成的故事内容转换为语音（TTS）进行播放。
  * **语音设置**：用户可在设置页切换**男声**或**女声**，并调节**语速**。
  * **历史记录**：保存已生成的故事列表，方便随时回顾和播放。

-----

## 💻 技术栈

| 组件 | 技术 | 描述 |
| :--- | :--- | :--- |
| **客户端** | **Android (Java/XML)** | 用户界面和故事播放、语音设置功能。 |
| **服务端** | **Python (Flask)** | 接收请求，调用 AI 模型生成故事内容。 |

-----

## 🚀 快速开始

### 1\. 服务端配置与启动

服务端使用 **Python + Flask** 框架，并依赖 **DeepSeek AI** 进行故事生成。

#### 依赖

  * Python 3.x
  * `flask`
  * `openai` 库（用于调用 DeepSeek API）

#### AI 模型与 API

  * **模型名称**：`deepseek-chat`
  * **API 平台**：DeepSeek (API Base URL: `https://api.deepseek.com/v1`)
  * **配置**：API Key 必须通过以下任一方式提供：
    1.  **命令行参数**：`python server.py your_deepseek_api_key`
    2.  **环境变量**：设置 `DEEPSEEK_API_KEY` 环境变量

#### 启动服务

```bash
# 确保已安装所需依赖 (e.g., pip install flask openai)
# 推荐使用命令行参数传入 API Key
python server.py your_deepseek_api_key 
```

服务器将在 `0.0.0.0:18080` 上运行。

#### 核心服务端逻辑

| 功能 | 描述 |
| :--- | :--- |
| **故事生成** | 采用严格的系统提示 (System Prompt)，确保生成的儿童故事积极、安全、内容健康，且长度控制在 500 到 800 字之间。 |
| **主题确定** | 如果客户端未提供 `user_request`，服务端会先调用 DeepSeek API **随机生成**一个适合儿童的故事主题。 |
| **多语言支持** | 动态生成包含目标语言要求的系统提示，指导 AI 以指定的语言进行故事创作。 |

#### 支持语言列表

服务端定义了以下支持的语言代码和全称，客户端发送请求时将使用这些代码：

| 代码 (ISO 639-1) | 全称 |
| :--- | :--- |
| `zh` | Simplified Chinese (默认) |
| `en` | English |
| `fr` | French |
| `de` | German |
| `it` | Italian |
| `ja` | Japanese |
| `es` | Spanish |
| `pt` | Portuguese |

#### 接口: `/generate_story`

接收 POST 请求，生成多语言儿童故事。

| 参数名 | 类型 | 是否可选 | 描述 | 默认值 |
| :--- | :--- | :--- | :--- | :--- |
| `user_request` | `string` | 可选 | 用户指定的故事主题。 | 随机 AI 生成故事主题 |
| `language` | `string` | 可选 | 请求的故事语言代码（例如：`zh-CN`、`en-US`）。 | `zh-CN` (中文) |

**请求示例 (JSON Body):**

```json
{
    "user_request": "一只会飞的小猫",
    "language": "en-US"
}
```

**响应示例 (JSON Body):**

```json
{
    "story": "故事内容...",
    "theme_used": "使用的故事主题",
    "theme_source": "主题来源（User Specified 或 AI Generated Theme）",
    "language_code": "en",
    "language_name": "English",
    "model": "deepseek-chat",
    "success": true
}
```

-----

### 2\. Android 客户端

客户端负责用户交互、发送请求和服务端通信。

#### 主要界面元素 (`activity_main.xml`)

| 元素 ID | 描述 |
| :--- | :--- |
| `settings_image_view` | **设置按钮**：进入语音设置页面。 |
| `history_image_view` | **历史记录按钮**：查看已生成故事列表。 |
| `play_button_image_view` | **播放/暂停按钮**：用于播放或暂停故事语音，生成故事后从不可见区域移入。 |
| `story_text_view` | 故事内容展示区域。 |
| `generator_button_image_view` | **生成故事按钮**：点击触发故事生成请求。 |

#### 核心交互逻辑 (`MainActivity.java`)

1.  **初始化**：默认将 `play_button_image_view` 和相关控件移至屏幕上方不可见区域（`-500dp` 左右）。
2.  **生成故事**：
      * 点击“生成故事”按钮。
      * **动画**：生成按钮向下移出屏幕，并开始旋转和闪烁动画。
      * **请求**：客户端发送请求，`user_request` 为空，`language` 为设备默认语言代码 (`Locale.getDefault().toLanguageTag()`)。
3.  **生成成功**：
      * 动画中止。
      * 生成按钮移回屏幕底部。
      * **播放按钮移入**：播放按钮及设置/历史按钮从上方移入屏幕可见区域，提示用户可以开始播放。
      * 故事内容显示，并调用 `GeneratorTTS` 生成本地语音文件。
      * 故事数据 (`StoryModel`) 及其音频路径保存到本地历史记录。
4.  **故事播放**：
      * 点击 `playButtonImageView` 控制 `MediaPlayer` 进行播放/暂停，并切换按钮图标。

-----

## 🛠️ 设置页面 (`SettingsActivity`)

设置页面允许用户自定义语音播放参数，配置项通过 `StorageUtils` 持久化存储。

### 核心功能

1.  **音色选择**：
      * 通过 `RadioGroup` 切换男声/女声 (`father_radio_button`/`monther_radio_button`)，配置项 `settingsConfig.isMan()` 立即保存。
2.  **语速调节**：
      * 使用 `Slider` 调节语速，将滑块值映射到实际播放速度 $speed = 1.0 + (\text{SliderValue} - 5) \times 0.1$，调节结果立即保存。
      * 语速设置在主页和历史记录页的 `MediaPlayer` 中生效。

-----

## 📖 历史记录 (`HistoryActivity` & `HistoryAdapter`)

历史记录功能允许用户查看和管理所有已生成的故事。

### 历史记录页面 (`HistoryActivity`)

1.  **数据加载**：从本地存储加载已保存的故事列表 (`StoryModelList`)。
2.  **列表展示**：使用 `ListView` 和定制的 `HistoryAdapter` 展示故事标题、时间戳和播放按钮。
3.  **资源清理**：退出页面时，通过 `historyAdapter.stopPlay()` 确保正在播放的音频停止，并释放 `MediaPlayer` 资源。

### 历史故事列表适配器 (`HistoryAdapter`)

`HistoryAdapter` 负责历史记录列表项的展示和播放控制。

1.  **单例播放器**：所有列表项共享一个 `MediaPlayer` 实例，确保同一时间只有一个故事音频在播放。
2.  **播放控制**：点击播放按钮触发 `audioPlayer(item)`：
      * **播放/暂停**：如果点击正在播放或暂停的当前故事，则进行暂停或继续播放。
      * **切换**：如果点击其他故事，则停止当前播放，加载并播放新故事的音频。
3.  **状态更新**：通过 `item.setPlaying(boolean)` 更新故事状态，并调用 `notifyDataSetChanged()` 刷新列表项图标。
4.  **应用语速**：所有历史故事播放时，都会应用构造函数中传入的最新用户语速设置。
