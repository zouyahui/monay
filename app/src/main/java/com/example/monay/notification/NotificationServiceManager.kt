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
import android.app.ActivityManager
import android.os.Build

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
        val isEnabled = flat?.contains(packageName) == true
        Log.d(TAG, "通知监听权限状态: $isEnabled, 配置: $flat")
        return isEnabled
    }
    
    /**
     * 获取通知访问设置Intent
     */
    fun getNotificationSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }
    
    /**
     * 重启通知监听服务
     */
    fun restartNotificationService() {
        try {
            Log.d(TAG, "尝试重启通知监听服务")
            
            val componentName = ComponentName(context, MyNotificationListener::class.java)
            
            // 先禁用服务
            context.packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            
            // 等待一小段时间
            Thread.sleep(100)
            
            // 重新启用服务
            context.packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            
            // 尝试直接启动服务
            try {
                val serviceIntent = Intent(context, MyNotificationListener::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                Log.d(TAG, "已尝试启动通知监听服务")
            } catch (e: Exception) {
                Log.e(TAG, "启动服务失败", e)
            }
            
            // 发送广播通知服务重新绑定
            val rebindIntent = Intent(NotificationListenerService.SERVICE_INTERFACE)
            context.sendBroadcast(rebindIntent)
            
            Log.d(TAG, "通知监听服务重启完成")
        } catch (e: Exception) {
            Log.e(TAG, "重启通知监听服务失败", e)
        }
    }
    
    /**
     * 打开通知监听设置页面
     */
    fun openNotificationListenerSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Log.d(TAG, "已打开通知监听设置页面")
        } catch (e: Exception) {
            Log.e(TAG, "打开通知监听设置页面失败", e)
            // 如果直接打开设置页面失败，尝试打开应用设置页面
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.data = android.net.Uri.parse("package:" + context.packageName)
                context.startActivity(intent)
                Log.d(TAG, "已打开应用设置页面")
            } catch (e: Exception) {
                Log.e(TAG, "打开应用设置页面也失败", e)
            }
        }
    }
    
    /**
     * 检查并请求通知监听权限
     */
    fun checkAndRequestNotificationPermission() {
        if (!isNotificationServiceEnabled()) {
            Log.d(TAG, "通知监听权限未授予，请求权限")
            openNotificationListenerSettings()
        } else {
            Log.d(TAG, "通知监听权限已授予")
            // 如果权限已授予，尝试重启服务以确保正常运行
            restartNotificationService()
        }
    }
    
    /**
     * 检查通知监听服务是否正在运行
     */
    fun isNotificationServiceRunning(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)
        
        for (service in runningServices) {
            if (MyNotificationListener::class.java.name == service.service.className) {
                Log.d(TAG, "通知监听服务正在运行")
                return true
            }
        }
        
        Log.d(TAG, "通知监听服务未运行")
        return false
    }
    
    /**
     * 检查通知监听服务状态并尝试修复
     */
    fun checkAndFixNotificationService(): Boolean {
        val isEnabled = isNotificationServiceEnabled()
        val isRunning = isNotificationServiceRunning()
        
        Log.d(TAG, "通知服务状态检查: 权限已授予=$isEnabled, 服务正在运行=$isRunning")
        
        if (!isEnabled) {
            Log.d(TAG, "通知监听权限未授予，无法修复服务")
            return false
        }
        
        if (!isRunning) {
            Log.d(TAG, "通知监听服务未运行，尝试重启")
            restartNotificationService()
            return true
        }
        
        return true
    }
} 