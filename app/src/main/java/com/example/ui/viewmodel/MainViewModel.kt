package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.*
import com.example.data.repository.TocaRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = TocaRepository(database)

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val cartListType = Types.newParameterizedType(List::class.java, CartItem::class.java)
    private val cartAdapter = moshi.adapter<List<CartItem>>(cartListType)

    // UI Navigation State
    private val _currentScreen = MutableStateFlow("login")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Active Selection States for Add/Edit
    var selectedProductForEdit = MutableStateFlow<Product?>(null)
    var selectedCustomerForEdit = MutableStateFlow<Customer?>(null)
    var selectedTableOrComanda = MutableStateFlow<TableOrComanda?>(null)
    var selectedSaleForDetails = MutableStateFlow<Sale?>(null)

    // POS Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _cartDiscount = MutableStateFlow(0.0)
    val cartDiscount: StateFlow<Double> = _cartDiscount.asStateFlow()

    private val _cartCustomer = MutableStateFlow<Customer?>(null)
    val cartCustomer: StateFlow<Customer?> = _cartCustomer.asStateFlow()

    private val _cartObservation = MutableStateFlow("")
    val cartObservation: StateFlow<String> = _cartObservation.asStateFlow()

    private val _activeTableForCart = MutableStateFlow<TableOrComanda?>(null)
    val activeTableForCart: StateFlow<TableOrComanda?> = _activeTableForCart.asStateFlow()

    // Printer & Scanner States (Simulated)
    private val _bluetoothDevices = MutableStateFlow<List<String>>(emptyList())
    val bluetoothDevices: StateFlow<List<String>> = _bluetoothDevices.asStateFlow()

    private val _isScanningBluetooth = MutableStateFlow(false)
    val isScanningBluetooth: StateFlow<Boolean> = _isScanningBluetooth.asStateFlow()

    private val _lastPrintedJob = MutableStateFlow<String?>(null)
    val lastPrintedJob: StateFlow<String?> = _lastPrintedJob.asStateFlow()

    // Scanner
    private val _scannerResult = MutableStateFlow<String?>(null)
    val scannerResult: StateFlow<String?> = _scannerResult.asStateFlow()

    // Search queries
    val productSearchQuery = MutableStateFlow("")
    val customerSearchQuery = MutableStateFlow("")

    // Exposed Flows from Repository
    val products: StateFlow<List<Product>> = productSearchQuery
        .debounce(100)
        .flatMapLatest { query ->
            if (query.isEmpty()) repository.allProducts else repository.searchProducts(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = customerSearchQuery
        .debounce(100)
        .flatMapLatest { query ->
            if (query.isEmpty()) repository.allCustomers else repository.searchCustomers(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tablesComandas: StateFlow<List<TableOrComanda>> = repository.allTablesComandas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<Sale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<FinancialTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCashSession: StateFlow<CashRegister?> = repository.activeCashSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appConfig: StateFlow<AppConfig?> = repository.appConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppConfig())

    val totalInvoicing: StateFlow<Double?> = repository.totalInvoicing
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val salesCount: StateFlow<Int> = repository.salesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Navigation function
    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // POS Cart Operations
    fun addToCart(product: Product, quantity: Int = 1, observation: String = "") {
        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.productId == product.id && it.observation == observation }

        if (existingIndex >= 0) {
            val existingItem = currentList[existingIndex]
            currentList[existingIndex] = existingItem.copy(quantity = existingItem.quantity + quantity)
        } else {
            currentList.add(CartItem(
                productId = product.id,
                productName = product.name,
                price = product.price,
                quantity = quantity,
                observation = observation
            ))
        }
        _cartItems.value = currentList
    }

    fun updateCartItemQuantity(productId: Int, observation: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            _cartItems.value = _cartItems.value.filterNot { it.productId == productId && it.observation == observation }
        } else {
            _cartItems.value = _cartItems.value.map {
                if (it.productId == productId && it.observation == observation) it.copy(quantity = newQuantity) else it
            }
        }
    }

    fun applyCartDiscount(value: Double) {
        _cartDiscount.value = value
    }

    fun setCartCustomer(customer: Customer?) {
        _cartCustomer.value = customer
    }

    fun setCartObservation(obs: String) {
        _cartObservation.value = obs
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _cartDiscount.value = 0.0
        _cartCustomer.value = null
        _cartObservation.value = ""
        _activeTableForCart.value = null
    }

    fun loadTableToCart(table: TableOrComanda) {
        _activeTableForCart.value = table
        val items = deserializeCartItems(table.itemsJson)
        _cartItems.value = items
        _cartDiscount.value = 0.0
        _cartCustomer.value = null
    }

    // Cart Totals
    val cartSubtotal: Double
        get() = _cartItems.value.sumOf { it.price * it.quantity }

    val cartTotal: Double
        get() = (cartSubtotal - _cartDiscount.value).coerceAtLeast(0.0)

    // Check out current cart as a Sale
    fun checkoutActiveCart(
        paymentMethod: String,
        emitFiscal: Boolean,
        fiscalType: String? = null,
        onComplete: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val items = _cartItems.value
            val total = cartTotal
            val discount = _cartDiscount.value
            val customerName = _cartCustomer.value?.name ?: "Consumidor Geral"
            val customerId = _cartCustomer.value?.id
            val tableId = _activeTableForCart.value?.id
            val jsonItems = serializeCartItems(items)

            val saleId = repository.checkoutSale(
                customerName = customerName,
                customerId = customerId,
                items = items,
                discount = discount,
                totalAmount = total,
                paymentMethod = paymentMethod,
                tableIdToClose = tableId,
                emitFiscal = emitFiscal,
                fiscalType = fiscalType,
                itemsJson = jsonItems
            )
            clearCart()
            onComplete(saleId)
        }
    }

    // Save Table/Comanda active order (without paying, just saving to table)
    fun saveCartToTable() {
        val table = _activeTableForCart.value ?: return
        val items = _cartItems.value
        val total = cartSubtotal
        val jsonItems = serializeCartItems(items)
        val status = if (items.isEmpty()) "IDLE" else "OPEN"

        viewModelScope.launch {
            repository.updateOrderItems(table.id, jsonItems, total, status)
            clearCart()
            navigateTo("tables_comandas")
        }
    }

    // Open/Close Cash Register
    fun openCash(initialAmount: Double) {
        viewModelScope.launch {
            repository.openCashRegister(initialAmount)
        }
    }

    fun closeCash(finalAmount: Double) {
        viewModelScope.launch {
            repository.closeCashRegister(finalAmount)
        }
    }

    // Product CRUD
    fun saveProduct(product: Product) {
        viewModelScope.launch {
            if (product.id == 0) {
                repository.insertProduct(product)
            } else {
                repository.updateProduct(product)
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // Customer CRUD
    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            if (customer.id == 0) {
                repository.insertCustomer(customer)
            } else {
                repository.updateCustomer(customer)
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // Table Or Comanda CRUD
    fun saveTableOrComanda(numberOrName: String) {
        viewModelScope.launch {
            repository.insertTableOrComanda(TableOrComanda(numberOrName = numberOrName, status = "IDLE"))
        }
    }

    fun deleteTableOrComanda(table: TableOrComanda) {
        viewModelScope.launch {
            repository.deleteTableOrComanda(table)
        }
    }

    // Finance CRUD
    fun addFinancialTransaction(type: String, category: String, desc: String, amount: Double) {
        viewModelScope.launch {
            repository.insertTransaction(
                FinancialTransaction(
                    type = type,
                    category = category,
                    description = desc,
                    amount = amount
                )
            )
        }
    }

    fun deleteFinancialTransaction(transaction: FinancialTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // Config save
    fun saveConfig(config: AppConfig) {
        viewModelScope.launch {
            repository.saveAppConfig(config)
        }
    }

    // Emit Fiscal for Sale
    fun emitFiscal(saleId: Int, type: String) {
        viewModelScope.launch {
            val updated = repository.emitFiscalForSale(saleId, type)
            if (updated != null) {
                selectedSaleForDetails.value = updated
            }
        }
    }

    // Bluetooth Printer Simulator
    fun scanBluetoothPrinters() {
        _isScanningBluetooth.value = true
        _bluetoothDevices.value = emptyList()
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200) // Simulated delay
            _bluetoothDevices.value = listOf(
                "Impressora Térmica 58mm (00:11:22:33:AA:BB)",
                "Bluetooth Printer Mini (AA:BB:CC:DD:EE:FF)",
                "M-Printer POS-80 (99:88:77:66:55:44)"
            )
            _isScanningBluetooth.value = false
        }
    }

    fun simulatePrint(text: String) {
        _lastPrintedJob.value = text
        // Clear printer job status after a few seconds
        viewModelScope.launch {
            kotlinx.coroutines.delay(4000)
            if (_lastPrintedJob.value == text) {
                _lastPrintedJob.value = null
            }
        }
    }

    // Barcode scanner simulator
    fun simulateBarcodeScan(code: String) {
        _scannerResult.value = code
        viewModelScope.launch {
            val matchingProduct = products.value.find { it.code == code }
            if (matchingProduct != null) {
                addToCart(matchingProduct, 1, "Código de Barras")
                _scannerResult.value = "Produto encontrado: ${matchingProduct.name} adicionado ao carrinho!"
            } else {
                _scannerResult.value = "Nenhum produto cadastrado com o código: $code"
            }
            kotlinx.coroutines.delay(3000)
            _scannerResult.value = null
        }
    }

    // Google Sign-In functions
    fun connectGoogleAccount(name: String, email: String, avatarUrl: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val current = appConfig.value ?: AppConfig()
            val updated = current.copy(
                isGoogleConnected = true,
                googleAccountName = name,
                googleAccountEmail = email,
                googleAccountAvatarUrl = avatarUrl,
                googleDriveBackupEnabled = true
            )
            repository.saveAppConfig(updated)
            onComplete()
        }
    }

    fun disconnectGoogleAccount() {
        viewModelScope.launch {
            val current = appConfig.value ?: AppConfig()
            val updated = current.copy(
                isGoogleConnected = false,
                googleAccountName = "",
                googleAccountEmail = "",
                googleAccountAvatarUrl = "",
                googleDriveBackupEnabled = false
            )
            repository.saveAppConfig(updated)
        }
    }

    // Cloud Backups List Data
    data class CloudBackup(
        val timestamp: Long,
        val fileName: String,
        val size: String,
        val accountEmail: String
    )

    private val _cloudBackups = MutableStateFlow<List<CloudBackup>>(emptyList())
    val cloudBackups: StateFlow<List<CloudBackup>> = _cloudBackups.asStateFlow()

    fun triggerGoogleDriveBackup(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val newBackup = CloudBackup(
                timestamp = System.currentTimeMillis(),
                fileName = "toca_backup_$dateStr.db",
                size = "${(120..450).random() / 10.0} KB",
                accountEmail = email
            )
            _cloudBackups.value = listOf(newBackup) + _cloudBackups.value
            onSuccess()
        }
    }

    // Serialization utilities
    fun serializeCartItems(items: List<CartItem>): String {
        return try {
            cartAdapter.toJson(items) ?: "[]"
        } catch (e: Exception) {
            "[]"
        }
    }

    fun deserializeCartItems(json: String): List<CartItem> {
        return try {
            cartAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
