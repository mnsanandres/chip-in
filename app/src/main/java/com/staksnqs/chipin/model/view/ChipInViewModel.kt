package com.staksnqs.chipin.model.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.staksnqs.chipin.model.entity.*

class ChipInViewModel(application: Application) : AndroidViewModel(application) {
    private val chipInRepository: ChipInRepository = ChipInRepository()

    fun insertActivityWithBuddies(activity: Activity, buddies: MutableList<Buddy?>?) {
        chipInRepository.insertActivityWithBuddies(activity, buddies)
    }

    fun updateActivityWithBuddies(activity: Activity, buddies: MutableList<Buddy?>?) {
        chipInRepository.updateActivityWithBuddies(activity, buddies)
    }

    fun deleteActivity(activity: Activity) {
        chipInRepository.deleteActivity(activity)
    }

    fun deleteBuddies(buddies: MutableList<Buddy?>?) {
        chipInRepository.deleteBuddies(buddies)
    }

    fun getActivities(): LiveData<List<Activity>> {
        return chipInRepository.getActivities()
    }

    fun getActivity(activityId: Long): LiveData<ActivityWithBuddies> {
        return chipInRepository.getActivity(activityId)
    }

    fun insertCredits(expense: Expense, credits: MutableList<Credit?>?) {
        chipInRepository.insertCredits(expense, credits)
    }

    fun getBuddyExpenses(activityId: Long, buddyId: Long): LiveData<BuddyExpenses> {
        return chipInRepository.getBuddyExpenses(activityId, buddyId)
    }

    fun getBuddyExpensesSum(activityId: Long, buddyId: Long): LiveData<List<ExpensePreview>> {
        return chipInRepository.getBuddyExpensesSum(activityId, buddyId)
    }

    fun getCreditedToBuddy(activityId: Long, buddyId: Long, expenseId: Long): LiveData<CreditToBuddy> {
        return chipInRepository.getCreditedToBuddy(activityId, buddyId, expenseId)
    }

    fun updateCredits(expense: Expense, credits: MutableList<Credit?>?) {
        chipInRepository.updateCredits(expense, credits)
    }
}