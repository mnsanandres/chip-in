package com.staksnqs.chipin

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.staksnqs.chipin.model.view.ChipInViewModel
import org.apmem.tools.layouts.FlowLayout

class LogExpenses : AppCompatActivity() {

    private var totalCost: Float = 0.0f
    private var evenSplit: Boolean = true
    private var evenSplitList: MutableList<Float> = mutableListOf()
    private var unevenSplitList: MutableList<Float> = mutableListOf()
    private var individualCostFields: MutableList<EditText> = mutableListOf()
    private var selectedList: MutableList<Boolean> = mutableListOf()
    private var totalCostField: EditText? = null
    private var switchWatcher = false
    private var saveButton: Button? = null
    private var deleteButton: Button? = null
    private var expenseName: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_expenses)

        val activityId = intent.getLongExtra("ACTIVITY_ID", -1)
        val buddyId = intent.getLongExtra("BUDDY_ID", -1)
        val buddyAvatar = intent.getStringExtra("BUDDY_AVATAR")

        val toolbar: Toolbar = findViewById(R.id.tool_bar)
        toolbar.title = null
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        findViewById<TextView>(R.id.activity_title).text = "Log Expense"
        var avatar = findViewById<ImageView>(R.id.buddy)
        avatar.background = ContextCompat.getDrawable(
            this@LogExpenses, resources.getIdentifier(
                buddyAvatar, "drawable",
                packageName
            )
        )

        totalCostField = findViewById(R.id.cost_value)

        saveButton = findViewById(R.id.save_expense)
        deleteButton = findViewById(R.id.delete_expense)
        expenseName = findViewById(R.id.name_value)
        expenseName!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                saveExpenseEnabler()
            }
        })

        val chipInViewModel = ViewModelProvider(this).get(ChipInViewModel::class.java)

        val insertPoint = findViewById<FlowLayout>(R.id.buddy_list)
        chipInViewModel.getActivity(activityId).observe(
            this, Observer { activityInfo ->
                if (activityInfo == null) {
                    finish()
                    return@Observer
                }
                activityInfo.buddies.forEach { buddy ->
                    val inflater: LayoutInflater =
                        this@LogExpenses.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val view = inflater.inflate(R.layout.buddy_frame_2, null)
                    view.id = View.generateViewId()

                    val nameLabel = findViewById<TextView>(R.id.who_value)
                    if (buddy.id == buddyId) {
                        nameLabel.text = buddy.name
                    }

                    val imageHolder = view.findViewById<ImageView>(R.id.buddy_avatar)
                    imageHolder.background = ContextCompat.getDrawable(
                        this@LogExpenses, resources.getIdentifier(
                            buddy.avatar, "drawable",
                            packageName
                        )
                    )
                    val overlay = view.findViewById<ImageView>(R.id.disabled_overlay)
                    overlay.visibility = View.VISIBLE
                    imageHolder.alpha = 0.5f

                    val textHolder = view.findViewById<TextView>(R.id.buddy_name)
                    textHolder.text = buddy.name

                    val params = GridLayout.LayoutParams()

                    val factor = resources.displayMetrics.density
                    val margin = 15
                    params.setMargins(margin, margin, margin, margin)
                    params.width = (110 * factor).toInt()
                    params.height = (150 * factor).toInt()
                    imageHolder.layoutParams.width = (60 * factor).toInt()
                    imageHolder.layoutParams.height = imageHolder.layoutParams.width
                    view.layoutParams = params
                    view.setOnClickListener {
                        if (overlay.visibility == View.VISIBLE) {
                            // Select buddy
                            imageHolder.alpha = 1.0f
                            overlay.visibility = View.INVISIBLE
                            individualCostFields[insertPoint.indexOfChild(view)].isEnabled = !evenSplit
                            selectedList[insertPoint.indexOfChild(view)] = true
                            totalCostField!!.hint = "Enter cost"
                        } else {
                            // Deselect buddy
                            imageHolder.alpha = 0.5f
                            overlay.visibility = View.VISIBLE
                            evenSplitList[insertPoint.indexOfChild(view)] = 0.0f
                            unevenSplitList[insertPoint.indexOfChild(view)] = 0.0f
                            individualCostFields[insertPoint.indexOfChild(view)].setText("0.0")
                            individualCostFields[insertPoint.indexOfChild(view)].isEnabled = false
                            selectedList[insertPoint.indexOfChild(view)] = false
                            totalCostField!!.hint = ""
                        }
                        if (evenSplit) adjustEvenSplit()
                        else adjustUnevenSplit()
                    }
                    evenSplitList.add(0.0f)
                    unevenSplitList.add(0.0f)
                    selectedList.add(false)

                    insertPoint.addView(view)

                    val costField = view.findViewById<EditText>(R.id.individual_cost)
                    individualCostFields.add(costField)
                    costField.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {}
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            if (!evenSplit && !switchWatcher) { // Careful not to lose this condition
                                Log.d("CCHIP", s.toString())
                                unevenSplitList[insertPoint.indexOfChild(view)] = if (s.toString().isEmpty()) {
                                    0.0f
                                } else {
                                    try {
                                        s.toString().toFloat()
                                    } catch (e: NumberFormatException) {
                                        costField!!.setText(unevenSplitList[insertPoint.indexOfChild(view)].toString())
                                        costField.setSelection(unevenSplitList[insertPoint.indexOfChild(view)].toString().length)
                                        unevenSplitList[insertPoint.indexOfChild(view)]
                                    }
                                }
                                adjustUnevenSplit()
                            }
                        }
                    })
                }
            }
        )
        chipInViewModel.getActivities().removeObservers(this)

        totalCostField!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (evenSplit && !switchWatcher) { // Careful not to lose this condition
                    totalCost = if (s.toString().isEmpty()) {
                        0.0f
                    } else {
                        try {
                            s.toString().toFloat()
                        } catch (e: NumberFormatException) {
                            totalCostField!!.setText(totalCost.toString())
                            totalCostField!!.setSelection(totalCost.toString().length)
                            totalCost
                        }
                    }
                    Log.d("CHIP", s.toString())
                    if (evenSplit) adjustEvenSplit()
                }
            }
        })

        val splitType = findViewById<RadioGroup>(R.id.split_type)
        splitType.setOnCheckedChangeListener { _, checkId ->
            switchSplitType(checkId)
        }


        val cancelNew = findViewById<ImageView>(R.id.cancel_new)
        cancelNew.setOnClickListener {
            cancelLogging()
        }

    }

    override fun onBackPressed() {
        cancelLogging()
    }

    private fun cancelLogging() {
        val builder = AlertDialog.Builder(this@LogExpenses)
        builder.setTitle("Discard expense logging?")
        builder.setMessage("Input will not be saved. Continue?")
        builder.setPositiveButton(android.R.string.yes) { _, _ ->
            finish()
        }
        builder.setNegativeButton(android.R.string.no) { _, _ -> }
        builder.show()
    }

    private fun switchSplitType(checkId: Int) {
        val radio: RadioButton = findViewById(checkId)
        evenSplit = radio.text == "Even split"
        Log.d("Chip", evenSplit.toString())
        totalCostField!!.isEnabled = evenSplit
        individualCostFields.forEach { field ->
            field.isEnabled = !evenSplit
        }
        if (evenSplit) {
            totalCostField!!.hint = "Enter cost"
            individualCostFields.forEach { field ->
                field.hint = "0.0"
            }
            switchWatcher = true
            adjustEvenSplit()
            switchWatcher = false
        } else {
            individualCostFields.forEach { field ->
                field.hint = "Enter cost"
            }
            switchWatcher = true
            adjustUnevenSplit()
            switchWatcher = false
        }
    }

    private fun adjustEvenSplit() {
        val divisor = selectedList.count {
            it
        }
        if (divisor > 0) {
            if (switchWatcher) {
                totalCost = evenSplitList.sum()
                totalCostField!!.setText("%.2f".format(totalCost))
                switchWatcher = false
            }
            val each = totalCost / divisor
            Log.d("Chipp", each.toString())
            var index = 0
            individualCostFields.forEach { field ->
                if (selectedList[index]) {
                    evenSplitList[index] = each
                    field.setText("%.2f".format(each))
                }
                index++
            }
        }
        saveExpenseEnabler()
    }

    private fun adjustUnevenSplit() {
        if (switchWatcher) {
            Log.d("CHHIP", unevenSplitList.sum().toString())
            var index = 0
            individualCostFields.forEach { field ->
                if (selectedList[index]) {
                    field.setText("%.2f".format(unevenSplitList[index]))
                }
                index++
            }
            switchWatcher = false
        }
        totalCost = unevenSplitList.sum()
        totalCostField!!.setText("%.2f".format(totalCost))
        saveExpenseEnabler()
    }

    private fun saveExpenseEnabler() {
        toggleButton(
            saveButton,
            expenseName!!.text.toString().isNotEmpty() && selectedList.count { it } > 0 && totalCost > 0)
    }

    private fun toggleButton(button: Button?, state: Boolean) {
        if (state) {
            button!!.isEnabled = true
            button.setBackgroundColor(Color.parseColor("#FFC400"))
            button.setTextColor(Color.parseColor("#363030"))
        } else {
            button!!.isEnabled = false
            button.setBackgroundColor(Color.parseColor("#DCDCDC"))
            button.setTextColor(Color.parseColor("#808080"))
        }
    }
}

