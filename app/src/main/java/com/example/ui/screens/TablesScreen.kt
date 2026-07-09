package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TableOrComanda
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablesScreen(
    viewModel: MainViewModel,
    onNavigateToPdv: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val tablesComandas by viewModel.tablesComandas.collectAsState()
    var showAddTableDialog by remember { mutableStateOf(false) }
    var selectedTableForDetails by remember { mutableStateOf<TableOrComanda?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mesas & Comandas", fontWeight = FontWeight.Bold, color = GoldAccent) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = GoldAccent)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddTableDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Nova Mesa/Comanda", tint = GoldAccent)
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
                .padding(16.dp)
        ) {
            // Display instructions
            Text(
                text = "CONTROLE DE ATENDIMENTO",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (tablesComandas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma mesa ou comanda cadastrada.", color = LightGray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(130.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(tablesComandas) { table ->
                        val isOpen = table.status == "OPEN"
                        Box(
                            modifier = Modifier
                                .height(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkGraySurface)
                                .border(
                                    1.dp,
                                    if (isOpen) ErrorRed.copy(alpha = 0.5f) else SuccessGreen.copy(alpha = 0.3f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    if (isOpen) {
                                        selectedTableForDetails = table
                                    } else {
                                        // Start a new order on this table/comanda
                                        viewModel.loadTableToCart(table)
                                        onNavigateToPdv()
                                    }
                                }
                                .padding(12.dp)
                                .testTag("table_item_${table.id}")
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (table.numberOrName.contains("Mesa")) Icons.Default.TableRestaurant else Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = if (isOpen) ErrorRed else SuccessGreen,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    // Small circle badge
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isOpen) ErrorRed else SuccessGreen)
                                    )
                                }

                                Column {
                                    Text(
                                        text = table.numberOrName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = OffWhite
                                        )
                                    )
                                    Text(
                                        text = if (isOpen) String.format("R$ %.2f", table.totalAmount) else "Livre",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isOpen) GoldAccent else SuccessGreen
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Mesa/Comanda Dialog
    if (showAddTableDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddTableDialog = false },
            title = { Text("Cadastrar Mesa / Comanda", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Ex: Mesa 06 ou Comanda 115", color = LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = LightGray.copy(alpha = 0.3f),
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.saveTableOrComanda(name.trim())
                            showAddTableDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkRedPrimary)
                ) {
                    Text("Cadastrar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddTableDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = LightGray)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = DarkGraySurface
        )
    }

    // Occupied Table Options / Details Dialog
    selectedTableForDetails?.let { table ->
        val items = viewModel.deserializeCartItems(table.itemsJson)

        AlertDialog(
            onDismissRequest = { selectedTableForDetails = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Detalhes - ${table.numberOrName}", color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = String.format("CONSUMO ACUMULADO: R$ %.2f", table.totalAmount),
                        color = GoldAccent,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Divider(color = LightGray.copy(alpha = 0.1f))

                    Text("Produtos Consumidos:", color = OffWhite, style = MaterialTheme.typography.labelMedium)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        if (items.isEmpty()) {
                            Text("Sem itens associados.", color = LightGray)
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(items) { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${item.quantity}x ${item.productName}",
                                            color = OffWhite,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = String.format("R$ %.2f", item.price * item.quantity),
                                            color = LightGray,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Adicionar Itens
                    OutlinedButton(
                        onClick = {
                            viewModel.loadTableToCart(table)
                            selectedTableForDetails = null
                            onNavigateToPdv()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                        border = borderStrokeOrNull(1.dp, GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ADICIONAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Receber/Fechar Mesa
                    Button(
                        onClick = {
                            viewModel.loadTableToCart(table)
                            selectedTableForDetails = null
                            onNavigateToPdv()
                            // Note: PDV screen automatically loads activeTableForCart and cart items!
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = OffWhite),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.1f)
                    ) {
                        Text("FECHAR MESA", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { selectedTableForDetails = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = LightGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fechar Painel", textAlign = TextAlign.Center)
                }
            },
            containerColor = DarkGraySurface
        )
    }
}
