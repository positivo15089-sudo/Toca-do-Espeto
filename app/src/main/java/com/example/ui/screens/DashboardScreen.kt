package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit
) {
    val sales by viewModel.sales.collectAsState()
    val totalInvoicing by viewModel.totalInvoicing.collectAsState()
    val activeCashSession by viewModel.activeCashSession.collectAsState()
    val products by viewModel.products.collectAsState()

    // Calculate low stock items count
    val lowStockCount = products.filter { it.stockQuantity <= it.minStock }.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkRedPrimary)
                                .border(1.dp, GoldAccent, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Toca do Espeto",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldAccent,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "Gestão Integrada",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = LightGray.copy(alpha = 0.7f),
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Cash Session Status Chip
                    val isOpen = activeCashSession != null
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isOpen) SuccessGreen.copy(alpha = 0.15f)
                                else ErrorRed.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (isOpen) SuccessGreen else ErrorRed,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable { onNavigate("cash_register") }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isOpen) SuccessGreen else ErrorRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOpen) "CAIXA ABERTO" else "CAIXA FECHADO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOpen) SuccessGreen else ErrorRed
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalBlack,
                    titleContentColor = GoldAccent
                )
            )
        },
        containerColor = CharcoalBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Metrics Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Invoicing Card
                MetricCard(
                    title = "Faturamento",
                    value = String.format("R$ %.2f", totalInvoicing ?: 0.0),
                    icon = Icons.Default.Paid,
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )

                // Sales Count Card
                MetricCard(
                    title = "Vendas",
                    value = sales.size.toString(),
                    icon = Icons.Default.ShoppingCart,
                    color = InfoBlue,
                    modifier = Modifier.weight(1f)
                )

                // Stock Alerts Card
                MetricCard(
                    title = "Aviso Estoque",
                    value = lowStockCount.toString(),
                    icon = Icons.Default.Inventory,
                    color = if (lowStockCount > 0) OrangeWarning else SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "PAINEL ADMINISTRATIVO",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Fast Menu Grid (Manual flow to handle in scrollable easily)
            val menuItems = listOf(
                DashboardMenuItem("Vendas (PDV)", Icons.Default.PointOfSale, "pdv", DarkRedPrimary),
                DashboardMenuItem("Mesas & Comandas", Icons.Default.TableRestaurant, "tables_comandas", DeepGold),
                DashboardMenuItem("Produtos", Icons.Default.Fastfood, "products", OrangeWarning),
                DashboardMenuItem("Clientes", Icons.Default.People, "customers", InfoBlue),
                DashboardMenuItem("Controle de Caixa", Icons.Default.LocalAtm, "cash_register", SuccessGreen),
                DashboardMenuItem("Histórico & Fiscal", Icons.Default.ReceiptLong, "sales_history", GoldAccent),
                DashboardMenuItem("Financeiro", Icons.Default.MonetizationOn, "finance", SuccessGreen),
                DashboardMenuItem("Relatórios & Analytics", Icons.Default.BarChart, "reports", InfoBlue),
                DashboardMenuItem("Configurações", Icons.Default.Settings, "settings", LightGray)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Let's lay them out in Rows of 2 elements for perfect grid presentation in scrollview
                for (i in menuItems.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val item1 = menuItems[i]
                        MenuButton(
                            item = item1,
                            onClick = { onNavigate(item1.screenRoute) },
                            modifier = Modifier.weight(1f)
                        )

                        if (i + 1 < menuItems.size) {
                            val item2 = menuItems[i + 1]
                            MenuButton(
                                item = item2,
                                onClick = { onNavigate(item2.screenRoute) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Low Stock Warning Panel if any
            if (lowStockCount > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(OrangeWarning.copy(alpha = 0.15f))
                        .border(1.dp, OrangeWarning, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = OrangeWarning,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Alerta de Reposição!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeWarning
                                )
                            )
                            Text(
                                text = "Existem $lowStockCount produtos operando com estoque abaixo do mínimo necessário.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OffWhite.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkGraySurface)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = LightGray,
                        fontWeight = FontWeight.Bold
                    )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = OffWhite,
                    fontSize = 15.sp
                )
            )
        }
    }
}

data class DashboardMenuItem(
    val title: String,
    val icon: ImageVector,
    val screenRoute: String,
    val accentColor: Color
)

@Composable
fun MenuButton(
    item: DashboardMenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkGraySurface)
            .border(1.dp, item.accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag("menu_button_${item.screenRoute}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(item.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = OffWhite
                ),
                maxLines = 1
            )
        }
    }
}
