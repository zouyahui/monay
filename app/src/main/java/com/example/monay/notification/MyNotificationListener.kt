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
        
        // 只处理支付宝和微信的通知
        if (packageName != WECHAT_PACKAGE && packageName != ALIPAY_PACKAGE) {
            return
        }

        val notification = sbn.notification
        val extras = notification.extras
        
        // 获取通知的标题和内容
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val text = extras.getString(Notification.EXTRA_TEXT) ?: return

        Log.d(TAG, "收到通知: 包名=$packageName, 标题=$title, 内容=$text")

        // 在协程中处理通知
        serviceScope.launch {
            try {
                transactionParser.parseAndSave(packageName, title, text)
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