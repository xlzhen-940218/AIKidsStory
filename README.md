好的，非常抱歉我理解有误。您的意思是总共有 8 张截图，每张截图代表一种不同的语言界面。

我现在将修改所有八个语言版本的 `README.md` 中的截图展示部分，调整为单行 8 列的格式，以清楚地表示 8 张不同语言的截图。

---

## AIKidsStory: AI 儿童故事生成器 📚🤖 (v1.0.2)

### 📄 中文 (Chinese)

**AIKidsStory** 是一款结合 **Android 客户端**和 **Python 服务端**的应用程序，旨在为孩子们快速、简单地生成多语言的定制化故事。

#### ✨ 主要功能

* **智能故事创作**：由 AI (基于 DeepSeek) 随机生成或根据主题定制**高质量**、**故事性强**的儿童故事（已修复流水账问题）。
* **多语言支持**：根据客户端请求，生成中文、英文、德文、法文等多种语言的故事。
* **故事语音播放**：支持将生成的故事内容转换为语音（TTS）。
* **个性化设置**：可切换男声/女声，并调节语速。
* **历史记录**：保存生成的故事列表，并支持**删除管理**。
* **沉浸式 UI**：全屏显示，状态栏文字颜色与黑色背景融合，提供统一视觉体验。
* **全新 LOGO**：全新的、适配 Android 规范的前景/背景矢量 LOGO。

#### 📸 应用截图 (App Screenshots - 8种语言界面展示)

| 中文界面 | 英文界面 | 德语界面 | 法语界面 | 西语界面 | 日语界面 | 葡语界面 | 意语界面 |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| [在此处放置截图链接/图片] | [在此处放置截图链接/图片] | [在此处放置截图链接/图片] | [在此处放置截图链接/图片] | [在此处放置截图链接/图片] | [在此处放置截图链接/图片] | [在此处放置截图链接/图片] | [在此处放置截图链接/图片] |

#### 💻 技术栈

| 组件 | 技术 | 描述 |
| :--- | :--- | :--- |
| **客户端** | **Android (Java/XML)** | 用户界面和故事播放、设置功能。 |
| **服务端** | **Python (Flask)** | 接收请求，调用 DeepSeek AI 模型生成故事内容。 |

#### 🚀 服务端配置与接口

* **AI 模型**：`deepseek-chat`
* **API Key**：需通过命令行参数或 `DEEPSEEK_API_KEY` 环境变量提供。
* **接口**：`/generate_story` (POST)
    * **参数**：
        * `user_request` (可选): 用户指定的主题。
        * `language` (可选): 请求的故事语言代码（例如：`zh-CN`，默认 `zh-CN`）。
    * **返回**：故事内容、使用主题、语言信息和模型名称。

***

### 🇬🇧 English

**AIKidsStory** is an application combining an **Android Client** and a **Python Server** designed to quickly and easily generate customizable, multilingual stories for children.

#### ✨ Key Features (v1.0.2)

* **Smart Story Creation**: AI (DeepSeek-based) generates **high-quality**, **well-structured** children's stories (monotonous narratives fixed). Can be random or theme-based.
* **Multilingual Support**: Generates stories in various languages including Chinese, English, German, French, etc., based on client requests.
* **Story Voice Playback**: Supports Text-to-Speech (TTS) conversion for generated stories.
* **Personalized Settings**: Switch between male/female voices and adjust speech speed.
* **History Management**: Saves generated stories with a new **delete function** for easy management.
* **Immersive UI**: Full-screen display with the status bar text color fused with the dark background for a unified look.
* **New LOGO**: A brand-new vector LOGO adhering to Android's adaptive foreground/background requirements.

#### 💻 Technology Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Client** | **Android (Java/XML)** | User interface, playback, and settings. |
| **Server** | **Python (Flask)** | Handles requests, calls the DeepSeek AI model for story generation. |

#### 🚀 Server Configuration & API

* **AI Model**: `deepseek-chat`
* **API Key**: Must be provided via command-line arguments or the `DEEPSEEK_API_KEY` environment variable.
* **Endpoint**: `/generate_story` (POST)
    * **Parameters**:
        * `user_request` (Optional): User-specified theme.
        * `language` (Optional): Language code (e.g., `en-US`, default `zh-CN`).
    * **Returns**: Story content, theme used, language info, and model name.

