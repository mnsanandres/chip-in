package com.staksnqs.chipin

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
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
                duesInfo.forEach{ due ->
                    val view = inflater.inflate(R.layout.dues_row, null)
                    view.findViewById<TextView>(R.id.column1).text = due.expense[0].name
                    view.findViewById<TextView>(R.id.column2).text = "%.2f".format(due.credit.amount)
                    insertPoint.addView(view)
                    if (index++ % 2 == 0) view.setBackgroundColor(Color.parseColor("#06AF9C"))

                    view.findViewById<ImageView>(R.id.view_expense).setOnClickListener{
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
            }
        )
    }
}
