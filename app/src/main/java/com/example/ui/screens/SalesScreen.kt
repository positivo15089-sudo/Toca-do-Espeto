package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Sale
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val sales by viewModel.sales.collectAsState()
    val selectedSale by viewModel.selectedSaleForDetails.collectAsState()
    val appConfig by viewModel.appConfig.collectAsState()

    var showDanfeViewer by remember { mutableStateOf(false) }
    var showSefazLogs by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico & Emissão Fiscal", fontWeight = FontWeight.Bold, color = GoldAccent) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedSale != null) {
                            viewModel.selectedSaleForDetails.value = null
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBlack)
            )
        },
        containerColor = CharcoalBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedSale != null) {
                // Sale Details View
                val sale = selectedSale!!
                val items = viewModel.deserializeCartItems(sale.itemsJson)
                val isFiscal = sale.isFiscalEmitted

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Status
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkGraySurface)
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Venda #${sale.id}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = OffWhite)
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isFiscal) SuccessGreen.copy(alpha = 0.15f)
                                            else Color.Gray.copy(alpha = 0.15f)
                                        )
                                        .border(
                                            1.dp,
                                            if (isFiscal) SuccessGreen else Color.Gray,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isFiscal) "${sale.fiscalType} EMITIDA" else "SEM NOTA FISCAL",
                                        color = if (isFiscal) SuccessGreen else Color.Gray,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Data: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(sale.timestamp))}",
                                color = LightGray,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Cliente: ${sale.customerName}",
                                color = LightGray,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Forma de Pagamento: ${sale.paymentMethod}",
                                color = LightGray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Items Panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkGraySurface)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "PRODUTOS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GoldAccent)
                        )

                        items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.productName, color = OffWhite, fontWeight = FontWeight.Bold)
                                    if (item.observation.isNotEmpty()) {
                                        Text("Obs: ${item.observation}", color = GoldAccent, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text("${item.quantity}x R$ ${item.price}", color = LightGray, style = MaterialTheme.typography.bodySmall)
                                }
                                Text(
                                    text = String.format("R$ %.2f", item.price * item.quantity),
                                    color = OffWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Divider(color = LightGray.copy(alpha = 0.05f))
                        }

                        // Calculations
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Subtotal", color = LightGray)
                            Text(String.format("R$ %.2f", sale.totalAmount + sale.discount), color = OffWhite)
                        }

                        if (sale.discount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Desconto", color = ErrorRed)
                                Text(String.format("- R$ %.2f", sale.discount), color = ErrorRed)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total", color = GoldAccent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(String.format("R$ %.2f", sale.totalAmount), color = GoldAccent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // Fiscal Emission Operations Panel
                    if (!isFiscal) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkGraySurface)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "EMISSÃO FISCAL SEFAZ",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GoldAccent)
                            )
                            Text(
                                text = "Esta venda ainda não possui documento fiscal transmitido para a Receita Federal. Selecione o tipo de nota para realizar a transmissão imediata:",
                                color = LightGray,
                                style = MaterialTheme.typography.bodySmall
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        showSefazLogs = "Transmitindo NFC-e..."
                                        viewModel.emitFiscal(sale.id, "NFC-e")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkRedPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Emitir NFC-e", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        showSefazLogs = "Transmitindo NF-e..."
                                        viewModel.emitFiscal(sale.id, "NF-e")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepGold, contentColor = CharcoalBlack),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Emitir NF-e", fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        // Fiscal Emitted: display Details & View DANFE
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkGraySurface)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "DADOS FISCAIS AUTORIZADOS",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GoldAccent)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Chave de Acesso:", color = LightGray, style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = sale.fiscalKey ?: "",
                                    color = GoldAccent,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Tipo:", color = LightGray, style = MaterialTheme.typography.labelSmall)
                                    Text(sale.fiscalType ?: "", color = OffWhite, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Protocolo SEFAZ:", color = LightGray, style = MaterialTheme.typography.labelSmall)
                                    Text("1352600201095", color = OffWhite, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { showDanfeViewer = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("VISUALIZAR DANFE (IMPRIMIR / PDF)")
                            }
                        }
                    }
                }
            } else {
                // Sales Log List View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "VENDAS RECENTES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (sales.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nenhuma venda registrada.", color = LightGray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(sales) { s ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(DarkGraySurface)
                                        .clickable { viewModel.selectedSaleForDetails.value = s }
                                        .padding(16.dp)
                                        .testTag("sale_log_item_${s.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Venda #${s.id}",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = OffWhite)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                if (s.isFiscalEmitted) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(SuccessGreen.copy(alpha = 0.15f))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(s.fiscalType ?: "", color = SuccessGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            Text(
                                                text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(s.timestamp)),
                                                color = LightGray,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = s.customerName,
                                                color = LightGray,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = String.format("R$ %.2f", s.totalAmount),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = GoldAccent)
                                            )
                                            Text(
                                                text = s.paymentMethod,
                                                color = LightGray,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Sefaz Transmission Log Simulation
    showSefazLogs?.let { initialText ->
        var stepText by remember { mutableStateOf(initialText) }
        var isDone by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1000)
            stepText = "Conectando ao WebService SEFAZ-SP...\nStatus: Canal Seguro Iniciado."
            kotlinx.coroutines.delay(1000)
            stepText = "Validando assinaturas digitais do XML...\nStatus: Nota fiscal validada."
            kotlinx.coroutines.delay(1000)
            stepText = "Protocolando solicitação na SEFAZ...\nStatus: Lote processado com sucesso!"
            kotlinx.coroutines.delay(1000)
            isDone = true
        }

        AlertDialog(
            onDismissRequest = { if (isDone) showSefazLogs = null },
            icon = {
                if (!isDone) CircularProgressIndicator(color = GoldAccent)
                else Icon(Icons.Default.CloudDone, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(48.dp))
            },
            title = { Text("Transmissão SEFAZ", color = OffWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = stepText,
                    color = LightGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(12.dp)
                )
            },
            confirmButton = {
                if (isDone) {
                    Button(
                        onClick = { showSefazLogs = null },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Text("Ver Recibo")
                    }
                }
            },
            containerColor = DarkGraySurface
        )
    }

    // DANFE Document Viewer (Thermal Receipt style!)
    if (showDanfeViewer && selectedSale != null) {
        val sale = selectedSale!!
        val items = viewModel.deserializeCartItems(sale.itemsJson)

        AlertDialog(
            onDismissRequest = { showDanfeViewer = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = CharcoalBlack, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Visualizar DANFE", color = CharcoalBlack, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                // Custom Thermal styling: White background, Charcoal text, Monospaced look!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(Color.White)
                        .border(1.dp, Color.Gray)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOCA DO ESPETO LTDA",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "CNPJ: ${appConfig?.cnpj ?: "12.345.678/0001-90"}\nIE: 123.456.789.111\nRua dos Churrasqueiros, 200 - São Paulo - SP",
                        color = Color.Black,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "------------------------------------------\nDANFE Simplificado da ${sale.fiscalType}\nDocumento Fiscal Auxiliar\n------------------------------------------",
                        color = Color.Black,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Items Table
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "COD  DESC  QTD  VL_UN  VL_TOT",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        items.forEach { item ->
                            Text(
                                text = "${item.productId}  ${item.productName.take(12)} ${item.quantity}x R$ ${item.price} R$ ${item.price * item.quantity}",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "------------------------------------------",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SUBTOTAL:", color = Color.Black, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(String.format("R$ %.2f", sale.totalAmount + sale.discount), color = Color.Black, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        if (sale.discount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("DESCONTO:", color = Color.Black, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text(String.format("R$ %.2f", sale.discount), color = Color.Black, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("VALOR TOTAL:", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(String.format("R$ %.2f", sale.totalAmount), color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Text(
                        text = "------------------------------------------",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "CHAVE DE ACESSO\n${sale.fiscalKey}\n\nProtocolo SEFAZ: 1352600201095\nData: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(sale.timestamp))}\nConsumidor: ${sale.customerName}\nCPG/CPF: ${sale.customerName}",
                        color = Color.Black,
                        fontSize = 8.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mock QR Code block
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.Black)
                            .padding(4.dp)
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val size = this.size
                            // Draw nice QR-like blocks
                            drawRect(color = Color.White, size = size * 0.25f)
                            drawRect(color = Color.White, topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.75f, 0f), size = size * 0.25f)
                            drawRect(color = Color.White, topLeft = androidx.compose.ui.geometry.Offset(0f, size.height * 0.75f), size = size * 0.25f)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tributos Totais Incidentes (Lei 12.741/12):\nFederal: R$ ${(sale.totalAmount * 0.04)} | Estadual: R$ ${(sale.totalAmount * 0.12)}",
                        color = Color.Black,
                        fontSize = 8.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Bluetooth thermal printer trigger
                        Button(
                            onClick = {
                                viewModel.simulatePrint("=== TOCA DO ESPETO ===\nDANFE ${sale.fiscalType}\nChave: ${sale.fiscalKey}\nTotal: R$ ${sale.totalAmount}\nOBRIGADO!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkRedPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Imprimir", fontSize = 11.sp)
                        }

                        // WhatsApp trigger
                        Button(
                            onClick = { /* simulated whatsapp share */ },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontSize = 11.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { /* simulated email share */ },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepGold, contentColor = CharcoalBlack),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("E-mail", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { showDanfeViewer = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Fechar")
                        }
                    }
                }
            },
            containerColor = OffWhite,
            titleContentColor = CharcoalBlack
        )
    }
}