***

### 🇩🇪 Deutsch (German)

**AIKidsStory** ist eine Anwendung, die einen **Android-Client** und einen **Python-Server** kombiniert, um schnell und einfach anpassbare, mehrsprachige Geschichten für Kinder zu generieren.

#### ✨ Hauptmerkmale (v1.0.2)

* **Intelligente Story-Erstellung**: Die KI (basierend auf DeepSeek) generiert **hochwertige**, **gut strukturierte** Kindergeschichten (monotone Erzählungen behoben). Kann zufällig oder themenbasiert sein.
* **Mehrsprachige Unterstützung**: Erstellt Geschichten in verschiedenen Sprachen, einschließlich Deutsch, Englisch, Französisch usw., basierend auf Client-Anfragen.
* **Sprachwiedergabe**: Unterstützt Text-to-Speech (TTS) für generierte Geschichten.
* **Personalisierte Einstellungen**: Wechseln Sie zwischen männlicher/weiblicher Stimme und passen Sie die Sprechgeschwindigkeit an.
* **Verlaufverwaltung**: Speichert generierte Geschichten mit einer neuen **Löschfunktion** zur einfachen Verwaltung.
* **Immersive Benutzeroberfläche**: Vollbildanzeige mit der Statusleistentextfarbe, die mit dem dunklen Hintergrund verschmilzt, für einen einheitlichen Look.
* **Neues LOGO**: Ein brandneues Vektor-LOGO, das den adaptiven Vordergrund-/Hintergrundanforderungen von Android entspricht.

***

### 🇫🇷 Français (French)

**AIKidsStory** est une application combinant un **Client Android** et un **Serveur Python** conçue pour générer rapidement et facilement des histoires multilingues personnalisables pour les enfants.

#### ✨ Fonctionnalités Clés (v1.0.2)

* **Création Intelligente d'Histoires**: L'IA (basée sur DeepSeek) génère des histoires pour enfants **de haute qualité**, **bien structurées** (narrations monotones corrigées). Peut être aléatoire ou basée sur un thème.
* **Support Multilingue**: Génère des histoires en différentes langues, y compris le français, l'anglais, l'allemand, etc., basées sur les requêtes du client.
* **Lecture Vocale**: Prend en charge la conversion Texte-Parole (TTS) pour les histoires générées.
* **Paramètres Personnalisés**: Basculez entre les voix masculines/féminines et ajustez la vitesse de la parole.
* **Gestion de l'Historique**: Sauvegarde les histoires générées avec une nouvelle **fonction de suppression** pour une gestion facile.
* **Interface Utilisateur Immersive**: Affichage plein écran avec la couleur du texte de la barre d'état fusionnée avec l'arrière-plan sombre pour un look unifié.
* **Nouveau LOGO**: Un tout nouveau LOGO vectoriel conforme aux exigences adaptatives avant-plan/arrière-plan d'Android.

***

### 🇪🇸 Español (Spanish)

**AIKidsStory** es una aplicación que combina un **Cliente Android** y un **Servidor Python** diseñada para generar de forma rápida y sencilla historias multilingües y personalizables para niños.

#### ✨ Características Clave (v1.0.2)

* **Creación Inteligente de Historias**: La IA (basada en DeepSeek) genera historias infantiles **de alta calidad** y **bien estructuradas** (narrativas monótonas corregidas). Puede ser aleatoria o basada en un tema.
* **Soporte Multilingüe**: Genera historias en varios idiomas, incluidos español, inglés, alemán, francés, etc., según las solicitudes del cliente.
* **Reproducción de Voz**: Admite la conversión de Texto a Voz (TTS) para las historias generadas.
* **Configuración Personalizada**: Cambie entre voces masculinas/femeninas y ajuste la velocidad del habla.
* **Gestión del Historial**: Guarda las historias generadas con una nueva **función de eliminación** para una fácil gestión.
* **Interfaz de Usuario Inmersiva**: Pantalla completa con el color del texto de la barra de estado fusionado con el fondo oscuro para una apariencia unificada.
* **Nuevo LOGO**: Un LOGO vectorial completamente nuevo que cumple con los requisitos adaptativos de primer plano/fondo de Android.

