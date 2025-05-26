package com.example.monay.notification

import java.util.regex.Pattern
import android.util.Log

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
object TransactionParser {
    private const val TAG = "TransactionParser"
    
    // 支付宝支付通知模式：[支付宝] 支付宝成功收款0.01元。
    private val ALIPAY_PAYMENT_PATTERN = Pattern.compile("支付宝成功(收款|付款)(\\d+(\\.\\d+)?)元")
    
    // 支付宝转账通知模式：[支付宝] XX向你转了0.01元
    private val ALIPAY_TRANSFER_PATTERN = Pattern.compile("(.*?)向你转了(\\d+(\\.\\d+)?)元")
    
    // 微信支付通知模式：[微信支付] 微信支付收款0.01元
    private val WECHAT_PAYMENT_PATTERN = Pattern.compile("微信支付(收款|收款|付款)(\\d+(\\.\\d+)?)元")
    
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
            val payType = matcher.group(1) ?: "" // 收款或付款
            val amount = matcher.group(2)?.toDoubleOrNull() ?: 0.0
            val type = if (payType.contains("收款")) "收入" else "支出"
            
            // 提取商家信息
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
        
        // 如果没有找到明确的商家标识，尝试从内容中推断
        return "未知商家"
    }
    
    /**
     * 根据商家和内容自动分类交易
     */
    private fun categorizeTransaction(merchant: String, content: String): String {
        val lowerContent = content.lowercase()
        val lowerMerchant = merchant.lowercase()
        
        // 食品和餐饮
        if (lowerContent.contains("餐") || lowerContent.contains("饭") || 
            lowerContent.contains("食") || lowerContent.contains("超市") ||
            lowerMerchant.contains("餐厅") || lowerMerchant.contains("食品") ||
            lowerMerchant.contains("超市") || lowerMerchant.contains("外卖")) {
            return "餐饮"
        }
        
        // 交通
        if (lowerContent.contains("打车") || lowerContent.contains("出租") || 
            lowerContent.contains("地铁") || lowerContent.contains("公交") ||
            lowerContent.contains("高铁") || lowerContent.contains("车费") ||
            lowerMerchant.contains("交通")) {
            return "交通"
        }
        
        // 购物
        if (lowerContent.contains("购物") || lowerContent.contains("商城") || 
            lowerContent.contains("淘宝") || lowerContent.contains("京东") ||
            lowerMerchant.contains("商城") || lowerMerchant.contains("购物") ||
            lowerMerchant.contains("店")) {
            return "购物"
        }
        
        // 娱乐
        if (lowerContent.contains("电影") || lowerContent.contains("游戏") || 
            lowerContent.contains("娱乐") || lowerContent.contains("影院") ||
            lowerMerchant.contains("电影") || lowerMerchant.contains("娱乐")) {
            return "娱乐"
        }
        
        // 住房
        if (lowerContent.contains("房租") || lowerContent.contains("水电") || 
            lowerContent.contains("物业") || lowerContent.contains("住宿") ||
            lowerMerchant.contains("房屋") || lowerMerchant.contains("物业")) {
            return "住房"
        }
        
        // 医疗
        if (lowerContent.contains("医院") || lowerContent.contains("药店") || 
            lowerContent.contains("医疗") || lowerContent.contains("诊所") ||
            lowerMerchant.contains("医院") || lowerMerchant.contains("药")) {
            return "医疗"
        }
        
        // 教育
        if (lowerContent.contains("学校") || lowerContent.contains("教育") || 
            lowerContent.contains("培训") || lowerContent.contains("课程") ||
            lowerMerchant.contains("教育") || lowerMerchant.contains("学校")) {
            return "教育"
        }
        
        // 默认分类
        return "其他"
    }
} 