package com.example.monay.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monay.ui.theme.ExpenseColor
import com.example.monay.ui.theme.IncomeColor
import com.example.monay.ui.theme.TransferColor
import java.text.SimpleDateFormat
import java.util.*

// 定义用于传递账单输入数据的数据类
data class BillInputData(
    val type: String,
    val category: String,
    val amount: String,
    val time: Long = System.currentTimeMillis(),
    val remark: String
)

// 预定义的账单类别
data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val type: String // 支出/收入/转账
)

val expenseCategories = listOf(
    CategoryItem("餐饮", Icons.Default.Fastfood, "支出"),
    CategoryItem("交通", Icons.Default.LocalTaxi, "支出"),
    CategoryItem("购物", Icons.Default.ShoppingCart, "支出"),
    CategoryItem("住房", Icons.Default.House, "支出"),
    CategoryItem("娱乐", Icons.Default.SportsEsports, "支出"),
    CategoryItem("医疗", Icons.Default.MedicalServices, "支出"),
    CategoryItem("教育", Icons.Default.School, "支出"),
    CategoryItem("其他", Icons.Default.More, "支出")
)

val incomeCategories = listOf(
    CategoryItem("工资", Icons.Default.Workspaces, "收入"),
    CategoryItem("奖金", Icons.Default.EmojiEvents, "收入"),
    CategoryItem("利息", Icons.Default.AccountBalance, "收入"),
    CategoryItem("其他", Icons.Default.More, "收入")
)

val transferCategories = listOf(
    CategoryItem("转账", Icons.Default.SwapHoriz, "转账"),
    CategoryItem("还款", Icons.Default.Payment, "转账")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillScreen(
    onSaveBill: (BillInputData) -> Unit
) {
    var selectedType by remember { mutableStateOf("支出") }
    var selectedCategory by remember { mutableStateOf<CategoryItem?>(null) }
    var amount by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    
    val categoryList = when (selectedType) {
        "支出" -> expenseCategories
        "收入" -> incomeCategories
        else -> transferCategories
    }
    
    if (selectedCategory == null || selectedCategory!!.type != selectedType) {
        selectedCategory = categoryList.firstOrNull()
    }
    
    // 处理日期选择
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("记一笔") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { /* 返回逻辑 */ }) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 类型选择器
            TypeSelector(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )
            
            // 金额输入
            AmountInput(
                amount = amount,
                onAmountChange = { amount = it },
                type = selectedType
            )
            
            // 分类选择器
            CategorySelector(
                categories = categoryList,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
            
            // 日期选择
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange, 
                        contentDescription = "选择日期",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = dateFormatter.format(Date(selectedDate)),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // 备注输入
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "备注",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = remark,
                        onValueChange = { remark = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("添加备注信息（可选）") },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        ),
                        maxLines = 3
                    )
                }
            }
            
            // 保存按钮
            Button(
                onClick = {
                    if (amount.isNotBlank() && selectedCategory != null) {
                        val billData = BillInputData(
                            type = selectedType,
                            category = selectedCategory!!.name,
                            amount = amount,
                            time = selectedDate,
                            remark = remark
                        )
                        onSaveBill(billData)
                    }
                },
                enabled = amount.isNotBlank() && selectedCategory != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "保存",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    val types = listOf("支出", "收入", "转账")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        types.forEach { type ->
            val isSelected = type == selectedType
            val color = when(type) {
                "支出" -> ExpenseColor
                "收入" -> IncomeColor
                else -> TransferColor
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onTypeSelected(type) }
                    .padding(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) color else color.copy(alpha = 0.1f))
                ) {
                    val icon = when(type) {
                        "支出" -> Icons.Default.ArrowDownward
                        "收入" -> Icons.Default.ArrowUpward
                        else -> Icons.Default.SwapHoriz
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = type,
                        tint = if (isSelected) Color.White else color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = type,
                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun AmountInput(
    amount: String,
    onAmountChange: (String) -> Unit,
    type: String
) {
    val amountColor = when(type) {
        "支出" -> ExpenseColor
        "收入" -> IncomeColor
        else -> TransferColor
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "金额",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "¥",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                TextField(
                    value = amount,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d+(\\.\\d{0,2})?\$"))) {
                            onAmountChange(it)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = amountColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    ),
                    placeholder = { 
                        Text(
                            text = "0.00",
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        ) 
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun CategorySelector(
    categories: List<CategoryItem>,
    selectedCategory: CategoryItem?,
    onCategorySelected: (CategoryItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "分类",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = category == selectedCategory
                    val backgroundColor = when(category.type) {
                        "支出" -> ExpenseColor
                        "收入" -> IncomeColor
                        else -> TransferColor
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(64.dp)
                            .clickable { onCategorySelected(category) }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) backgroundColor else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    border = if (isSelected) BorderStroke(2.dp, backgroundColor) else BorderStroke(0.dp, Color.Transparent),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.name,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) backgroundColor else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAddBillScreen() {
    AddBillScreen(onSaveBill = {})
} 