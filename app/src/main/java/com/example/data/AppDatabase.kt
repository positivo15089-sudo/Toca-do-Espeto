package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        Customer::class,
        TableOrComanda::class,
        Sale::class,
        CashRegister::class,
        FinancialTransaction::class,
        AppConfig::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun tableOrComandaDao(): TableOrComandaDao
    abstract fun saleDao(): SaleDao
    abstract fun cashRegisterDao(): CashRegisterDao
    abstract fun financialTransactionDao(): FinancialTransactionDao
    abstract fun appConfigDao(): AppConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "toca_do_espeto_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(db: AppDatabase) {
            // Seed products
            val productDao = db.productDao()
            if (productDao.getProductById(1) == null) {
                productDao.insertProduct(Product(name = "Espeto de Carne Premium", code = "101", price = 12.00, costPrice = 4.50, stockQuantity = 150.0, minStock = 20.0, category = "Espetos"))
                productDao.insertProduct(Product(name = "Espeto de Queijo Coalho", code = "102", price = 11.50, costPrice = 3.80, stockQuantity = 80.0, minStock = 15.0, category = "Espetos"))
                productDao.insertProduct(Product(name = "Espeto de Pão de Alho", code = "103", price = 9.00, costPrice = 2.50, stockQuantity = 120.0, minStock = 15.0, category = "Espetos"))
                productDao.insertProduct(Product(name = "Espeto de Coração de Frango", code = "104", price = 11.00, costPrice = 4.00, stockQuantity = 90.0, minStock = 10.0, category = "Espetos"))
                productDao.insertProduct(Product(name = "Espeto Medalhão de Frango", code = "105", price = 13.50, costPrice = 5.00, stockQuantity = 60.0, minStock = 10.0, category = "Espetos"))
                productDao.insertProduct(Product(name = "Cerveja Heineken Long Neck", code = "201", price = 9.50, costPrice = 4.20, stockQuantity = 240.0, minStock = 48.0, category = "Bebidas"))
                productDao.insertProduct(Product(name = "Refrigerante Coca-Cola Lata", code = "202", price = 6.00, costPrice = 2.40, stockQuantity = 300.0, minStock = 50.0, category = "Bebidas"))
                productDao.insertProduct(Product(name = "Suco de Laranja Copo 300ml", code = "203", price = 8.00, costPrice = 2.00, stockQuantity = 100.0, minStock = 10.0, category = "Bebidas"))
                productDao.insertProduct(Product(name = "Porção de Mandioca Frita", code = "301", price = 18.00, costPrice = 5.50, stockQuantity = 40.0, minStock = 5.0, category = "Acompanhamentos"))
                productDao.insertProduct(Product(name = "Porção de Batata Frita com Queijo", code = "302", price = 22.00, costPrice = 7.00, stockQuantity = 45.0, minStock = 5.0, category = "Acompanhamentos"))
            }

            // Seed tables/comandas
            val tableDao = db.tableOrComandaDao()
            tableDao.insertTableOrComanda(TableOrComanda(numberOrName = "Mesa 01", status = "IDLE"))
            tableDao.insertTableOrComanda(TableOrComanda(numberOrName = "Mesa 02", status = "IDLE"))
            tableDao.insertTableOrComanda(TableOrComanda(numberOrName = "Mesa 03", status = "IDLE"))
            tableDao.insertTableOrComanda(TableOrComanda(numberOrName = "Mesa 04", status = "IDLE"))
            tableDao.insertTableOrComanda(TableOrComanda(numberOrName = "Mesa 05", status = "IDLE"))
            tableDao.insertTableOrComanda(TableOrComanda(numberOrName = "Comanda 101", status = "IDLE"))
            tableDao.insertTableOrComanda(TableOrComanda(numberOrName = "Comanda 102", status = "IDLE"))
            tableDao.insertTableOrComanda(TableOrComanda(numberOrName = "Comanda 103", status = "IDLE"))

            // Seed configuration
            val configDao = db.appConfigDao()
            if (configDao.getConfigSync() == null) {
                configDao.insertConfig(AppConfig())
            }

            // Seed cash register
            val cashRegisterDao = db.cashRegisterDao()
            if (cashRegisterDao.getActiveSessionSync() == null) {
                cashRegisterDao.insertSession(CashRegister(initialAmount = 250.00, status = "OPEN"))
            }

            // Seed customer
            val customerDao = db.customerDao()
            customerDao.insertCustomer(Customer(name = "Consumidor Geral", cpfCnpj = "999.999.999-99", phone = "(11) 99999-9999", email = "consumidor@geral.com", address = "Rua Principal, 100"))
            customerDao.insertCustomer(Customer(name = "João da Silva", cpfCnpj = "123.456.789-00", phone = "(11) 98765-4321", email = "joao@email.com", address = "Av. das Nações, 500"))
            customerDao.insertCustomer(Customer(name = "Maria Souza", cpfCnpj = "987.654.321-11", phone = "(11) 97654-3210", email = "maria@email.com", address = "Rua do Bosque, 20"))
        }
    }
}
