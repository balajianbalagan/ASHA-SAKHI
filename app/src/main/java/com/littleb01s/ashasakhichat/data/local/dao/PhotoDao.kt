package com.littleb01s.ashasakhichat.data.local.dao

import androidx.room.*
import com.littleb01s.ashasakhichat.data.local.entity.Photo
import java.util.Date

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: Photo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<Photo>)

    @Update
    suspend fun update(photo: Photo)

    @Delete
    suspend fun delete(photo: Photo)

    @Query("SELECT * FROM TBL_CHECKUP_PHOTOS WHERE photoId = :photoId")
    suspend fun getPhotoById(photoId: Int): Photo?

    @Query("SELECT * FROM TBL_CHECKUP_PHOTOS WHERE checkupId = :checkupId")
    suspend fun getPhotosByCheckupId(checkupId: Int): List<Photo>

    @Query("SELECT * FROM TBL_CHECKUP_PHOTOS WHERE needsUpload = 1")
    suspend fun getPhotosNeedingUpload(): List<Photo>

    @Query("SELECT * FROM TBL_CHECKUP_PHOTOS WHERE needsDownload = 1")
    suspend fun getPhotosNeedingDownload(): List<Photo>

    @Query("UPDATE TBL_CHECKUP_PHOTOS SET needsUpload = 0, lastUploadedAt = :timestamp WHERE photoId = :photoId")
    suspend fun markAsUploaded(photoId: Int, timestamp: Date = Date())

    @Query("UPDATE TBL_CHECKUP_PHOTOS SET needsDownload = 0, lastDownloadedAt = :timestamp WHERE photoId = :photoId")
    suspend fun markAsDownloaded(photoId: Int, timestamp: Date = Date())
} 