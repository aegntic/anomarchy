package com.example.omarchy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    WorkspaceSessionEntity::class,
    WindowEntity::class,
    BufferEntity::class,
    NoteEntity::class,
    MacroEntity::class
  ],
  version = 3,
  exportSchema = false
)
abstract class OmarchyDatabase : RoomDatabase() {

  abstract fun workspaceDao(): WorkspaceDao

  companion object {
    @Volatile
    private var INSTANCE: OmarchyDatabase? = null

    fun getInstance(context: Context): OmarchyDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          OmarchyDatabase::class.java,
          "omarchy_workspace.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }

    fun createInMemory(context: Context): OmarchyDatabase {
      return Room.inMemoryDatabaseBuilder(
        context.applicationContext,
        OmarchyDatabase::class.java
      )
        .allowMainThreadQueries()
        .build()
    }
  }
}
