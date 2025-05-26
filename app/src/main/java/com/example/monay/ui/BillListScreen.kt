package com.example.monay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.monay.data.BillEntity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.More
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import java.text.SimpleDateFormat
import java.util.*
import com.example.monay.ui.theme.ExpenseColor
import com.example.monay.ui.theme.IncomeColor
import com.example.monay.ui.theme.TransferColor
import com.example.monay.notification.NotificationServiceManager
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun BillListScreen(
    bills: List<BillEntity>,
    onNavigateToAddBill: () -> Unit,
    notificationServiceManager: NotificationServiceManager,
    onDeleteBill: (Int) -> Unit = {}
) {
    var hasNotificationPermission by remember { 
        mutableStateOf(notificationServiceManager.isNotificationServiceEnabled()) 
    }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showServiceInfoDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 创建 ActivityResultLauncher 来启动设置并处理结果
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // 从设置返回后重新检查权限
        val permissionGranted = notificationServiceManager.isNotificationServiceEnabled()
        Log.d("BillListScreen", "权限检查结果: $permissionGranted")
        hasNotificationPermission = permissionGranted
        
        // 如果权限已授予，尝试重启通知监听服务确保正常运行
        if (hasNotificationPermission) {
            coroutineScope.launch {
                notificationServiceManager.restartNotificationService()
                showPermissionDialog = false
                
                // 显示服务已启用信息
                showServiceInfoDialog = true
                delay(3000) // 显示3秒后自动关闭
                showServiceInfoDialog = false
            }
        }
    }

    LaunchedEffect(Unit) {
        hasNotificationPermission = notificationServiceManager.isNotificationServiceEnabled()
        if (!hasNotificationPermission) {
            showPermissionDialog = true
        }
    }

    // 权限对话框
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("通知读取权限") },
            text = { 
                Column {
                    Text("为了自动记录支付宝和微信交易，请授予应用通知读取权限。")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "开启后，记账应用可以自动读取支付宝、微信的支付通知，实现自动记账，无需手动输入。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            confirmButton = {
                Button(onClick = {
                    val intent = notificationServiceManager.getNotificationSettingsIntent()
                    permissionLauncher.launch(intent)
                }) {
                    Text("前往设置")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("稍后再说")
                }
            }
        )
    }
    
    // 服务启用成功提示
    if (showServiceInfoDialog) {
        AlertDialog(
            onDismissRequest = { showServiceInfoDialog = false },
            title = { Text("自动记账已启用") },
            text = { 
                Text("支付宝和微信的交易将被自动记录。您可以在任何时候在设置中关闭此功能。")
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = IncomeColor
                )
            },
            confirmButton = {
                Button(onClick = { showServiceInfoDialog = false }) {
                    Text("知道了")
                }
            }
        )
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("我的账单") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // 添加通知服务状态指示器
                    Box(
                        modifier = Modifier.padding(end = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { 
                                if (hasNotificationPermission) {
                                    showServiceInfoDialog = true
                                } else {
                                    showPermissionDialog = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (hasNotificationPermission) 
                                    Icons.Default.Notifications 
                                else 
                                    Icons.Default.NotificationsOff,
                                contentDescription = "通知权限状态",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddBill,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Filled.Add, 
                    contentDescription = "添加新账单",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        if (bills.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无账单记录",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击右下角的加号按钮添加新账单，或打开通知权限自动记录支付记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            // 账单列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    end = 16.dp,
                    bottom = padding.calculateBottomPadding() + 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bills.size) { index ->
                    val bill = bills[index]
                    
                    // 创建滑动删除状态
                    val dismissState = rememberDismissState(
                        confirmStateChange = { dismissValue ->
                            if (dismissValue == DismissValue.DismissedToStart) {
                                // 显示确认删除对话框
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "确定要删除这条账单记录吗？",
                                        actionLabel = "确定",
                                        duration = SnackbarDuration.Long
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        onDeleteBill(bill.id.toInt())
                                    }
                                }
                                false // 不自动删除，等待用户确认
                            } else {
                                false
                            }
                        }
                    )
                    
                    SwipeToDismiss(
                        state = dismissState,
                        directions = setOf(DismissDirection.EndToStart),
                        background = {
                            DismissBackground(dismissState)
                        },
                        dismissContent = {
                            BillCard(bill = bill)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DismissBackground(dismissState: DismissState) {
    val color = if (dismissState.dismissDirection == DismissDirection.EndToStart) {
        Color(0xFFFF6B6B) // 红色背景，表示删除
    } else {
        Color.Transparent
    }
    
    val alignment = if (dismissState.dismissDirection == DismissDirection.EndToStart) {
        Alignment.CenterEnd
    } else {
        Alignment.CenterStart
    }
    
    val icon = if (dismissState.dismissDirection == DismissDirection.EndToStart) {
        Icons.Default.Delete
    } else {
        null
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = "删除",
                tint = Color.White
            )
        }
    }
}

@Composable
fun BillCard(bill: BillEntity) {
    val (iconVector, iconTint, backgroundColor) = when(bill.type.lowercase()) {
        "支出" -> Triple(getBillCategoryIcon(bill.category), ExpenseColor, ExpenseColor.copy(alpha = 0.1f))
        "收入" -> Triple(Icons.Default.ArrowUpward, IncomeColor, IncomeColor.copy(alpha = 0.1f))
        "转账" -> Triple(Icons.Default.SwapHoriz, TransferColor, TransferColor.copy(alpha = 0.1f))
        else -> Triple(Icons.Default.ShoppingCart, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    }
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(bill.time))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标背景
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 账单信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = bill.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if(!bill.remark.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = bill.remark,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // 金额
            Text(
                text = getFormattedAmount(bill.type, bill.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = iconTint
            )
        }
    }
}

fun getFormattedAmount(type: String, amount: Double): String {
    return when(type.lowercase()) {
        "支出" -> "-¥%.2f".format(amount)
        "收入" -> "+¥%.2f".format(amount)
        else -> "¥%.2f".format(amount)
    }
}

@Composable
fun getBillCategoryIcon(category: String): ImageVector {
    return when(category.lowercase()) {
        "餐饮", "食品" -> Icons.Default.Fastfood
        "交通" -> Icons.Default.LocalTaxi
        "住房" -> Icons.Default.House
        "教育" -> Icons.Default.School
        "医疗" -> Icons.Default.MedicalServices
        "娱乐" -> Icons.Default.SportsEsports
        "工作" -> Icons.Default.Workspaces
        else -> Icons.Default.ShoppingCart
    }
} 