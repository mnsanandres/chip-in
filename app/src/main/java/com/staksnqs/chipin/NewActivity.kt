package com.staksnqs.chipin

import android.app.ActionBar
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.staksnqs.chipin.model.entity.Activity
import com.staksnqs.chipin.model.entity.Buddy
import com.staksnqs.chipin.model.view.ChipInViewModel
import kotlinx.android.synthetic.main.activity_new.*
import java.text.SimpleDateFormat
import java.util.*


class NewActivity : AppCompatActivity() {

    private var newBuddyDialog: Dialog? = null
    private var avatarDialog: Dialog? = null
    private var avatarBackground: String = ""
    private var defaultAvatar: Drawable? = null
    private var activityName: EditText? = null
    private var activityDate: EditText? = null
    private var saveActivity: Button? = null
    private var deleteActivity: Button? = null
    private var hasBuddies: Boolean = false
    private var activity: Activity? = null
    private var buddies: MutableList<Buddy?>? = mutableListOf()
    private var buddiesToRemove: MutableList<Buddy?>? = mutableListOf()
    private var editMode = false
    private var activityId = -1L

    private lateinit var chipInViewModel: ChipInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        defaultAvatar = ContextCompat.getDrawable(this@NewActivity, R.drawable.avatar_01)
        avatarBackground = "avatar_01"

        activityId = intent.getLongExtra("ACTIVITY_ID", -1)
        if (activityId != -1L) {
            editMode = true
            hasBuddies = true
        }

        chipInViewModel = ViewModelProvider(this).get(ChipInViewModel::class.java)

        setContentView(R.layout.activity_new)

        val toolbar: Toolbar = findViewById(R.id.tool_bar)
        toolbar.title = null
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        if (activityId != -1L) {
            findViewById<TextView>(R.id.activity_title).text = "Edit Activity"
        }

        val cancelNew = findViewById<ImageView>(R.id.cancel_new)
        cancelNew.setOnClickListener {
            cancelNewActivity()
        }

