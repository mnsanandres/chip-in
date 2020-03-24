package com.staksnqs.chipin.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class Activity(
    @PrimaryKey(autoGenerate = true) var id: Long,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "date") var date: Long
) {
    @Ignore
    constructor(name: String?, date: Long) : this(0, name, date)
}