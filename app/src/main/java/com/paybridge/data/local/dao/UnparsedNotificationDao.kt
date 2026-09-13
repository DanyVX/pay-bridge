package com.paybridge.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.paybridge.data.local.entity.UnparsedNotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnparsedNotificationDao {

    @Insert
    suspend fun insert(notification: UnparsedNotificationEntity): Long

    @Query("SELECT COUNT(*) FROM unparsed_notifications WHERE provider = :provider AND occurredAt >= :since")
    fun countByProviderSince(provider: String, since: Long): Flow<Int>

    @Query("SELECT DISTINCT provider FROM unparsed_notifications WHERE occurredAt >= :since")
    suspend fun providersWithUnparsedSince(since: Long): List<String>
}
