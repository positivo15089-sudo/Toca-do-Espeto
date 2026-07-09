package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch
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
import com.example.data.model.Product
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddEditDialog by remember { mutableStateOf<Product?>(null) }
    var showAdjustStockDialog by remember { mutableStateOf<Product?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(searchQuery) {
        viewModel.productSearchQuery.value = searchQuery
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Produtos & Estoque", fontWeight = FontWeight.Bold, color = GoldAccent) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = GoldAccent)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddEditDialog = Product(name = "", code = "", price = 0.0, costPrice = 0.0, stockQuantity = 0.0, minStock = 0.0, category = "Espetos") }) {
                        Icon(Icons.Default.Add, contentDescription = "Novo Produto", tint = GoldAccent)
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
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Pesquisar produto por nome ou código...", color = LightGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldAccent) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = LightGray.copy(alpha = 0.3f),
                    focusedTextColor = OffWhite,
                    unfocusedTextColor = OffWhite,
                    focusedContainerColor = DarkGraySurface,
                    unfocusedContainerColor = DarkGraySurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("product_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                text = "CATÁLOGO DE PRODUTOS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum produto cadastrado.", color = LightGray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(products) { prod ->
                        val isLowStock = prod.stockQuantity <= prod.minStock
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkGraySurface)
                                .border(
                                    1.dp,
                                    if (isLowStock) ErrorRed.copy(alpha = 0.4f) else Color.Transparent,
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prod.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = OffWhite
                                            )
                                        )
                                        Text(
                                            text = "SKU: ${prod.code} | Categoria: ${prod.category}",
                                            color = LightGray,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    // Edit / Delete icons
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { showAdjustStockDialog = prod },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Inventory, contentDescription = "Ajustar Estoque", tint = GoldAccent, modifier = Modifier.size(18.dp))
                                        }

                                        IconButton(
                                            onClick = { showAddEditDialog = prod },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = InfoBlue, modifier = Modifier.size(18.dp))
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteProduct(prod) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = ErrorRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Financial metrics
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Column {
                                            Text("Venda", color = LightGray, style = MaterialTheme.typography.labelSmall)
                                            Text(String.format("R$ %.2f", prod.price), color = GoldAccent, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text("Custo", color = LightGray, style = MaterialTheme.typography.labelSmall)
                                            Text(String.format("R$ %.2f", prod.costPrice), color = LightGray, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Stock indicator badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isLowStock) ErrorRed.copy(alpha = 0.15f)
                                                else SuccessGreen.copy(alpha = 0.15f)
                                            )
                                            .border(
                                                1.dp,
                                                if (isLowStock) ErrorRed else SuccessGreen,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Estoque: ${prod.stockQuantity} (Mín: ${prod.minStock})",
                                            color = if (isLowStock) ErrorRed else SuccessGreen,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
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

    // Add / Edit Product Dialog
    showAddEditDialog?.let { prod ->
        val isNew = prod.id == 0
        var name by remember { mutableStateOf(prod.name) }
        var code by remember { mutableStateOf(prod.code) }
        var priceStr by remember { mutableStateOf(if (prod.price > 0) prod.price.toString() else "") }
        var costPriceStr by remember { mutableStateOf(if (prod.costPrice > 0) prod.costPrice.toString() else "") }
        var stockStr by remember { mutableStateOf(if (prod.stockQuantity > 0) prod.stockQuantity.toString() else "") }
        var minStockStr by remember { mutableStateOf(if (prod.minStock > 0) prod.minStock.toString() else "") }
        var category by remember { mutableStateOf(prod.category) }
        val categories = listOf("Espetos", "Bebidas", "Acompanhamentos", "Sobremesas")

        AlertDialog(
            onDismissRequest = { showAddEditDialog = null },
            title = { Text(if (isNew) "Adicionar Produto" else "Editar Produto", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Produto", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Código de Barras / SKU", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Preço Venda", color = LightGray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = costPriceStr,
                            onValueChange = { costPriceStr = it },
                            label = { Text("Preço Custo", color = LightGray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockStr,
                            onValueChange = { stockStr = it },
                            label = { Text("Estoque Atual", color = LightGray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = minStockStr,
                            onValueChange = { minStockStr = it },
                            label = { Text("Estoque Mínimo", color = LightGray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Text("Categoria:", color = OffWhite, style = MaterialTheme.typography.labelSmall)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
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
                        if (name.isNotBlank() && code.isNotBlank()) {
                            viewModel.saveProduct(
                                prod.copy(
                                    name = name.trim(),
                                    code = code.trim(),
                                    price = priceStr.toDoubleOrNull() ?: 0.0,
                                    costPrice = costPriceStr.toDoubleOrNull() ?: 0.0,
                                    stockQuantity = stockStr.toDoubleOrNull() ?: 0.0,
                                    minStock = minStockStr.toDoubleOrNull() ?: 0.0,
                                    category = category
                                )
                            )
                            showAddEditDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkRedPrimary)
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddEditDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = LightGray)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = DarkGraySurface
        )
    }

    // Adjust Stock Dialog
    showAdjustStockDialog?.let { prod ->
        var adjAmount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdjustStockDialog = null },
            title = { Text("Ajustar Estoque", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Produto: ${prod.name}", color = OffWhite, fontWeight = FontWeight.Bold)
                    Text("Estoque Atual: ${prod.stockQuantity}", color = LightGray)

                    OutlinedTextField(
                        value = adjAmount,
                        onValueChange = { adjAmount = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("Quantidade para adicionar/subtrair", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Para subtrair, insira sinal de menos (ex: -10).", color = LightGray, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = adjAmount.toDoubleOrNull()
                        if (amt != null) {
                            scope.launch {
                                viewModel.repository.adjustStock(prod.id, amt)
                                showAdjustStockDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkRedPrimary)
                ) {
                    Text("Lançar Ajuste")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAdjustStockDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = LightGray)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = DarkGraySurface
        )
    }
}
