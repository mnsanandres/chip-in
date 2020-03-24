package com.staksnqs.chipin.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "buddies")
data class Buddy(
    @PrimaryKey(autoGenerate = true) var id: Long,
    @ColumnInfo(name = "name") var name: String?,
    var activityId: Long,
    var avatar: String
) {
    @Ignore
    constructor(name: String, activityId: Long, avatar: String) : this(0, name, activityId, avatar)
}