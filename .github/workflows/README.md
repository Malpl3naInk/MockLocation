# Github CI

```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃                   GitHub Actions 触发器                   ┃
┗━━━━━━━━━━━━━━━━━━━━━━┳━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
                        │
         ┌──────────────┼──────────────┬─────────────────┐
         │              │              │                 │
    ┌────▼────┐   ┌─────▼──────┐  ┌────▼────────┐  ┌────▼────────┐
    │  push   │   │pull_request│  │workflow_    │  │本地开发      │
    │ (main)  │   │            │  │dispatch     │  │./gradlew    │
    └────┬────┘   └─────┬──────┘  └────┬────────┘  └────┬────────┘
         │              │              │                │
         │              │              │ 手动触发       │ 无参数
         │              │              │ VERSION_NAME  │
         │              │              │ (必填)        │
         │              │              │ VERSION_      │
         │              │              │ SUFFIX=""     │
         └──────┬───────┴──────────────┘                │
                │                                       │
    ┌───────────▼────────────┐              ┌──────────▼─────────┐
    │  GitHub Actions 环境   │              │   本地开发环境      │
    │                        │              │                    │
    │ push/PR:               │              │ 读取 global        │
    │ -PACTION               │              │ gradle.properties  │
    │ -PVERSION_SUFFIX=beta  │              │                    │
    │                        │              │ 设置环境变量:       │
    │ 设置环境变量:           │              │ ┌────────────────┐ │
    │ ┌────────────────────┐ │              │ │ KEYSTORE_FILE  │ │
    │ │ KEYSTORE_FILE      │ │              │ │ KEYSTORE_*     │ │
    │ │ KEYSTORE_PASSWORD  │ │              │ │ KEY_ALIAS      │ │
    │ │ KEY_ALIAS          │ │              │ │ KEY_PASSWORD   │ │
    │ │ KEY_PASSWORD       │ │              │ └────────────────┘ │
    │ └────────────────────┘ │              └──────────┬─────────┘
    └───────────┬────────────┘                         │
                │                                      │
                └──────────────┬───────────────────────┘
                               │
                  ┌────────────▼────────────┐
                  │  Gradle 构建系统启动     │
                  └────────────┬────────────┘
                               │
                  ┌────────────▼────────────┐
                  │ 执行 Git 命令           │
                  │ ┌────────────────────┐  │
                  │ │ gitCommitCount()   │  │
                  │ │ → 42               │  │
                  │ │ gitCommitHash()    │  │
                  │ │ → "abc1234"        │  │
                  │ └────────────────────┘  │
                  └────────────┬────────────┘
                               │
         ┌─────────────────────┼─────────────────────┐
         │                     │                     │
    ┌────▼────────┐   ┌───────▼─────────┐   ┌───────▼────────┐
    │ push/PR     │   │workflow_dispatch│   │  本地构建       │
    │ (自动触发)   │   │  (手动触发)     │    │  (无参数)      │
    └────┬────────┘   └───────┬─────────┘   └───────┬────────┘
         │                    │                    │
         │ ACTION=true        │ ACTION=false       │ ACTION=false
         │ VERSION_SUFFIX     │ VERSION_SUFFIX     │ VERSION_SUFFIX
         │   = "beta"         │   = "" (空)        │   = null
         │                    │ VERSION_NAME       │
         │                    │   (必填)           │
         │                    │                    │
    ┌────▼────────────────────┐┐                ┌───▼──────────┐
    │ 版本后缀:              ││                │版本后缀:     │
    │   "-beta+git.abc1234"  ││                │ "-alpha"     │
    │                        ││                │ "+local.42"  │
    └────┬───────────────────┘│                └───┬──────────┘
         │                    │                    │
    ┌────▼────────────────┐ ┌─▼────────────────┐ ┌─▼────────────────┐
    │ 最终版本名:         │ │ 最终版本名:       │ │ 最终版本名:       │
    │ "1.0.0-beta        │ │ "{VERSION_NAME}" │ │ "1.0.0-alpha"    │
    │  +git.abc1234"     │ │ (纯净无后缀)      │ │ "+local.42"      │
    │                    │ │                  │ │                  │
    │ 示例: 1.0.0-beta   │ │ 示例: 2.0.0      │ │ 示例: 1.0.0-alpha│
    │       +git.abc1234 │ │ (输入的版本)     │ │       +local.42  │
    └────────┬────────────┘ └────────┬──────────┘ └────────┬─────────┘
             │                       │                     │
             └───────────┬───────────┴─────────────────────┘
                         │
                  ┌──────▼──────────────┐
                  │   生成 APK 文件      │
                  │                     │
                  │ app/build/outputs/  │
                  │   apk/release/*.apk │
                  └──────┬──────────────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
    ┌────▼────────┐ ┌────▼────────┐ ┌────▼─────────────┐
    │ SSH 隧道    │ │ MinIO 上传  │ │ GitHub Actions:  │
    │ ssh -L ... │ │ mc cp ...   │ │ 安装 MinIO 客户端│
    └─────────────┘ └─────────────┘ └──────────────────┘
```

