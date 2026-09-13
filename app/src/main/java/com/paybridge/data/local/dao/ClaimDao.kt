package com.paybridge.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.paybridge.data.local.entity.ClaimEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClaimDao {

    @Insert
    suspend fun insert(claim: ClaimEntity): Long

    @Update
    suspend fun update(claim: ClaimEntity)

    @Query("SELECT * FROM claims WHERE id = :id")
    suspend fun getById(id: Long): ClaimEntity?

    @Query("SELECT * FROM claims WHERE id = :id")
    fun observeById(id: Long): Flow<ClaimEntity?>

    @Query("SELECT * FROM claims ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ClaimEntity>>

    @Query("SELECT * FROM claims WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingOldestFirst(): List<ClaimEntity>

    /**
     * Candidates for an exact-amount match: pending, amount matches exactly, and either the
     * claim is provider-less or its provider matches. Ordered oldest-first so callers can just
     * take the head of the list to implement "match the oldest pending claim" without a second
     * sort step.
     */
    @Query(
        """
        SELECT * FROM claims
        WHERE status = 'PENDING'
          AND expectedAmount = :amount
          AND (expectedProvider IS NULL OR expectedProvider = :provider)
          AND :receivedAt BETWEEN createdAt AND timeoutAt
        ORDER BY createdAt ASC
        """
    )
    suspend fun findExactMatchCandidates(amount: Long, provider: String, receivedAt: Long): List<ClaimEntity>

    /**
     * Broadened, amount-agnostic lookup used only for mismatch detection once the exact-amount
     * query above returns nothing — see MatchingEngine for why this is scoped to "exactly one
     * candidate" before it's treated as a mismatch.
     */
    @Query(
        """
        SELECT * FROM claims
        WHERE status = 'PENDING'
          AND (expectedProvider IS NULL OR expectedProvider = :provider)
          AND :receivedAt BETWEEN createdAt AND timeoutAt
        ORDER BY createdAt ASC
        """
    )
    suspend fun findProviderScopedPending(provider: String, receivedAt: Long): List<ClaimEntity>

    @Query("SELECT * FROM claims WHERE status = 'PENDING' AND timeoutAt <= :cutoff")
    suspend fun getPendingPastTimeout(cutoff: Long): List<ClaimEntity>
}
