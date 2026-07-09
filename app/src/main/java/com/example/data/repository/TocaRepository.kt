package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class TocaRepository(private val database: AppDatabase) {

    // DAOs
    private val productDao = database.productDao()
    private val customerDao = database.customerDao()
    private val tableOrComandaDao = database.tableOrComandaDao()
    private val saleDao = database.saleDao()
    private val cashRegisterDao = database.cashRegisterDao()
    private val financialTransactionDao = database.financialTransactionDao()
    private val appConfigDao = database.appConfigDao()

    // Flows
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allTablesComandas: Flow<List<TableOrComanda>> = tableOrComandaDao.getAllTablesComandas()
    val allSales: Flow<List<Sale>> = saleDao.getAllSales()
    val allTransactions: Flow<List<FinancialTransaction>> = financialTransactionDao.getAllTransactions()
    val activeCashSession: Flow<CashRegister?> = cashRegisterDao.getActiveSession()
    val allCashSessions: Flow<CashRegister?> = cashRegisterDao.getActiveSession() // placeholder/active
    val appConfig: Flow<AppConfig?> = appConfigDao.getConfig()
    val totalInvoicing: Flow<Double?> = saleDao.getTotalInvoicing()
    val salesCount: Flow<Int> = saleDao.getSalesCount()

    // Products
    fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query, query)
    }

    suspend fun insertProduct(product: Product) = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
    suspend fun adjustStock(id: Int, amount: Double) = productDao.adjustStock(id, amount)

    // Customers
    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)
    suspend fun insertCustomer(customer: Customer) = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)

    // Tables & Comandas
    suspend fun getTableOrComandaById(id: Int) = tableOrComandaDao.getTableOrComandaById(id)
    suspend fun insertTableOrComanda(tableOrComanda: TableOrComanda) = tableOrComandaDao.insertTableOrComanda(tableOrComanda)
    suspend fun updateTableOrComanda(tableOrComanda: TableOrComanda) = tableOrComandaDao.updateTableOrComanda(tableOrComanda)
    suspend fun updateOrderItems(id: Int, itemsJson: String, total: Double, status: String) =
        tableOrComandaDao.updateOrderItems(id, itemsJson, total, status)
    suspend fun deleteTableOrComanda(tableOrComanda: TableOrComanda) = tableOrComandaDao.deleteTableOrComanda(tableOrComanda)

    // Sales & PDV Checkout
    suspend fun getSaleById(id: Int): Sale? = saleDao.getSaleById(id)

    suspend fun checkoutSale(
        customerName: String,
        customerId: Int?,
        items: List<CartItem>,
        discount: Double,
        totalAmount: Double,
        paymentMethod: String,
        tableIdToClose: Int? = null,
        emitFiscal: Boolean = false,
        fiscalType: String? = null,
        itemsJson: String // Serialized items
    ): Long {
        // Create Sale Entity
        var sale = Sale(
            customerName = customerName,
            customerId = customerId,
            itemsJson = itemsJson,
            discount = discount,
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            paymentStatus = "PAGO",
            isFiscalEmitted = emitFiscal,
            fiscalType = fiscalType
        )

        // Generate Fiscal Data if requested
        if (emitFiscal) {
            val accessKey = generateMockFiscalKey(fiscalType ?: "NFC-e")
            sale = sale.copy(
                fiscalKey = accessKey,
                fiscalStatus = "TRANSMITIDO",
                xmlContent = generateMockXml(sale, accessKey)
            )
        }

        // Insert sale
        val saleId = saleDao.insertSale(sale)

        // Adjust Stock
        for (item in items) {
            productDao.adjustStock(item.productId, -item.quantity.toDouble())
        }

        // Add Financial Transaction
        financialTransactionDao.insertTransaction(
            FinancialTransaction(
                type = "RECEITA",
                category = "Venda",
                description = "Venda PDV #${saleId} - $customerName",
                amount = totalAmount
            )
        )

        // Close Mesa/Comanda if applicable
        if (tableIdToClose != null) {
            val table = tableOrComandaDao.getTableOrComandaById(tableIdToClose)
            if (table != null) {
                tableOrComandaDao.updateTableOrComanda(
                    table.copy(
                        status = "IDLE",
                        totalAmount = 0.0,
                        itemsJson = "[]"
                    )
                )
            }
        }

        return saleId
    }

    // Fiscal emission for existing sale
    suspend fun emitFiscalForSale(saleId: Int, fiscalType: String): Sale? {
        val sale = saleDao.getSaleById(saleId) ?: return null
        if (sale.isFiscalEmitted) return sale

        val accessKey = generateMockFiscalKey(fiscalType)
        val updatedSale = sale.copy(
            isFiscalEmitted = true,
            fiscalType = fiscalType,
            fiscalKey = accessKey,
            fiscalStatus = "TRANSMITIDO",
            xmlContent = generateMockXml(sale, accessKey)
        )
        saleDao.updateSale(updatedSale)
        return updatedSale
    }

    // Cash Register (Caixa)
    suspend fun getActiveCashSession() = cashRegisterDao.getActiveSessionSync()
    suspend fun openCashRegister(initialAmount: Double) {
        val active = cashRegisterDao.getActiveSessionSync()
        if (active == null) {
            cashRegisterDao.insertSession(
                CashRegister(
                    initialAmount = initialAmount,
                    status = "OPEN"
                )
            )
            // Register input transaction
            financialTransactionDao.insertTransaction(
                FinancialTransaction(
                    type = "RECEITA",
                    category = "Caixa",
                    description = "Abertura de Caixa (Troco Inicial)",
                    amount = initialAmount
                )
            )
        }
    }

    suspend fun closeCashRegister(finalAmount: Double) {
        val active = cashRegisterDao.getActiveSessionSync()
        if (active != null) {
            cashRegisterDao.updateSession(
                active.copy(
                    closeTimestamp = System.currentTimeMillis(),
                    finalAmount = finalAmount,
                    status = "CLOSED"
                )
            )
        }
    }

    // Financial Transactions
    suspend fun insertTransaction(transaction: FinancialTransaction) = financialTransactionDao.insertTransaction(transaction)
    suspend fun deleteTransaction(transaction: FinancialTransaction) = financialTransactionDao.deleteTransaction(transaction)

    // Config
    suspend fun getAppConfigSync() = appConfigDao.getConfigSync()
    suspend fun saveAppConfig(config: AppConfig) = appConfigDao.insertConfig(config)

    // Simulated Helper Methods
    private fun generateMockFiscalKey(type: String): String {
        // Generate random 44-digit key starting with state (35 for SP), year-month (2607), CNPJ (12345678000190), type, random
        val prefix = if (type == "NF-e") "35" else "35"
        val calendar = Calendar.getInstance()
        val year = SimpleDateFormat("yy", Locale.US).format(calendar.time)
        val month = SimpleDateFormat("MM", Locale.US).format(calendar.time)
        val cnpj = "12345678000190"
        val serie = "001"
        val number = String.format(Locale.US, "%09d", (1000..9999).random())
        val tpEmis = "1"
        val code = String.format(Locale.US, "%08d", (10000000..99999999).random())
        val checkDigit = "7"
        return "$prefix$year$month$cnpj${if (type == "NF-e") "55" else "65"}$serie$number$tpEmis$code$checkDigit"
    }

    private fun generateMockXml(sale: Sale, accessKey: String): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <nfeProc xmlns="http://www.portalfiscal.inf.br/nfe" versao="4.00">
                <NFe>
                    <infNFe Id="NFe$accessKey" versao="4.00">
                        <ide>
                            <cUF>35</cUF>
                            <cNF>${accessKey.takeLast(9).take(8)}</cNF>
                            <natOp>Venda de mercadoria</natOp>
                            <mod>${if (sale.fiscalType == "NF-e") "55" else "65"}</mod>
                            <serie>1</serie>
                            <nNF>${accessKey.substring(25, 34)}</nNF>
                            <dhEmi>${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(sale.timestamp))}</dhEmi>
                            <tpNF>1</tpNF>
                            <idDest>1</idDest>
                            <cMunFG>3550308</cMunFG>
                            <tpImp>1</tpImp>
                            <tpEmis>1</tpEmis>
                            <cDV>7</cDV>
                            <tpAmb>2</tpAmb>
                            <finNFe>1</finNFe>
                            <indFinal>1</indFinal>
                            <indPres>1</indPres>
                        </ide>
                        <emit>
                            <CNPJ>12345678000190</CNPJ>
                            <xNome>TOCA DO ESPETO LTDA</xNome>
                            <xFant>Toca do Espeto</xFant>
                            <IE>123456789111</IE>
                            <CRT>1</CRT>
                        </emit>
                        <dest>
                            <xNome>${sale.customerName}</xNome>
                            <indIEDest>9</indIEDest>
                        </dest>
                        <total>
                            <ICMSTot>
                                <vBC>0.00</vBC>
                                <vICMS>0.00</vICMS>
                                <vProd>${sale.totalAmount + sale.discount}</vProd>
                                <vDesc>${sale.discount}</vDesc>
                                <vNF>${sale.totalAmount}</vNF>
                            </ICMSTot>
                        </total>
                        <pgto>
                            <detPag>
                                <tPag>${getTPagCode(sale.paymentMethod)}</tPag>
                                <vPag>${sale.totalAmount}</vPag>
                            </detPag>
                        </pgto>
                    </infNFe>
                </NFe>
                <protNFe versao="4.00">
                    <infProt>
                        <tpAmb>2</tpAmb>
                        <verAplic>SP-NFE-2026</verAplic>
                        <chNFe>$accessKey</chNFe>
                        <dhRecb>${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(sale.timestamp))}</dhRecb>
                        <nProt>13526000${(100000..999999).random()}</nProt>
                        <digVal>simulatedHashValue=</digVal>
                        <cStat>100</cStat>
                        <xMotivo>Autorizado o uso da NF-e</xMotivo>
                    </infProt>
                </protNFe>
            </nfeProc>
        """.trimIndent()
    }

    private fun getTPagCode(method: String): String {
        return when (method) {
            "Dinheiro" -> "01"
            "Cartão de Crédito" -> "03"
            "Cartão de Débito" -> "04"
            "Vale-Alimentação" -> "10"
            "Pix" -> "17"
            else -> "99"
        }
    }
}
