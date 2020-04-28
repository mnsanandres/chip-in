package com.staksnqs.chipin

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.staksnqs.chipin.model.entity.*
import com.staksnqs.chipin.model.view.ChipInViewModel
import org.apmem.tools.layouts.FlowLayout
import java.io.File

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
    private var activity: Activity? = null
    private var activityInfo: ActivityWithBuddies? = null
    private var creditInfo: CreditToBuddy? = null

    private var expense: Expense? = null
    private var image_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_expenses)

        val activityId = intent.getLongExtra("ACTIVITY_ID", -1)
        val buddyId = intent.getLongExtra("BUDDY_ID", -1)
        val buddyAvatar = intent.getStringExtra("BUDDY_AVATAR")
        val expenseId = intent.getLongExtra("EXPENSE_ID", -1)

        val toolbar: Toolbar = findViewById(R.id.tool_bar)
        toolbar.title = null
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val title = "${if (expenseId == -1L) "Log" else "Edit"} Expense"
        findViewById<TextView>(R.id.activity_title).text = title
        val avatar = findViewById<ImageView>(R.id.buddy)
        if (expenseId == -1L) {
            avatar.background = ContextCompat.getDrawable(
                this@LogExpenses, resources.getIdentifier(
                    buddyAvatar, "drawable",
                    packageName
                )
            )
        }

        val splitType = findViewById<RadioGroup>(R.id.split_type)

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

        val nameLabel = findViewById<TextView>(R.id.who_value)

        val params = GridLayout.LayoutParams()

        val factor = resources.displayMetrics.density
        val margin = 15
        params.setMargins(margin, margin, margin, margin)
        params.width = (110 * factor).toInt()
        params.height = (150 * factor).toInt()

        if (buddyId != -1L) {
            findViewById<RelativeLayout>(R.id.hint).visibility = View.GONE
            findViewById<CheckBox>(R.id.group_split).visibility = View.GONE
        }

        val inflater: LayoutInflater =
            this@LogExpenses.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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
            nameLabel.text = "Group"
        }

        val cameraButton = findViewById<ImageButton>(R.id.upload_receipt)
        cameraButton.setOnClickListener {
//            val intent = Intent(this, UploadPhotoActivity::class.java)
//            startActivity(intent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED){
                    //permission was not enabled
                    val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    //show popup to request permission
                    requestPermissions(permission, 1000)
                }
                else{
                    //permission already granted
                    openCamera()
                }
            }
            else{
                //system os is < marshmallow
                openCamera()
            }
        }

        val chipInViewModel = ViewModelProvider(this).get(ChipInViewModel::class.java)

        val insertPoint = findViewById<FlowLayout>(R.id.buddy_list)
        if (expenseId == -1L) {
            chipInViewModel.getActivity(activityId).observe(
                this, Observer { activityInfo ->
                    insertPoint.removeAllViews()
                    this.activityInfo = activityInfo
                    if (activityInfo == null) {
                        finish()
                        return@Observer
                    }
                    activity = activityInfo.activity
                    var index = 0
                    activityInfo.buddies.forEach { buddy ->
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


                        imageHolder.layoutParams.width = (60 * factor).toInt()
                        imageHolder.layoutParams.height = imageHolder.layoutParams.width
                        view.layoutParams = params
                        view.setOnClickListener {
                            buddyClickListener(overlay, imageHolder, insertPoint, view)
                        }
                        evenSplitList.add(0.0f)
                        unevenSplitList.add(0.0f)
                        selectedList.add(false)

                        insertPoint.addView(view)

                        val costField = view.findViewById<EditText>(R.id.individual_cost)
                        individualCostFields.add(costField)
                        costField.addTextChangedListener(costFieldChangeWatcher(insertPoint, view, costField))
                    }
                }
            )
            chipInViewModel.getActivities().removeObservers(this)
        } else {
            chipInViewModel.getCreditedToBuddy(activityId, buddyId, expenseId).observe(
                this, Observer { expenseInfo ->
                    insertPoint.removeAllViews()
                    creditInfo = expenseInfo
                    if (expenseInfo == null) {
                        finish()
                        return@Observer
                    }
                    expense = expenseInfo.expense
                    var creditorName = "Group"
                    if (buddyId != -1L) {
                        val creditor = expenseInfo.creditor[0]
                        creditorName = creditor.name!!
                        avatar.background = ContextCompat.getDrawable(
                            this@LogExpenses, resources.getIdentifier(
                                creditor.avatar, "drawable",
                                packageName
                            )
                        )
                    }
                    findViewById<EditText>(R.id.name_value).setText(expense!!.name)
                    findViewById<TextView>(R.id.who_value).text = creditorName
                    val creditMap = expenseInfo.credits.map { it.credit.fromBuddyId to it.credit.amount }.toMap()
                    evenSplit =
                        creditMap.values.count { it == expenseInfo.credits[0].credit.amount } == creditMap.values.count()
                    if (evenSplit) splitType.check(R.id.even)
                    else splitType.check(R.id.uneven)
                    val totalCost = creditMap.values.sum()
                    totalCostField!!.setText("%.2f".format(totalCost))

                    var index = 0
                    expenseInfo.buddies.forEach { buddy ->
                        val view = inflater.inflate(R.layout.buddy_frame_2, null)
                        view.id = View.generateViewId()

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

                        imageHolder.layoutParams.width = (60 * factor).toInt()
                        imageHolder.layoutParams.height = imageHolder.layoutParams.width
                        view.layoutParams = params
                        view.setOnClickListener {
                            buddyClickListener(overlay, imageHolder, insertPoint, view)
                        }

                        val costField = view.findViewById<EditText>(R.id.individual_cost)
                        individualCostFields.add(costField)
                        if (creditMap.containsKey(buddy.id)) {
                            costField.setText("%.2f".format(creditMap[buddy.id]))
                            overlay.visibility = View.INVISIBLE
                            imageHolder.alpha = 1.0f
                            Log.d("CHIP", "${evenSplit} ${evenSplitList} ${unevenSplitList}")
                            if (evenSplit) {
                                evenSplitList.add(creditMap.getValue(buddy.id))
                                unevenSplitList.add(0.0f)
                            } else {
                                evenSplitList.add(0.0f)
                                unevenSplitList.add(creditMap.getValue(buddy.id))
                                costField.isEnabled = true
                            }
                            selectedList.add(true)
                        } else {
                            evenSplitList.add(0.0f)
                            unevenSplitList.add(0.0f)
                            selectedList.add(false)
                        }
                        insertPoint.addView(view)
                        costField.addTextChangedListener(costFieldChangeWatcher(insertPoint, view, costField))
                    }
                }
            )
            chipInViewModel.getCreditedToBuddy(activityId, buddyId, expenseId).removeObservers(this)
        }


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

        splitType.setOnCheckedChangeListener { _, checkId ->
            switchSplitType(checkId)
        }


        val cancelNew = findViewById<ImageView>(R.id.cancel_new)
        cancelNew.setOnClickListener {
            cancelLogging()
        }

        saveButton!!.setOnClickListener {
            val splitCheck = findViewById<CheckBox>(R.id.group_split)
            val splitLater = splitCheck.isEnabled && splitCheck.isChecked
            Log.d("CHIP", "$splitLater $buddyId")

            val debtDict = mutableMapOf<Int, MutableMap<Int, Float>>()//.withDefault { mutableMapOf() }
            if (buddyId == -1L && splitLater) {
                val total = unevenSplitList.sum()
                val share = total / selectedList.count { it }
                val debt = mutableListOf<MutableList<Float>>()

                var index = 0f
                unevenSplitList.forEach { amount ->
                    if (selectedList[index.toInt()]) debt.add(mutableListOf(index, share - amount))
                    else debt.add(mutableListOf(index, 0f))
                    index++
                }

                Log.d("CHIP", "debts b: $debt")
                fun selector(d: MutableList<Float>): Float = d[1]
                debt.sortBy { selector(it) }
                Log.d("CHIP", "debts a: $debt")

                // Say i is Alice, j is Bob
                for (i in 0 until unevenSplitList.size) {
                    val iKey = debt[i][0].toInt()
                    // Alice's group balance is > 0
                    if (selectedList[iKey] && debt[i][1] > 0) {
                        // Check for other members that paid more than the share
                        for (j in 0 until unevenSplitList.size) {
                            val jKey = debt[j][0].toInt()
                            // Do while Alice has balance; find Bob
                            if (selectedList[jKey] && debt[j][1] < 0 && debt[i][1] != 0f) {
                                if (iKey !in debtDict) {
                                    debtDict[iKey] = mutableMapOf()
                                }
                                if (jKey !in debtDict[iKey]!!) {
                                    debtDict[iKey]!![jKey] = 0f
                                }
                                // If Alice's balance is less than or equal Bob's due
                                if (debt[i][1] + debt[j][1] <= 0) {
                                    debt[j][1] += debt[i][1]
                                    debtDict[iKey]!![jKey] = debt[i][1]
                                    debt[i][1] = 0f
                                }
                                // Subtract Bob's due from her balance; Bob is in Alice's credit list
                                else {
                                    debt[i][1] += debt[j][1]
                                    debtDict[iKey]!![jKey] = -debt[j][1]
                                    debt[j][1] = 0f
                                }
                            }
                        }
                    }
                }
                Log.d("CHIP", "debts f: $debt")
                Log.d("CHIP", debtDict.toString())
            }
//            finish()
//            return@setOnClickListener

            if (expenseId == -1L) {
                val creditList: MutableList<Credit?>? = mutableListOf()
                val groupCredit: MutableList<GroupCredit?>? = mutableListOf()
                expense = Expense(name = expenseName!!.text.toString(), activityId = activityId, buddyId = buddyId)
                for (i in activityInfo!!.buddies.indices) {
                    if (selectedList[i]) {
                        val buddy = activityInfo!!.buddies[i]
                        // Insert group credit cache
                        if (splitLater && debtDict.containsKey(i)) {
                            debtDict[i]!!.forEach { (k, v) ->
                                val gCredit = GroupCredit(
                                    expenseId = -1,
                                    amount = v,
                                    activityId = activityId,
                                    fromBuddyId = activityInfo!!.buddies[i].id,
                                    toBuddyId = activityInfo!!.buddies[k].id
                                )
                                groupCredit?.add(gCredit)
                            }
                        }
                        else {
                            val credit = Credit(
                                expenseId = -1,
                                amount = if (evenSplit) evenSplitList[i] else unevenSplitList[i],
                                activityId = activityId,
                                fromBuddyId = buddy.id,
                                toBuddyId = buddyId
                            )
                            creditList?.add(credit)
                        }
                    }
                }
                val imagePath = "${getExternalFilesDir(null)}/uploads/receipts/-1.jpg"
                chipInViewModel.insertCredits(expense!!, creditList, groupCredit, imagePath)
            } else {
                val creditList: MutableList<Credit?>? = mutableListOf()
                val groupCredit: MutableList<GroupCredit?>? = mutableListOf()
                expense!!.name = expenseName!!.text.toString()
                val currentCredits = creditInfo!!.credits.map { it.who[0].id to it.credit }.toMap()
                for (i in creditInfo!!.buddies.indices) {
                    val buddy = creditInfo!!.buddies[i]
                    var credit: Credit?

                    if (splitLater && debtDict.containsKey(i)) {
                        debtDict[i]!!.forEach { (k, v) ->
                            val gCredit = GroupCredit(
                                expenseId = expense!!.id,
                                amount = v,
                                activityId = activityId,
                                fromBuddyId = creditInfo!!.buddies[i].id,
                                toBuddyId = creditInfo!!.buddies[k].id
                            )
                            groupCredit?.add(gCredit)
                        }
                    } else {
                        if (selectedList[i]) {
                            if (buddy.id in currentCredits) {
                                credit = currentCredits[buddy.id]
                                credit!!.amount = if (evenSplit) evenSplitList[i] else unevenSplitList[i]
                            } else {
                                credit = Credit(
                                    expenseId = -1,
                                    amount = if (evenSplit) evenSplitList[i] else unevenSplitList[i],
                                    activityId = activityId,
                                    fromBuddyId = buddy.id,
                                    toBuddyId = buddyId
                                )
                            }
                            creditList?.add(credit)
                        } else {
                            if (buddy.id in currentCredits) {
                                credit = currentCredits[buddy.id]
                                credit!!.amount = -1.0f
                                creditList?.add(credit)
                            }
                        }
                    }
                }
                // Update group credit cache
                chipInViewModel.updateCredits(expense!!, creditList, groupCredit)
            }
            finish()
        }

        if (expenseId != -1L) {
            toggleButton(deleteButton, true)
            deleteButton!!.setOnClickListener {
                val builder = AlertDialog.Builder(this@LogExpenses)
                builder.setTitle("Deleting expense")
                builder.setMessage("This action cannot be undone. Continue?")
                builder.setPositiveButton(android.R.string.yes) { _, _ ->
                    chipInViewModel.deleteExpense(expenseId)
                    Toast.makeText(this, "Expense deleted.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                builder.setNegativeButton(android.R.string.no) { _, _ -> }
                builder.show()
            }
        }
    }

    private fun costFieldChangeWatcher(
        insertPoint: FlowLayout,
        view: View?,
        costField: EditText
    ): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!evenSplit && !switchWatcher) { // Careful not to lose this condition
                    unevenSplitList[insertPoint.indexOfChild(view)] = if (s.toString().isEmpty()) {
                        0.0f
                    } else {
                        try {
                            s.toString().toFloat()
                        } catch (e: NumberFormatException) {
                            costField.setText(unevenSplitList[insertPoint.indexOfChild(view)].toString())
                            costField.setSelection(unevenSplitList[insertPoint.indexOfChild(view)].toString().length)
                            unevenSplitList[insertPoint.indexOfChild(view)]
                        }
                    }
                    adjustUnevenSplit()
                }
            }
        }
    }

    private fun buddyClickListener(
        overlay: ImageView,
        imageHolder: ImageView,
        insertPoint: FlowLayout,
        view: View?
    ) {
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
        findViewById<CheckBox>(R.id.group_split).isEnabled = !evenSplit
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

    private fun openCamera() {
//        val values = ContentValues()
//        values.put(MediaStore.Images.Media.TITLE, "New Picture")
//        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
//        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val expenseId = expense?.id ?: -1
        val fileName = "${getExternalFilesDir(null)}/uploads/receipts/$expenseId.jpg"
        Log.d("CHIP", "Saving image to: $fileName")
        image_uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", File(fileName))
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(cameraIntent, 1001)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //called when user presses ALLOW or DENY from Permission Request Popup
        when(requestCode){
            1000 -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup was granted
                    openCamera()
                }
                else{
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //called when image was captured from camera intent
        if (resultCode == RESULT_OK){
            //set image captured to image view
//            image_view.setImageURI(image_uri)
        }
    }
}

