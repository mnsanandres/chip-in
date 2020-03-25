package com.staksnqs.chipin.model.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CreditFromBuddy(
    @Embedded val credit: Credit,
    @Relation(
        parentColumn = "fromBuddyId",
        entityColumn = "id"
    )
    val who: List<Buddy>
)