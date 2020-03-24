package com.staksnqs.chipin.model.entity

import androidx.room.Embedded
import androidx.room.Relation

data class BuddyExpenses(
    @Embedded val buddy: Buddy,
    @Relation(
        parentColumn = "id",
        entityColumn = "buddyId"
    )
    val expenses: List<Expense>
)