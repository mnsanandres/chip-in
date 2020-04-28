package com.staksnqs.chipin.model.view

import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LiveData
import com.staksnqs.chipin.MainActivity
import com.staksnqs.chipin.model.entity.*
import java.io.File

class ChipInRepository {
    private val chipInDao = MainActivity.chipInDatabase!!.ChipInDao()

    fun insertActivityWithBuddies(activity: Activity, buddies: MutableList<Buddy?>?) {
        AsyncTask.execute {
            val activityId = chipInDao.insertActivity(activity)
            buddies!!.forEach { buddy ->
                buddy!!.activityId = activityId
                chipInDao.insertBuddy(buddy)
            }
        }
    }

    fun updateActivityWithBuddies(activity: Activity, buddies: MutableList<Buddy?>?) {
        AsyncTask.execute {
            chipInDao.updateActivity(activity)
            buddies!!.forEach { buddy ->
                if (buddy!!.activityId == -1L) {
                    buddy.activityId = activity.id
                    chipInDao.insertBuddy(buddy)
                } else {
                    chipInDao.updateBuddy(buddy)
                }
            }
        }
    }

    fun deleteActivity(activity: Activity) {
        AsyncTask.execute {
            val expenses = chipInDao.getActivityExpensesSync(activity.id)
            expenses.forEach { expense ->
                val credits = chipInDao.getCreditsFromExpense(expense.id)
                val groupCredits = chipInDao.getGroupCredits(expense.id)
                credits.forEach { credit ->
                    chipInDao.deleteCredit(credit)
                }
                groupCredits.forEach { groupCredit ->
                    chipInDao.deleteGroupCredit(groupCredit)
                }
                chipInDao.deleteExpense(expense)
            }
            val activityInfo = chipInDao.getActivityWithBuddiesSync(activity.id)
            activityInfo.buddies.forEach { buddy ->
                chipInDao.deleteBuddy(buddy)
            }
            chipInDao.deleteActivity(activity)
        }
    }

    fun deleteBuddies(buddies: MutableList<Buddy?>?) {
        AsyncTask.execute {
            buddies!!.forEach { buddy ->
                if (buddy != null) {
                    chipInDao.deleteBuddy(buddy)
                }
            }
        }
    }

    fun getActivities(): LiveData<List<Activity>> {
        return chipInDao.getActivities()
    }

    fun getActivity(activityId: Long): LiveData<ActivityWithBuddies> {
        return chipInDao.getActivityWithBuddies(activityId)
    }

    fun insertCredits(
        expense: Expense,
        credits: MutableList<Credit?>?,
        groupCredits: MutableList<GroupCredit?>? = mutableListOf(),
        imagePath : String
    ) {
        AsyncTask.execute {
            val expenseId = chipInDao.insertExpense(expense)
            credits!!.forEach { credit ->
                if (credit != null) {
                    credit.expenseId = expenseId
                    chipInDao.insertCredit(credit)
                }
            }
            if (groupCredits!!.isNotEmpty()) {
                groupCredits.forEach { groupCredit ->
                    if (groupCredit != null) {
                        groupCredit.expenseId = expenseId
                        chipInDao.insertGroupCredit(groupCredit)
                        Log.d("CHIP_A", "Added $groupCredit")
                    }
                }
            }
            val image = File(imagePath)
            if (image.exists()) {
                val newId = File(imagePath.replace("-1.jpg", "$expenseId.jpg"))
                image.renameTo(newId)
            }
        }
    }

    fun updateCredits(
        expense: Expense,
        credits: MutableList<Credit?>?,
        groupCredits: MutableList<GroupCredit?>? = mutableListOf()
    ) {
        AsyncTask.execute {
            chipInDao.updateExpense(expense)
            credits!!.forEach { credit ->
                if (credit != null) {
                    if (credit.expenseId == -1L) {
                        credit.expenseId = expense.id
                        chipInDao.insertCredit(credit)
                    } else {
                        if (credit.amount == -1.0f) {
                            chipInDao.deleteCredit(credit)
                        } else {
                            chipInDao.updateCredit(credit)
                        }
                    }
                }
            }
            if (groupCredits!!.isNotEmpty()) {
                val prevGroupCredits = chipInDao.getGroupCredits(expense.id)
                prevGroupCredits.forEach { groupCredit ->
                    chipInDao.deleteGroupCredit(groupCredit)
                    Log.d("CHIP_D", "Delete $groupCredit")
                }
                groupCredits.forEach { groupCredit ->
                    if (groupCredit != null) {
                        groupCredit.expenseId = expense.id
                        chipInDao.insertGroupCredit(groupCredit)
                        Log.d("CHIP_A", "Added $groupCredit")
                    }
                }
            }
        }
    }

    fun getBuddyExpenses(activityId: Long, buddyId: Long): LiveData<BuddyExpenses> {
        return chipInDao.getBuddyExpenses(activityId, buddyId)
    }

    fun getBuddyExpensesSum(activityId: Long, buddyId: Long): LiveData<List<ExpensePreview>> {
        return chipInDao.getBuddyExpensesSum(activityId, buddyId)
    }

    fun getCreditedToBuddy(activityId: Long, buddyId: Long, expenseId: Long): LiveData<CreditToBuddy> {
        return chipInDao.getCreditedToBuddy(activityId, buddyId, expenseId)
    }

    fun getDues(activityId: Long, buddyId: Long, creditorId: Long): LiveData<List<Dues>> {
        return chipInDao.getDues(activityId, buddyId, creditorId)
    }

    fun getBuddyDueSum(activityId: Long, buddyId: Long): LiveData<List<DuesPreview>> {
        return chipInDao.getBuddyDueSum(activityId, buddyId)
    }

    fun deleteExpense(expenseId: Long) {
        AsyncTask.execute {
            val expense = chipInDao.getExpense(expenseId)
            val credits = chipInDao.getCreditsFromExpense(expenseId)
            val groupCredits = chipInDao.getGroupCredits(expenseId)
            credits.forEach { credit ->
                chipInDao.deleteCredit(credit)
//                Log.d("CHIP", credit.toString())
            }
            groupCredits.forEach { groupCredit ->
                chipInDao.deleteGroupCredit(groupCredit)
            }
            chipInDao.deleteExpense(expense[0])
//            Log.d("CHIP", expense[0].toString())
        }
    }

}