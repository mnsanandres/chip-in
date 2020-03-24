package com.staksnqs.chipin.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(@PrimaryKey(autoGenerate = true) var id: Long,
                   @ColumnInfo(name = "name") var name: String?,
                   var activityId: Long,
                   var buddyId: Long
) {
    @Ignore
    constructor(name: String, activityId: Long, buddyId: Long) : this(0, name, activityId, buddyId)
}