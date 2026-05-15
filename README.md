# 定时提醒 App - ReminderApp

## 项目结构

```
ReminderApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/reminder/app/
│   │   │   ├── MainActivity.kt          # 主界面
│   │   │   ├── AddReminderActivity.kt   # 添加提醒
│   │   │   ├── ReminderAdapter.kt       # 列表适配器
│   │   │   ├── Reminder.kt              # 数据模型
│   │   │   ├── ReminderDatabase.kt      # Room数据库
│   │   │   ├── ReminderDao.kt           # 数据库操作
│   │   │   ├── ReminderReceiver.kt      # 广播接收器
│   │   │   ├── ReminderService.kt       # 前台服务(保活)
│   │   │   └── App.kt                   # Application类
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml
│   │   │   │   ├── activity_add_reminder.xml
│   │   │   │   └── item_reminder.xml
│   │   │   ├── values/
│   │   │   │   ├── strings.xml
│   │   │   │   ├── colors.xml
│   │   │   │   └── themes.xml
│   │   │   └── drawable/
│   │   │       └── ic_notification.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/wrapper/
```

## 核心功能实现

### 1. 数据层 - Room数据库
- 存储提醒：id, title, content, triggerTime, isEnabled

### 2. 定时机制
- 使用 AlarmManager + BroadcastReceiver
- 兼容Android 12+的精确闹钟权限
- 配合前台服务提高后台存活率

### 3. 通知
- 系统级通知
- 响铃+震动
- 点击通知打开App

### 4. 后台保活
- 使用前台服务(Notification)保持进程
- 适配国产ROM的特殊要求
test