## Secrets 配置

需要在 GitHub Repository Settings → Secrets and variables → Actions 中配置以下 secrets：

| Secret 名称 | 说明 |
|-------------|------|
| `KEYSTORE_FILE_BASE64` | 签名密钥库文件的 Base64 编码 |
| `KEYSTORE_PASSWORD` | 密钥库密码 |
| `KEY_ALIAS` | 密钥别名 |
| `KEY_PASSWORD` | 密钥密码 |
| `SSH_PRIVATE_KEY` | SSH 私钥（用于建立隧道） |
| `SSH_HOST` | SSH 远程主机地址 |
| `SSH_USER` | SSH 用户名 |
| `MINIO_ACCESS_KEY` | MinIO 访问密钥 |
| `MINIO_SECRET_KEY` | MinIO 密钥 |

## 工作流程说明

### 触发条件

1. **push** - 推送到 `main` 或 `master` 分支时触发
2. **pull_request** - 创建或更新 PR 时触发
3. **workflow_dispatch** - 手动触发，**必须**输入参数：
   - `version_name` (必填): 版本名，如 `2.0.0`

### 构建步骤

1. **检出代码** - 获取完整 git 历史（用于计算 versionCode 和获取 commit hash）
2. **设置 JDK 17** - 使用 Eclipse Temurin 发行版
3. **解码密钥库** - 将 Base64 编码的密钥库解码为文件
4. **生成开源许可证** - 运行 `licenseReleaseReport` 任务
5. **构建 Release APK** - 根据触发类型设置不同参数：
   - push/PR: `-PACTION -PVERSION_SUFFIX=beta` → `1.0.0-beta+git.xxx`
   - 手动触发: `-PVERSION_NAME={输入版本} -PVERSION_SUFFIX=` → `{version_name}` (纯净版本)
6. **安装 MinIO 客户端** - 下载并安装 `mc` 工具
7. **建立 SSH 隧道** - 连接到远程主机并转发 MinIO 端口（9000）
8. **配置 MinIO** - 设置 MinIO 别名
9. **上传 APK** - 将构建好的 APK 上传到 MinIO 存储桶

### 版本命名规则

根据构建来源，版本名会自动添加不同后缀：

| 构建来源 | 触发方式 | 版本名示例 | 说明 |
|---------|---------|-----------|------|
| GitHub Actions | push/PR 到 main/master | `1.0.0-beta+git.abc1234` | beta 后缀 + git commit hash |
| GitHub Actions | workflow_dispatch (手动) | `2.0.0` | 纯净版本，使用输入的 version_name |
| 本地开发 | `./gradlew assembleRelease` | `1.0.0-alpha+local.42` | alpha 后缀 + commit 计数 |
| 本地开发 (自定义) | `./gradlew assembleRelease -PVERSION_NAME=2.0.0` | `2.0.0-alpha+local.42` | 自定义版本名 + alpha 后缀 |

versionCode 基于 git commit 数量自动生成。