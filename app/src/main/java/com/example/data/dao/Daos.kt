package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Query("SELECT * FROM products WHERE code = :code OR name LIKE '%' || :search || '%'")
    fun searchProducts(code: String, search: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET stockQuantity = stockQuantity + :amount WHERE id = :id")
    suspend fun adjustStock(id: Int, amount: Double)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Int): Customer?

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR cpfCnpj LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)
}

@Dao
interface TableOrComandaDao {
    @Query("SELECT * FROM tables_comandas ORDER BY numberOrName ASC")
    fun getAllTablesComandas(): Flow<List<TableOrComanda>>

    @Query("SELECT * FROM tables_comandas WHERE id = :id")
    suspend fun getTableOrComandaById(id: Int): TableOrComanda?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTableOrComanda(tableOrComanda: TableOrComanda)

    @Update
    suspend fun updateTableOrComanda(tableOrComanda: TableOrComanda)

    @Query("UPDATE tables_comandas SET itemsJson = :itemsJson, totalAmount = :total, status = :status WHERE id = :id")
    suspend fun updateOrderItems(id: Int, itemsJson: String, total: Double, status: String)

    @Delete
    suspend fun deleteTableOrComanda(tableOrComanda: TableOrComanda)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Int): Sale?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Update
    suspend fun updateSale(sale: Sale)

    @Query("SELECT SUM(totalAmount) FROM sales")
    fun getTotalInvoicing(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM sales")
    fun getSalesCount(): Flow<Int>
}

@Dao
interface CashRegisterDao {
    @Query("SELECT * FROM cash_registers ORDER BY openTimestamp DESC")
    fun getAllSessions(): Flow<List<CashRegister>>

    @Query("SELECT * FROM cash_registers WHERE status = 'OPEN' LIMIT 1")
    fun getActiveSession(): Flow<CashRegister?>

    @Query("SELECT * FROM cash_registers WHERE status = 'OPEN' LIMIT 1")
    suspend fun getActiveSessionSync(): CashRegister?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(cashRegister: CashRegister)

    @Update
    suspend fun updateSession(cashRegister: CashRegister)
}

@Dao
interface FinancialTransactionDao {
    @Query("SELECT * FROM financial_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<FinancialTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinancialTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: FinancialTransaction)
}

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config WHERE id = 1")
    fun getConfig(): Flow<AppConfig?>

    @Query("SELECT * FROM app_config WHERE id = 1")
    suspend fun getConfigSync(): AppConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AppConfig)
}