        activityName = findViewById(R.id.name_value)
        activityDate = findViewById(R.id.date_picker)
        saveActivity = findViewById(R.id.save_activity)
        deleteActivity = findViewById(R.id.delete_activity)
        activityName!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                saveActivityEnabler()
            }
        })

        saveActivity!!.setOnClickListener(saveActivity())

        name_label.typeface = Typeface.createFromAsset(assets, "fonts/bungee.ttf")
        name_value.typeface = Typeface.createFromAsset(assets, "fonts/bungee.ttf")
        date_label.typeface = Typeface.createFromAsset(assets, "fonts/bungee.ttf")
        date_picker.typeface = Typeface.createFromAsset(assets, "fonts/bungee.ttf")
        buddy_label.typeface = Typeface.createFromAsset(assets, "fonts/bungee.ttf")
        date_picker.inputType = InputType.TYPE_NULL
        date_picker.setOnClickListener {
            val calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val picker = DatePickerDialog(
                this@NewActivity,
                R.style.DatePickerDialogTheme,
                DatePickerDialog.OnDateSetListener { _, yearNow, monthOfYear, dayOfMonth ->
                    val dateText = "${monthOfYear + 1}/$dayOfMonth/$yearNow"
                    date_picker.setText(dateText)
                    saveActivityEnabler()
                },
                year,
                month,
                day
            )
            picker.show()
        }
        add_buddy.setOnClickListener(showBuddyForm())

        val insertPoint = findViewById<ViewGroup>(R.id.buddy_list)
        if (editMode) {
            chipInViewModel.getActivity(activityId).observe(
                this, Observer { activityInfo ->
                    if (activityInfo == null) {
                        return@Observer
                    }
                    activity = activityInfo.activity
                    buddies = activityInfo.buddies.toMutableList()
                    activityName!!.setText(activity!!.name)
                    activityDate!!.setText(SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date(activity!!.date)))

                    toggleButton(deleteActivity, true)
                    deleteActivity!!.setOnClickListener {
                        val builder = AlertDialog.Builder(this@NewActivity)
                        builder.setTitle("Deleting activity")
                        builder.setMessage("Delete ${activity!!.name}?")
                        builder.setPositiveButton(android.R.string.yes) { _, _ ->
                            chipInViewModel.deleteActivity(activity!!)
                            Toast.makeText(this, "Activity deleted.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        builder.setNegativeButton(android.R.string.no) { _, _ -> }
                        builder.show()
                    }

                    activityInfo.buddies.forEach { buddy ->
                        val inflater: LayoutInflater =
                            this@NewActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val view = inflater.inflate(R.layout.buddy_frame, null)
                        view.id = buddy.id.toInt()
                        val imageHolder = view.findViewById<ImageView>(R.id.buddy_avatar)
                        imageHolder.background = ContextCompat.getDrawable(
                            this@NewActivity, resources.getIdentifier(
                                buddy.avatar, "drawable",
                                packageName
                            )
                        )
                        val textHolder = view.findViewById<TextView>(R.id.buddy_name)
                        textHolder.text = buddy.name

                        val params = GridLayout.LayoutParams()

                        val factor = resources.displayMetrics.density
                        val margin = 15
                        params.setMargins(margin, margin, margin, margin)
                        params.width = (110 * factor).toInt()
                        params.height = (150 * factor).toInt()
                        imageHolder.layoutParams.width = (90 * factor).toInt()
                        imageHolder.layoutParams.height = imageHolder.layoutParams.width
                        view.layoutParams = params

                        view.setOnClickListener(
                            showBuddyForm(
                                currentId = buddy.id,
                                currentAvatar = buddy.avatar,
                                currentName = buddy.name.toString()
                            )
                        )

                        insertPoint.addView(view, insertPoint.childCount - 1)
                    }
                }
            )
        }
    }

    private fun saveActivity(): (View) -> Unit {
        return {
            val format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            if (!editMode) {
                chipInViewModel.insertActivityWithBuddies(
                    Activity(
                        name = activityName!!.text.toString(),
                        date = format.parse(activityDate!!.text.toString()).time
                    ), buddies
                )
            } else {
                activity!!.name = activityName!!.text.toString()
                chipInViewModel.deleteBuddies(buddiesToRemove)
                chipInViewModel.updateActivityWithBuddies(activity!!, buddies)
            }
            finish()
        }
    }

    private fun showBuddyForm(
        currentId: Long = -1,
        currentAvatar: String = "avatar_01",
        currentName: String = ""
    ): (View) -> Unit {
        return {
            newBuddyDialog = Dialog(this@NewActivity)
            newBuddyDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            newBuddyDialog!!.setCancelable(false)
            newBuddyDialog!!.setContentView(R.layout.add_buddy_form)
            newBuddyDialog!!.window!!.setLayout(
                (6 * resources.displayMetrics.widthPixels) / 7,
                ActionBar.LayoutParams.WRAP_CONTENT
            )
            val buddyImage = newBuddyDialog!!.findViewById<ImageView>(R.id.buddy_avatar)
            buddyImage.background = defaultAvatar
            avatarBackground = "avatar_01"
            val buddyNameLabel = newBuddyDialog!!.findViewById<TextView>(R.id.buddy_name_label)
            val buddyNameValue = newBuddyDialog!!.findViewById<EditText>(R.id.buddy_name_value)
            val saveBuddy = newBuddyDialog!!.findViewById<Button>(R.id.save_buddy)
            val deleteBuddy = newBuddyDialog!!.findViewById<Button>(R.id.delete_buddy)
            val cancelBuddy = newBuddyDialog!!.findViewById<ImageView>(R.id.cancel_buddy)
            val currentImage = newBuddyDialog!!.findViewById<ImageView>(R.id.buddy_avatar)
            buddyNameValue.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    saveBuddyEnabler(saveBuddy, buddyNameValue.text.toString())
                }
            })
            if (currentId != -1L) {
                deleteBuddy.isEnabled = true
                deleteBuddy.setBackgroundColor(Color.parseColor("#FFC400"))
                deleteBuddy.setTextColor(Color.parseColor("#363030"))
                val computedAvatar = ContextCompat.getDrawable(
                    this@NewActivity, resources.getIdentifier(
                        currentAvatar, "drawable",
                        packageName
                    )
                )
                currentImage.background = computedAvatar
                buddyNameValue.setText(currentName)
                avatarBackground = currentAvatar
            }
            buddyNameLabel.typeface = Typeface.createFromAsset(assets, "fonts/bungee.ttf")
            buddyNameValue.typeface = Typeface.createFromAsset(assets, "fonts/bungee.ttf")
            saveBuddy.typeface = Typeface.createFromAsset(assets, "fonts/bungee.ttf")

            buddyImage.setOnClickListener {
                avatarDialog = Dialog(this@NewActivity)
                avatarDialog!!.setContentView(R.layout.activity_avatar_select)
                avatarDialog!!.window!!.setLayout(
                    (6 * resources.displayMetrics.widthPixels) / 7,
                    ActionBar.LayoutParams.WRAP_CONTENT
                )
                avatarDialog!!.show()
            }

            val insertPoint = findViewById<ViewGroup>(R.id.buddy_list)
            cancelBuddy.setOnClickListener { newBuddyDialog!!.dismiss() }
            saveBuddy.setOnClickListener {
                val inflater: LayoutInflater =
                    this@NewActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val view = inflater.inflate(R.layout.buddy_frame, null)
                view.id = View.generateViewId()
                val imageHolder = view.findViewById<ImageView>(R.id.buddy_avatar)
                imageHolder.background = ContextCompat.getDrawable(
                    this@NewActivity, resources.getIdentifier(
                        avatarBackground, "drawable",
                        packageName
                    )
                )
                val textHolder = view.findViewById<TextView>(R.id.buddy_name)
                textHolder.text = newBuddyDialog!!.findViewById<EditText>(R.id.buddy_name_value).text

                val params = GridLayout.LayoutParams()

                val factor = resources.displayMetrics.density
                val margin = 15
                params.setMargins(margin, margin, margin, margin)
                params.width = (110 * factor).toInt()
                params.height = (150 * factor).toInt()
                imageHolder.layoutParams.width = (90 * factor).toInt()
                imageHolder.layoutParams.height = imageHolder.layoutParams.width
                view.layoutParams = params
                var buddy: Buddy?
                val insertIndex =
                    if (currentId != -1L) insertPoint.indexOfChild(insertPoint.findViewById(currentId.toInt())) else insertPoint.childCount - 1
                view.setOnClickListener(
                    showBuddyForm(
                        currentId = view.id.toLong(),
                        currentAvatar = avatarBackground,
                        currentName = textHolder.text.toString()
                    )
                )

                if (insertIndex == insertPoint.childCount - 1) {
                    buddy = Buddy(
                        name = textHolder.text.toString(),
                        avatar = avatarBackground,
                        activityId = -1
                    )
                    buddies!!.add(buddy)
                } else {
                    buddy = buddies!!.get(insertIndex)
                    buddy!!.name = textHolder.text.toString()
                    buddy.avatar = avatarBackground
                }
                if (currentId != -1L) insertPoint.removeViewAt(insertIndex)
                insertPoint.addView(view, insertIndex, params)

                newBuddyDialog!!.dismiss()
                hasBuddies = true
                saveActivityEnabler()
            }
            deleteBuddy.setOnClickListener {
                if (currentId != -1L) {
                    val builder = AlertDialog.Builder(this@NewActivity)
                    builder.setTitle("Deleting member")
                    builder.setMessage("Delete $currentName?")
                    builder.setPositiveButton(android.R.string.yes) { _, _ ->
                        val removeIndex = insertPoint.indexOfChild(insertPoint.findViewById(currentId.toInt()))
                        insertPoint.removeView(insertPoint.findViewById(currentId.toInt()))
                        val toRemove = buddies!!.removeAt(removeIndex)
                        buddiesToRemove!!.add(toRemove)
                        newBuddyDialog!!.dismiss()
                        hasBuddies = insertPoint.childCount > 1
                        saveActivityEnabler()
                    }
                    builder.setNegativeButton(android.R.string.no) { _, _ -> }
                    builder.show()
                }
            }
            newBuddyDialog!!.show()
        }
    }

    fun selectAvatar(avatar: View) {
        val buddyImage = newBuddyDialog!!.findViewById<ImageView>(R.id.buddy_avatar)
        avatarBackground = avatar.tag.toString()
        buddyImage.background = avatar.background
        avatarDialog!!.dismiss()
    }

    override fun onBackPressed() {
        cancelNewActivity()
    }

    private fun cancelNewActivity() {
        val builder = AlertDialog.Builder(this@NewActivity)
        builder.setTitle("Discard ${if (activityId == -1L) "new" else "edits to"} activity?")
        builder.setMessage("All your progress will be lost. Continue?")
        builder.setPositiveButton(android.R.string.yes) { _, _ ->
            finish()
        }
        builder.setNegativeButton(android.R.string.no) { _, _ -> }
        builder.show()
    }

    private fun saveActivityEnabler() {
        toggleButton(
            saveActivity,
            (!activityName!!.text.isNullOrEmpty() and !activityDate!!.text.isNullOrEmpty() and hasBuddies)
        )
    }

    private fun saveBuddyEnabler(button: Button?, buddyName: String) {
        toggleButton(button, buddyName.isNotEmpty())
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
