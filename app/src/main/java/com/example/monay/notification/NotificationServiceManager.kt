package com.example.monay.notification

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通知服务管理类
 * 负责通知监听服务的启动、状态检查等功能
 */
@Singleton
class NotificationServiceManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "NotificationServiceManager"
    }
    
    /**
     * 检查通知监听权限是否已授予
     */
    fun isNotificationServiceEnabled(): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat?.contains(packageName) == true
    }
    
    /**
     * 获取通知访问设置Intent
     */
    fun getNotificationSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }
    
    /**
     * 重启通知监听服务
     * 在某些情况下，通知监听服务可能被系统杀死，可以调用此方法重启服务
     */
    fun restartNotificationService() {
        Log.d(TAG, "重启通知监听服务")
        
        // 先关闭服务
        val closeIntent = Intent(context, MyNotificationListener::class.java)
        context.stopService(closeIntent)
        
        // 重新请求系统绑定服务
        toggleNotificationListenerService()
    }
    
    /**
     * 切换通知监听服务状态
     * 这是一个通用方法，用于刷新系统对通知监听服务的绑定
     */
    fun toggleNotificationListenerService() {
        val componentName = ComponentName(context, MyNotificationListener::class.java)
        val packageManager = context.packageManager

        // 切换服务状态
        packageManager.setComponentEnabledSetting(
            componentName,
            if (isNotificationServiceEnabled()) {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            },
            PackageManager.DONT_KILL_APP
        )

        // 重启服务
        NotificationListenerService.requestRebind(componentName)
    }

    fun openNotificationListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
} 