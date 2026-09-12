package com.example.omarchy.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class OmarchyWorkspaceRepository(
  private val dao: WorkspaceDao
) {

  val savedSession: Flow<WorkspaceSessionEntity?> = dao.getWorkspaceSession()
  val savedWindows: Flow<List<WindowEntity>> = dao.getAllWindows()
  val savedBuffers: Flow<List<BufferEntity>> = dao.getAllBuffers()
  val savedNotes: Flow<List<NoteEntity>> = dao.getAllNotes()
  val savedMacros: Flow<List<MacroEntity>> = dao.getAllMacros()

  suspend fun saveWorkspaceSnapshot(snapshot: WorkspaceSnapshot) = withContext(Dispatchers.IO) {
    dao.saveWorkspaceSnapshot(snapshot)
  }

  suspend fun loadWorkspaceSnapshot(): WorkspaceSnapshot? = withContext(Dispatchers.IO) {
    dao.loadWorkspaceSnapshot()
  }

  suspend fun saveMacro(macro: MacroEntity) = withContext(Dispatchers.IO) {
    dao.insertMacro(macro)
  }

  suspend fun saveMacros(macros: List<MacroEntity>) = withContext(Dispatchers.IO) {
    dao.insertMacros(macros)
  }

  suspend fun deleteMacro(id: String) = withContext(Dispatchers.IO) {
    dao.deleteMacro(id)
  }

  suspend fun getMacrosSync(): List<MacroEntity> = withContext(Dispatchers.IO) {
    dao.getAllMacrosSync()
  }

  suspend fun hasSavedSession(): Boolean = withContext(Dispatchers.IO) {
    dao.getWorkspaceSessionSync() != null
  }

  suspend fun clearSavedWorkspace() = withContext(Dispatchers.IO) {
    dao.clearAll()
  }
}
