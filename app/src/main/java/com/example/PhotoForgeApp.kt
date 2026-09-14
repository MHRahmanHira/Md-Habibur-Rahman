package com.example

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ai.GeminiAIProvider
import com.example.ai.ImageStorageManager
import com.example.data.local.PhotoForgeDatabase
import com.example.data.repository.PhotoForgeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PhotoForgeApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { PhotoForgeDatabase.getDatabase(this, applicationScope) }
    val aiProvider by lazy { GeminiAIProvider() }
    val storageManager by lazy { ImageStorageManager(this) }
    val repository by lazy { PhotoForgeRepository(database, aiProvider, storageManager) }

    // App language: true for Bangla (default), false for English
    var isBangla by mutableStateOf(true)

    override fun onCreate() {
        super.onCreate()
        // Ensure default data is seeded if database exists
        applicationScope.launch(Dispatchers.IO) {
            val user = database.userDao().getCurrentUserSync()
            if (user == null) {
                PhotoForgeDatabase.populateInitialData(database)
            }
        }
    }
}
