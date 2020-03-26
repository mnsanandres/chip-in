package com.staksnqs.chipin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.staksnqs.chipin.model.view.ChipInViewModel

class ViewExpense : AppCompatActivity() {

    private lateinit var chipInViewModel: ChipInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_expense)
        val activityId = intent.getLongExtra("ACTIVITY_ID", -1)
        val buddyId = intent.getLongExtra("BUDDY_ID", -1)
        val buddyName = intent.getStringExtra("BUDDY_NAME")
        val expenseId = intent.getLongExtra("EXPENSE_ID", -1)

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
        val editButton = findViewById<ImageView>(R.id.edit_activity)
        editButton.visibility = View.VISIBLE
        editButton.setOnClickListener {
            val intent = Intent(baseContext, LogExpenses::class.java)
            intent.putExtra("EXPENSE_ID", expenseId)
            intent.putExtra("ACTIVITY_ID", activityId)
            intent.putExtra("BUDDY_ID", buddyId)
            intent.putExtra("BUDDY_NAME", buddyName)
            startActivity(intent)
        }
        val avatar = findViewById<ImageView>(R.id.buddy)

        val params = GridLayout.LayoutParams()
        val factor = resources.displayMetrics.density
        val margin = 15
        params.setMargins(margin, margin, margin, margin)
        params.width = (110 * factor).toInt()
        params.height = (150 * factor).toInt()
        val inflater: LayoutInflater =
            this@ViewExpense.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val groupAvatar = inflater.inflate(R.layout.group_avatar, null)
        if (buddyId == -1L) {
            val avatarParent = findViewById<RelativeLayout>(R.id.expense_info)
            val index = avatarParent.indexOfChild(avatar)
            avatarParent.removeView(avatar)
            avatarParent.addView(groupAvatar, index)

            val rParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            rParams.setMargins(margin, margin, margin, margin)
            rParams.width = (90 * factor).toInt()
            rParams.height = (100 * factor).toInt()

            val groupImageHolder = groupAvatar.findViewById<RelativeLayout>(R.id.clique)
            groupImageHolder.layoutParams = rParams
        }

        chipInViewModel = ViewModelProvider(this).get(ChipInViewModel::class.java)
        var totalCost = 0.0f

        val insertPoint = findViewById<ViewGroup>(R.id.buddy_list)
        chipInViewModel.getCreditedToBuddy(activityId, buddyId, expenseId).observe(
            this, Observer { expenseInfo ->
                insertPoint.removeAllViews()
                val expense = expenseInfo.expense
                var ownerAvatar = "group"
                var ownerName = "Group"
                if (buddyId != -1L) {
                    val owner = expenseInfo.creditor[0]
                    ownerAvatar = owner.avatar
                    ownerName = owner.name!!
                    avatar.background = ContextCompat.getDrawable(
                        this@ViewExpense, resources.getIdentifier(
                            ownerAvatar, "drawable",
                            packageName
                        )
                    )
                }
                findViewById<TextView>(R.id.name_value).text = expense.name
                findViewById<TextView>(R.id.who_value).text = ownerName
                Log.d("CHIPIN", expenseInfo.toString())
                var index = 0
                expenseInfo.credits.forEach { creditInfo ->
                    val buddy = creditInfo.who[0]
                    val cost = creditInfo.credit.amount
                    totalCost += cost

                    if (index < 3) {
                        val miniAvatar = groupAvatar.findViewWithTag<ImageView>("clique_$index")
                        miniAvatar.layoutParams.width = (60 * factor).toInt()
                        miniAvatar.layoutParams.height = miniAvatar.layoutParams.width
                        miniAvatar.setBackgroundResource(
                            resources.getIdentifier(
                                buddy.avatar, "drawable",
                                packageName
                            )
                        )
                        miniAvatar.visibility = View.VISIBLE
                        index++
                    }

                    val view = inflater.inflate(R.layout.buddy_frame_2, null)
                    view.id = View.generateViewId()
                    val imageHolder = view.findViewById<ImageView>(R.id.buddy_avatar)
                    imageHolder.background = ContextCompat.getDrawable(
                        this@ViewExpense, resources.getIdentifier(
                            buddy.avatar, "drawable",
                            packageName
                        )
                    )
                    val textHolder = view.findViewById<TextView>(R.id.buddy_name)
                    textHolder.text = buddy.name

                    imageHolder.layoutParams.width = (60 * factor).toInt()
                    imageHolder.layoutParams.height = imageHolder.layoutParams.width
                    view.layoutParams = params

                    val costField = view.findViewById<EditText>(R.id.individual_cost)
                    costField.setText("%.2f".format(cost))

                    insertPoint.addView(view)
                }
                findViewById<TextView>(R.id.cost_value).text = "%.2f".format(totalCost)
            }
        )
        chipInViewModel.getCreditedToBuddy(activityId, buddyId, expenseId).removeObservers(this)
    }
}
