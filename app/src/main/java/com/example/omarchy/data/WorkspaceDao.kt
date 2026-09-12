package com.example.omarchy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao {

  @Query("SELECT * FROM workspace_session WHERE id = 1 LIMIT 1")
  fun getWorkspaceSession(): Flow<WorkspaceSessionEntity?>

  @Query("SELECT * FROM workspace_session WHERE id = 1 LIMIT 1")
  suspend fun getWorkspaceSessionSync(): WorkspaceSessionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWorkspaceSession(session: WorkspaceSessionEntity)

  @Query("SELECT * FROM windows ORDER BY orderIndex ASC")
  fun getAllWindows(): Flow<List<WindowEntity>>

  @Query("SELECT * FROM windows ORDER BY orderIndex ASC")
  suspend fun getAllWindowsSync(): List<WindowEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWindows(windows: List<WindowEntity>)

  @Query("DELETE FROM windows")
  suspend fun clearWindows()

  @Query("SELECT * FROM buffers ORDER BY bufferOrder ASC")
  fun getAllBuffers(): Flow<List<BufferEntity>>

  @Query("SELECT * FROM buffers ORDER BY bufferOrder ASC")
  suspend fun getAllBuffersSync(): List<BufferEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBuffers(buffers: List<BufferEntity>)

  @Query("DELETE FROM buffers")
  suspend fun clearBuffers()

  @Query("SELECT * FROM notes ORDER BY noteOrder ASC")
  fun getAllNotes(): Flow<List<NoteEntity>>

  @Query("SELECT * FROM notes ORDER BY noteOrder ASC")
  suspend fun getAllNotesSync(): List<NoteEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotes(notes: List<NoteEntity>)

  @Query("DELETE FROM notes")
  suspend fun clearNotes()

  @Query("SELECT * FROM command_macros ORDER BY createdAt ASC")
  fun getAllMacros(): Flow<List<MacroEntity>>

  @Query("SELECT * FROM command_macros ORDER BY createdAt ASC")
  suspend fun getAllMacrosSync(): List<MacroEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMacros(macros: List<MacroEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMacro(macro: MacroEntity)

  @Query("DELETE FROM command_macros WHERE id = :id")
  suspend fun deleteMacro(id: String)

  @Query("DELETE FROM command_macros")
  suspend fun clearMacros()

  @Query("DELETE FROM workspace_session")
  suspend fun clearWorkspaceSession()

  @Transaction
  suspend fun saveWorkspaceSnapshot(snapshot: WorkspaceSnapshot) {
    insertWorkspaceSession(snapshot.session)
    clearWindows()
    if (snapshot.windows.isNotEmpty()) {
      insertWindows(snapshot.windows)
    }
    clearBuffers()
    if (snapshot.buffers.isNotEmpty()) {
      insertBuffers(snapshot.buffers)
    }
    clearNotes()
    if (snapshot.notes.isNotEmpty()) {
      insertNotes(snapshot.notes)
    }
    if (snapshot.macros.isNotEmpty()) {
      insertMacros(snapshot.macros)
    }
  }

  @Transaction
  suspend fun loadWorkspaceSnapshot(): WorkspaceSnapshot? {
    val session = getWorkspaceSessionSync() ?: return null
    val windows = getAllWindowsSync()
    val buffers = getAllBuffersSync()
    val notes = getAllNotesSync()
    val macros = getAllMacrosSync()
    return WorkspaceSnapshot(session, windows, buffers, notes, macros)
  }

  @Transaction
  suspend fun clearAll() {
    clearWorkspaceSession()
    clearWindows()
    clearBuffers()
    clearNotes()
    clearMacros()
  }
}
