package com.staksnqs.chipin

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.staksnqs.chipin.model.view.ChipInViewModel

class DuesList : AppCompatActivity() {

    private lateinit var chipInViewModel: ChipInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dues_list)

        val activityId = intent.getLongExtra("ACTIVITY_ID", -1)
        val buddyId = intent.getLongExtra("BUDDY_ID", -1)
        val buddyName = intent.getStringExtra("BUDDY_NAME")
        val creditor = intent.getStringExtra("CREDITOR")
        val creditorId = intent.getLongExtra("CREDITOR_ID", -1)

        val toolbar: Toolbar = findViewById(R.id.tool_bar)
        toolbar.title = null
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        findViewById<TextView>(R.id.activity_title).text = "$buddyName's Dues to $creditor"

        val backButton = findViewById<ImageView>(R.id.cancel_new)
        backButton.setBackgroundResource(android.R.drawable.ic_menu_revert)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            backButton.foreground = ContextCompat.getDrawable(this@DuesList, android.R.drawable.ic_menu_revert)
            backButton.foregroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yellow))
        }
        backButton.setOnClickListener {
            finish()
        }

        chipInViewModel = ViewModelProvider(this).get(ChipInViewModel::class.java)
        val inflater: LayoutInflater =
            this@DuesList.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val insertPoint = findViewById<LinearLayout>(R.id.dues_list)

        chipInViewModel.getDues(activityId, buddyId, creditorId).observe(
            this, Observer { duesInfo ->
                insertPoint.removeAllViews()
                var index = 0
                duesInfo.forEach { due ->
                    val view = inflater.inflate(R.layout.dues_row, null)
                    view.findViewById<TextView>(R.id.column1).text = due.expense[0].name
                    view.findViewById<TextView>(R.id.column2).text = "%.2f".format(due.credit.amount)
                    view.findViewById<TextView>(R.id.column2).gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    insertPoint.addView(view)
                    if (index++ % 2 == 0) view.setBackgroundColor(Color.parseColor("#06AF9C"))

                    view.findViewById<ImageView>(R.id.view_expense).setOnClickListener {
                        val intent = Intent(baseContext, ViewExpense::class.java)
                        intent.putExtra("EXPENSE_ID", due.expense[0].id)
                        intent.putExtra("ACTIVITY_ID", due.expense[0].activityId)
                        intent.putExtra("BUDDY_ID", due.buddies[0].id)
                        intent.putExtra("BUDDY_NAME", due.buddies[0].name)
                        startActivity(intent)
                    }
                }
            }
        )

        chipInViewModel.getBuddyDueSum(activityId, buddyId).observe(
            this, Observer { duesPreview ->
                Log.d("CHIP", duesPreview.toString())

                val dueInfo = duesPreview.filter { it.id == creditorId }[0]

                val netTotal = inflater.inflate(R.layout.dues_row, null)
                netTotal.findViewById<TextView>(R.id.column1).text = "Sub-total"
                netTotal.findViewById<TextView>(R.id.column2).text = "%.2f".format(dueInfo.total)
                netTotal.findViewById<TextView>(R.id.column2).gravity = Gravity.END or Gravity.CENTER_VERTICAL
                insertPoint.addView(netTotal)
                netTotal.setBackgroundColor(Color.parseColor("#FFE48C"))
                netTotal.findViewById<ImageView>(R.id.view_expense).visibility = View.INVISIBLE

                val lessRow = inflater.inflate(R.layout.dues_row, null)
                lessRow.findViewById<TextView>(R.id.column1).text = "Less: $creditor's due to $buddyName"
                lessRow.findViewById<TextView>(R.id.column2).text = "-%.2f".format(dueInfo.owed)
                lessRow.findViewById<TextView>(R.id.column2).gravity = Gravity.END or Gravity.CENTER_VERTICAL
                insertPoint.addView(lessRow)
                lessRow.findViewById<ImageView>(R.id.view_expense).visibility = View.INVISIBLE

                val totalRow = inflater.inflate(R.layout.dues_row, null)
                totalRow.findViewById<TextView>(R.id.column1).text = "Total"
                totalRow.findViewById<TextView>(R.id.column2).text = "%.2f".format(dueInfo.total - dueInfo.owed)
                totalRow.findViewById<TextView>(R.id.column2).gravity = Gravity.END or Gravity.CENTER_VERTICAL
                insertPoint.addView(totalRow)
                totalRow.setBackgroundColor(Color.parseColor("#FFC400"))
                totalRow.findViewById<ImageView>(R.id.view_expense).visibility = View.INVISIBLE
            }
        )
    }
}
