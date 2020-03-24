package com.staksnqs.chipin.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "credits")
data class Credit(
    @PrimaryKey(autoGenerate = true) val id: Long,
    var expenseId: Long,
    val amount: Float,
    val activityId: Long,
    val fromBuddyId: Long,
    val toBuddyId: Long
) {
    @Ignore
    constructor(expenseId: Long, amount: Float, activityId: Long, fromBuddyId: Long, toBuddyId: Long) : this(
        0,
        expenseId,
        amount,
        activityId,
        fromBuddyId,
        toBuddyId
    )
}