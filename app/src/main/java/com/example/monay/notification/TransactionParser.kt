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

/**
 * 交易解析结果数据类
 */
data class TransactionInfo(
    val isValid: Boolean = false,
    val type: String = "支出", // 支出、收入、转账
    val amount: Double = 0.0,
    val merchant: String = "",
    val category: String = "其他",
    val remark: String = ""
)

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
    }
    
    // 支付宝支付通知模式：[支付宝] 支付宝成功收款0.01元。
    private val ALIPAY_PAYMENT_PATTERN = Pattern.compile("支付宝成功(收款|付款)(\\d+(\\.\\d+)?)元")
    
    // 支付宝转账通知模式：[支付宝] XX向你转了0.01元
    private val ALIPAY_TRANSFER_PATTERN = Pattern.compile("(.*?)向你转了(\\d+(\\.\\d+)?)元")
    
    // 微信支付通知模式 - 更新为新的格式
    private val WECHAT_PAYMENT_PATTERN = Pattern.compile("已支付¥(\\d+(\\.\\d+)?)")
    
    // 微信转账通知模式：[微信] 你已成功收款0.01元
    private val WECHAT_TRANSFER_PATTERN = Pattern.compile("你已成功(收款|付款)(\\d+(\\.\\d+)?)元")
    
    /**
     * 解析支付宝通知
     */
    fun parseAlipayNotification(title: String?, content: String?): TransactionInfo {
        if (title.isNullOrEmpty() || content.isNullOrEmpty()) {
            return TransactionInfo()
        }
        
        Log.d(TAG, "解析支付宝通知：title=$title, content=$content")
        
        // 处理支付宝支付通知
        var matcher = ALIPAY_PAYMENT_PATTERN.matcher(content)
        if (matcher.find()) {
            val payType = matcher.group(1) // 收款或付款
            val amount = matcher.group(2)?.toDoubleOrNull() ?: 0.0
            val type = if (payType == "收款") "收入" else "支出"
            
            // 提取商家信息，通常在通知的其他部分
            val merchant = extractMerchant(content)
            val category = categorizeTransaction(merchant, content)
            
            return TransactionInfo(
                isValid = true,
                type = type,
                amount = amount,
                merchant = merchant,
                category = category,
                remark = content
            )
        }
        
        // 处理支付宝转账通知
        matcher = ALIPAY_TRANSFER_PATTERN.matcher(content)
        if (matcher.find()) {
            val sender = matcher.group(1) ?: ""
            val amount = matcher.group(2)?.toDoubleOrNull() ?: 0.0
            
            return TransactionInfo(
                isValid = true,
                type = "收入",
                amount = amount,
                merchant = sender,
                category = "转账",
                remark = "来自${sender}的转账"
            )
        }
        
        return TransactionInfo()
    }
    
    /**
     * 解析微信通知
     */
    fun parseWechatNotification(title: String?, content: String?): TransactionInfo {
        if (title.isNullOrEmpty() || content.isNullOrEmpty()) {
            return TransactionInfo()
        }
        
        Log.d(TAG, "解析微信通知：title=$title, content=$content")
        
        // 处理微信支付通知
        var matcher = WECHAT_PAYMENT_PATTERN.matcher(content)
        if (matcher.find()) {
            val amount = matcher.group(1)?.toDoubleOrNull() ?: 0.0
            
            // 提取商家信息
            val merchant = extractMerchant(content)
            val category = categorizeTransaction(merchant, content)
            
            return TransactionInfo(
                isValid = true,
                type = "支出",
                amount = amount,
                merchant = merchant,
                category = category,
                remark = content
            )
        }
        
        // 处理微信转账通知
        matcher = WECHAT_TRANSFER_PATTERN.matcher(content)
        if (matcher.find()) {
            val payType = matcher.group(1) // 收款或付款
            val amount = matcher.group(2)?.toDoubleOrNull() ?: 0.0
            val type = if (payType == "收款") "收入" else "支出"
            
            return TransactionInfo(
                isValid = true,
                type = type,
                amount = amount,
                merchant = "微信好友",
                category = "转账",
                remark = content
            )
        }
        
        return TransactionInfo()
    }
    
    /**
     * 从通知内容中提取商家信息
     */
    private fun extractMerchant(content: String): String {
        // 微信特定的商家提取逻辑
        if (content.contains("微信支付")) {
            val lines = content.split("\n")
            // 通常商家名称在第一行
            return lines.firstOrNull()?.trim() ?: "未知商家"
        }
        
        // 常见的商家信息前缀
        val prefixes = listOf("商家:", "收款方:", "店名:", "店铺:")
        
        for (prefix in prefixes) {
            val index = content.indexOf(prefix)
            if (index != -1) {
                val startIndex = index + prefix.length
                val endIndex = content.indexOf("\n", startIndex).takeIf { it != -1 } ?: content.length
                return content.substring(startIndex, endIndex).trim()
            }
        }
        
        return "未知商家"
    }
    
    /**
     * 根据商家和内容自动分类交易
     */
    private fun categorizeTransaction(merchant: String, content: String): String {
        val lowerContent = content.lowercase()
        val lowerMerchant = merchant.lowercase()
        
        return when {
            lowerContent.contains("外卖") || lowerMerchant.contains("外卖") -> "餐饮"
            lowerContent.contains("超市") || lowerMerchant.contains("超市") -> "购物"
            lowerContent.contains("打车") || lowerContent.contains("地铁") -> "交通"
            else -> "其他"
        }
    }

    suspend fun parseAndSave(packageName: String, title: String, content: String) {
        Log.d(TAG, "解析通知: 包名: $packageName, 标题: $title, 内容: $content")
        
        when (packageName) {
            ALIPAY_PACKAGE -> handleAlipayNotification(title, content)
            WECHAT_PACKAGE -> handleWechatNotification(title, content)
        }
    }

    private suspend fun handleAlipayNotification(title: String, content: String) {
        Log.d(TAG, "处理支付宝通知: title=$title, content=$content")
        
        // 处理支付宝支付通知
        if (content.contains("成功付款")) {
            val regex = """付款(\d+(\.\d{1,2})?)元""".toRegex()
            val matchResult = regex.find(content)
            val amount = matchResult?.groupValues?.get(1)?.toDoubleOrNull()
            
            if (amount != null && amount > 0) {
                // 提取商家信息
                val merchant = content.substringAfter("商家: ").substringBefore("\n").trim()
                
                saveBill(
                    amount = amount,
                    type = BillType.EXPENSE,
                    category = categorizeTransaction(merchant, content),
                    note = "$merchant: $content"
                )
                Log.d(TAG, "成功保存支付宝支付记录: 金额=$amount, 商家=$merchant")
            }
        }
        // 处理支付宝转账通知
        else if (content.contains("向你转了")) {
            val regex = """向你转了(\d+(\.\d{1,2})?)元""".toRegex()
            val matchResult = regex.find(content)
            val amount = matchResult?.groupValues?.get(1)?.toDoubleOrNull()
            
            if (amount != null && amount > 0) {
                // 提取转账人姓名
                val sender = content.substringBefore("向你转了").trim()
                
                saveBill(
                    amount = amount,
                    type = BillType.INCOME,
                    category = "转账",
                    note = "来自${sender}的转账"
                )
                Log.d(TAG, "成功保存支付宝转账记录: 金额=$amount, 来自=$sender")
            }
        }
    }

    private suspend fun handleWechatNotification(title: String, content: String) {
        Log.d(TAG, "处理微信通知: title=$title, content=$content")
        
        // 处理微信支付通知 - 适应真实通知格式
        // 检查内容是否包含"微信支付"和"已支付"关键词
        if (content.contains("微信支付") && (content.contains("已支付") || content.contains("¥"))) {
            // 尝试多种格式匹配金额
            var amount: Double? = null
            var matchedPattern = ""
            
            // 格式1: "已支付¥9.70"
            val regex1 = """已支付¥(\d+(\.\d{1,2})?)""".toRegex()
            val match1 = regex1.find(content)
            if (match1 != null) {
                amount = match1.groupValues[1].toDoubleOrNull()
                matchedPattern = "已支付¥"
            }
            
            // 格式2: 只匹配"¥9.70"
            if (amount == null) {
                val regex2 = """¥(\d+(\.\d{1,2})?)""".toRegex()
                val match2 = regex2.find(content)
                if (match2 != null) {
                    amount = match2.groupValues[1].toDoubleOrNull()
                    matchedPattern = "¥"
                }
            }
            
            // 格式3: 匹配数字后跟"元"
            if (amount == null) {
                val regex3 = """(\d+(\.\d{1,2})?)元""".toRegex()
                val match3 = regex3.find(content)
                if (match3 != null) {
                    amount = match3.groupValues[1].toDoubleOrNull()
                    matchedPattern = "元"
                }
            }
            
            if (amount != null && amount > 0) {
                // 提取商家名称
                // 对于"微信支付 已支付¥9.70"格式，没有明确的商家名称
                // 尝试从内容中提取可能的商家信息
                val lines = content.split("\n")
                var merchant = "未知商家"
                
                // 如果有多行，第一行可能是"微信支付"，第二行可能是商家
                if (lines.size > 1) {
                    merchant = lines[1].trim()
                } else if (lines.isNotEmpty()) {
                    // 如果只有一行，尝试提取"微信支付"之前的内容作为商家
                    val contentBeforeKeyword = lines[0].substringBefore("微信支付").trim()
                    if (contentBeforeKeyword.isNotEmpty()) {
                        merchant = contentBeforeKeyword
                    }
                }
                
                Log.d(TAG, "成功匹配微信支付金额: $amount, 使用模式: $matchedPattern, 商家: $merchant")
                
                saveBill(
                    amount = amount,
                    type = BillType.EXPENSE,
                    category = categorizeTransaction(merchant, content),
                    note = content
                )
                Log.d(TAG, "成功保存微信支付记录: 金额=$amount, 商家=$merchant")
            } else {
                Log.e(TAG, "无法从内容中提取金额: $content")
            }
        }
        // 处理微信转账通知
        else if (content.contains("转账")) {
            val regex = """转账(\d+(\.\d{1,2})?)元""".toRegex()
            val matchResult = regex.find(content)
            val amount = matchResult?.groupValues?.get(1)?.toDoubleOrNull()
            
            if (amount != null && amount > 0) {
                val type = if (content.contains("向你转账")) {
                    BillType.INCOME
                } else {
                    BillType.EXPENSE
                }
                
                // 提取转账人姓名
                val sender = content.substringBefore("向你转账").trim()
                
                saveBill(
                    amount = amount,
                    type = type,
                    category = "转账",
                    note = if (type == BillType.INCOME) "来自${sender}的转账" else "转账给${sender}"
                )
                Log.d(TAG, "成功保存微信转账记录: 金额=$amount, 类型=$type")
            }
        }
    }

    private suspend fun saveBill(
        amount: Double,
        type: BillType,
        category: String,
        note: String
    ) {
        val bill = Bill(
            amount = amount,
            type = type,
            category = category,
            note = note,
            date = LocalDateTime.now()
        )
        val billEntity = BillEntity.fromBill(bill)
        billRepository.insertBill(billEntity)
        Log.d(TAG, "保存账单: $billEntity")
    }
} 