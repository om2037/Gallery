package com.dark.cloud_gallery.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dark.cloud_gallery.domain.model.MediaItem

@Database(entities = [MediaItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaItemDao(): MediaItemDao
}
