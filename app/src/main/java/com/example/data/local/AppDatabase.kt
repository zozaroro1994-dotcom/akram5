package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Debt
import com.example.data.model.FixedExpense
import com.example.data.model.Income
import com.example.data.model.Saving
import com.example.data.model.User
import com.example.data.model.VariableExpense

@Database(
    entities = [
        User::class,
        Income::class,
        FixedExpense::class,
        VariableExpense::class,
        Debt::class,
        Saving::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun incomeDao(): IncomeDao
    abstract fun fixedExpenseDao(): FixedExpenseDao
    abstract fun variableExpenseDao(): VariableExpenseDao
    abstract fun debtDao(): DebtDao
    abstract fun savingDao(): SavingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "home_accountant_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
