package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdvScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val cartDiscount by viewModel.cartDiscount.collectAsState()
    val cartCustomer by viewModel.cartCustomer.collectAsState()
    val cartObservation by viewModel.cartObservation.collectAsState()
    val activeTableForCart by viewModel.activeTableForCart.collectAsState()
    val customers by viewModel.customers.collectAsState()

    var productSearch by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    val categories = listOf("Todos", "Espetos", "Bebidas", "Acompanhamentos")

    var showDiscountDialog by remember { mutableStateOf(false) }
    var showCustomerDialog by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showAddProductDetailsDialog by remember { mutableStateOf<Product?>(null) }
    var showCheckoutSuccessDialog by remember { mutableStateOf<Long?>(null) }
    var showScannerDialog by remember { mutableStateOf(false) }

    // Synchronize VM product search query
    LaunchedEffect(productSearch) {
        viewModel.productSearchQuery.value = productSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (activeTableForCart != null) "PDV - ${activeTableForCart!!.numberOrName}" else "PDV - Venda Direta",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GoldAccent)
                        )
                        if (cartCustomer != null) {
                            Text(
                                text = "Cliente: ${cartCustomer!!.name}",
                                style = MaterialTheme.typography.bodySmall.copy(color = LightGray)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearCart()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = GoldAccent)
                    }
                },
                actions = {
                    // Manual barcode scanner simulator button
                    IconButton(onClick = { showScannerDialog = true }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Simular Leitor", tint = GoldAccent)
                    }
                    if (cartItems.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearCart() }) {
                            Text("Limpar", color = ErrorRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBlack)
            )
        },
        containerColor = CharcoalBlack
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp)
        ) {
            // Left Panel: Products Catalog & Search (Weight = 1.3f)
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .padding(end = 8.dp)
            ) {
                // Search Input
                OutlinedTextField(
                    value = productSearch,
                    onValueChange = { productSearch = it },
                    placeholder = { Text("Buscar espetos, bebidas, código...", color = LightGray) },
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
                        .padding(bottom = 8.dp)
                        .testTag("pdv_product_search"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Category Chips Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) DarkRedPrimary else DarkGraySurface)
                                .border(1.dp, if (isSelected) GoldAccent else Color.Transparent, RoundedCornerShape(20.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) OffWhite else LightGray
                                )
                            )
                        }
                    }
                }

                // Products list
                val filteredProducts = if (selectedCategory == "Todos") products else products.filter { it.category == selectedCategory }

                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhum produto encontrado.", color = LightGray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredProducts) { prod ->
                            ProductPdvItem(
                                product = prod,
                                onSelect = { showAddProductDetailsDialog = prod }
                            )
                        }
                    }
                }
            }

            // Right Panel: Intelligent Shopping Cart (Weight = 1f)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkGraySurface)
                    .padding(12.dp)
            ) {
                Text(
                    text = "CARRINHO DE COMPRAS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Cart list items
                if (cartItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = LightGray.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Carrinho vazio", color = LightGray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cartItems) { item ->
                            CartPdvItem(
                                item = item,
                                onQtyChange = { qty -> viewModel.updateCartItemQuantity(item.productId, item.observation, qty) }
                            )
                        }
                    }
                }

                Divider(color = LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                // Cart Totals and Options Panel
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Action buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Associate Client button
                        OutlinedButton(
                            onClick = { showCustomerDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                            border = borderStrokeOrNull(1.dp, GoldAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cliente", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Apply Discount button
                        OutlinedButton(
                            onClick = { showDiscountDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                            border = borderStrokeOrNull(1.dp, GoldAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Discount, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Desconto", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Calculation breakdown details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", color = LightGray, style = MaterialTheme.typography.bodyMedium)
                        Text(String.format("R$ %.2f", viewModel.cartSubtotal), color = OffWhite, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }

                    if (cartDiscount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Desconto", color = ErrorRed, style = MaterialTheme.typography.bodyMedium)
                            Text(String.format("- R$ %.2f", cartDiscount), color = ErrorRed, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Geral", color = GoldAccent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(String.format("R$ %.2f", viewModel.cartTotal), color = GoldAccent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Final Check out buttons based on table association
                    if (activeTableForCart != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Lançar na Mesa
                            Button(
                                onClick = { viewModel.saveCartToTable() },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepGold, contentColor = CharcoalBlack),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("LANÇAR NA MESA", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }

                            // Fechar Mesa/Pagar
                            Button(
                                onClick = { showCheckoutDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = OffWhite),
                                modifier = Modifier.weight(1.1f),
                                shape = RoundedCornerShape(8.dp),
                                enabled = cartItems.isNotEmpty()
                            ) {
                                Text("PAGAR / CONCLUIR", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    } else {
                        // Direct checkout
                        Button(
                            onClick = { showCheckoutDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkRedPrimary, contentColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("pdv_checkout_button"),
                            shape = RoundedCornerShape(8.dp),
                            enabled = cartItems.isNotEmpty()
                        ) {
                            Text("FINALIZAR VENDA", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Product Custom Addition Details Dialog (Qty & Obs)
    showAddProductDetailsDialog?.let { prod ->
        var qty by remember { mutableStateOf(1) }
        var obs by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddProductDetailsDialog = null },
            title = { Text("Adicionar Item", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(prod.name, color = OffWhite, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(String.format("Valor Unitário: R$ %.2f", prod.price), color = LightGray, style = MaterialTheme.typography.bodyMedium)

                    // Qty selectors
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { if (qty > 1) qty-- },
                            modifier = Modifier.background(DarkRedPrimary, CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White)
                        }
                        Text(
                            text = qty.toString(),
                            color = OffWhite,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        IconButton(
                            onClick = { qty++ },
                            modifier = Modifier.background(DarkRedPrimary, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        }
                    }

                    OutlinedTextField(
                        value = obs,
                        onValueChange = { obs = it },
                        label = { Text("Observações (ex: bem passado, sem cebola)", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = LightGray.copy(alpha = 0.3f),
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addToCart(prod, qty, obs)
                        showAddProductDetailsDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkRedPrimary, contentColor = Color.White)
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddProductDetailsDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = LightGray)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = DarkGraySurface
        )
    }

    // Apply Discount Dialog
    if (showDiscountDialog) {
        var discountStr by remember { mutableStateOf(if (cartDiscount > 0) cartDiscount.toString() else "") }
        AlertDialog(
            onDismissRequest = { showDiscountDialog = false },
            title = { Text("Aplicar Desconto (R$)", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = discountStr,
                    onValueChange = { discountStr = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Valor do desconto em R$", color = LightGray) },
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
                        val dVal = discountStr.toDoubleOrNull() ?: 0.0
                        viewModel.applyCartDiscount(dVal)
                        showDiscountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkRedPrimary)
                ) {
                    Text("Aplicar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscountDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = LightGray)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = DarkGraySurface
        )
    }

    // Associate Customer Dialog
    if (showCustomerDialog) {
        var searchQuery by remember { mutableStateOf("") }
        LaunchedEffect(searchQuery) {
            viewModel.customerSearchQuery.value = searchQuery
        }

        AlertDialog(
            onDismissRequest = { showCustomerDialog = false },
            title = { Text("Selecionar Cliente", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(300.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar por nome ou CPF...", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = LightGray.copy(alpha = 0.3f),
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        item {
                            // "Sem Cliente" option
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (cartCustomer == null) DarkRedPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable {
                                        viewModel.setCartCustomer(null)
                                        showCustomerDialog = false
                                    }
                                    .padding(12.dp)
                            ) {
                                Text("Consumidor Geral (Sem identificação)", color = OffWhite, fontWeight = FontWeight.Bold)
                            }
                        }

                        items(customers) { cust ->
                            val isSelected = cartCustomer?.id == cust.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) DarkRedPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable {
                                        viewModel.setCartCustomer(cust)
                                        showCustomerDialog = false
                                    }
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(cust.name, color = OffWhite, fontWeight = FontWeight.Bold)
                                    Text("CPF: ${cust.cpfCnpj}", color = LightGray, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showCustomerDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = GoldAccent)
                ) {
                    Text("Fechar")
                }
            },
            containerColor = DarkGraySurface
        )
    }

    // Checkout & Payment Selection Dialog
    if (showCheckoutDialog) {
        var paymentMethod by remember { mutableStateOf("Pix") }
        val paymentMethods = listOf("Pix", "Dinheiro", "Cartão de Crédito", "Cartão de Débito", "Vale-Alimentação")
        var emitFiscalDoc by remember { mutableStateOf(true) }
        var fiscalTypeChoice by remember { mutableStateOf("NFC-e") }

        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text("Finalizar e Pagar", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(String.format("VALOR TOTAL A PAGAR: R$ %.2f", viewModel.cartTotal), color = GoldAccent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Text("Selecione a Forma de Pagamento:", color = OffWhite, style = MaterialTheme.typography.labelMedium)

                    // Payments radio column
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        paymentMethods.forEach { method ->
                            val isSelected = paymentMethod == method
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) DarkRedPrimary.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable { paymentMethod = method }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { paymentMethod = method },
                                    colors = RadioButtonDefaults.colors(selectedColor = GoldAccent, unselectedColor = LightGray)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(method, color = OffWhite, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    Divider(color = LightGray.copy(alpha = 0.1f))

                    // Fiscal Emission switches
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Emitir Documento Fiscal", color = OffWhite)
                        }
                        Switch(
                            checked = emitFiscalDoc,
                            onCheckedChange = { emitFiscalDoc = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = DarkRedPrimary)
                        )
                    }

                    if (emitFiscalDoc) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (fiscalTypeChoice == "NFC-e") DarkRedPrimary else Color(0xFF2C2C2C))
                                    .border(1.dp, if (fiscalTypeChoice == "NFC-e") GoldAccent else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { fiscalTypeChoice = "NFC-e" }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("NFC-e (Cupom)", color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (fiscalTypeChoice == "NF-e") DarkRedPrimary else Color(0xFF2C2C2C))
                                    .border(1.dp, if (fiscalTypeChoice == "NF-e") GoldAccent else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { fiscalTypeChoice = "NF-e" }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("NF-e (Completa)", color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.checkoutActiveCart(
                            paymentMethod = paymentMethod,
                            emitFiscal = emitFiscalDoc,
                            fiscalType = if (emitFiscalDoc) fiscalTypeChoice else null,
                            onComplete = { saleId ->
                                showCheckoutDialog = false
                                showCheckoutSuccessDialog = saleId
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.testTag("confirm_checkout_button")
                ) {
                    Text("CONFIRMAR E PAGAR")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCheckoutDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = LightGray)
                ) {
                    Text("Voltar")
                }
            },
            containerColor = DarkGraySurface
        )
    }

    // Checkout Success / Pix QR Code / Print Dialog
    showCheckoutSuccessDialog?.let { saleId ->
        AlertDialog(
            onDismissRequest = { showCheckoutSuccessDialog = null },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(64.dp))
            },
            title = {
                Text("Venda Realizada com Sucesso!", color = OffWhite, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Venda registrada no sistema sob o código #$saleId.", color = LightGray, textAlign = TextAlign.Center)

                    // Simulated QR Code for Pix Payments
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Let's draw a beautiful visual representation of Pix QR Code in Compose canvas!
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            // Pix colors & pattern simulation
                            val size = this.size
                            val pixColor = Color(0xFF32BCAD) // Central Pix Teal color
                            // Draw nice geometric mock QR blocks
                            drawRect(color = Color.Black, size = size * 0.25f)
                            drawRect(color = Color.Black, topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.75f, 0f), size = size * 0.25f)
                            drawRect(color = Color.Black, topLeft = androidx.compose.ui.geometry.Offset(0f, size.height * 0.75f), size = size * 0.25f)

                            // Random blocks
                            for (x in 3..12) {
                                for (y in 3..12) {
                                    if ((x + y) % 3 == 0 || (x * y) % 5 == 1) {
                                        drawRect(
                                            color = if ((x + y) % 7 == 0) pixColor else Color.Black,
                                            topLeft = androidx.compose.ui.geometry.Offset(size.width * (x / 16f), size.height * (y / 16f)),
                                            size = androidx.compose.ui.geometry.Size(size.width / 16f, size.height / 16f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text("Chave Pix Copia e Cola Gerada:\n00020126580014BR.GOV.BCB.PIX0114tocadoespeto26", color = GoldAccent, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Print ticket
                        OutlinedButton(
                            onClick = {
                                viewModel.simulatePrint("=== TOCA DO ESPETO ===\nCupom da Venda #$saleId\nValor Total: R$ ${viewModel.cartTotal}\nObrigado pela preferência!")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                            border = borderStrokeOrNull(1.dp, GoldAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Imprimir", fontSize = 11.sp)
                        }

                        // Share receipt
                        OutlinedButton(
                            onClick = { /* simulated sharing via whatsapp */ },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                            border = borderStrokeOrNull(1.dp, GoldAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compartilhar", fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCheckoutSuccessDialog = null
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkRedPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voltar ao Painel")
                }
            },
            containerColor = DarkGraySurface
        )
    }

    // Barcode Scanner Simulator Dialog
    if (showScannerDialog) {
        var manualCode by remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current

        AlertDialog(
            onDismissRequest = { showScannerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Leitor de Código de Barras", color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Simulated camera scanning view
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .border(2.dp, GoldAccent, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Draw red scanning line animation simulation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(ErrorRed)
                        )
                        Text(
                            "MIRA DA CÂMERA ATIVA\n(Aproxime o espeto ou bebida)",
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    Text("Digite manualmente ou use os atalhos abaixo:", color = LightGray, style = MaterialTheme.typography.bodySmall)

                    OutlinedTextField(
                        value = manualCode,
                        onValueChange = { manualCode = it },
                        placeholder = { Text("Código de barras (ex: 101, 102, 201)", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = LightGray.copy(alpha = 0.3f),
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Quick simulation chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.simulateBarcodeScan("101")
                                showScannerDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkRedPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ler Espeto (101)", fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.simulateBarcodeScan("201")
                                showScannerDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkRedPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ler Heineken (201)", fontSize = 10.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualCode.isNotEmpty()) {
                            viewModel.simulateBarcodeScan(manualCode)
                            showScannerDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showScannerDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = LightGray)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = DarkGraySurface
        )
    }
}

@Composable
fun ProductPdvItem(
    product: Product,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkGraySurface)
            .clickable(onClick = onSelect)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = OffWhite)
                )
                Text(
                    text = "Código: ${product.code} | Categoria: ${product.category}",
                    style = MaterialTheme.typography.bodySmall.copy(color = LightGray)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (product.stockQuantity <= product.minStock) ErrorRed.copy(alpha = 0.2f)
                                else SuccessGreen.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Estoque: ${product.stockQuantity}",
                            color = if (product.stockQuantity <= product.minStock) ErrorRed else SuccessGreen,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = String.format("R$ %.2f", product.price),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = GoldAccent)
            )
        }
    }
}

@Composable
fun CartPdvItem(
    item: CartItem,
    onQtyChange: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF252525))
            .padding(10.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = OffWhite)
                    )
                    if (item.observation.isNotEmpty()) {
                        Text(
                            text = "Obs: ${item.observation}",
                            color = GoldAccent,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                }

                Text(
                    text = String.format("R$ %.2f", item.price * item.quantity),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = OffWhite)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = String.format("Unit: R$ %.2f", item.price),
                    style = MaterialTheme.typography.bodySmall.copy(color = LightGray)
                )

                // +/- buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                            .clickable { onQtyChange(item.quantity - 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = OffWhite, modifier = Modifier.size(14.dp))
                    }

                    Text(
                        text = item.quantity.toString(),
                        color = OffWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(DarkRedPrimary)
                            .clickable { onQtyChange(item.quantity + 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = OffWhite, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
