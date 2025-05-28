package com.example.monay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.monay.ui.theme.MonayTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import com.example.monay.viewmodel.BillViewModel
import androidx.compose.runtime.collectAsState
import com.example.monay.ui.BillListScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.monay.ui.AddBillScreen
import com.example.monay.ui.BillInputData
import com.example.monay.ui.StatisticsScreen
import com.example.monay.ui.TestNotificationScreen
import com.example.monay.data.BillEntity
import com.example.monay.notification.TransactionInfo
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.monay.notification.NotificationServiceManager
import javax.inject.Inject

// 定义导航项数据类
data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var notificationServiceManager: NotificationServiceManager
    
    private val billViewModel: BillViewModel by viewModels()
    
    // 添加本地广播接收器以接收交易通知
    private val transactionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("MainActivity", "收到交易广播")
            if (intent.action == "com.example.monay.TRANSACTION_DETECTED") {
                val transaction = intent.getSerializableExtra("transaction") as? TransactionInfo
                if (transaction != null) {
                    Log.d("MainActivity", "收到交易信息: $transaction")
                    handleTransaction(transaction)
                }
            }
        }
    }
    
    private fun handleTransaction(transaction: TransactionInfo) {
        lifecycleScope.launch {
            try {
                val billType = when (transaction.type) {
                    "收入" -> "收入"
                    "支出" -> "支出"
                    else -> "支出"
                }
                
                val billEntity = BillEntity(
                    accountId = 1,
                    type = billType,
                    category = transaction.category,
                    amount = transaction.amount,
                    time = System.currentTimeMillis(),
                    remark = transaction.remark
                )
                
                Log.d("MainActivity", "准备保存账单: $billEntity")
                billViewModel.insertBill(billEntity)
                Log.d("MainActivity", "账单已保存到ViewModel")
                
                // 可选：显示Toast通知用户
                runOnUiThread {
                    showToast("已自动记录${transaction.type}: ¥${transaction.amount}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "保存账单时出错", e)
            }
        }
    }
    
    private fun showToast(message: String) {
        // 在这里实现Toast提示，也可以使用Snackbar
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 注册广播接收器
        LocalBroadcastManager.getInstance(this).registerReceiver(
            transactionReceiver,
            IntentFilter("com.example.monay.TRANSACTION_DETECTED")
        )
        
        // 检查并修复通知服务
        if (notificationServiceManager.isNotificationServiceEnabled()) {
            Log.d("MainActivity", "通知监听权限已授予，检查服务状态")
            notificationServiceManager.checkAndFixNotificationService()
        }
        
        setContent {
            MonayTheme {
                var showPermissionDialog by remember { mutableStateOf(!notificationServiceManager.isNotificationServiceEnabled()) }

                if (showPermissionDialog) {
                    AlertDialog(
                        onDismissRequest = { showPermissionDialog = false },
                        title = { Text("需要通知权限") },
                        text = { Text("为了实现自动记账功能，需要获取通知访问权限。请在设置中允许访问通知。") },
                        confirmButton = {
                            Button(onClick = {
                                notificationServiceManager.checkAndRequestNotificationPermission()
                                showPermissionDialog = false
                            }) {
                                Text("去设置")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showPermissionDialog = false }) {
                                Text("取消")
                            }
                        }
                    )
                }

                // 创建导航项
                val navItems = listOf(
                    BottomNavItem(
                        route = "billList",
                        title = "账单",
                        icon = Icons.Default.Home
                    ),
                    BottomNavItem(
                        route = "statistics",
                        title = "统计",
                        icon = Icons.Default.BarChart
                    ),
                    BottomNavItem(
                        route = "test",
                        title = "测试",
                        icon = Icons.Default.BugReport
                    )
                )
                
                val navController = rememberNavController()
                
                // 获取当前导航状态
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                // 判断是否是主界面
                val showBottomBar = navItems.any { item -> 
                    currentDestination?.hierarchy?.any { it.route == item.route } == true 
                }
                
                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                navItems.forEach { item ->
                                    val selected = currentDestination?.hierarchy?.any { 
                                        it.route == item.route 
                                    } == true
                                    
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                // 避免在底部导航栏之间切换时创建多个实例
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                // 避免相同目的地多次入栈
                                                launchSingleTop = true
                                                // 重用已有的实例
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(item.icon, contentDescription = item.title) },
                                        label = { Text(item.title) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController, 
                        startDestination = "billList",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("billList") {
                            val bills by billViewModel.bills.collectAsState()
                            BillListScreen(
                                bills = bills,
                                onNavigateToAddBill = { navController.navigate("addBill") },
                                notificationServiceManager = notificationServiceManager,
                                onDeleteBill = { billId ->
                                    billViewModel.deleteBill(billId)
                                }
                            )
                        }
                        
                        composable("statistics") {
                            StatisticsScreen(
                                viewModel = billViewModel,
                                onNavigateToAddBill = { navController.navigate("addBill") }
                            )
                        }

                        composable("test") {
                            TestNotificationScreen(context = this@MainActivity)
                        }
                        
                        composable("addBill") {
                            AddBillScreen(onSaveBill = { billInputData ->
                                lifecycleScope.launch {
                                    val billEntity = BillEntity(
                                        accountId = 1,
                                        type = billInputData.type,
                                        category = billInputData.category,
                                        amount = billInputData.amount.toDoubleOrNull() ?: 0.0,
                                        time = billInputData.time,
                                        remark = billInputData.remark.ifBlank { null }
                                    )
                                    billViewModel.insertBill(billEntity)
                                    navController.popBackStack()
                                }
                            })
                        }
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        // 注销广播接收器
        LocalBroadcastManager.getInstance(this).unregisterReceiver(transactionReceiver)
        super.onDestroy()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MonayTheme {
        Greeting("Android")
    }
}