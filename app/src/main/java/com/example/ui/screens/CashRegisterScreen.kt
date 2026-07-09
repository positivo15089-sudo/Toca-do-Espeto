package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.CashRegister
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashRegisterScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val activeSession by viewModel.activeCashSession.collectAsState()
    val sales by viewModel.sales.collectAsState()

    var floatAmountStr by remember { mutableStateOf("") }
    var closingAmountStr by remember { mutableStateOf("") }
    var showOpenDialog by remember { mutableStateOf(false) }
    var showCloseDialog by remember { mutableStateOf(false) }

    // Calculate session sales values
    val currentSessionSales = if (activeSession != null) {
        sales.filter { it.timestamp >= activeSession!!.openTimestamp }
    } else {
        emptyList()
    }
    val currentSessionTotalInvoiced = currentSessionSales.sumOf { it.totalAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Controle de Caixa", fontWeight = FontWeight.Bold, color = GoldAccent) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = GoldAccent)
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
            // Active Session Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkGraySurface)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STATUS DO CAIXA",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GoldAccent)
                            )
                            Text(
                                text = if (activeSession != null) "CAIXA OPERANDO (ABERTO)" else "CAIXA FECHADO",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeSession != null) SuccessGreen else ErrorRed
                                )
                            )
                        }

                        // Status Dot Icon
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (activeSession != null) SuccessGreen else ErrorRed)
                        )
                    }

                    if (activeSession != null) {
                        val session = activeSession!!
                        Divider(color = LightGray.copy(alpha = 0.1f))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Abertura:", color = LightGray)
                                Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(session.openTimestamp)), color = OffWhite)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Fundo de Troco Inicial:", color = LightGray)
                                Text(String.format("R$ %.2f", session.initialAmount), color = OffWhite, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Vendas em Cartão/Pix/Dinheiro:", color = LightGray)
                                Text(String.format("R$ %.2f", currentSessionTotalInvoiced), color = SuccessGreen, fontWeight = FontWeight.Bold)
                            }
                            Divider(color = LightGray.copy(alpha = 0.05f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Saldo Estimado em Caixa:", color = GoldAccent, fontWeight = FontWeight.Bold)
                                Text(String.format("R$ %.2f", session.initialAmount + currentSessionTotalInvoiced), color = GoldAccent, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showCloseDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("close_cash_button")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("FECHAR CAIXA", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Closed state action
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showOpenDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("open_cash_button")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ABRIR NOVO CAIXA", fontWeight = FontWeight.Bold, color = OffWhite)
                        }
                    }
                }
            }

            // Session sales summary
            if (activeSession != null) {
                Text(
                    text = "VENDAS DA SESSÃO ATUAL",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GoldAccent)
                )

                if (currentSessionSales.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhuma venda realizada nesta sessão.", color = LightGray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(currentSessionSales) { s ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkGraySurface)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Venda #${s.id} - ${s.customerName}", color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(s.timestamp)) + " | " + s.paymentMethod, color = LightGray, fontSize = 11.sp)
                                }
                                Text(String.format("R$ %.2f", s.totalAmount), color = GoldAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    // Open Cash Dialog
    if (showOpenDialog) {
        AlertDialog(
            onDismissRequest = { showOpenDialog = false },
            title = { Text("Abrir Caixa (Abertura)", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Informe o valor do fundo de troco inicial disponível no caixa físico:", color = LightGray)
                    OutlinedTextField(
                        value = floatAmountStr,
                        onValueChange = { floatAmountStr = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("Ex: 150.00", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valAmt = floatAmountStr.toDoubleOrNull() ?: 0.0
                        viewModel.openCash(valAmt)
                        floatAmountStr = ""
                        showOpenDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Abrir Caixa")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showOpenDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = LightGray)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = DarkGraySurface
        )
    }

    // Close Cash Dialog
    if (showCloseDialog) {
        val estimatedTotal = (activeSession?.initialAmount ?: 0.0) + currentSessionTotalInvoiced

        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text("Fechar Turno de Caixa", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Confirmar encerramento de atividades do operador. Confirme os valores:", color = LightGray)
                    Text(String.format("Faturamento estimado: R$ %.2f", estimatedTotal), color = SuccessGreen, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = closingAmountStr,
                        onValueChange = { closingAmountStr = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("Valor físico contado em caixa", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valAmt = closingAmountStr.toDoubleOrNull() ?: estimatedTotal
                        viewModel.closeCash(valAmt)
                        closingAmountStr = ""
                        showCloseDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Confirmar Fechamento")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCloseDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = LightGray)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = DarkGraySurface
        )
    }
}
