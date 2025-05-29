package com.example.monay.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.monay.MainActivity
import com.example.monay.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MyNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "MyNotificationListener"
        private const val ALIPAY_PACKAGE = "com.eg.android.AlipayGphone"
        private const val WECHAT_PACKAGE = "com.tencent.mm"
        private const val NOTIFICATION_CHANNEL_ID = "monay_service"
        private const val TRANSACTION_CHANNEL_ID = "transaction_notification"
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val TRANSACTION_NOTIFICATION_BASE_ID = 1000
        private var lastNotificationTime = 0L
        private const val MIN_NOTIFICATION_INTERVAL = 3000 // 3秒最小间隔
        
        // 添加银行APP包名
        private val BANK_PACKAGES = listOf(
            "cmb.pb", // 招商银行
            "com.icbc", // 工商银行
            "com.ccb.android", // 建设银行
            "com.chinamworld.main" // 中国银行
        )
    }

    @Inject
    lateinit var transactionParser: TransactionParser

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob)

    private val targetPackages = listOf("com.tencent.mm", "com.eg.android.AlipayGphone")
    
    // 应用包名，用于识别测试通知
    private val appPackageName = "com.example.monay"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "通知监听服务创建")
        createNotificationChannel()
        startForeground()
        // 使用我们创建的组件进行依赖注入
        NotificationComponent.create(applicationContext).inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "通知监听服务启动")
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 服务通知渠道
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "自动记账服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于保持自动记账服务运行"
                setShowBadge(false)
            }

            // 交易记录通知渠道
            val transactionChannel = NotificationChannel(
                TRANSACTION_CHANNEL_ID,
                "交易记录通知",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "用于通知用户交易已记录"
                setShowBadge(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(transactionChannel)
        }
    }

    private fun startForeground() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("自动记账服务运行中")
            .setContentText("正在监听支付宝和微信的支付通知")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val packageName = sbn.packageName
            val notification = sbn.notification
            
            // 记录所有收到的通知
            Log.d(TAG, "收到通知: 包名=$packageName, ID=${sbn.id}")
            
            // 检查是否是来自应用内的测试通知
            val isTestNotification = isTestNotification(packageName, notification)
            
            // 如果不是目标应用的通知且不是测试通知，则跳过
            if (!targetPackages.contains(packageName) && !isTestNotification && !BANK_PACKAGES.contains(packageName)) {
                Log.d(TAG, "跳过非目标应用通知: $packageName")
                return
            }
            
            // 获取通知的标题和内容
            val extras = notification.extras
            val title = extras.getString(Notification.EXTRA_TITLE)
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            
            // 记录通知详情
            Log.d(TAG, "处理通知: 包名=$packageName, 标题=$title, 内容=$text")
            
            // 检查通知是否包含必要信息
            if (title == null || text == null) {
                Log.w(TAG, "通知缺少标题或内容，跳过处理")
                return
            }
            
            // 特殊处理银行信用卡交易提醒
            if (title.contains("信用卡") || title.contains("招商银行") || text.contains("信用卡交易提醒")) {
                Log.i(TAG, "检测到信用卡交易提醒: 标题=$title, 内容=$text")
                processNotification(packageName, title, text, isTestNotification)
                return
            }
            
            // 特殊处理测试通知
            if (isTestNotification) {
                Log.i(TAG, "检测到测试通知: 标题=$title, 内容=$text")
                // 如果是微信支付测试通知，使用与真实微信通知相同的处理流程
                if (title.contains("微信") && (text.contains("支付") || text.contains("已支付"))) {
                    Log.i(TAG, "处理微信支付测试通知")
                    processNotification(packageName, title, text, true)
                    return
                }
                
                // 处理支付宝信用卡交易提醒测试通知
                if (title.contains("招商银行信用卡") && text.contains("信用卡交易提醒")) {
                    Log.i(TAG, "处理支付宝信用卡交易提醒测试通知")
                    processNotification(packageName, title, text, true)
                    return
                }
            }
            
            // 处理实际通知
            processNotification(packageName, title, text, false)
            
        } catch (e: Exception) {
            Log.e(TAG, "处理通知时发生异常", e)
        }
    }
    
    /**
     * 判断是否为应用内发送的测试通知
     */
    private fun isTestNotification(packageName: String, notification: Notification): Boolean {
        // 检查包名是否为应用自身
        if (packageName != appPackageName) {
            return false
        }
        
        // 获取通知的标题和内容
        val extras = notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return false
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return false
        
        // 检查通知内容是否符合测试通知特征
        val isMockWechatPay = title.contains("微信") && (text.contains("支付") || text.contains("已支付"))
        val isMockAlipay = title.contains("支付宝") && (text.contains("付款") || text.contains("转"))
        
        val isTestNotification = isMockWechatPay || isMockAlipay
        if (isTestNotification) {
            Log.d(TAG, "检测到应用内测试通知: $title - $text")
        }
        
        return isTestNotification
    }

    private fun processNotification(packageName: String, title: String, text: String, isTestNotification: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "开始解析通知: 包名=$packageName, 标题=$title, 内容=$text, 是测试通知=$isTestNotification")
                
                // 对于微信支付通知，记录更详细的日志
                if (packageName == "com.tencent.mm" || 
                    (isTestNotification && title.contains("微信"))) {
                    logWechatNotificationDetails(title, text)
                }
                
                val transaction = transactionParser.parseNotification(packageName, title, text)
                if (transaction != null) {
                    Log.i(TAG, "成功解析交易: $transaction")
                    
                    // 发送广播通知应用其他部分
                    val intent = Intent("com.example.monay.TRANSACTION_DETECTED")
                    intent.putExtra("transaction", transaction)
                    LocalBroadcastManager.getInstance(this@MyNotificationListener).sendBroadcast(intent)
                    
                    // 发送系统通知提示用户
                    withContext(Dispatchers.Main) {
                        showTransactionNotification(transaction)
                    }
                } else {
                    Log.w(TAG, "无法解析交易信息: 包名=$packageName, 标题=$title, 内容=$text")
                }
            } catch (e: Exception) {
                Log.e(TAG, "解析通知时发生异常", e)
            }
        }
    }
    
    /**
     * 显示交易记录通知
     */
    private fun showTransactionNotification(transaction: TransactionInfo) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNotificationTime < MIN_NOTIFICATION_INTERVAL) {
            Log.d(TAG, "通知频率过高，延迟显示交易通知")
            return
        }
        lastNotificationTime = currentTime
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val amountText = if (transaction.type == "支出") "-¥${transaction.amount}" else "+¥${transaction.amount}"
        val title = "${transaction.type}已自动记录"
        val content = "${transaction.merchant}: $amountText (${transaction.category})"
        
        val notification = NotificationCompat.Builder(this, TRANSACTION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationId = TRANSACTION_NOTIFICATION_BASE_ID + (Math.random() * 1000).toInt()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
        
        Log.d(TAG, "已显示交易记录通知: $title, $content, ID=$notificationId")
    }
    
    /**
     * 记录微信通知的详细信息，用于调试
     */
    private fun logWechatNotificationDetails(title: String, text: String) {
        Log.d(TAG, "微信通知详情分析:")
        Log.d(TAG, "- 标题: '$title'")
        Log.d(TAG, "- 内容: '$text'")
        
        // 检查标题格式
        if (title == "微信·现在" || title.contains("微信支付")) {
            Log.d(TAG, "- 标题匹配微信支付通知格式")
        } else {
            Log.d(TAG, "- 标题不匹配标准微信支付通知格式")
        }
        
        // 检查内容格式
        if (text.contains("已支付")) {
            Log.d(TAG, "- 内容包含'已支付'关键词")
            
            // 尝试提取金额
            val amountRegex = "已支付¥([0-9.]+)".toRegex()
            val matchResult = amountRegex.find(text)
            if (matchResult != null) {
                val amount = matchResult.groupValues[1]
                Log.d(TAG, "- 成功提取金额: ¥$amount")
            } else {
                Log.d(TAG, "- 无法提取金额，正则表达式不匹配")
            }
        } else {
            Log.d(TAG, "- 内容不包含'已支付'关键词")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "通知监听服务销毁")
        serviceJob.cancel()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "通知监听服务已连接")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "通知监听服务已断开")
        // 请求重新连接
        requestRebind(ComponentName(this, MyNotificationListener::class.java))
    }
} 