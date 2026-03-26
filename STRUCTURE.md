# Project Structure

## UI Layer (Activity/Screen)

```
MainActivity
│
├── MainScreen
│   ├── PageMainView ───────► SettingsActivity
│   │                            ├── SettingsScreen
│   │                            ├── SettingSpeedPreset
│   │                            ├── SettingThemeMode
│   │                            ├── SettingJoystickSize
│   │                            └── SettingRandomOffset
│   │
│   └── PageSimulationView
│       ├── OptionsPointView ───────► MapPickerActivity
│       │                                └── MapPickerScreen
│       │
│       ├── OptionsRouteView ───────► WaypointActivity
│       │   │                            ├── WaypointScreen
│       │   │                            ├── SheetPointView
│       │   │                            └── SheetDetailView
│       │   │
│       │   └── (Export) ───────────► System File Picker
│       │
│       ├── SheetPointsView
│       └── SheetRoutesView ───────► WaypointActivity (Edit)
│
├── MapPickerActivity
│   └── MapPickerScreen
│
├── WaypointActivity
│   ├── WaypointScreen
│   ├── WaypointViewModel
│   ├── SheetPointView
│   └── SheetDetailView
│
└── SettingsActivity
    ├── SettingsScreen
    ├── SettingSpeedPreset
    ├── SettingThemeMode
    ├── SettingJoystickSize
    └── SettingRandomOffset
```

## Service Layer

```
service/
├── locationService/
│   ├── LocationService
│   ├── controller/
│   │   ├── MockLocationController
│   │   ├── RealLocationController
│   │   ├── TestProviderManager
│   │   ├── NotificationController
│   │   └── OverlayServiceController
│   └── state/
│       ├── LocationStateHolder
│       └── LocationMode
│
└── overlayService/
    ├── OverlayService
    ├── OverlayScreen
    ├── state/
    │   └── OverlayStateHolder
    └── views/
        ├── OverlayPointView
        └── OverlayRouteView
```

## Data Layer

```
data/
├── local/
│   ├── db/
│   │   ├── AppDatabase
│   │   ├── MockPointDao
│   │   ├── MockRouteDao
│   │   ├── MockPointEntity
│   │   └── MockRouteEntity
│   │
│   ├── repository/
│   │   ├── MockServiceStatusRepository
│   │   └── MockServiceStatus
│   │
│   ├── PrefsHelper
│   └── FileHelper
│
└── models/
    ├── RouteObject
    └── CandidateLocation
```

## UI Components & Dialogs

```
ui/
├── components/
│   ├── JoystickControl
│   ├── PillSelector
│   ├── LatLngScatter
│   ├── CircleIconButton
│   └── DebugOnly
│
├── dialog/
│   ├── AddWaypointDialog
│   ├── DeleteConfirmDialog
│   ├── UnsavedChangesDialog
│   ├── ErrorDialog
│   └── RequestPermissionDialog
│
└── theme/
    ├── Theme
    └── Type
```

## Utils

```
utils/
├── simulators/
│   ├── LocationSimulator (interface)
│   ├── StaticPointSimulator
│   └── DynamicRouteSimulator
│
├── extensions/
│   ├── StringExtensions
│   ├── TextFieldStateExtensions
│   ├── TextFieldBufferExtensions
│   └── RouteObjectExtensions
│
├── logger/
│   ├── Logger
│   ├── LoggerFile
│   └── CrashHandler
│
├── PermissionHelper
├── PermissionHandler
├── RouteHelper
├── AzimuthHelper
├── LatLngInputFilter
├── KalmanFilter
└── Enums
```

## Native Layer

```
native/
├── NativeLib
└── mocklocation (C++ library)
```
