# DeepSeek-Token-view
一个可以查看deepseek余额的app
## 功能介绍
  1. 输入 DeepSeek API Key，一键查询账号余额
  2. 展示套餐剩余额度、已消耗 Token
  3. 轻量化安卓客户端，无多余权限，运行流畅
  4. 本地存储 API 密钥，不会上传至第三方服务器

## 文件结构
```
  DeepSeek-Token-view
  ├── .idea/              # IDE配置文件
  ├── app/                # App主模块代码
  ├── gradle/             # Gradle构建工具配置
  ├── src/                # 通用源码目录
  ├── build.gradle.kts    # 项目构建配置
  ├── gradle.properties   # 全局参数配置
  └── settings.gradle.kts # 模块管理配置
```
## 使用教程

### 1. 获取安装包
  前往右侧 Releases 下载 Token v1.0 版本 APK 安装包。
### 2. 获取 DeepSeek API Key
打开 DeepSeek 开放平台
登录账号，进入「API Keys」页面
新建密钥，复制生成的 sk-xxxx 开头密钥
### 3. App 操作步骤
安装 APK 并打开应用
将复制的 API Key 粘贴至输入框
点击查询按钮，即可查看账号余额与 Token 使用情况
### 4. 安全说明
API 密钥仅保存在设备本地，不会上传任何第三方服务器
仅向 DeepSeek 官方接口发起余额查询请求，无额外网络请求
代码完全开源，可自行编译核对逻辑，无隐私窃取行为

编译运行（开发者）
克隆本仓库

```javascript
git clone https://github.com/NIXNAUG/DeepSeek-Token-view.git


```
使用 Android Studio 打开项目
同步 Gradle 依赖，连接安卓设备 / 启动模拟器
点击运行，直接编译安装 App

## 开源协议
本项目采用 MIT License，可自由修改、分发、商用，保留原作者版权声明即可。
反馈与交流
如有 Bug、功能建议，欢迎提交 Issues；也可 Fork 仓库自行二次开发。