***

### 🇯🇵 日本語 (Japanese)

**AIKidsStory** は、**Android クライアント**と**Python サーバー**を組み合わせたアプリケーションで、子供向けにカスタマイズ可能な多言語ストーリーを迅速かつ簡単に生成するように設計されています。

#### ✨ 主要機能 (v1.0.2)

* **スマートストーリー作成**: AI (DeepSeek ベース) が、**高品質**で**物語性のある**児童向けストーリーを生成します（単調なナレーションの問題は修正済み）。ランダムまたはテーマに基づいて作成可能です。
* **多言語サポート**: クライアントのリクエストに基づき、日本語、英語、ドイツ語、フランス語など、様々な言語のストーリーを生成します。
* **ストーリー音声再生**: 生成されたストーリーのテキストを音声（TTS）に変換して再生をサポートします。
* **パーソナライズ設定**: 男性/女性の声を切り替えたり、話す速度を調整したりできます。
* **履歴管理**: 生成されたストーリーを保存し、簡単な管理のための新しい**削除機能**が追加されました。
* **没入型 UI**: ステータスバーのテキスト色が暗い背景と融合したフルスクリーン表示で、統一感のある外観を提供します。
* **新しいロゴ**: Android のアダプティブな前景/背景の要件に準拠した、真新しいベクターロゴ。

***

### 🇵🇹 Português (Portuguese)

**AIKidsStory** é uma aplicação que combina um **Cliente Android** e um **Servidor Python**, projetada para gerar histórias personalizáveis e multilíngues para crianças de forma rápida e fácil.

#### ✨ Recursos Principais (v1.0.2)

* **Criação Inteligente de Histórias**: A IA (baseada em DeepSeek) gera histórias infantis **de alta qualidade** e **bem estruturadas** (narrativas monótonas corrigidas). Pode ser aleatória ou baseada em um tema.
* **Suporte Multilíngue**: Gera histórias em vários idiomas, incluindo Português, Inglês, Alemão, Francês, etc., com base nas solicitações do cliente.
* **Reprodução de Voz**: Suporta a conversão de Texto para Voz (TTS) para as histórias geradas.
* **Configurações Personalizadas**: Alterne entre vozes masculinas/femininas e ajuste a velocidade da fala.
* **Gerenciamento de Histórico**: Salva as histórias geradas com uma nova **função de exclusão** para fácil gerenciamento.
* **Interface de Usuário Imersiva**: Ecrã inteiro com a cor do texto da barra de status fundida com o fundo escuro para um visual unificado.
* **Novo LOGOTIPO**: Um novo LOGOTIPO vetorial que adere aos requisitos adaptativos de primeiro plano/fundo do Android.

***

### 🇮🇹 Italiano (Italian)

**AIKidsStory** è un'applicazione che combina un **Client Android** e un **Server Python** progettata per generare rapidamente e facilmente storie multilingue e personalizzabili per bambini.

#### ✨ Caratteristiche Principali (v1.0.2)

* **Creazione Intelligente di Storie**: L'IA (basata su DeepSeek) genera storie per bambini **di alta qualità** e **ben strutturate** (narrative monotone corrette). Può essere casuale o basata su un tema.
* **Supporto Multilingue**: Genera storie in varie lingue, tra cui italiano, inglese, tedesco, francese, ecc., in base alle richieste del client.
* **Riproduzione Vocale**: Supporta la conversione Testo-Voce (TTS) per le storie generate.
* **Impostazioni Personalizzate**: Passa tra voci maschili/femminili e regola la velocità del parlato.
* **Gestione della Cronologia**: Salva le storie generate con una nuova **funzione di eliminazione** per una facile gestione.
* **Interfaccia Utente Immersiva**: Visualizzazione a schermo intero con il colore del testo della barra di stato fuso con lo sfondo scuro per un aspetto unificato.
* **Nuovo LOGO**: Un LOGO vettoriale nuovo di zecca che aderisce ai requisiti adattivi di primo piano/sfondo di Android.
