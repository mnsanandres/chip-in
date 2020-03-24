package com.staksnqs.chipin.model.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ExpenseCredits(
    @Embedded val expense: Expense,
    @Relation(
        parentColumn = "id",
        entityColumn = "expenseId"
    )
    val credits: List<Credit>
)