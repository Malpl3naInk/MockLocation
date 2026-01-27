# MockLocation

[DEV DOC](DEVDOC.md) | [ACTION DOC](.github/workflows/README.md)

## 功能特性

### 摇杆位置模拟
应用支持通过摇杆控制实时移动模拟位置：
- 在运行点位位置模拟时，会显示悬浮摇杆窗口
- 拖动摇杆可以控制移动方向和速度
- 速度：根据摇杆偏离中心的距离动态调整（0-100%）
- 默认最大速度：5 m/s（约18 km/h，相当于步行速度）
- 松开摇杆后自动停止移动，但保持最后的方向（bearing 不归零）

### 技术实现
- `StaticPointSimulator`：点位模拟器，支持根据摇杆输入动态计算位置变化
- `JoystickState`：使用 StateFlow 在 UI 和模拟器之间共享摇杆状态
- `FloatingUI`：提供摇杆控制界面，实时显示方向角度和速度百分比

## Roadmap

### Feature requests from [ZCShou/GoGoGo](https://github.com/ZCShou/GoGoGo/issues)

| Issue                                               | Subject                                                      |
|-----------------------------------------------------|--------------------------------------------------------------|
| [#355](https://github.com/ZCShou/GoGoGo/issues/355) | [功能] 路线模拟功能请求                                      |
| [#350](https://github.com/ZCShou/GoGoGo/issues/350) | [Feature] 建议可以隐藏悬浮窗                                 |
| [#346](https://github.com/ZCShou/GoGoGo/issues/346) | [Feature] 最小化悬浮窗                                       |
| [#335](https://github.com/ZCShou/GoGoGo/issues/335) | [Feature] 提供ios版本                                        |
| [#331](https://github.com/ZCShou/GoGoGo/issues/331) | [Feature] 添加地图复位回正，指向正北的功能                    |
| [#325](https://github.com/ZCShou/GoGoGo/issues/325) | 可以加入对arm v7的支持吗？                                    |
| [#314](https://github.com/ZCShou/GoGoGo/issues/314) | [Feature] New function，哥哥可否添加一个悬浮框开启关闭的功能？ |
| [#304](https://github.com/ZCShou/GoGoGo/issues/304) | [Feature] New function (请求摇杆隐藏/缩小)                   |
| [#284](https://github.com/ZCShou/GoGoGo/issues/284) | [Feature] 通过root或者lsposed实现功能，避免使用开发者选项     |
| [#279](https://github.com/ZCShou/GoGoGo/issues/279) | [Feature] 不移动摇杆时增加随机极小范围运动的选项             |
| [#254](https://github.com/ZCShou/GoGoGo/issues/254) | [Feature] 可否增加设置模拟精度的选项？                        |
| [#234](https://github.com/ZCShou/GoGoGo/issues/234) | [Feature] New function (root 权限一键选择模拟位置信息应用)   |
| [#185](https://github.com/ZCShou/GoGoGo/issues/185) | 悬浮窗关闭功能                                               |
| [#176](https://github.com/ZCShou/GoGoGo/issues/176) | [Feature] 添加 ADB 定位控制功能                              |
| [#133](https://github.com/ZCShou/GoGoGo/issues/133) | 增加线路模拟导航功能                                         |
| [#118](https://github.com/ZCShou/GoGoGo/issues/118) | [Feature] Add English language support                       |
| [#116](https://github.com/ZCShou/GoGoGo/issues/116) | [Feature] 考虑加root吗                                       |
| [#103](https://github.com/ZCShou/GoGoGo/issues/103) | [Feature] 请问有高度模拟吗？                                  |
| [#88](https://github.com/ZCShou/GoGoGo/issues/88)   | [Feature] 能否加入多地图选择的功能，比如支持 mapbox           |
| [#77](https://github.com/ZCShou/GoGoGo/issues/77)   | [Feature] 摇杆移动添加控制整体完成时间功能                   |
| [#50](https://github.com/ZCShou/GoGoGo/issues/50)   | [Feature] 加入基站伪装以实现更全面的虚拟定位                 |
| [#46](https://github.com/ZCShou/GoGoGo/issues/46)   | [Feature] New function (能否模拟步频等)                      |
| [#35](https://github.com/ZCShou/GoGoGo/issues/35)   | [Feature] 关于摇杆的一些建议（大小、路径点等）                  |


[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B30142%2Fmocklocation.svg?type=large&issueType=license)](https://app.fossa.com/projects/custom%2B30142%2Fmocklocation?ref=badge_large&issueType=license)
