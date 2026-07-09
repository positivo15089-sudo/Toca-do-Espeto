package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddEditDialog by remember { mutableStateOf<Customer?>(null) }

    LaunchedEffect(searchQuery) {
        viewModel.customerSearchQuery.value = searchQuery
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestão de Clientes", fontWeight = FontWeight.Bold, color = GoldAccent) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = GoldAccent)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddEditDialog = Customer(name = "", cpfCnpj = "", phone = "", email = "", address = "") }) {
                        Icon(Icons.Default.Add, contentDescription = "Novo Cliente", tint = GoldAccent)
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
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar cliente por nome ou CPF/CNPJ...", color = LightGray) },
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
                    .testTag("customer_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                text = "CLIENTES CADASTRADOS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum cliente cadastrado.", color = LightGray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(customers) { cust ->
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
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cust.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = OffWhite
                                            )
                                        )
                                        Text(
                                            text = "CPF/CNPJ: ${cust.cpfCnpj}",
                                            color = GoldAccent,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { showAddEditDialog = cust },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = InfoBlue, modifier = Modifier.size(18.dp))
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteCustomer(cust) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = ErrorRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                Divider(color = LightGray.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                                // Contact Info Row
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = LightGray, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(cust.phone, color = LightGray, style = MaterialTheme.typography.bodySmall)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Email, contentDescription = null, tint = LightGray, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(cust.email, color = LightGray, style = MaterialTheme.typography.bodySmall)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Home, contentDescription = null, tint = LightGray, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(cust.address, color = LightGray, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Customer Dialog
    showAddEditDialog?.let { cust ->
        val isNew = cust.id == 0
        var name by remember { mutableStateOf(cust.name) }
        var cpfCnpj by remember { mutableStateOf(cust.cpfCnpj) }
        var phone by remember { mutableStateOf(cust.phone) }
        var email by remember { mutableStateOf(cust.email) }
        var address by remember { mutableStateOf(cust.address) }

        AlertDialog(
            onDismissRequest = { showAddEditDialog = null },
            title = { Text(if (isNew) "Adicionar Cliente" else "Editar Cliente", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome Completo", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = cpfCnpj,
                        onValueChange = { cpfCnpj = it },
                        label = { Text("CPF ou CNPJ", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefone / WhatsApp", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Endereço Completo", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.saveCustomer(
                                cust.copy(
                                    name = name.trim(),
                                    cpfCnpj = cpfCnpj.trim(),
                                    phone = phone.trim(),
                                    email = email.trim(),
                                    address = address.trim()
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
}
