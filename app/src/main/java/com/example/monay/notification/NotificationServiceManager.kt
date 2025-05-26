package com.example.monay.notification

import android.content.Context
import android.content.Intent
import android.provider.Settings
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
        val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(context)
        val isEnabled = enabledListeners.contains(packageName)
        
        Log.d(TAG, "通知监听服务启用状态: $isEnabled")
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
    private fun toggleNotificationListenerService() {
        Log.d(TAG, "切换通知监听服务状态")
        
        val componentName = context.packageName + "/" + MyNotificationListener::class.java.name
        val cmd = "cmd notification " + (if (isNotificationServiceEnabled()) "disable" else "enable") + " listener $componentName"
        
        try {
            Runtime.getRuntime().exec(cmd)
        } catch (e: Exception) {
            Log.e(TAG, "切换通知监听服务失败", e)
        }
        
        // 稍等一会儿后再次切换回来
        try {
            Thread.sleep(1000)
            Runtime.getRuntime().exec("cmd notification " + 
                                       (if (isNotificationServiceEnabled()) "enable" else "disable") + 
                                       " listener $componentName")
        } catch (e: Exception) {
            Log.e(TAG, "恢复通知监听服务失败", e)
        }
    }
} 