package com.dark.cloud_gallery.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dark.cloud_gallery.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mediaItem: MediaItem)

    @Update
    suspend fun update(mediaItem: MediaItem)

    @Query("SELECT * FROM media_items WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getAllMediaItems(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE deviceModel = :deviceModel AND isDeleted = 0 ORDER BY timestamp DESC")
    fun getMediaItemsByDevice(deviceModel: String): Flow<List<MediaItem>>

    @Query("SELECT DISTINCT deviceModel FROM media_items WHERE isDeleted = 0")
    fun getDeviceModels(): Flow<List<String>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaItemById(id: Long): MediaItem?
}
