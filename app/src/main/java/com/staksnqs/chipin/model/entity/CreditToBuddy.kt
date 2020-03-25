package com.staksnqs.chipin.model.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CreditToBuddy(
    @Embedded val expense: Expense,
    @Relation(
        parentColumn = "buddyId",
        entityColumn = "id"
    )
    val creditor: List<Buddy>,
    @Relation(
        parentColumn = "activityId",
        entityColumn = "activityId"
    )
    val buddies: List<Buddy>,
    @Relation(
        parentColumn = "id",
        entityColumn = "expenseId",
        entity = Credit::class
    )
    val credits: List<CreditFromBuddy>
)