package com.staksnqs.chipin.model.dao


import androidx.lifecycle.LiveData
import androidx.room.*
import com.staksnqs.chipin.model.entity.*

@Dao
interface ChipInDao {
    @Insert
    fun insertActivity(activity: Activity): Long

    @Insert
    fun insertBuddy(buddy: Buddy)

    @Insert
    fun insertCredit(credit: Credit)

    @Insert
    fun insertExpense(expense: Expense): Long

    @Update
    fun updateActivity(activity: Activity)

    @Update
    fun updateBuddy(buddy: Buddy)

    @Delete
    fun deleteActivity(activity: Activity)

    @Delete
    fun deleteBuddy(buddy: Buddy)

    @Query("SELECT * FROM activities")
    fun getActivities(): LiveData<List<Activity>>

    @Transaction
    @Query("SELECT * FROM activities WHERE id = :activityId")
    fun getActivityWithBuddies(activityId: Long): LiveData<ActivityWithBuddies>

    @Transaction
    @Query("SELECT * FROM activities WHERE id = :activityId")
    fun getActivityWithBuddiesSync(activityId: Long): ActivityWithBuddies

//    @Transaction
//    //@Query("SELECT * FROM buddies WHERE activityId = :activityId")
//    @Query("SELECT * FROM credits WHERE activityId = :activityId")
//    fun getCreditedToBuddy(activityId: Long): List<CreditToBuddy>

    @Query("SELECT * FROM buddies WHERE activityId = :activityId AND id = :buddyId")
    fun getBuddyExpenses(activityId: Long, buddyId: Long): LiveData<BuddyExpenses>

    @Transaction
    @Query("SELECT expenses.name, SUM(credits.amount) as total FROM credits " +
            "INNER JOIN expenses ON expenses.id = credits.expenseId " +
            "WHERE credits.activityId = :activityId AND toBuddyId = :buddyId GROUP BY expenseId ORDER BY expenseId")
    fun getBuddyExpensesSum(activityId: Long, buddyId: Long): LiveData<List<ExpensePreview>>
}