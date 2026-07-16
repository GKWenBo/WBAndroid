package com.wb.example.demo_04.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// @Database 标注 + 抽象类 = iOS 的 NSPersistentContainer / Core Data 栈。
// Room 在编译期通过 KSP 生成实现，开发者只写抽象方法。
@Database(entities = [CharacterEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "demo_04_db")
                // 教学简化：schema 变更时直接销毁重建，避免写 Migration。
                // 生产请用 addMigrations(...) 或 exportedSchema 维护迁移脚本。
                .fallbackToDestructiveMigration()
                .build()
    }
}
