# 定时提醒助手 - 应用规格文档

## 1. 项目概述

- **项目名称**: ReminderApp (定时提醒助手)
- **项目类型**: Android 原生应用
- **核心功能**: 支持用户设置一次性/循环定时提醒任务，到时推送系统级通知并响铃震动
- **风格**: 极简清爽商务风，深色模式+浅色模式双适配

## 2. 技术栈

- **语言**: Kotlin 1.9.x
- **最小SDK**: 26 (Android 8.0)
- **目标SDK**: 34 (Android 14)
- **UI框架**: ViewBinding + Material Components
- **架构**: MVVM
- **本地存储**: SharedPreferences（无 Room，无 Hilt）
- **定时机制**: AlarmManager (精确闹钟) + Foreground Service
- **通知**: NotificationManager + 通知渠道
- **异步**: Kotlin Coroutines

## 3. 功能列表

### 3.1 提醒类型
- [x] 单次提醒（未来任意日期时间）
- [x] 每日循环
- [x] 工作日循环（周一到周五）
- [x] 每周循环（可选周一/周二/.../周日）

### 3.2 提醒管理
- [x] 创建/编辑/删除闹钟
- [x] 一键开启/关闭闹钟
- [x] 远期日期选择（年月日+时刻）
- [x] 提前5分钟/10分钟预备提醒

### 3.3 提醒触发
- [x] 精确时间触发
- [x] 系统级通知（高优先级）
- [x] 响铃 + 震动
- [x] 点击通知跳转应用

### 3.4 后台稳定性
- [x] Foreground Service 常驻
- [x] AlarmManager 精确唤醒
- [x] 兼容 Android 12+ 精确闹钟权限
- [x] 兼容 Android 14 后台限制

### 3.5 快捷预设
- [x] 内置预设文案：「向OpenClaw提交设计需求」

### 3.6 设置
- [x] 铃声选择
- [x] 重复周期设置
- [x] 提醒延时设置
- [x] 后台保活开关
- [x] 深色/浅色模式切换

## 4. UI 设计

### 4.1 主题
- 极简清爽商务风，无广告
- 浅色模式：白色背景 + 蓝色强调 (#1976D2)
- 深色模式：深灰背景 (#121212) + 浅蓝强调 (#64B5F6)
- 圆角卡片式布局

### 4.2 主页面
- 顶部：标题栏 + 设置按钮
- 中部：闹钟列表（RecyclerView），每个条目显示：
  - 时间（大字体）
  - 日期/周期描述
  - 备注（如果有）
  - 开启/关闭开关
  - 编辑/删除按钮
- 底部：FloatingActionButton（+）新增闹钟
- 空状态：简洁插画 + 提示文字

### 4.3 新增/编辑闹钟页
- Toolbar + 返回按钮
- 日期按钮 → 弹出日历选择
- 时间按钮 → 弹出时间选择
- 周期选择：单次/每日/工作日/每周
- 提前提醒：关闭/5分钟/10分钟
- 备注输入框（带快捷预设按钮）
- 保存按钮

### 4.4 设置页
- 铃声选择（系统铃声列表）
- 提醒延时（关闭/5分钟/10分钟）
- 后台保活（开关）
- 深色模式（开关/跟随系统）
- 版本信息

## 5. 数据模型

```
Reminder:
  - id: Long (主键自增)
  - title: String (标题)
  - content: String? (详情，可空)
  - triggerTime: Long (触发时间戳，毫秒)
  - repeatMode: Enum (ONCE/DAILY/WEEKDAYS/WEEKLY)
  - repeatDays: Int (周几，bitmask：1=周一，2=周二，...)
  - preNotifyMinutes: Int (0/5/10)
  - isEnabled: Boolean
  - createdAt: Long

Settings:
  - ringtoneUri: String?
  - defaultPreNotify: Int
  - keepAliveEnabled: Boolean
  - darkMode: Enum (LIGHT/DARK_SYSTEM)
```

## 6. 权限

- POST_NOTIFICATIONS (Android 13+)
- SCHEDULE_EXACT_ALARM
- VIBRATE
- FOREGROUND_SERVICE
- FOREGROUND_SERVICE_SPECIAL_USE
- RECEIVE_BOOT_COMPLETED