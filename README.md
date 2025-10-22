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
| **服务端** | **Python** | 接收请求，调用 AI 模型生成故事内容。 |

-----

## 🚀 快速开始

### 1\. 服务端配置

服务端基于 Python 构建，提供一个 `/generate_story` 接口用于故事生成。

#### 接口: `/generate_story`

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
    "theme_source": "主题来源（例如：user/random）",
    "language_code": "zh-CN",
    "language_name": "Chinese",
    "model": "使用的AI模型名称",
    "success": true
}
```

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

-----

## 🛠️ 设置页面 (`SettingsActivity`)

设置页面允许用户自定义语音播放参数，配置项通过 `StorageUtils` 持久化存储。

### 核心功能

1.  **音色选择**：
      * 使用 `RadioGroup` 实现男女声切换。
      * **“Father”** 按钮对应 `settingsConfig.setMan(true)` (男声)。
      * **“Monther”** 按钮对应 `settingsConfig.setMan(false)` (女声)。
      * 用户的选择会立即保存到配置中。
2.  **语速调节**：
      * 使用 `Slider` 控件调节语速。
      * **语速映射**：`Slider` 的值范围被映射到实际的播放速度（`speed`）。
          * 映射关系为：$speed = 1.0 + (\text{SliderValue} - 5) \times 0.1$。
          * 例如：Slider 值为 **5** 时，速度为 **1.0x** (正常速度)；Slider 值为 **15** 时，速度为 **2.0x**。
      * 语速调整后也会立即保存。

-----

## 📖 历史记录 (`HistoryActivity` & `HistoryAdapter`)

历史记录功能允许用户查看和管理所有已生成的故事。

### 历史记录页面 (`HistoryActivity`)

1.  **数据加载**：从本地存储 (`"history"`) 加载 `StoryModelList`。
2.  **列表展示**：使用 `ListView` 和定制的 `HistoryAdapter` 展示故事列表。
3.  **播放速度传递**：将当前配置的语速 (`settingsConfig.getSpeed()`) 传递给 `HistoryAdapter`，确保历史故事播放时也能应用用户设置的语速。
4.  **资源清理**：在 `onDestroy` 方法中，调用 `historyAdapter.stopPlay()` 确保退出页面时，MediaPlayer 资源被释放，避免内存泄漏。

### 历史故事列表适配器 (`HistoryAdapter`)

`HistoryAdapter` 负责历史记录列表项的展示和播放控制。

#### 列表项布局 (`adapter_history.xml`)

每个列表项包含：

  * **播放按钮** (`play_button_image_view`)：用于控制该故事的播放/暂停。
  * **故事标题** (`story_title_view`)：显示故事主题。
  * **生成时间** (`story_time_view`)：显示故事生成的时间。

#### 播放逻辑 (`audioPlayer`)

1.  **单例MediaPlayer**：列表中所有的播放操作共享一个 `MediaPlayer` 实例 (`mp`)，保证同一时间只有一个故事在播放。
2.  **播放状态管理**：`StoryModel` 中包含 `isPlaying` 状态，用于更新当前列表项的播放按钮图标（**播放图标** $\leftrightarrow$ **暂停图标**）。
3.  **播放/暂停/切换**：
      * 如果点击当前正在播放的故事：执行暂停 (`mp.pause()`)。
      * 如果点击当前已暂停的故事：执行继续播放 (`mp.start()`)。
      * 如果点击一个新的故事：停止当前播放，重置 `MediaPlayer`，加载新故事的音频路径 (`item.getAudioPath()`)。
4.  **应用语速**：在开始播放前，通过 `mp.setPlaybackParams().setSpeed(speed)` 应用用户在设置中配置的语速。
5.  **播放完成**：设置 `OnCompletionListener`，在播放结束后停止 `mp`，并将对应故事的 `isPlaying` 状态设为 `false`，刷新列表。
