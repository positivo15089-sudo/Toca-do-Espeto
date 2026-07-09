package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val sales by viewModel.sales.collectAsState()
    val totalInvoicing by viewModel.totalInvoicing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relatórios & Analytics", fontWeight = FontWeight.Bold, color = GoldAccent) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "INDICADORES E DESEMPENHO",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GoldAccent, letterSpacing = 1.sp)
            )

            // Total Invoiced Card with large elegant text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(DarkRedSecondary, CharcoalBlack)
                        )
                    )
                    .border(1.dp, GoldAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text("FATURAMENTO ACUMULADO", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format("R$ %.2f", totalInvoicing ?: 0.0),
                        color = OffWhite,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("Total processado através do PDV inteligente offline-first.", color = LightGray.copy(alpha = 0.7f), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }

            // 1. Weekly Invoicing Trend Chart (Stunning Area/Line Canvas Chart)
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
                        Text("FATURAMENTO SEMANAL (EVOLUÇÃO R$)", color = OffWhite, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("Julho 2026", color = GoldAccent, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Line Chart using custom canvas drawing
                    val trendData = listOf(350.0, 520.0, 410.0, 780.0, 620.0, 950.0, 1150.0)
                    val days = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        val paddingX = 40f
                        val paddingY = 40f
                        val graphWidth = width - (paddingX * 2)
                        val graphHeight = height - (paddingY * 2)

                        val maxVal = trendData.maxOrNull() ?: 1.0
                        val stepX = graphWidth / (trendData.size - 1)

                        // Draw Grid lines
                        for (i in 0..4) {
                            val y = paddingY + (graphHeight * (i / 4f))
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.15f),
                                start = Offset(paddingX, y),
                                end = Offset(width - paddingX, y),
                                strokeWidth = 2f
                            )
                        }

                        // Plot Line and Area Points
                        val points = trendData.mapIndexed { index, valData ->
                            val x = paddingX + (index * stepX)
                            val ratio = valData / maxVal
                            val y = height - paddingY - (graphHeight * ratio).toFloat()
                            Offset(x, y)
                        }

                        // Draw area gradient under the line
                        val areaPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(points.first().x, height - paddingY)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(points.last().x, height - paddingY)
                            close()
                        }
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(DarkRedPrimary.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )

                        // Draw line connecting points
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = GoldAccent,
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 5f
                            )
                        }

                        // Draw points dots
                        points.forEach { pt ->
                            drawCircle(
                                color = Color.White,
                                radius = 8f,
                                center = pt
                            )
                            drawCircle(
                                color = DarkRedPrimary,
                                radius = 5f,
                                center = pt
                            )
                        }
                    }

                    // Render Days labels below chart
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        days.forEach { day ->
                            Text(day, color = LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Curva ABC / Best Selling Products (Quantity Progress Bars)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkGraySurface)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "TOP 5 PRODUTOS MAIS VENDIDOS",
                        color = OffWhite,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val topProducts = listOf(
                        TopProductItem("Espeto de Carne Premium", 450, 0.9f, DarkRedPrimary),
                        TopProductItem("Cerveja Heineken Long Neck", 320, 0.72f, GoldAccent),
                        TopProductItem("Espeto de Queijo Coalho", 210, 0.55f, DeepGold),
                        TopProductItem("Espeto de Pão de Alho", 185, 0.45f, OrangeWarning),
                        TopProductItem("Refrigerante Coca-Cola Lata", 140, 0.35f, InfoBlue)
                    )

                    topProducts.forEach { prod ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(prod.name, color = OffWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${prod.units} un", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Custom progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color.DarkGray.copy(alpha = 0.5f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(prod.percentage)
                                        .background(prod.color)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Sales By Category Distribution (Grid of progress)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkGraySurface)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "VENDAS POR CATEGORIA (%)",
                        color = OffWhite,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val categories = listOf(
                        CategoryShare("Espetaria Completa", "62%", 0.62f, DarkRedPrimary),
                        CategoryShare("Bebidas e Cervejas", "24%", 0.24f, GoldAccent),
                        CategoryShare("Acompanhamentos", "11%", 0.11f, DeepGold),
                        CategoryShare("Sobremesas e Outros", "3%", 0.03f, InfoBlue)
                    )

                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(cat.color)
                            )
                            Text(
                                text = cat.name,
                                color = OffWhite,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = cat.share,
                                color = GoldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

data class TopProductItem(
    val name: String,
    val units: Int,
    val percentage: Float,
    val color: Color
)

data class CategoryShare(
    val name: String,
    val share: String,
    val ratio: Float,
    val color: Color
)
