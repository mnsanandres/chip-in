package com.staksnqs.chipin.model.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.staksnqs.chipin.model.entity.Activity
import com.staksnqs.chipin.model.entity.ActivityWithBuddies
import com.staksnqs.chipin.model.entity.Buddy

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
}