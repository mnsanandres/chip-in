package com.staksnqs.chipin.model.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CreditToBuddy(
    @Embedded val from: Buddy,
    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val creditees: List<Buddy>
)