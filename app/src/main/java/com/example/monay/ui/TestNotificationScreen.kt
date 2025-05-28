package com.example.monay.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import com.example.monay.R
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestNotificationScreen(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val TAG = "TestNotification"

    // 创建通知渠道
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "test_channel",
            "测试通知",
            NotificationManager.IMPORTANCE_HIGH // 使用高优先级
        ).apply {
            description = "用于测试自动记账功能"
            enableLights(true)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    // 显示Snackbar
    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar(
                message = snackbarMessage,
                duration = SnackbarDuration.Short
            )
            showSnackbar = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("测试通知") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "点击按钮发送测试通知",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // 特别强调的微信支付通知测试按钮
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "微信支付通知测试",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "发送真实格式的微信支付通知",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            try {
                                val notification = NotificationCompat.Builder(context, "test_channel")
                                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                                    .setContentTitle("微信·现在")
                                    .setContentText("微信支付 已支付¥9.70")
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                    .setAutoCancel(true)
                                    .build()
                                
                                // 使用随机ID确保通知不会被覆盖
                                val notificationId = System.currentTimeMillis().toInt()
                                notificationManager.notify(notificationId, notification)
                                
                                Log.d(TAG, "已发送微信支付测试通知: ID=$notificationId, 标题='微信·现在', 内容='微信支付 已支付¥9.70'")
                                snackbarMessage = "已发送微信支付测试通知"
                                showSnackbar = true
                            } catch (e: Exception) {
                                Log.e(TAG, "发送通知失败", e)
                                snackbarMessage = "发送通知失败: ${e.message}"
                                showSnackbar = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("发送微信支付通知", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 支付宝支付通知测试按钮
            Button(
                onClick = {
                    val notification = NotificationCompat.Builder(context, "test_channel")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("支付宝")
                        .setContentText("支付宝成功付款9.90元。")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()

                    notificationManager.notify(1, notification)
                    snackbarMessage = "已发送支付宝支付测试通知"
                    showSnackbar = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("测试支付宝支付通知")
            }

            // 支付宝转账通知测试按钮
            Button(
                onClick = {
                    val notification = NotificationCompat.Builder(context, "test_channel")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("支付宝")
                        .setContentText("张三向你转了10.00元")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()

                    notificationManager.notify(2, notification)
                    snackbarMessage = "已发送支付宝转账测试通知"
                    showSnackbar = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("测试支付宝转账通知")
            }

            // 微信支付通知测试按钮（旧格式）
            Button(
                onClick = {
                    val notification = NotificationCompat.Builder(context, "test_channel")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("微信支付")
                        .setContentText("美团外卖\n已支付¥9.90")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()

                    notificationManager.notify(3, notification)
                    snackbarMessage = "已发送微信支付测试通知（旧格式）"
                    showSnackbar = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("测试微信支付通知（旧格式）")
            }

            // 微信转账通知测试按钮
            Button(
                onClick = {
                    val notification = NotificationCompat.Builder(context, "test_channel")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("微信")
                        .setContentText("李四向你转账10.00元")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()

                    notificationManager.notify(5, notification)
                    snackbarMessage = "已发送微信转账测试通知"
                    showSnackbar = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("测试微信转账通知")
            }
        }
    }
} 