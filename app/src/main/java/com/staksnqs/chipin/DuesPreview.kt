package com.staksnqs.chipin

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.staksnqs.chipin.model.view.ChipInViewModel
import org.apmem.tools.layouts.FlowLayout

class DuesPreview : AppCompatActivity() {

    private lateinit var chipInViewModel: ChipInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dues_preview)

        val activityId = intent.getLongExtra("ACTIVITY_ID", -1)
        val buddyId = intent.getLongExtra("BUDDY_ID", -1)
        val buddyName = intent.getStringExtra("BUDDY_NAME")
        val buddyAvatar = intent.getStringExtra("BUDDY_AVATAR")

        val toolbar: Toolbar = findViewById(R.id.tool_bar)
        toolbar.title = null
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        findViewById<TextView>(R.id.activity_title).text = "$buddyName's Dues"

        val backButton = findViewById<ImageView>(R.id.cancel_new)
        backButton.setBackgroundResource(android.R.drawable.ic_menu_revert)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            backButton.foreground = ContextCompat.getDrawable(this@DuesPreview, android.R.drawable.ic_menu_revert)
            backButton.foregroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yellow))
        }
        backButton.setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.buddy).background = ContextCompat.getDrawable(
            this@DuesPreview, resources.getIdentifier(
                buddyAvatar, "drawable",
                packageName
            )
        )

        findViewById<TextView>(R.id.name_value).text = buddyName

        chipInViewModel = ViewModelProvider(this).get(ChipInViewModel::class.java)
        val inflater: LayoutInflater =
            this@DuesPreview.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val insertPoint = findViewById<FlowLayout>(R.id.buddy_list)

        chipInViewModel.getBuddyDueSum(activityId, buddyId).observe(
            this, Observer { duesPreview ->
                insertPoint.removeAllViews()
                var totalDue = 0.0f
                Log.d("CHIP", duesPreview.toString())
                duesPreview.forEach{ dueInfo ->
                    Log.d("CHIP", "For ${dueInfo.name}: total: ${dueInfo.total}, owed: ${dueInfo.owed}")
                    val actual = dueInfo.total - dueInfo.owed
                    totalDue += if (actual < 0) 0.0f else actual
                    val view = inflater.inflate(R.layout.buddy_frame_2, null)
                    view.id = View.generateViewId()
                    val imageHolder = view.findViewById<ImageView>(R.id.buddy_avatar)
                    val background = ContextCompat.getDrawable(
                        this@DuesPreview, resources.getIdentifier(
                            dueInfo.avatar, "drawable",
                            packageName
                        )
                    )
                    imageHolder.background = background
                    val nameField = view.findViewById<TextView>(R.id.buddy_name)
                    nameField.text = dueInfo.name
                    val costField = view.findViewById<EditText>(R.id.individual_cost)
                    costField.setText("%.2f".format(if (actual < 0) 0.0f else actual))
                    insertPoint.addView(view)

                    view.setOnClickListener{
                        val intent = Intent(baseContext, DuesList::class.java)
                        intent.putExtra("ACTIVITY_ID", activityId)
                        intent.putExtra("BUDDY_ID", buddyId)
                        intent.putExtra("BUDDY_NAME", buddyName)
                        intent.putExtra("CREDITOR", dueInfo.name)
                        intent.putExtra("CREDITOR_ID", dueInfo.id)
                        startActivity(intent)
                    }
                }
//                Log.d("CHIP", duesPreview.toString())
                findViewById<TextView>(R.id.cost_value).text = "%.2f".format(totalDue)
            }
        )

    }
}
