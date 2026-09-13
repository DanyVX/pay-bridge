package com.paybridge.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.paybridge.data.local.entity.IncomingNotificationEntity

@Dao
interface IncomingNotificationDao {

    @Insert
    suspend fun insert(notification: IncomingNotificationEntity): Long

    @Query("UPDATE incoming_notifications SET consumedByClaimId = :claimId WHERE id = :id")
    suspend fun markConsumed(id: Long, claimId: Long)

    /**
     * Unconsumed notifications that could satisfy a claim just created with the given amount
     * and (nullable) provider — the "notification arrived slightly before the shopkeeper
     * finished typing the claim" case.
     */
    @Query(
        """
        SELECT * FROM incoming_notifications
        WHERE consumedByClaimId IS NULL
          AND amount = :amount
          AND (:claimProvider IS NULL OR provider = :claimProvider)
          AND receivedAt BETWEEN :windowStart AND :windowEnd
        ORDER BY receivedAt ASC
        """
    )
    suspend fun findUnconsumedMatching(
        amount: Long,
        claimProvider: String?,
        windowStart: Long,
        windowEnd: Long,
    ): List<IncomingNotificationEntity>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM incoming_notifications
            WHERE dedupeKey = :key AND receivedAt BETWEEN :bucketStart AND :bucketEnd
        )
        """
    )
    suspend fun existsWithDedupeKeyWithinBucket(key: String, bucketStart: Long, bucketEnd: Long): Boolean
}
