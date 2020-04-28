package com.staksnqs.chipin

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.staksnqs.chipin.model.entity.Activity
import com.staksnqs.chipin.model.view.ChipInViewModel
import java.text.SimpleDateFormat
import java.util.*

class LoadActivity : AppCompatActivity() {

    private lateinit var chipInViewModel: ChipInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("CHIP", "Create")
        buildComponents()
    }

    private fun buildComponents() {
        setContentView(R.layout.activity_load)

        val toolbar: Toolbar = findViewById(R.id.tool_bar)
        toolbar.title = null
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        findViewById<TextView>(R.id.activity_title).text = "My Activities"

        chipInViewModel = ViewModelProvider(this).get(ChipInViewModel::class.java)

        val backButton = findViewById<ImageView>(R.id.cancel_new)
        backButton.setBackgroundResource(android.R.drawable.ic_menu_revert)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            backButton.foreground = ContextCompat.getDrawable(this@LoadActivity, android.R.drawable.ic_menu_revert)
            backButton.foregroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yellow))
        }
        backButton.setOnClickListener {
            finish()
        }

        val inflater: LayoutInflater =
            this@LoadActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val insertPoint = findViewById<LinearLayout>(R.id.activity_list)
        chipInViewModel.getActivities().observe(
            this, Observer { activities: List<Activity>? ->
                insertPoint.removeAllViews()
                var index = 0
                activities!!.forEach { activity ->
                    val view = inflater.inflate(R.layout.activity_row, null)
                    view.findViewById<TextView>(R.id.column1).text =
                        SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date(activity.date))
                    view.findViewById<TextView>(R.id.column2).text = activity.name
                    view.setOnClickListener(logActivity(activity.id))
                    insertPoint.addView(view)
                    if (index++ % 2 == 0) view.setBackgroundColor(Color.parseColor("#06AF9C"))
                    view.findViewById<ImageView>(R.id.edit_activity).setOnClickListener {
                        val intent = Intent(baseContext, NewActivity::class.java)
                        intent.putExtra("ACTIVITY_ID", activity.id)
                        startActivity(intent)
                    }
                    view.findViewById<ImageView>(R.id.delete_activity).setOnClickListener {
                        val builder = AlertDialog.Builder(this@LoadActivity)
                        builder.setTitle("Deleting activity")
                        builder.setMessage("Delete ${activity.name}?")
                        builder.setPositiveButton(android.R.string.yes) { _, _ ->
                            insertPoint.removeView(view)
                            chipInViewModel.deleteActivity(activity)
                            Toast.makeText(this, "Activity deleted.", Toast.LENGTH_SHORT).show()
                        }
                        builder.setNegativeButton(android.R.string.no) { _, _ -> }
                        builder.show()
                    }
                }
                Log.d("CHIP", activities.toString())
            }
        )
        chipInViewModel.getActivities().removeObservers(this)
    }

    private fun logActivity(activityId: Long): (View) -> Unit {
        return {
            val intent = Intent(baseContext, LogActivity::class.java)
            intent.putExtra("ACTIVITY_ID", activityId)
            startActivity(intent)
        }
    }
}
