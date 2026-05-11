# 定时提醒助手 - 应用规格文档

## 1. 项目概述

- **项目名称**: ReminderApp (定时提醒助手)
- **项目类型**: Android 原生应用
- **核心功能**: 支持用户设置一次性定时提醒任务，到时推送系统级通知并响铃震动

## 2. 技术栈

- **语言**: Kotlin 1.9.x
- **最小SDK**: 26 (Android 8.0)
- **目标SDK**: 34 (Android 14)
- **UI框架**: Jetpack Compose + Material 3
- **架构**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **数据库**: Room (本地存储提醒)
- **定时机制**: AlarmManager (精确闹钟) + BroadcastReceiver
- **通知**: NotificationManager + 通知渠道
- **异步**: Kotlin Coroutines + Flow

## 3. 功能列表

### 3.1 提醒管理
- [x] 创建一次性定时提醒（选择年、月、日、时、分）
- [x] 填写提醒标题（必填）
- [x] 填写提醒详情（可选）
- [x] 查看所有已设置提醒列表
- [x] 删除单个提醒
- [x] 清空所有提醒

### 3.2 提醒触发
- [x] 精确时间触发（到秒级）
- [x] 系统级通知（高优先级）
- [x] 响铃提醒
- [x] 震动提醒
- [x] 点击通知跳转到应用

### 3.3 后台稳定性
- [x] 使用AlarmManager确保精确唤醒
- [x] 设置重复闹钟（如果设备重启）
- [x] 兼容Android 12+ 精确闹钟权限
- [x] 兼容Android 14 后台限制

## 4. UI/UX 设计方向

- **视觉风格**: Material Design 3，简洁现代
- **主色调**: 蓝色系 (#1976D2)
- **布局**: 单页面 + 底部弹出/对话框
  - 首页：显示提醒列表 + FAB添加按钮
  - 添加提醒：日期时间选择器 + 标题/详情输入
- **交互**: 滑动删除提醒、长按菜单

## 5. 数据模型

```
Reminder:
  - id: Long (主键自增)
  - title: String (标题)
  - content: String? (详情，可空)
  - triggerTime: Long (触发时间戳)
  - createdAt: Long (创建时间戳)
  - isTriggered: Boolean (是否已触发)
```