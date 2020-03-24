package com.staksnqs.chipin

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.staksnqs.chipin.model.dao.ChipInDatabase
import kotlinx.android.synthetic.main.activity_main.*

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

        chipInDatabase = Room.databaseBuilder(
            applicationContext,
            ChipInDatabase::class.java, "chipin.db"
        ).build()

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
