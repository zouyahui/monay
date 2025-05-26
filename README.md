# 简易记账App

## 项目概述

简易记账App是一款旨在帮助用户轻松管理个人财务的应用程序。通过简洁直观的界面设计，用户可以快速添加、编辑和查看他们的收支情况，并通过图表清晰地了解自己的财务状况。

### 技术栈

- **编程语言**: Kotlin
- **UI框架**: Jetpack Compose
- **本地数据库**: Room
- **架构模式**: MVVM
- **依赖注入**: Hilt
- **网络请求**: Retrofit (如果适用)
# 安卓记账工具

## 产品简介

本安卓记账工具致力于为用户提供便捷、高效的自动记账体验。通过自动读取支付宝与微信的支付信息，实现实时同步、自动分类、智能统计等功能，帮助用户轻松管理个人财务。

## 核心功能

- 自动读取支付宝支付信息并记账
- 自动读取微信支付信息并记账
- 实时同步支付宝和微信的消费记录
- 自动识别支付宝和微信的收支通知
- 无需手动输入，自动生成账单
- 支持支付宝、微信支付的账单分类
- 自动获取支付宝、微信的交易详情
- 实时推送消费提醒并自动记账
- 自动同步支付宝、微信的余额变动
- 一键导入支付宝、微信的历史账单
- 支持多账户自动记账（支付宝、微信）
- 自动识别转账、收款、消费等多种交易类型
- 自动统计支付宝、微信的月度/年度支出
- 支持支付宝、微信账单的自动备份
- 自动同步支付宝、微信的红包、转账、消费等信息

## 使用说明

1. 安装应用后，授权读取通知权限。
2. 绑定支付宝与微信账户。
3. 开启自动记账功能，系统将自动同步并分类所有收支信息。
4. 可在账单页面查看详细的消费、收入、转账等记录。
5. 支持导入历史账单，便于数据迁移与备份。

## 隐私与安全

- 所有数据仅存储于本地，绝不上传云端。
- 仅在用户授权下读取通知内容。
- 支持一键备份与恢复账单数据。

## 联系与反馈

如有建议或问题，请通过应用内反馈功能联系我们。 
## 功能模块

1. **账单录入模块**
   - 快速添加收入与支出记录。
2. **统计报表模块**
   - 图形化展示用户的财务状况。
3. **数据同步模块**
   - 支持云端备份与恢复（可选）。
4. **用户设置模块**
   - 货币单位设置、通知提醒等个性化选项。

## 页面结构

- 主界面: 列表视图显示所有记录，支持筛选功能。
- 添加/编辑界面: 输入新账单信息或编辑现有记录。
- 统计页面: 图形化展示财务状况。
- 设置页面: 提供用户偏好设置入口。

## 如何贡献

如果您希望为本项目做出贡献，请遵循以下步骤：

1. Fork这个仓库到您的GitHub账户。
2. 创建一个描述性的分支 (`git checkout -b feature/AmazingFeature`)。
3. 进行您的更改，并确保您已阅读并遵循[代码规范](#代码规范)。
4. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)。
5. 推送到分支 (`git push origin feature/AmazingFeature`)。
6. 打开Pull Request。

## 更新README指南

每当您对代码进行更新时，请记得同步更新README.md文件，以反映最新的变化。以下是建议的更新内容：

- **新增功能**：简要描述新增的功能及其用途。
- **修复问题**：列出已解决的问题或bug。
- **改进措施**：描述任何性能优化或其他改进措施。
- **依赖库更新**：如果有依赖库版本更新，请在此处说明。

例如：

### 代码清理（2024-07-25）

- 移除了 `app/src/main/java/com/example/monay/notification/MyNotificationListener.kt` 中未使用的 `title` 和 `text` 变量。

### 配置更新（2024-07-25）

- 在 `app/src/main/AndroidManifest.xml` 中，将 `MainActivity` 的 `android:name` 属性修改为 `.Hilt_MainActivity`，以解决运行时 `ClassNotFoundException`。

### v1.0.2 更新日志（2024-03-21）

- 修复问题：将 MainActivity 的父类从 ComponentActivity 改为 AppCompatActivity，以解决 Hilt 依赖注入问题
- 改进措施：优化了 MainActivity 的代码结构，移除了未使用的导入和变量
- 技术更新：确保 Hilt 与 AppCompatActivity 正确集成

### v1.0.3 更新日志（2024-03-21）

