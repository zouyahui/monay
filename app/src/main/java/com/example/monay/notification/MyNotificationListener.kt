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
import com.example.monay.MainActivity
import com.example.monay.R
import com.example.monay.data.BillEntity
import com.example.monay.repository.BillRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MyNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "MyNotificationListener"
        private const val ALIPAY_PACKAGE = "com.eg.android.AlipayGphone"
        private const val WECHAT_PACKAGE = "com.tencent.mm"
        private const val NOTIFICATION_CHANNEL_ID = "monay_service"
        private const val FOREGROUND_NOTIFICATION_ID = 1
    }

    @Inject
    lateinit var transactionParser: TransactionParser

    @Inject
    lateinit var billRepository: BillRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "通知监听服务创建")
        createNotificationChannel()
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "通知监听服务启动")
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "自动记账服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于保持自动记账服务运行"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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
        val packageName = sbn.packageName
        
        // 记录所有收到的通知
        Log.d(TAG, "收到通知: 包名=$packageName")
        
        // 只处理支付宝和微信的通知
        if (packageName != WECHAT_PACKAGE && packageName != ALIPAY_PACKAGE) {
            Log.d(TAG, "跳过非目标应用通知: $packageName")
            return
        }

        val notification = sbn.notification
        val extras = notification.extras
        
        // 获取通知的标题和内容
        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getString(Notification.EXTRA_TEXT)
        
        Log.d(TAG, "通知详情: 包名=$packageName, 标题=$title, 内容=$text")
        
        if (title == null) {
            Log.e(TAG, "通知标题为空，跳过处理")
            return
        }
        
        if (text == null) {
            Log.e(TAG, "通知内容为空，跳过处理")
            return
        }

        // 增加详细日志
        if (packageName == WECHAT_PACKAGE) {
            Log.d(TAG, "微信通知详情: 标题='$title', 内容='$text'")
            
            // 检查是否是微信支付相关通知
            val isWechatPay = title.contains("微信") || text.contains("微信支付")
            Log.d(TAG, "是否是微信支付相关通知: $isWechatPay")
            
            if (isWechatPay) {
                // 尝试匹配金额
                val patterns = listOf(
                    """已支付¥(\d+(\.\d{1,2})?)""".toRegex(),
                    """¥(\d+(\.\d{1,2})?)""".toRegex(),
                    """(\d+(\.\d{1,2})?)元""".toRegex()
                )
                
                var matched = false
                for ((index, pattern) in patterns.withIndex()) {
                    val matchResult = pattern.find(text)
                    if (matchResult != null) {
                        Log.d(TAG, "使用模式${index + 1}成功匹配金额: ${matchResult.groupValues[1]}")
                        matched = true
                        break
                    }
                }
                
                if (!matched) {
                    Log.d(TAG, "所有金额匹配模式均未成功")
                }
            }
        } else if (packageName == ALIPAY_PACKAGE) {
            Log.d(TAG, "支付宝通知详情: 标题='$title', 内容='$text'")
        }

        // 在协程中处理通知
        serviceScope.launch {
            try {
                Log.d(TAG, "开始解析通知: 包名=$packageName, 标题=$title, 内容=$text")
                transactionParser.parseAndSave(packageName, title, text)
                Log.d(TAG, "通知解析完成")
            } catch (e: Exception) {
                Log.e(TAG, "处理通知失败", e)
            }
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