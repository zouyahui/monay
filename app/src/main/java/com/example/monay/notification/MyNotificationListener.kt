package com.example.monay.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
// Import Hilt annotations
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.monay.repository.BillRepository
import android.app.Notification
import android.util.Log
import com.example.monay.data.BillEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint // Add Hilt entry point annotation
class MyNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "MyNotificationListener"
        private const val ALIPAY_PACKAGE = "com.eg.android.AlipayGphone"
        private const val WECHAT_PACKAGE = "com.tencent.mm"
    }

    @Inject // Inject BillRepository
    lateinit var billRepository: BillRepository

    @Inject
    lateinit var transactionParser: TransactionParser

    // Define a CoroutineScope for suspending functions
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob)

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

        // 根据包名处理不同的通知
        when (packageName) {
            ALIPAY_PACKAGE -> processAlipayNotification(title, text)
            WECHAT_PACKAGE -> processWechatNotification(title, text)
        }
    }

    /**
     * 处理支付宝通知
     */
    private fun processAlipayNotification(title: String, content: String) {
        Log.d(TAG, "处理支付宝通知: $title - $content")
        
        // 使用支付宝通知解析器解析通知内容
        val transactionInfo = transactionParser.parseAlipayNotification(title, content)
        
        // 如果解析成功，保存交易记录
        if (transactionInfo.isValid) {
            saveTransaction(transactionInfo, "支付宝")
        }
    }

    /**
     * 处理微信通知
     */
    private fun processWechatNotification(title: String, content: String) {
        Log.d(TAG, "处理微信通知: $title - $content")
        
        // 过滤非支付相关通知
        if (!isPaymentRelatedNotification(title, content)) {
            return
        }
        
        // 使用微信通知解析器解析通知内容
        val transactionInfo = transactionParser.parseWechatNotification(title, content)
        
        // 如果解析成功，保存交易记录
        if (transactionInfo.isValid) {
            saveTransaction(transactionInfo, "微信")
        }
    }

    /**
     * 判断微信通知是否与支付相关
     */
    private fun isPaymentRelatedNotification(title: String, content: String): Boolean {
        val paymentKeywords = listOf("微信支付", "收款", "付款", "转账", "交易", "¥", "元")
        
        return paymentKeywords.any { title.contains(it) || content.contains(it) }
    }

    /**
     * 保存交易记录到数据库
     */
    private fun saveTransaction(transaction: TransactionInfo, source: String) {
        // 创建账单实体
        val bill = BillEntity(
            accountId = 1, // 默认账户ID，可以根据需要修改
            type = transaction.type,
            category = transaction.category,
            amount = transaction.amount,
            time = System.currentTimeMillis(),
            remark = "[${source}自动记账] ${transaction.merchant} - ${transaction.remark}"
        )

        Log.d(TAG, "保存交易记录: $bill")
        
        // 在协程中执行数据库操作
        serviceScope.launch {
            try {
                val insertedId = billRepository.insertBill(bill)
                Log.d(TAG, "账单保存成功，ID: $insertedId")
                
                // 显示记账成功通知
                showRecordSuccessNotification(bill)
            } catch (e: Exception) {
                Log.e(TAG, "保存账单失败", e)
            }
        }
    }

    /**
     * 显示记账成功通知
     */
    private fun showRecordSuccessNotification(bill: BillEntity) {
        // 格式化时间
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(bill.time))
        
        // 格式化金额
        val formattedAmount = String.format("%.2f", bill.amount)
        
        // 通知内容
        val notificationTitle = "自动记账成功"
        val notificationContent = "${bill.type}: ${bill.category} - ¥$formattedAmount\n时间: $formattedDate"
        
        // 这里可以添加发送系统通知的代码，如果需要的话
        Log.d(TAG, "记账成功: $notificationTitle - $notificationContent")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel() // Cancel the coroutine scope when the service is destroyed
    }

} 