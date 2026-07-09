package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val code: String,
    val price: Double,
    val costPrice: Double,
    val stockQuantity: Double,
    val minStock: Double,
    val category: String, // Espetos, Bebidas, Acompanhamentos, Sobremesas, etc.
    val imageUrl: String = ""
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val cpfCnpj: String,
    val phone: String,
    val email: String,
    val address: String
)

@Entity(tableName = "tables_comandas")
data class TableOrComanda(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val numberOrName: String, // e.g., "Mesa 01", "Comanda 12"
    val status: String, // IDLE, OPEN
    val totalAmount: Double = 0.0,
    val itemsJson: String = "[]" // JSON representation of List<CartItem>
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val customerName: String = "Cliente Geral",
    val customerId: Int? = null,
    val itemsJson: String, // JSON representation of List<CartItem>
    val discount: Double = 0.0,
    val totalAmount: Double,
    val paymentMethod: String, // Pix, Dinheiro, Cartão de Crédito, Cartão de Débito, Vale-Alimentação
    val paymentStatus: String = "PAGO", // PAGO, PENDENTE
    val isFiscalEmitted: Boolean = false,
    val fiscalType: String? = null, // NFC-e, NF-e
    val fiscalKey: String? = null, // 44 digit access key
    val fiscalStatus: String? = null, // TRANSMITIDO, REJEITADO, CONTINGENCIA
    val xmlContent: String? = null
)

@Entity(tableName = "cash_registers")
data class CashRegister(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val openTimestamp: Long = System.currentTimeMillis(),
    val closeTimestamp: Long? = null,
    val initialAmount: Double,
    val finalAmount: Double? = null,
    val status: String = "OPEN" // OPEN, CLOSED
)

@Entity(tableName = "financial_transactions")
data class FinancialTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // RECEITA, DESPESA
    val category: String, // Venda, Insumos, Funcionários, Aluguel, Outros
    val description: String,
    val amount: Double
)

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 1,
    val companyName: String = "Toca do Espeto Ltda",
    val cnpj: String = "12.345.678/0001-90",
    val ie: String = "123.456.789.111",
    val taxRegime: String = "Simples Nacional",
    val bluetoothPrinterMac: String = "",
    val googleDriveBackupEnabled: Boolean = false,
    val biometricLoginEnabled: Boolean = false,
    val isGoogleConnected: Boolean = false,
    val googleAccountName: String = "",
    val googleAccountEmail: String = "",
    val googleAccountAvatarUrl: String = ""
)

// Data class representing an item in the cart/order
data class CartItem(
    val productId: Int,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val observation: String = ""
)
