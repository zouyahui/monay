package com.example.monay.ui

import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.monay.data.CategoryAmount
import com.example.monay.data.StatisticTimeUnit
import com.example.monay.ui.theme.ChartColors
import com.example.monay.ui.theme.ExpenseColor
import com.example.monay.ui.theme.IncomeColor
import com.example.monay.viewmodel.BillViewModel
import com.example.monay.viewmodel.StatisticsState
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: BillViewModel,
    onNavigateToAddBill: () -> Unit
) {
    val statisticsState by viewModel.statisticsState.collectAsState()
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计分析") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // 时间周期选择器
            TimePeriodSelector(
                statisticsState = statisticsState,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                onPreviousYear = { viewModel.previousYear() },
                onNextYear = { viewModel.nextYear() },
                onTimeUnitChanged = { viewModel.setTimeUnit(it) }
            )
            
            // 总收支统计卡片
            TotalSummaryCard(statisticsState = statisticsState)
            
            // 支出饼图
            if (statisticsState.expensesByCategory.isNotEmpty()) {
                CategoryPieChart(
                    title = "支出分类",
                    categories = statisticsState.expensesByCategory,
                    total = statisticsState.totalExpense,
                    colorScheme = ChartColors
                )
            } else {
                EmptyChartPlaceholder(title = "支出分类", message = "暂无支出记录")
            }
            
            // 收入饼图
            if (statisticsState.incomesByCategory.isNotEmpty()) {
                CategoryPieChart(
                    title = "收入分类",
                    categories = statisticsState.incomesByCategory,
                    total = statisticsState.totalIncome,
                    colorScheme = ChartColors
                )
            } else {
                EmptyChartPlaceholder(title = "收入分类", message = "暂无收入记录")
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // 为底部留出空间
        }
    }
}

@Composable
fun TimePeriodSelector(
    statisticsState: StatisticsState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onTimeUnitChanged: (StatisticTimeUnit) -> Unit
) {
    val timeUnitLabels = mapOf(
        StatisticTimeUnit.MONTH to "月度统计",
        StatisticTimeUnit.YEAR to "年度统计"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 时间单位选择器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                timeUnitLabels.forEach { (unit, label) ->
                    val isSelected = statisticsState.timeUnit == unit
                    Text(
                        text = label,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onTimeUnitChanged(unit) }
                            .padding(8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 时间选择器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    if (statisticsState.timeUnit == StatisticTimeUnit.MONTH) {
                        onPreviousMonth()
                    } else {
                        onPreviousYear()
                    }
                }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "上一个时间段")
                }
                
                // 显示当前时间段
                Text(
                    text = if (statisticsState.timeUnit == StatisticTimeUnit.MONTH) {
                        "${statisticsState.year}年${statisticsState.month}月"
                    } else {
                        "${statisticsState.year}年"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = { 
                    if (statisticsState.timeUnit == StatisticTimeUnit.MONTH) {
                        onNextMonth()
                    } else {
                        onNextYear()
                    }
                }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "下一个时间段")
                }
            }
        }
    }
}

@Composable
fun TotalSummaryCard(statisticsState: StatisticsState) {
    val timeRangeText = if (statisticsState.timeUnit == StatisticTimeUnit.MONTH) {
        "${statisticsState.year}年${statisticsState.month}月"
    } else {
        "${statisticsState.year}年"
    }
    
    val balance = statisticsState.totalIncome - statisticsState.totalExpense
    val balanceColor = if (balance >= 0) IncomeColor else ExpenseColor
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "$timeRangeText 收支统计",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 收入统计
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "收入",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¥%.2f".format(statisticsState.totalIncome),
                        color = IncomeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                
                // 支出统计
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "支出",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¥%.2f".format(statisticsState.totalExpense),
                        color = ExpenseColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                
                // 结余统计
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "结余",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¥%.2f".format(balance),
                        color = balanceColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryPieChart(
    title: String,
    categories: List<CategoryAmount>,
    total: Double,
    colorScheme: List<Color>
) {
    val androidColorList = colorScheme.map { it.toArgb() }.toIntArray()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(350.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // MPAndroidChart 饼图
            AndroidView(
                factory = { context ->
                    PieChart(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        description.isEnabled = false
                        isDrawHoleEnabled = true
                        setHoleColor(AndroidColor.TRANSPARENT)
                        holeRadius = 40f
                        setTransparentCircleAlpha(0)
                        setDrawCenterText(true)
                        centerText = "总计\n¥%.2f".format(total)
                        setCenterTextSize(14f)
                        setUsePercentValues(true)
                        
                        // 图例配置
                        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                        legend.orientation = Legend.LegendOrientation.HORIZONTAL
                        legend.setDrawInside(false)
                        legend.textSize = 12f
                        legend.formSize = 12f
                        
                        setExtraOffsets(10f, 10f, 10f, 10f)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                update = { chart ->
                    val entries = categories.map { category ->
                        PieEntry(category.amount.toFloat(), category.category)
                    }
                    
                    val dataSet = PieDataSet(entries, "").apply {
                        // 如果提供的颜色不够，循环使用颜色数组
                        colors = androidColorList.toList()
                        valueTextSize = 12f
                        valueTextColor = AndroidColor.BLACK
                        valueFormatter = PercentFormatter(chart)
                        setDrawValues(true)
                    }
                    
                    val pieData = PieData(dataSet)
                    chart.data = pieData
                    chart.invalidate() // 刷新图表
                }
            )
        }
    }
}

@Composable
fun EmptyChartPlaceholder(title: String, message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
} 