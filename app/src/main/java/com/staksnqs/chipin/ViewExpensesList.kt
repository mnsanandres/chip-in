package com.staksnqs.chipin

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.staksnqs.chipin.model.view.ChipInViewModel

class ViewExpensesList : AppCompatActivity() {

    private lateinit var chipInViewModel: ChipInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_expenses_list)

        val activityId = intent.getLongExtra("ACTIVITY_ID", -1)
        val buddyId = intent.getLongExtra("BUDDY_ID", -1)
        val buddyName = intent.getStringExtra("BUDDY_NAME")

        val toolbar: Toolbar = findViewById(R.id.tool_bar)
        toolbar.title = null
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        findViewById<TextView>(R.id.activity_title).text = "$buddyName's Expenses"

        val backButton = findViewById<ImageView>(R.id.cancel_new)
        backButton.setBackgroundResource(android.R.drawable.ic_menu_revert)
        backButton.setOnClickListener {
            finish()
        }

        chipInViewModel = ViewModelProvider(this).get(ChipInViewModel::class.java)

        val inflater: LayoutInflater =
            this@ViewExpensesList.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val insertPoint = findViewById<LinearLayout>(R.id.expenses_list)
        chipInViewModel.getBuddyExpensesSum(activityId, buddyId).observe(
            this, Observer { expenses ->
                if (expenses == null) {
                    return@Observer
                }
                var index = 0

//                Log.d("CHIP", chipInViewModel.getBuddyExpensesSum(activityId, buddyId).toString())

                expenses.forEach { expense ->
                    val view = inflater.inflate(R.layout.activity_row, null)
                    view.findViewById<TextView>(R.id.column1).text = expense.name
                    view.findViewById<TextView>(R.id.column2).text = "%.2f".format(expense.total)
                    insertPoint.addView(view)
                    if (index++ % 2 == 0) view.setBackgroundColor(Color.parseColor("#06AF9C"))

                    view.findViewById<ImageView>(R.id.edit_activity).setOnClickListener {
                        Toast.makeText(this, "Not yet implemeneted.", Toast.LENGTH_SHORT).show()
                    }
                    view.findViewById<ImageView>(R.id.delete_activity).setOnClickListener {
                        Toast.makeText(this, "Not yet implemeneted.", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        )
        chipInViewModel.getActivities().removeObservers(this)
    }
}