- 修复问题：修正了 build.gradle.kts 中 Lint 配置的语法错误，将单引号改为双引号
- 改进措施：优化了 Gradle 构建配置的语法规范

### v1.0.4 更新日志（2024-03-21）

- 修复问题：修正了 build.gradle.kts 中 Lint 配置的语法，使用正确的 Kotlin DSL 语法
- 改进措施：优化了 Gradle 构建配置，禁用 Lint 检查以加快构建速度

### v1.0.5 更新日志（2024-03-21）

- 修复问题：修正了 AndroidManifest.xml 中 MainActivity 的配置，将 android:name 属性改回 .MainActivity
- 改进措施：优化了 AndroidManifest.xml 的配置，确保 Hilt 依赖注入正常工作

### v1.0.6 更新日志（2024-03-21）

- 修复问题：添加了 KSP 配置，确保 Hilt 注解处理器正确生成代码
- 改进措施：优化了 Gradle 构建配置，添加了 Room 数据库的 schema 位置配置

### v1.0.7 更新日志（2024-03-21）

- 修复问题：更新了 Hilt 依赖配置，使用明确的版本号而不是版本目录
- 改进措施：优化了依赖管理，确保 Hilt 注解处理器正确工作

### v1.0.8 更新日志（2024-03-21）

- 修复问题：更新了项目级 build.gradle.kts 中的 Hilt 插件配置，使用明确的版本号
- 改进措施：优化了 Gradle 插件配置，确保 Hilt 注解处理器正确工作

### v1.0.9 更新日志（2024-03-21）

- 问题说明：出现 SDK XML 版本不匹配警告（SDK XML versions up to 3 but an SDK XML file of version 4 was encountered）
- 解决方案：
  1. 此警告不影响应用正常运行，仅表示 Android Studio 和命令行工具版本存在差异
  2. 建议通过 Android Studio 的 SDK Manager 更新以下组件：
     - Android SDK Build-Tools
     - Android SDK Command-line Tools
     - Android SDK Platform-Tools
  3. 更新步骤：
     - 打开 Android Studio
     - 进入 Tools -> SDK Manager
     - 在 SDK Tools 标签页中更新相关组件
- 改进措施：优化了构建配置，确保项目可以正常构建和运行

### v1.0.10 更新日志（2024-03-21）

- 修复问题：修复了应用启动闪退问题，错误信息为 "You need to use a Theme.AppCompat theme (or descendant) with this activity"
- 解决方案：
  1. 将 themes.xml 中的主题父类从 `android:Theme.Material.Light.NoActionBar` 改为 `Theme.AppCompat.Light.NoActionBar`
  2. 确保主题与 AppCompatActivity 兼容
- 改进措施：优化了应用的主题配置，确保与 AppCompat 组件正确集成

### v1.0.11 更新日志（2024-03-21）

- 新增功能：实现了"添加账单"功能，用户可以通过浮动操作按钮进入添加界面并保存账单记录。
- 涉及文件：`app/src/main/java/com/example/monay/ui/AddBillScreen.kt`, `app/src/main/java/com/example/monay/ui/BillListScreen.kt`, `app/src/main/java/com/example/monay/MainActivity.kt`, `app/src/main/java/com/example/monay/viewmodel/BillViewModel.kt`
- 改进措施：集成了 Navigation Compose，实现了 BillListScreen 到 AddBillScreen 的导航；在 BillViewModel 中添加了 insertBill 方法，通过 Repository 保存账单数据。

### v1.1.0 UI/UX改进更新日志（2024-07-26）

- 新增功能：全面改进应用UI/UX设计，打造专业美观的财务管理界面
- 涉及文件：
  - `app/src/main/java/com/example/monay/ui/theme/Color.kt`
  - `app/src/main/java/com/example/monay/ui/theme/Theme.kt`
  - `app/src/main/java/com/example/monay/ui/BillListScreen.kt`
  - `app/src/main/java/com/example/monay/ui/AddBillScreen.kt`
  - `app/src/main/java/com/example/monay/MainActivity.kt`

