package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FinancialTransaction
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    var showAddTransactionDialog by remember { mutableStateOf(false) }

    // Computations
    val totalInflows = transactions.filter { it.type == "RECEITA" }.sumOf { it.amount }
    val totalOutflows = transactions.filter { it.type == "DESPESA" }.sumOf { it.amount }
    val currentBalance = totalInflows - totalOutflows

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fluxo Financeiro", fontWeight = FontWeight.Bold, color = GoldAccent) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = GoldAccent)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddTransactionDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Nova Transação", tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBlack)
            )
        },
        containerColor = CharcoalBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Financial Ledger Dashboard Cards
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkGraySurface)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "RESUMO FINANCEIRO",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GoldAccent)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Receitas Card
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SuccessGreen.copy(alpha = 0.08f))
                                .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Receitas", color = LightGray, fontSize = 11.sp)
                                Text(String.format("+ R$ %.2f", totalInflows), color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        // Despesas Card
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ErrorRed.copy(alpha = 0.08f))
                                .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Despesas", color = LightGray, fontSize = 11.sp)
                                Text(String.format("- R$ %.2f", totalOutflows), color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    Divider(color = LightGray.copy(alpha = 0.1f))

                    // Consolidated Balance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Saldo Consolidado:", color = OffWhite, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format("R$ %.2f", currentBalance),
                            color = if (currentBalance >= 0) SuccessGreen else ErrorRed,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Text(
                text = "LIVRO CAIXA / TRANSAÇÕES",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GoldAccent, letterSpacing = 1.sp)
            )

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma transação financeira registrada.", color = LightGray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(transactions) { trans ->
                        val isReceipt = trans.type == "RECEITA"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkGraySurface)
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isReceipt) SuccessGreen.copy(alpha = 0.15f)
                                                else ErrorRed.copy(alpha = 0.15f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isReceipt) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                            contentDescription = null,
                                            tint = if (isReceipt) SuccessGreen else ErrorRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = trans.description,
                                            color = OffWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Categoria: ${trans.category} | ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(trans.timestamp))}",
                                            color = LightGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = String.format("${if (isReceipt) "+" else "-"} R$ %.2f", trans.amount),
                                        color = if (isReceipt) SuccessGreen else ErrorRed,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )

                                    IconButton(
                                        onClick = { viewModel.deleteFinancialTransaction(trans) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = ErrorRed.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Transaction Dialog
    if (showAddTransactionDialog) {
        var type by remember { mutableStateOf("RECEITA") }
        var category by remember { mutableStateOf("Outros") }
        var description by remember { mutableStateOf("") }
        var amountStr by remember { mutableStateOf("") }

        val categoriesInflow = listOf("Venda", "Aporte de Caixa", "Outros")
        val categoriesOutflow = listOf("Insumos", "Funcionários", "Aluguel", "Infraestrutura", "Outros")

        AlertDialog(
            onDismissRequest = { showAddTransactionDialog = false },
            title = { Text("Registrar Transação Financeira", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tipo de Lançamento:", color = OffWhite, style = MaterialTheme.typography.labelSmall)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (type == "RECEITA") SuccessGreen else Color(0xFF2C2C2C))
                                .clickable {
                                    type = "RECEITA"
                                    category = "Outros"
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Entrada (Receita)", color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (type == "DESPESA") ErrorRed else Color(0xFF2C2C2C))
                                .clickable {
                                    type = "DESPESA"
                                    category = "Outros"
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Saída (Despesa)", color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição do Lançamento", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Valor (R$)", color = LightGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Selecione a Categoria:", color = OffWhite, style = MaterialTheme.typography.labelSmall)

                    val activeCategories = if (type == "RECEITA") categoriesInflow else categoriesOutflow
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activeCategories.forEach { cat ->
                            val isSelected = category == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) DarkRedPrimary else Color(0xFF2C2C2C))
                                    .border(1.dp, if (isSelected) GoldAccent else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { category = cat }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(cat, color = OffWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valAmt = amountStr.toDoubleOrNull()
                        if (description.isNotBlank() && valAmt != null) {
                            viewModel.addFinancialTransaction(type, category, description.trim(), valAmt)
                            showAddTransactionDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkRedPrimary)
                ) {
                    Text("Registrar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddTransactionDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = LightGray)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = DarkGraySurface
        )
    }
}
