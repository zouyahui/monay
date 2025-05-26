package com.example.monay.notification

import java.util.regex.Pattern
import android.util.Log
import com.example.monay.data.model.Bill
import com.example.monay.data.model.BillType
import com.example.monay.repository.BillRepository
import com.example.monay.data.BillEntity
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 交易信息数据类
 */
data class TransactionInfo(
    val isValid: Boolean = false,
    val type: String = "支出", // 支出、收入、转账
    val amount: Double = 0.0,
    val merchant: String = "",
    val category: String = "其他",
    val remark: String = ""
) {
    companion object {
        fun invalid() = TransactionInfo(
            isValid = false,
            type = "",
            amount = 0.0,
            merchant = "",
            category = "",
            remark = ""
        )
    }
}

/**
 * 交易通知解析器
 * 负责解析支付宝和微信的通知内容，提取交易信息
 */
@Singleton
class TransactionParser @Inject constructor(
    private val billRepository: BillRepository
) {
    companion object {
        private const val TAG = "TransactionParser"
        private const val ALIPAY_PACKAGE = "com.eg.android.AlipayGphone"
        private const val WECHAT_PACKAGE = "com.tencent.mm"

        /**
         * 解析支付宝通知
         */
        fun parseAlipayNotification(title: String, content: String): TransactionInfo {
            return try {
                when {
                    // 支付场景
                    title.contains("支付宝") && content.contains("成功付款") -> {
                        val amount = extractAmount(content)
                        val merchant = extractMerchant(content)
                        TransactionInfo(
                            isValid = true,
                            type = "支出",
                            category = "其他",
                            amount = amount,
                            merchant = merchant,
                            remark = content
                        )
                    }
                    // 收款场景
                    title.contains("支付宝") && content.contains("收款到账") -> {
                        val amount = extractAmount(content)
                        TransactionInfo(
                            isValid = true,
                            type = "收入",
                            category = "其他",
                            amount = amount,
                            merchant = "未知",
                            remark = content
                        )
                    }
                    // 转账场景
                    title.contains("支付宝") && content.contains("转账") -> {
                        val amount = extractAmount(content)
                        val isReceive = content.contains("收到")
                        TransactionInfo(
                            isValid = true,
                            type = if (isReceive) "收入" else "支出",
                            category = "转账",
                            amount = amount,
                            merchant = "未知",
                            remark = content
                        )
                    }
                    else -> TransactionInfo.invalid()
                }
            } catch (e: Exception) {
                Log.e(TAG, "解析支付宝通知失败", e)
                TransactionInfo.invalid()
            }
        }

        /**
         * 解析微信通知
         */
        fun parseWechatNotification(title: String, content: String): TransactionInfo {
            return try {
                when {
                    // 支付场景
                    content.contains("微信支付") -> {
                        val amount = extractAmount(content)
                        val merchant = extractMerchant(content)
                        TransactionInfo(
                            isValid = true,
                            type = "支出",
                            category = "其他",
                            amount = amount,
                            merchant = merchant,
                            remark = content
                        )
                    }
                    // 收款场景
                    content.contains("收款到账") -> {
                        val amount = extractAmount(content)
                        TransactionInfo(
                            isValid = true,
                            type = "收入",
                            category = "其他",
                            amount = amount,
                            merchant = "未知",
                            remark = content
                        )
                    }
                    // 转账场景
                    content.contains("转账") -> {
                        val amount = extractAmount(content)
                        val isReceive = content.contains("收到")
                        TransactionInfo(
                            isValid = true,
                            type = if (isReceive) "收入" else "支出",
                            category = "转账",
                            amount = amount,
                            merchant = "未知",
                            remark = content
                        )
                    }
                    else -> TransactionInfo.invalid()
                }
            } catch (e: Exception) {
                Log.e(TAG, "解析微信通知失败", e)
                TransactionInfo.invalid()
            }
        }

        /**
         * 从文本中提取金额
         */
        private fun extractAmount(text: String): Double {
            val regex = """¥?(\d+(\.\d{1,2})?)""".toRegex()
            val matchResult = regex.find(text)
            return matchResult?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
        }

        /**
         * 从文本中提取商家名称
         */
        private fun extractMerchant(text: String): String {
            // 这里可以根据实际通知格式优化提取逻辑
            return "未知商家"
        }
    }

    /**
     * 解析并保存交易信息
     */
    suspend fun parseAndSave(packageName: String, title: String, content: String) {
        withContext(Dispatchers.IO) {
            val transactionInfo = when (packageName) {
                ALIPAY_PACKAGE -> parseAlipayNotification(title, content)
                WECHAT_PACKAGE -> parseWechatNotification(title, content)
                else -> TransactionInfo.invalid()
            }

            if (transactionInfo.isValid) {
                val bill = BillEntity(
                    accountId = 1, // 默认账户ID
                    type = transactionInfo.type,
                    category = transactionInfo.category,
                    amount = transactionInfo.amount,
                    time = System.currentTimeMillis(),
                    remark = "[自动记账] ${transactionInfo.merchant} - ${transactionInfo.remark}"
                )

                try {
                    billRepository.insertBill(bill)
                    Log.d(TAG, "自动记账成功：$bill")
                } catch (e: Exception) {
                    Log.e(TAG, "保存账单失败", e)
                }
            }
        }
    }
} 