- 主要改进：
  1. **全新配色方案**：
     - 创建专业财务应用配色系统，包含主色调、明暗模式颜色
     - 为不同账单类型（收入/支出/转账）设置独特的颜色标识
     - 添加图表专用色彩组合，为未来的数据可视化做准备

  2. **账单列表界面优化**：
     - 重新设计账单卡片布局，采用现代圆角卡片设计
     - 增加类别图标与颜色标识，使不同类型账单一目了然
     - 优化账单金额显示，根据类型自动添加"+"或"-"前缀
     - 添加空状态界面，引导用户添加第一笔账单
     - 改善顶部栏设计，统一应用颜色风格

  3. **记账界面全面升级**：
     - 添加直观的账单类型选择器（支出/收入/转账）
     - 设计专业的金额输入区域，根据账单类型自动变色
     - 引入可视化的分类选择器，配有图标和横向滚动功能
     - 增加日期选择器，允许用户修改账单日期
     - 改进备注输入区域的易用性
     - 优化保存按钮样式与交互逻辑

  4. **整体体验提升**：
     - 统一全应用的圆角、阴影和间距设计
     - 优化字体大小、粗细和颜色对比度
     - 添加细节交互动效和状态反馈
     - 改善整体视觉层次和信息架构

- 技术亮点：
  - 使用 Material Design 3 组件和主题系统
  - 实现自适应状态栏颜色，与应用主题保持一致
  - 添加类型安全的颜色系统，便于未来扩展
  - 采用组件化设计，将UI元素拆分为可复用组件

### v1.1.1 问题修复（2024-07-26）

- 修复问题：解决编译时Material Icons引用未解析的问题
- 解决方案：
  - 添加Material Icons扩展依赖：`implementation("androidx.compose.material:material-icons-extended:1.5.4")`
  - 修复组件中缺少的import语句，如`import androidx.compose.foundation.background`和`import androidx.compose.foundation.border`
- 涉及文件：
  - `app/build.gradle.kts`
  - `app/src/main/java/com/example/monay/ui/BillListScreen.kt`
  - `app/src/main/java/com/example/monay/ui/AddBillScreen.kt`
- 技术说明：
  - Material Icons在Jetpack Compose中分为基础图标包和扩展图标包
  - 基础图标包（material-icons-core）只包含常用图标
  - 扩展图标包（material-icons-extended）包含更多专用图标，如我们使用的类别图标（Fastfood、LocalTaxi等）

### v1.2.0 自动记账功能（2024-07-26）

- 新增功能：后台监控支付宝与微信交易记录，实现自动记账
- 涉及文件：
  - `app/src/main/java/com/example/monay/notification/TransactionParser.kt`（新增）
  - `app/src/main/java/com/example/monay/notification/NotificationServiceManager.kt`（新增）
  - `app/src/main/java/com/example/monay/notification/MyNotificationListener.kt`（更新）
  - `app/src/main/java/com/example/monay/ui/BillListScreen.kt`（更新）
  - `app/src/main/java/com/example/monay/MainActivity.kt`（更新）
  - `app/src/main/java/com/example/monay/di/AppModule.kt`（更新）

- 主要功能：
  1. **自动解析交易通知**：
     - 支付宝支付通知解析
     - 支付宝转账通知解析
     - 微信支付通知解析
     - 微信转账通知解析
  
  2. **智能交易分类**：
     - 基于商家和交易内容自动分类
     - 支持常见类别如餐饮、交通、购物、娱乐等
     - 自动区分收入、支出和转账类型
  
  3. **通知服务管理**：
     - 创建通知服务管理类，控制通知监听服务
     - 提供权限检查和服务重启功能
     - 优化通知监听权限授予流程
  
  4. **UI交互优化**：
     - 改进权限请求对话框，提供更详细的说明
     - 添加服务状态指示器，显示当前自动记账状态
     - 授权成功后提供成功提示，增强用户体验
  
- 技术亮点：
  - 使用正则表达式匹配不同的通知模式
  - 应用Hilt依赖注入框架管理服务依赖
  - 使用Kotlin协程处理异步数据库操作
  - 实现了完整的错误处理和日志记录机制

- 使用说明：
  1. 首次打开应用会请求通知读取权限
  2. 授权后，应用将自动读取支付宝和微信的交易通知
  3. 交易信息会被自动解析、分类并保存到账单列表
  4. 用户可以随时在通知设置中关闭或重新开启此功能

### v1.3.0 统计分析功能（2024-07-27）

- 新增功能：强大的收支统计分析功能，提供饼图可视化展示
- 涉及文件：
  - `app/src/main/java/com/example/monay/data/StatisticsEntities.kt`（新增）
  - `app/src/main/java/com/example/monay/ui/StatisticsScreen.kt`（新增）
  - `app/src/main/java/com/example/monay/repository/BillRepository.kt`（更新）
  - `app/src/main/java/com/example/monay/viewmodel/BillViewModel.kt`（更新）
  - `app/src/main/java/com/example/monay/MainActivity.kt`（更新）
  - `app/build.gradle.kts`（更新）
  - `settings.gradle.kts`（更新）

