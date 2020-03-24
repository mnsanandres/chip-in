package com.staksnqs.chipin.model.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ActivityWithBuddies(
    @Embedded val activity: Activity,
    @Relation(
        parentColumn = "id",
        entityColumn = "activityId"
    )
    val buddies: List<Buddy>
)