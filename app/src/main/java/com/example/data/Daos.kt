package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}

@Dao
interface JobNotificationDao {
    @Query("SELECT * FROM job_notifications ORDER BY applicationEndDateEpoch ASC")
    fun getAllNotifications(): Flow<List<JobNotification>>

    @Query("SELECT * FROM job_notifications WHERE isApprovedByAdmin = 1 ORDER BY applicationEndDateEpoch ASC")
    fun getApprovedNotifications(): Flow<List<JobNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: JobNotification): Long

    @Update
    suspend fun updateNotification(notification: JobNotification)

    @Query("DELETE FROM job_notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)

    @Query("SELECT * FROM job_notifications WHERE id = :id LIMIT 1")
    suspend fun getNotificationById(id: Long): JobNotification?
}

@Dao
interface TrackedApplicationDao {
    @Query("SELECT * FROM tracked_applications ORDER BY appliedDateEpoch DESC")
    fun getAllTrackedApplications(): Flow<List<TrackedApplication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackedApplication(application: TrackedApplication): Long

    @Update
    suspend fun updateTrackedApplication(application: TrackedApplication)

    @Query("DELETE FROM tracked_applications WHERE id = :id")
    suspend fun deleteTrackedApplication(id: Long)
}
