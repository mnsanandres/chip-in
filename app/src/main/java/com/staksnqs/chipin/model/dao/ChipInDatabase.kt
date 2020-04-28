package com.staksnqs.chipin.model.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.staksnqs.chipin.model.entity.*

@Database(
    entities = [Activity::class, Buddy::class, Credit::class, Expense::class, GroupCredit::class],
    version = 2
)
abstract class ChipInDatabase : RoomDatabase() {
    abstract fun ChipInDao(): ChipInDao

    companion object {
        @Volatile
        private var instance: ChipInDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,
            ChipInDatabase::class.java, "chipin.db"
        ).build()
    }


}
