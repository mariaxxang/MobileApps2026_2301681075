package com.example.sharedgroceryapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

import com.example.sharedgroceryapp.utils.TestDetector

@Database(entities = [ShoppingList::class, GroceryItem::class], version = 3, exportSchema = false)
abstract class GroceryDatabase : RoomDatabase() {
    
    abstract fun groceryDao(): GroceryDao

    companion object {
        @Volatile
        private var INSTANCE: GroceryDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE grocery_items ADD COLUMN category TEXT NOT NULL DEFAULT 'Other'")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE grocery_items ADD COLUMN category TEXT NOT NULL DEFAULT 'Other'")
            }
        }

        fun getDatabase(context: Context): GroceryDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbName = if (TestDetector.isUnderTest) "grocery_database_test" else "grocery_database"
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GroceryDatabase::class.java,
                    dbName
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
