package com.staksnqs.chipin.model.dao


import android.webkit.PluginStub
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

    @Transaction
    @Query("SELECT * FROM buddies")
    fun getCreditedToBuddy(): List<CreditToBuddy>
}