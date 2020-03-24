package com.staksnqs.chipin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_avatar_select.*

class AvatarSelect : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avatar_select)

        val avatarImage = findViewById<ImageView>(R.id.avatar_01)
        Log.d("AvatarSelect", "-------------------------> ${avatarImage.background}")
    }

    fun selectAvatar(avatar: View) {
        Log.d("AvatarSelect", "--------------------->${avatar.background}")
    }
}
