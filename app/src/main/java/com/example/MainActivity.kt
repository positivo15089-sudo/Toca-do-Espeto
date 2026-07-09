package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val viewModel: MainViewModel = viewModel()
                    TocaApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TocaApp(viewModel: MainViewModel) {
    // Collect the dynamic screen navigation state
    val currentScreen by viewModel.currentScreen.collectAsState()

    when (currentScreen) {
        "login" -> {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { viewModel.navigateTo("dashboard") }
            )
        }
        "dashboard" -> {
            DashboardScreen(
                viewModel = viewModel,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        }
        "pdv" -> {
            PdvScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.navigateTo("dashboard") }
            )
        }
        "tables_comandas" -> {
            TablesScreen(
                viewModel = viewModel,
                onNavigateToPdv = { viewModel.navigateTo("pdv") },
                onNavigateBack = { viewModel.navigateTo("dashboard") }
            )
        }
        "products" -> {
            ProductsScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.navigateTo("dashboard") }
            )
        }
        "customers" -> {
            CustomersScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.navigateTo("dashboard") }
            )
        }
        "sales_history" -> {
            SalesScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.navigateTo("dashboard") }
            )
        }
        "cash_register" -> {
            CashRegisterScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.navigateTo("dashboard") }
            )
        }
        "finance" -> {
            FinanceScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.navigateTo("dashboard") }
            )
        }
        "reports" -> {
            ReportsScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.navigateTo("dashboard") }
            )
        }
        "settings" -> {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.navigateTo("dashboard") }
            )
        }
        else -> {
            // Fallback
            DashboardScreen(
                viewModel = viewModel,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        }
    }
}
