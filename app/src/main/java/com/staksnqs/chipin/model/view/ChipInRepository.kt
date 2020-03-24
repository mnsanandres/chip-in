package com.staksnqs.chipin.model.view

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.staksnqs.chipin.MainActivity
import com.staksnqs.chipin.model.entity.Activity
import com.staksnqs.chipin.model.entity.ActivityWithBuddies
import com.staksnqs.chipin.model.entity.Buddy

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
            activityInfo!!.buddies.forEach { buddy ->
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
}