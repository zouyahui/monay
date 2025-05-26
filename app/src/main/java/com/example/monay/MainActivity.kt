package com.example.monay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.BarChart
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
import com.example.monay.data.BillEntity
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                                notificationServiceManager.openNotificationListenerSettings()
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
                    )
                )
                
                val navController = rememberNavController()
                val billViewModel: BillViewModel by viewModels()
                
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