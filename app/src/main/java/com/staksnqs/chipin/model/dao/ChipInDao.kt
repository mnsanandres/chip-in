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

    @Update
    fun updateExpense(expense: Expense)

    @Update
    fun updateCredit(credit: Credit)

    @Delete
    fun deleteActivity(activity: Activity)

    @Delete
    fun deleteBuddy(buddy: Buddy)

    @Delete
    fun deleteCredit(credit: Credit)

    @Query("SELECT * FROM activities")
    fun getActivities(): LiveData<List<Activity>>

    @Transaction
    @Query("SELECT * FROM activities WHERE id = :activityId")
    fun getActivityWithBuddies(activityId: Long): LiveData<ActivityWithBuddies>

    @Transaction
    @Query("SELECT * FROM activities WHERE id = :activityId")
    fun getActivityWithBuddiesSync(activityId: Long): ActivityWithBuddies

    @Transaction
    @Query("SELECT * FROM expenses WHERE activityId = :activityId AND buddyId = :buddyId AND id = :expenseId")
    fun getCreditedToBuddy(activityId: Long, buddyId: Long, expenseId: Long): LiveData<CreditToBuddy>

    @Transaction
    @Query("SELECT * FROM buddies WHERE activityId = :activityId AND id = :buddyId")
    fun getBuddyExpenses(activityId: Long, buddyId: Long): LiveData<BuddyExpenses>

    @Transaction
    @Query(
        "SELECT expenses.id, expenses.name, SUM(credits.amount) as total FROM credits " +
                "INNER JOIN expenses ON expenses.id = credits.expenseId " +
                "WHERE credits.activityId = :activityId AND toBuddyId = :buddyId " +
                "GROUP BY expenseId " +
                "ORDER BY expenseId"
    )
    fun getBuddyExpensesSum(activityId: Long, buddyId: Long): LiveData<List<ExpensePreview>>

    @Transaction
    @Query("SELECT * FROM credits WHERE activityId = :activityId AND fromBuddyId = :buddyId AND toBuddyId = :creditorId AND toBuddyId != :buddyId")
    fun getDues(activityId: Long, buddyId: Long, creditorId: Long): LiveData<List<Dues>>

    @Transaction
    @Query(
        "SELECT buddies.id, buddies.name, buddies.avatar, sum(credits.amount) as total FROM credits " +
                "INNER JOIN buddies on credits.toBuddyId = buddies.id " +
                "WHERE credits.activityId = :activityId AND credits.fromBuddyId = :buddyId AND credits.toBuddyId != :buddyId " +
                "GROUP BY credits.toBuddyId " +
                "ORDER BY buddies.id"
    )
    fun getBuddyDueSum(activityId: Long, buddyId: Long): LiveData<List<DuesPreview>>
}