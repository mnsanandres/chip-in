package com.staksnqs.chipin.model.entity

import androidx.room.Embedded
import androidx.room.Relation

data class Dues(
    @Embedded val credit: Credit,
    @Relation(
        parentColumn = "expenseId",
        entityColumn = "id"
    )
    val expense: List<Expense>,
    @Relation(
        parentColumn = "toBuddyId",
        entityColumn = "id"
    )
    val buddies: List<Buddy>
)