- 主要功能：
  1. **饼图数据可视化**：
     - 月度支出饼图，按类别展示比例
     - 月度收入饼图，按类别展示比例
     - 年度支出饼图，按类别展示比例
     - 年度收入饼图，按类别展示比例
  
  2. **时间周期选择**：
     - 月度/年度切换功能
     - 上一月/下一月快速导航
     - 上一年/下一年快速导航
     - 显示当前选中的时间周期
  
  3. **收支总览**：
     - 显示所选周期的总收入
     - 显示所选周期的总支出
     - 计算并展示收支结余
     - 使用绿色/红色直观区分收入/支出
  
  4. **底部导航优化**：
     - 添加统计页面到底部导航栏
     - 使用图表图标直观表示统计功能
     - 实现无缝导航体验
  
- 技术亮点：
  - 集成MPAndroidChart库实现饼图可视化
  - 使用JitPack仓库管理第三方库依赖
  - 设计高复用性的数据实体类支持统计查询
  - 实现响应式的时间选择器组件
  - 开发高度定制化的饼图样式和交互

- 使用说明：
  1. 点击底部导航栏的"统计"图标进入统计页面
  2. 使用顶部的月/年切换按钮选择统计周期
  3. 查看收入和支出饼图，了解不同类别的占比
  4. 通过左右箭头在不同月份或年份间切换
  5. 饼图为空时会显示提示信息，引导用户添加账单

### v1.3.1 账单管理功能增强（2024-07-27）

- 新增功能：添加滑动删除账单功能，提升用户账单管理体验
- 涉及文件：
  - `app/src/main/java/com/example/monay/data/BillDao.kt`（更新）
  - `app/src/main/java/com/example/monay/repository/BillRepository.kt`（更新）
  - `app/src/main/java/com/example/monay/viewmodel/BillViewModel.kt`（更新）
  - `app/src/main/java/com/example/monay/ui/BillListScreen.kt`（更新）
  - `app/src/main/java/com/example/monay/MainActivity.kt`（更新）

- 主要功能：
  1. **滑动删除账单**：
     - 支持从右向左滑动账单卡片显示删除选项
     - 滑动操作流畅，带有红色背景和删除图标提示
     - 实现阈值检测，需滑动超过50%才触发删除操作
  
  2. **删除确认机制**：
     - 添加底部Snackbar确认提示，防止误操作
     - 用户可通过点击"确定"按钮完成删除
     - 取消操作时账单卡片自动回弹到原位置
  
  3. **数据同步机制**：
     - 删除账单后自动刷新列表显示
     - 删除账单后自动更新统计数据
     - 确保用户界面与数据库保持一致

- 技术亮点：
  - 使用SwipeToDismiss组件实现滑动删除功能
  - 应用SnackbarHostState管理删除确认交互
  - 采用协程处理异步删除操作
  - 集成DismissState监控滑动状态变化
  - 优化滑动体验的触发阈值和视觉反馈

- 使用说明：
  1. 在账单列表页面，将账单项从右向左滑动
  2. 滑动超过一半距离时，会显示删除确认提示
  3. 点击"确定"按钮完成删除，或等待提示自动关闭取消操作
  4. 删除后，账单记录将从列表中移除，统计数据也会相应更新

### v1.3.0 自动记账功能更新（2024-07-26）

- 新增功能：自动记账系统
  - 支持自动监听并记录支付宝支付通知
  - 支持自动监听并记录支付宝转账通知
  - 支持自动监听并记录微信支付通知
  - 支持自动监听并记录微信转账通知

- 主要特点：
  1. 自动识别交易类型（收入/支出）
  2. 自动提取交易金额
  3. 自动分类交易
  4. 实时记录交易信息

- 使用说明：
  1. 首次使用时，需要授予通知访问权限
  2. 在系统设置中允许应用访问通知
  3. 确保支付宝和微信的通知功能已开启
  4. 交易完成后，系统会自动记录账单

- 隐私说明：
  - 所有数据仅保存在本地
  - 不会上传任何个人信息
  - 仅读取必要的通知内容

- 技术实现：
  - 使用 NotificationListenerService 监听系统通知
  - 实现智能解析算法识别交易信息
  - 采用 Room 数据库本地存储
  - 使用 Hilt 依赖注入