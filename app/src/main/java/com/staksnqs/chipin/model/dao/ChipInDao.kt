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

    @Insert
    fun insertGroupCredit(groupCredit: GroupCredit)

    @Update
    fun updateActivity(activity: Activity)

    @Update
    fun updateBuddy(buddy: Buddy)

    @Update
    fun updateExpense(expense: Expense)

    @Update
    fun updateCredit(credit: Credit)

    @Update
    fun updateGroupCredit(groupCredit: GroupCredit)

    @Delete
    fun deleteActivity(activity: Activity)

    @Delete
    fun deleteBuddy(buddy: Buddy)

    @Delete
    fun deleteCredit(credit: Credit)

    @Delete
    fun deleteExpense(expense: Expense)

    @Delete
    fun deleteGroupCredit(groupCredit: GroupCredit)

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
    @Query("SELECT * FROM expenses WHERE activityId = :activityId")
    fun getActivityExpensesSync(activityId: Long): List<Expense>

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
        "SELECT id, name, avatar, sum(total) as total, sum(owed) as owed FROM (" +
                "SELECT buddies.id as id, buddies.name as name, buddies.avatar as avatar, sum(credits.amount) AS total, 0 AS owed FROM credits " +
                "INNER JOIN buddies ON credits.toBuddyId = buddies.id " +
                "WHERE credits.activityId = :activityId AND credits.fromBuddyId = :buddyId AND credits.toBuddyId != :buddyId " +
                "GROUP BY buddies.id " +

                "UNION ALL " +
                "SELECT buddies.id as id, buddies.name as name, buddies.avatar as avatar, 0 AS total, SUM(credits.amount) AS owed FROM credits " +
                "INNER JOIN buddies ON credits.fromBuddyId = buddies.id " +
                "WHERE credits.activityId = :activityId AND credits.toBuddyId = :buddyId AND credits.fromBuddyId != :buddyId " +
                "GROUP BY buddies.id " +

                // Group credits
                "UNION ALL " +
                "SELECT buddies.id as id, buddies.name as name, buddies.avatar as avatar, SUM(group_credits.amount) AS total, 0 AS owed FROM group_credits " +
                "INNER JOIN buddies ON group_credits.toBuddyId = buddies.id " +
                "WHERE group_credits.activityId = :activityId AND group_credits.fromBuddyId = :buddyId AND group_credits.toBuddyId != :buddyId " +
                "GROUP BY buddies.id " +

                "UNION ALL " +
                "SELECT buddies.id as id, buddies.name as name, buddies.avatar as avatar, 0 AS total, SUM(group_credits.amount) AS owed FROM group_credits " +
                "INNER JOIN buddies ON group_credits.fromBuddyId = buddies.id " +
                "WHERE group_credits.activityId = :activityId AND group_credits.toBuddyId = :buddyId AND group_credits.fromBuddyId != :buddyId " +
                "GROUP BY buddies.id " +


                ") GROUP BY id ORDER BY id"
    )
    fun getBuddyDueSum(activityId: Long, buddyId: Long): LiveData<List<DuesPreview>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    fun getExpense(expenseId: Long): List<Expense>

    @Query("SELECT * FROM credits WHERE expenseId = :expenseId")
    fun getCreditsFromExpense(expenseId: Long): List<Credit>

    @Query("SELECT * FROM group_credits WHERE expenseId = :expenseId")
    fun getGroupCredits(expenseId: Long): List<GroupCredit>
}