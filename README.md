# Token — DeepSeek 余额查询

一个简洁优雅的 Android App，使用 DeepSeek API Key 快速查询账户余额。支持**桌面小组件**，无需打开 App 即可一眼看到余额。

## ✨ 特性

- 🔑 **API Key 配置** — 输入 DeepSeek API Key，自动安全存储到本地
- 💰 **余额查询** — 一键查询账户总余额及赠送额度
- 🌗 **深色 / 浅色主题** — 自动跟随系统，并定制 DeepSeek 品牌色
- 🧩 **桌面小组件** — 支持 Android AppWidget，适配 vivo OriginOS 卡片原子组件
- 📋 **一键复制** — 点击复制按钮快速复制完整余额信息
- 🔄 **充值快捷入口** — 内置按钮直达 DeepSeek 平台充值页面
- ✨ **Shimmer 加载动画** — 查询时展示骨架屏，体验流畅

## 📸 预览

| 余额卡片 | 桌面小组件 |
| :---: | :---: |
| 总余额 + 赠送额度 | 卡片一键查看余额 |
| 一键复制 + 充值按钮 | 点击跳转 App |

## 🛠 技术栈

| 技术 | 用途 |
| ---- | ---- |
| **Kotlin** | 开发语言 |
| **Jetpack Compose** | 声明式 UI |
| **Material 3** | 设计系统 |
| **OkHttp** | HTTP 网络请求 |
| **kotlinx.serialization** | JSON 解析 |
| **SharedPreferences** | 本地数据存储 |
| **AppWidget (RemoteViews)** | 桌面小组件 |

## 📦 项目结构

```text
DeepSeekBalance/
├── app/
│   ├── build.gradle.kts                # 应用构建配置
│   └── src/main/
│       ├── AndroidManifest.xml          # 清单：权限、Activity、Widget
│       ├── java/com/example/deepseekbalance/
│       │   ├── MainActivity.kt          # 唯一入口 Activity
│       │   ├── data/
│       │   │   ├── PreferencesManager.kt # API Key 本地存储
│       │   │   └── WidgetDataStore.kt    # 小组件数据持久化
│       │   ├── model/
│       │   │   └── Balance.kt           # 数据模型（API 响应 & UI 状态）
│       │   ├── network/
│       │   │   ├── ApiClient.kt         # OkHttp 客户端单例
│       │   │   └── DeepSeekApi.kt       # DeepSeek API 调用封装
│       │   ├── ui/
│       │   │   ├── screen/
│       │   │   │   └── BalanceScreen.kt  # 主界面（Compose）
│       │   │   └── theme/
│       │   │       └── Theme.kt         # 品牌色 / 排版 / 形状
│       │   └── widget/
│       │       └── BalanceWidgetReceiver.kt  # 桌面小组件
│       └── res/
│           ├── layout/widget_balance.xml # 小组件布局
│           ├── xml/balance_widget_info.xml # 小组件配置
│           ├── drawable/                 # 背景和图标
│           ├── values/                   # 字符串、主题
│           └── mipmap-*/                 # 启动图标
├── build.gradle.kts                      # 顶层构建配置
├── settings.gradle.kts                   # 项目设置 & 仓库
└── gradle.properties
```

## 🚀 构建运行

### 环境要求

- **Android Studio** Hedgehog (2023.1) 或更新
- **JDK** 17
- **Android SDK** 34 (minSdk 26 / targetSdk 34)
- **Gradle** 8.5+

### 步骤

```bash
# 1. 克隆项目
git clone https://github.com/NIXNAUG/DeepSeek-Token-view.git
cd DeepSeek-Token-view

# 2. 用 Android Studio 打开项目根目录，等待 Gradle 同步

# 3. 连接设备或启动模拟器，点击 Run
```

> 首次打开时 Gradle 会下载依赖（Compose BOM、OkHttp 等），同步完成后即可编译运行。

## 📖 使用说明

1. **获取 API Key** — 前往 [DeepSeek 开放平台](https://platform.deepseek.com/api_keys) 注册并创建 API Key
2. **配置** — 在 App 输入框中填入 API Key，点击「保存并查询」
3. **查看余额** — 自动拉取并展示账户余额
4. **添加桌面小组件** — 长按桌面 → 添加卡片 → 搜索「Token」→ 放置即可

## 🔒 安全 & 隐私

- API Key 仅存储在设备本地 `SharedPreferences` 中，不会上传至任何第三方
- 所有网络请求直接发送至 DeepSeek 官方 API (`api.deepseek.com`)
- 应用仅申请 `INTERNET` 网络权限

## 📄 License

MIT License

---

**注意**：此应用为第三方工具，与 DeepSeek 官方无关。API Key 由用户自行保管，请勿泄露。
