package com.example

import android.app.Application
import com.example.omarchy.data.OmarchyDatabase
import com.example.omarchy.data.OmarchyWorkspaceRepository

class OmarchyApplication : Application() {

  val database: OmarchyDatabase by lazy {
    OmarchyDatabase.getInstance(this)
  }

  val repository: OmarchyWorkspaceRepository by lazy {
    OmarchyWorkspaceRepository(database.workspaceDao())
  }

  override fun onCreate() {
    super.onCreate()
    instance = this
  }

  companion object {
    lateinit var instance: OmarchyApplication
      private set
  }
}
