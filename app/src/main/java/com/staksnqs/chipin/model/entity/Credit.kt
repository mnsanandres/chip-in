package com.staksnqs.chipin.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credits")
data class Credit(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "expense") val expense: String?,
    val amount: Float,
    val fromBuddyId: Long,
    val toBuddyId: Long,
    val activityId: Long
)