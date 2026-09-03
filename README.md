<div align="center">

# MockLocation

**一款开源 Android 位置模拟（虚拟定位）工具** —— 支持点位模拟、路线模拟与悬浮摇杆实时控制，基于 Jetpack Compose + Mapbox 构建。

[![Release](https://img.shields.io/badge/Release-Latest-orange?style=for-the-badge)](https://github.com/Malpl3naInk/MockLocation/releases)
[![Android CI](https://img.shields.io/github/actions/workflow/status/Malpl3naInk/MockLocation/build-android.yml?style=for-the-badge&label=Android%20CI)](https://github.com/Malpl3naInk/MockLocation/actions/workflows/build-android.yml)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue?style=for-the-badge)](LICENSE)
[![Language: Kotlin](https://img.shields.io/badge/Language-Kotlin-purple?style=for-the-badge)](https://kotlinlang.org/)
[![Platform: Android](https://img.shields.io/badge/Platform-Android-green?style=for-the-badge)](https://www.android.com/)
[![minSdk: 26](https://img.shields.io/badge/minSdk-26-3DDC84?style=for-the-badge)](https://developer.android.com/studio)

</div>

---

## 📑 目录

- [功能特性](#features)
- [工作原理](#how-it-works)
- [快速开始](#getting-started)
- [从源码构建](#building)
- [技术栈与依赖](#dependencies)
- [贡献指南](#contributing)
- [许可证](#license)
- [免责声明](#disclaimer)

---

<a name="features"></a>

## ✨ 功能特性

### 🎮 摇杆位置模拟

应用支持通过摇杆控制实时移动模拟位置：

- 运行**点位模拟**时会自动显示悬浮摇杆窗口
- 拖动摇杆即可控制移动**方向**与**速度**
- 速度根据摇杆偏离中心的距离**动态调整（0–100%）**
- 默认最大速度 `5 m/s`（约 18 km/h，相当于步行速度）
- 松开摇杆后自动停止移动，但保留最后方向（bearing 不归零）
- 支持隐藏/最小化/显示悬浮窗，并可自定义摇杆大小

### 📍 点位模拟

- 保存常用位置（名称 / 经纬度 / 海拔），一键开始模拟
- 支持手动输入坐标或直接在地图上选取
- 提供 **Google Plus Code**（开放位置码）解析支持
- 可编辑、删除、导出/分享已保存点位

### 🗺️ 路线模拟

- 在地图上以**路径点**方式绘制路线，支持拖拽编辑与增删
- 路径点可设为普通道路 / 人行道 / 环形道路等连接类型
- 支持**暂停 / 继续 / 复位正北**等路线控制
- 模拟过程中可叠加**随机偏移**，使移动轨迹更自然
- 可编辑、删除、导出/分享已保存路线

### ⚙️ 其他特性

- **速度预设**：步行 / 跑步 / 骑行等模板，可自定义最大速度
- **权限引导**：悬浮窗、后台定位、通知等权限一键授予向导
- **主题切换**：浅色 / 深色 / 跟随系统
- **多语言**：简体中文 / English（应用内可切换）
- **前台服务**：后台持续模拟位置，配合通知栏状态显示
- **崩溃与日志**：集成 Firebase Crashlytics，支持导出日志排查
- **开源许可证查看**：应用内提供第三方开源许可列表

<a name="how-it-works"></a>

## 🛠️ 工作原理

MockLocation 通过系统提供的“模拟位置”机制工作：

1. 在系统 **开发者选项 → 选择模拟位置信息应用** 中将本应用设为模拟位置提供方；
2. 应用通过**前台服务**向系统持续上报经纬度数据，其它应用即可读取到被模拟的位置。

模拟数据由 `LocationSimulator` 生成（静态点 / 动态路线），并经过 **Kalman 滤波** 平滑，配合速度、朝向与随机偏移控制，使移动轨迹更接近真实。

> Root / Xposed 等更底层的模拟方式仍在规划中。

<a name="getting-started"></a>

## 🚀 快速开始

### 下载

前往 [Releases](https://github.com/Malpl3naInk/MockLocation/releases) 下载最新 APK（支持 `arm64-v8a` 与 `armeabi-v7a`）。

> CI 自动构建产物与触发方式说明见 [Workflow README](.github/workflows/README.md)。

### 首次使用

1. 安装并打开应用，按引导授予**悬浮窗、通知、后台定位**等权限；
2. 在系统 **开发者选项 → 选择模拟位置信息应用** 中选中 MockLocation；
3. 回到主页，添加一个**点位**或**路线**；
4. 点击 **开始**，悬浮摇杆窗口出现后即可实时控制移动。

<a name="building"></a>

## 🔨 从源码构建

### 环境要求

| 工具 | 版本 |
|------|------|
| JDK | 17 |
| Android Gradle Plugin | 8.13.2 |
| Kotlin | 2.3.10 |
| Android SDK | compileSdk 36 · minSdk 26 · targetSdk 36 |

### 构建命令

```bash
# 调试包（可直接安装）
./gradlew assembleDebug

# Release 包（需通过环境变量配置签名）
# KEYSTORE_FILE / KEYSTORE_PASSWORD / KEY_ALIAS / KEY_PASSWORD \
#   ./gradlew assembleRelease
```

构建产物位于 `app/build/outputs/apk/`。

- `versionName` / `versionCode` 由 Git 提交信息自动推导（逻辑见 `app/build.gradle.kts`），本地构建为 `-alpha+local.<count>`，CI 构建为 `-beta+git.<hash>`；

<a name="dependencies"></a>

## 📦 技术栈与依赖

| 依赖 / 组件            | 版本        | 说明                                   |
|------------------------|-------------|----------------------------------------|
| Kotlin                 | 2.3.10      | 编程语言                               |
| Android Gradle Plugin  | 8.13.2      | 构建系统                               |
| KSP                     | 2.3.4       | 注解处理（Room）                      |
| Jetpack Compose BOM    | 2024.09.00  | 声明式 UI 工具链                       |
| Material3              | 1.4.0       | Material Design 组件                   |
| Activity Compose       | 1.12.0      | Activity-Compose 集成                  |
| Core KTX               | 1.17.0      | Kotlin 扩展                            |
| Lifecycle ViewModel    | 2.8.7       | MVVM 架构                              |
| Lifecycle Runtime      | 2.10.0      | 生命周期管理                           |
| Lifecycle Service      | 2.8.7       | Service 生命周期支持                   |
| SavedState KTX         | 1.4.0       | 状态保存                               |
| Room                   | 2.7.1       | 本地数据库                             |
| Gson                   | 2.10.1      | JSON 序列化                            |
| Mapbox Maps            | 11.19.0     | 地图渲染与交互                         |
| Open Location Code     | 1.0.4       | Google Plus Code 支持                  |
| Firebase（BOM）         | 33.12.0     | Analytics / Crashlytics                |

> 依赖版本以 [`gradle/libs.versions.toml`](gradle/libs.versions.toml) 为准。

第三方开源许可由 `licenseReleaseReport` 任务生成，可在应用内 **设置 → 开源许可证** 查看。

<a name="contributing"></a>

## 🤝 贡献指南

欢迎任何形式的贡献——Bug 反馈、功能建议或代码提交：

1. **报告问题**：请在 [Issues](https://github.com/Malpl3naInk/MockLocation/issues) 提交，附上复现步骤、机型/系统版本及日志（应用内支持导出日志）。
2. **功能建议**：请先在 [Issues](https://github.com/Malpl3naInk/MockLocation/issues) 中搜索是否已有相关讨论，避免重复。

<a name="license"></a>

## 📄 许可证

本项目采用 **GNU General Public License v3.0（GPL-3.0）** 开源。

- 完整条款见 [LICENSE](LICENSE)；
- 你可自由使用、修改与分发本软件，但**衍生作品必须以相同许可证开源**。

<a name="disclaimer"></a>

## ⚠️ 免责声明

- 本项目**仅用于学习与开发测试**，请勿用于任何违法、欺诈或违反第三方服务条款的用途；
- 使用本工具所产生的任何后果由使用者自行承担；
- 模拟位置可能影响依赖定位的第三方应用，请谨慎使用。
