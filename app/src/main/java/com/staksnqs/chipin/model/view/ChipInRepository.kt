package com.staksnqs.chipin.model.view

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.staksnqs.chipin.MainActivity
import com.staksnqs.chipin.model.entity.*

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

    fun insertCredits(expense: Expense, credits: MutableList<Credit?>?) {
        AsyncTask.execute {
            val expenseId = chipInDao.insertExpense(expense)
            credits!!.forEach { credit ->
                if (credit != null) {
                    credit.expenseId = expenseId
                    chipInDao.insertCredit(credit)
                }
            }
        }
    }

    fun updateCredits(expense: Expense, credits: MutableList<Credit?>?) {
        AsyncTask.execute {
            chipInDao.updateExpense(expense)
            credits!!.forEach { credit ->
                if (credit != null) {
                    if (credit.expenseId == -1L) {
                        credit.expenseId = expense.id
                        chipInDao.insertCredit(credit)
                    }
                    else {
                        if (credit.amount == -1.0f) {
                            chipInDao.deleteCredit(credit)
                        }
                        else {
                            chipInDao.updateCredit(credit)
                        }
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

}