package com.example.vtiu.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.vtiu.data.local.room.entity.ProfileEntity

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE userId = :userId")
    suspend fun getProfile(userId: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: ProfileEntity)
}
