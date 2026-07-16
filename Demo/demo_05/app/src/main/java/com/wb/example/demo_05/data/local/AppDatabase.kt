package com.wb.example.demo_05.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CachedUserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cachedUserDao(): CachedUserDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "demo_05_db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
