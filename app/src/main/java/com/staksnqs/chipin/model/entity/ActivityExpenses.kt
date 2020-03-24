package com.staksnqs.chipin.model.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ActivityExpenses(
    @Embedded val expense: Activity,
    @Relation(
        parentColumn = "id",
        entityColumn = "activityId"
    )
    val credits: List<Expense>
)