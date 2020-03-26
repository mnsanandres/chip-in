package com.staksnqs.chipin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.staksnqs.chipin.model.view.ChipInViewModel
import java.text.SimpleDateFormat
import java.util.*


class LogActivity : AppCompatActivity() {

    private lateinit var chipInViewModel: ChipInViewModel
    private var activityId: Long = -1
    private var buddyId: Long = -1
    private var buddyAvatar: String = ""
    private var buddyName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityId = intent.getLongExtra("ACTIVITY_ID", -1)
        buildComponents()
    }

    override fun onResume() {
        super.onResume()
        buildComponents()
    }

    private fun buildComponents() {
        setContentView(R.layout.activity_log)

        val toolbar: Toolbar = findViewById(R.id.tool_bar)
        toolbar.title = null
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        findViewById<TextView>(R.id.activity_title).text = "Log Activity"

        chipInViewModel = ViewModelProvider(this).get(ChipInViewModel::class.java)

        val params = GridLayout.LayoutParams()

        val factor = resources.displayMetrics.density
        val margin = 15
        val insertPoint = findViewById<ViewGroup>(R.id.buddy_list)
        params.setMargins(margin, margin, margin, margin)
        params.width = (110 * factor).toInt()
        params.height = (150 * factor).toInt()

        val rParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        rParams.setMargins(margin, margin, margin, margin)
        rParams.width = (90 * factor).toInt()
        rParams.height = (100 * factor).toInt()

        val groupAvatar = findViewById<RelativeLayout>(R.id.group_expense)
        val groupImageHolder = groupAvatar.findViewById<RelativeLayout>(R.id.clique)
        groupImageHolder.layoutParams = rParams
        groupAvatar.setOnClickListener {
            buddyId = -1
            buddyAvatar = "group"
            buddyName = "Group"
            registerForContextMenu(groupAvatar)
            openContextMenu(groupAvatar)
        }

        chipInViewModel.getActivity(activityId).observe(
            this, Observer { activityInfo ->
                if (activityInfo == null) {
                    finish()
                    return@Observer
                }
                findViewById<TextView>(R.id.column2).text = activityInfo.activity.name
                findViewById<TextView>(R.id.column1).text =
                    SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date(activityInfo.activity.date))
                var index = 0
                activityInfo.buddies.forEach { buddy ->
                    val inflater: LayoutInflater =
                        this@LogActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val view = inflater.inflate(R.layout.buddy_frame, null)
                    view.id = View.generateViewId()
                    val imageHolder = view.findViewById<ImageView>(R.id.buddy_avatar)
                    val background = ContextCompat.getDrawable(
                        this@LogActivity, resources.getIdentifier(
                            buddy.avatar, "drawable",
                            packageName
                        )
                    )
                    imageHolder.background = background

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

                    val textHolder = view.findViewById<TextView>(R.id.buddy_name)
                    textHolder.text = buddy.name

                    imageHolder.layoutParams.width = (90 * factor).toInt()
                    imageHolder.layoutParams.height = imageHolder.layoutParams.width
                    view.layoutParams = params
                    view.setOnClickListener {
                        buddyId = buddy.id
                        buddyAvatar = buddy.avatar
                        buddyName = buddy.name!!
                        registerForContextMenu(view)
                        openContextMenu(view)
                    }

                    insertPoint.addView(view)
                }
            }
        )

        val backButton = findViewById<ImageView>(R.id.cancel_new)
        backButton.setBackgroundResource(android.R.drawable.ic_menu_revert)
        backButton.setOnClickListener {
            finish()
        }
        val editButton = findViewById<ImageView>(R.id.edit_activity)
        editButton.visibility = View.VISIBLE
        editButton.setOnClickListener {
            val intent = Intent(baseContext, NewActivity::class.java)
            intent.putExtra("ACTIVITY_ID", activityId)
            startActivity(intent)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: LayoutInflater =
            this@LogActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val header = inflater.inflate(R.layout.menu_header, null)
        val title = header.findViewById<TextView>(R.id.title_text)
        title.text = "Select action"
        menu!!.setHeaderView(header)
        menu.add(Menu.NONE, 1, Menu.NONE, "Log expenses")
        menu.add(Menu.NONE, 2, Menu.NONE, "View expenses")
        if (buddyId != -1L) {
            menu.add(Menu.NONE, 3, Menu.NONE, "View dues")
        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            1 -> logExpensesActivity()
            2 -> viewExpensesActivity()
            3 -> viewDuesActivity()
        }
        return true
    }


    private fun logExpensesActivity() {
        val intent = Intent(baseContext, LogExpenses::class.java)
        intent.putExtra("ACTIVITY_ID", activityId)
        intent.putExtra("BUDDY_ID", buddyId)
        intent.putExtra("BUDDY_AVATAR", buddyAvatar)
        startActivity(intent)
    }

    private fun viewExpensesActivity() {
        val intent = Intent(baseContext, ViewExpensesList::class.java)
        intent.putExtra("ACTIVITY_ID", activityId)
        intent.putExtra("BUDDY_ID", buddyId)
        intent.putExtra("BUDDY_NAME", buddyName)
        intent.putExtra("BUDDY_AVATAR", buddyAvatar)
        startActivity(intent)
    }

    private fun viewDuesActivity() {
        val intent = Intent(baseContext, DuesPreview::class.java)
        intent.putExtra("ACTIVITY_ID", activityId)
        intent.putExtra("BUDDY_ID", buddyId)
        intent.putExtra("BUDDY_NAME", buddyName)
        intent.putExtra("BUDDY_AVATAR", buddyAvatar)
        startActivity(intent)
    }
}
