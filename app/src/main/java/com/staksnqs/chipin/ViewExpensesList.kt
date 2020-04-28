package com.staksnqs.chipin

import android.app.AlertDialog
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
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
        val buddyAvatar = intent.getStringExtra("BUDDY_AVATAR")

        val toolbar: Toolbar = this.findViewById(R.id.tool_bar)
        toolbar.title = null
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        findViewById<TextView>(R.id.activity_title).text = "$buddyName's Expenses"

        val editButton = findViewById<ImageView>(R.id.edit_activity)
        editButton.setBackgroundResource(android.R.drawable.ic_input_add)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            editButton.foreground = null
        }
        editButton.visibility = View.VISIBLE
        editButton.setOnClickListener {
            val intent = Intent(baseContext, LogExpenses::class.java)
            intent.putExtra("ACTIVITY_ID", activityId)
            intent.putExtra("BUDDY_ID", buddyId)
            intent.putExtra("BUDDY_AVATAR", buddyAvatar)
            startActivity(intent)
        }

        val backButton = findViewById<ImageView>(R.id.cancel_new)
        backButton.setBackgroundResource(android.R.drawable.ic_menu_revert)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            backButton.foreground = ContextCompat.getDrawable(this@ViewExpensesList, android.R.drawable.ic_menu_revert)
            backButton.foregroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yellow))
        }
        backButton.setOnClickListener {
            finish()
        }

        chipInViewModel = ViewModelProvider(this).get(ChipInViewModel::class.java)

        val inflater: LayoutInflater =
            this@ViewExpensesList.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val insertPoint = findViewById<LinearLayout>(R.id.expenses_list)

        chipInViewModel.getBuddyExpensesSum(activityId, buddyId).observe(
            this, Observer { expenses ->
                insertPoint.removeAllViews()
                if (expenses == null) {
                    return@Observer
                }
                var index = 0

//                Log.d("CHIP", chipInViewModel.getBuddyExpensesSum(activityId, buddyId).toString())

                var grandTotal = 0f
                expenses.forEach { expense ->
                    val view = inflater.inflate(R.layout.activity_row, null)
                    view.findViewById<TextView>(R.id.column1).text = expense.name
                    view.findViewById<TextView>(R.id.column1).textSize = 12f
                    view.findViewById<TextView>(R.id.column2).text = "%.2f".format(expense.total)
                    view.findViewById<TextView>(R.id.column2).gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    grandTotal += expense.total
                    insertPoint.addView(view)
                    if (index++ % 2 == 0) view.setBackgroundColor(Color.parseColor("#06AF9C"))

                    view.setOnClickListener {
                        val intent = Intent(baseContext, ViewExpense::class.java)
                        intent.putExtra("EXPENSE_ID", expense.id)
                        intent.putExtra("ACTIVITY_ID", activityId)
                        intent.putExtra("BUDDY_ID", buddyId)
                        intent.putExtra("BUDDY_NAME", buddyName)
                        startActivity(intent)
                    }

                    view.findViewById<ImageView>(R.id.edit_activity).setOnClickListener {
                        val intent = Intent(baseContext, LogExpenses::class.java)
                        intent.putExtra("EXPENSE_ID", expense.id)
                        intent.putExtra("ACTIVITY_ID", activityId)
                        intent.putExtra("BUDDY_ID", buddyId)
                        intent.putExtra("BUDDY_NAME", buddyName)
                        startActivity(intent)
                    }
                    view.findViewById<ImageView>(R.id.delete_activity).setOnClickListener {
                        val builder = AlertDialog.Builder(this@ViewExpensesList)
                        builder.setTitle("Deleting expense")
                        builder.setMessage("This action cannot be undone. Continue?")
                        builder.setPositiveButton(android.R.string.yes) { _, _ ->
                            chipInViewModel.deleteExpense(expense.id)
                            Toast.makeText(this, "Expense deleted.", Toast.LENGTH_SHORT).show()
                        }
                        builder.setNegativeButton(android.R.string.no) { _, _ -> }
                        builder.show()
                    }

                }

                val view = inflater.inflate(R.layout.activity_row, null)
                view.findViewById<TextView>(R.id.column1).text = "Total"
                view.findViewById<TextView>(R.id.column1).textSize = 12f
                view.findViewById<TextView>(R.id.column2).text = "%.2f".format(grandTotal)
                view.findViewById<TextView>(R.id.column2).gravity = Gravity.END or Gravity.CENTER_VERTICAL
                view.setBackgroundColor(Color.parseColor("#FFC400"))
                insertPoint.addView(view)

                Log.d("CHIP", "Total expense: $grandTotal")
            }
        )
        chipInViewModel.getActivities().removeObservers(this)
    }
}
