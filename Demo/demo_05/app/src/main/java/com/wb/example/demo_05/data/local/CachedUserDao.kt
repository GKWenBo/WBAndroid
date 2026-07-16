package com.wb.example.demo_05.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedUserDao {
    @Query("SELECT * FROM cached_users ORDER BY cachedAt DESC")
    fun observeAll(): Flow<List<CachedUserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<CachedUserEntity>)

    @Query("DELETE FROM cached_users")
    suspend fun clear()
}
