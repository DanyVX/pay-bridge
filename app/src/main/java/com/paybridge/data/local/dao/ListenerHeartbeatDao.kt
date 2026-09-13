package com.paybridge.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.paybridge.data.local.entity.ListenerHeartbeatEntity

@Dao
interface ListenerHeartbeatDao {

    @Insert
    suspend fun insert(heartbeat: ListenerHeartbeatEntity): Long

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM listener_heartbeats
            WHERE wasEnabled = 0 AND timestamp BETWEEN :start AND :end
        )
        """
    )
    suspend fun hasDisabledHeartbeatBetween(start: Long, end: Long): Boolean

    @Query("SELECT * FROM listener_heartbeats ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): ListenerHeartbeatEntity?
}
