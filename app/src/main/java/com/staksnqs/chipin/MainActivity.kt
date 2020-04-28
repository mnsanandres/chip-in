package com.staksnqs.chipin

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.staksnqs.chipin.model.dao.ChipInDatabase
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        var chipInDatabase: ChipInDatabase? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val typeface = Typeface.createFromAsset(assets, "fonts/bungee.ttf")
        buttonNewActivity.typeface = typeface
        buttonLoadActivity.typeface = typeface
        buttonSettings.typeface = typeface

        Log.d("CHIP", getDatabasePath("chipin.db").toString())

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                CREATE TABLE IF NOT EXISTS group_credits (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    expenseId INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    activityId INTEGER NOT NULL,
                    fromBuddyId INTEGER NOT NULL,
                    toBuddyId INTEGER NOT NULL
                )
            """.trimIndent()
                )
            }
        }

        chipInDatabase = Room.databaseBuilder(
            applicationContext,
            ChipInDatabase::class.java, "chipin.db"
        ).addMigrations(MIGRATION_1_2).build()

        val p = packageManager.getPackageInfo(packageName, 0)
        Log.d("CHIP", "directory: ${p.applicationInfo.dataDir}")

        Log.d("CHIP", filesDir.toString())
        Log.d("CHIP", cacheDir.toString())
        Log.d("CHIP", Environment.getExternalStorageDirectory().toString())
        Log.d("CHIP-", getExternalFilesDir(null).toString())
        Log.d("CHIP", externalCacheDir.toString())
        Log.d("CHIP_", externalMediaDirs.joinToString())

        val appDirectory = "${getExternalFilesDir(null)}/uploads"
        val uploadDirectory = File(appDirectory)
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdir()
            val receiptsPath = "${uploadDirectory}/receipts"
            val receiptsDirectory = File(receiptsPath)
            if (!receiptsDirectory.exists()) {
                receiptsDirectory.mkdir()
            }
        }

        buttonNewActivity.setOnClickListener {
            val intent = Intent(this, NewActivity::class.java)
            startActivity(intent)
        }

        buttonLoadActivity.setOnClickListener {
            startActivity(Intent(this, LoadActivity::class.java))
        }

        buttonSettings.setOnClickListener {
            Toast.makeText(this, "Not yet implemented.", Toast.LENGTH_SHORT).show()
        }
    }
}
