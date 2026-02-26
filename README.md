# MockLocation

 - [Workflow README](.github/workflows/README.md)
 - [Development](#Development)

## Features

### 摇杆位置模拟
应用支持通过摇杆控制实时移动模拟位置：
- 在运行点位位置模拟时，会显示悬浮摇杆窗口
- 拖动摇杆可以控制移动方向和速度
- 速度：根据摇杆偏离中心的距离动态调整（0-100%）
- 默认最大速度：5 m/s（约18 km/h，相当于步行速度）
- 松开摇杆后自动停止移动，但保持最后的方向（bearing 不归零）

## Development

### Project Structure

```
MocakLocation
├─activity
│  ├─main
│  │  └─views
│  ├─settings
│  └─waypoint
│      └─views
├─data
│  ├─local
│  │  ├─db
│  │  └─repository
│  └─models
├─service
│  ├─locationService
│  │  ├─controller
│  │  └─state
│  └─overlayService
│      ├─state
│      └─views
├─ui
│  ├─components
│  ├─dialog
│  └─theme
└─utils
    ├─extensions
    ├─logger
    └─simulators
```

### Color system

| Paramater                               | Color          |
|-----------------------------------------|----------------|
| AlertDialog.containerColor              | surfaceVariant |
| BottomSheetScaffold.containerColor      | background     |
| BottomSheetScaffold.sheetContainerColor | surface        |

## Roadmap

### Feature requests from [ZCShou/GoGoGo](https://github.com/ZCShou/GoGoGo/issues)

- [x] [#355](https://github.com/ZCShou/GoGoGo/issues/355) - [路线模拟] 路线模拟功能请求
- [x] [#350](https://github.com/ZCShou/GoGoGo/issues/350) - [悬浮窗控制] 建议可以隐藏悬浮窗
- [x] [#346](https://github.com/ZCShou/GoGoGo/issues/346) - [悬浮窗控制] 最小化悬浮窗
- [ ] [#335](https://github.com/ZCShou/GoGoGo/issues/335) - [平台支持] 提供iOS版本
- [ ] [#331](https://github.com/ZCShou/GoGoGo/issues/331) - [地图功能] 添加地图复位回正，指向正北的功能
- [ ] [#325](https://github.com/ZCShou/GoGoGo/issues/325) - [平台支持] 加入对ARM v7的支持
- [x] [#314](https://github.com/ZCShou/GoGoGo/issues/314) - [悬浮窗控制] 添加悬浮框开启关闭的功能
- [x] [#304](https://github.com/ZCShou/GoGoGo/issues/304) - [悬浮窗控制] 请求摇杆隐藏/缩小
- [ ] [#284](https://github.com/ZCShou/GoGoGo/issues/284) - [权限管理] 通过Root或Lsposed实现功能，避免使用开发者选项
- [ ] [#279](https://github.com/ZCShou/GoGoGo/issues/279) - [模拟增强] 不移动摇杆时增加随机极小范围运动的选项
- [ ] [#254](https://github.com/ZCShou/GoGoGo/issues/254) - [模拟精度] 增加设置模拟精度的选项
- [ ] [#234](https://github.com/ZCShou/GoGoGo/issues/234) - [权限管理] Root权限一键选择模拟位置信息应用
- [x] [#185](https://github.com/ZCShou/GoGoGo/issues/185) - [悬浮窗控制] 悬浮窗关闭功能
- [ ] [#176](https://github.com/ZCShou/GoGoGo/issues/176) - [控制方式] 添加ADB定位控制功能
- [x] [#133](https://github.com/ZCShou/GoGoGo/issues/133) - [路线模拟] 增加线路模拟导航功能
- [x] [#118](https://github.com/ZCShou/GoGoGo/issues/118) - [国际化] 添加英语语言支持
- [ ] [#116](https://github.com/ZCShou/GoGoGo/issues/116) - [权限管理] 考虑加入Root支持
- [ ] [#103](https://github.com/ZCShou/GoGoGo/issues/103) - [模拟增强] 高度模拟功能
- [ ] [#88](https://github.com/ZCShou/GoGoGo/issues/88) - [地图功能] 加入多地图选择的功能，比如支持Mapbox
- [ ] [#77](https://github.com/ZCShou/GoGoGo/issues/77) - [路线控制] 摇杆移动添加控制整体完成时间功能
- [ ] [#50](https://github.com/ZCShou/GoGoGo/issues/50) - [模拟增强] 加入基站伪装以实现更全面的虚拟定位
- [ ] [#46](https://github.com/ZCShou/GoGoGo/issues/46) - [模拟增强] 模拟步频功能
- [ ] [#35](https://github.com/ZCShou/GoGoGo/issues/35) - [悬浮窗控制] 摇杆大小调整和路径点显示

### Suggested Features

- [ ] [数据管理] 点位和路线的分组管理（收藏夹功能）
- [ ] [数据管理] 历史记录功能，记录最近使用的位置
- [ ] [用户体验] 快速切换面板，在常用位置间快速切换
- [ ] [数据导入导出] 支持GPX格式的导入和导出
- [ ] [数据导入导出] 支持KML格式的导入和导出
- [ ] [地图功能] 位置搜索功能（地址搜索和POI搜索）
- [ ] [轨迹功能] 轨迹录制和回放功能
- [ ] [速度控制] 预设速度模板（步行、跑步、骑行、驾车等）
- [ ] [模拟增强] 根据经纬度自动获取真实海拔数据
- [ ] [自动化] 定时任务功能（定时启动/停止模拟）
- [ ] [路线模拟] 多点随机模式，在多个点位之间随机切换
- [ ] [路线模拟] 路线平滑处理，使移动轨迹更自然
- [ ] [性能优化] 后台省电模式
- [ ] [数据管理] 配置备份和恢复功能
- [ ] [模拟增强] 信号强度模拟
- [ ] [模拟增强] GPS卫星数量模拟
- [ ] [地图功能] 离线地图支持
- [ ] [用户体验] 主题切换功能（深色/浅色模式）
- [ ] [悬浮窗控制] 悬浮窗透明度调节
- [ ] [悬浮窗控制] 悬浮窗大小自定义
- [ ] [路线控制] 路线暂停/继续功能
- [ ] [路线控制] 路线进度显示和跳转
- [x] [数据管理] 点位和路线的导出分享功能
- [ ] [安全性] 应用锁功能（密码/生物识别保护）


## Dependences

| Dependency                        | Version        | Purpose                           |
|------------------------------------|---------------|-----------------------------------|
| Core KTX                           | 1.17.0        | Kotlin extensions                 |
| Jetpack Compose BOM                | 2024.09.00    | UI toolkit                        |
| Activity Compose                   | 1.12.0        | Activity-Compose integration      |
| Material3                          | 1.4.0         | UI Components                     |
| Lifecycle ViewModel                | 2.8.7         | MVVM Architecture                 |
| Lifecycle Runtime                  | 2.10.0        | Lifecycle management              |
| Lifecycle Service                  | 2.8.7         | Service lifecycle support         |
| SavedState KTX                     | 1.4.0         | State preservation                |
| Room                               | 2.6.1         | Database storage                  |
| Gson                               | 2.10.1        | JSON serialization                |
| Google Open Location Code          | 1.0.4         | Plus codes support                |

> 依赖版本以 `build.gradle` 文件为准。

[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B30142%2Fmocklocation.svg?type=large&issueType=license)](https://app.fossa.com/projects/custom%2B30142%2Fmocklocation?ref=badge_large&issueType=license)
