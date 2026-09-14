package com.example.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.ImageStorageManager
import com.example.data.model.CreditTransactionEntity
import com.example.data.model.JobEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectVersionEntity
import com.example.data.model.ToolConfigEntity
import com.example.data.model.UserEntity
import com.example.data.repository.PhotoForgeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PhotoForgeViewModel(
    private val repository: PhotoForgeRepository,
    private val storageManager: ImageStorageManager
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val projects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tools: StateFlow<List<ToolConfigEntity>> = repository.allTools
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allToolsAdmin: StateFlow<List<ToolConfigEntity>> = repository.allToolsAdmin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<CreditTransactionEntity>> = repository.creditTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val jobs: StateFlow<List<JobEntity>> = repository.recentJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Editor State
    private val _sourceBitmap = MutableStateFlow<Bitmap?>(null)
    val sourceBitmap: StateFlow<Bitmap?> = _sourceBitmap.asStateFlow()

    private val _resultBitmap = MutableStateFlow<Bitmap?>(null)
    val resultBitmap: StateFlow<Bitmap?> = _resultBitmap.asStateFlow()

    private val _selectedTool = MutableStateFlow<ToolConfigEntity?>(null)
    val selectedTool: StateFlow<ToolConfigEntity?> = _selectedTool.asStateFlow()

    private val _customPrompt = MutableStateFlow("")
    val customPrompt: StateFlow<String> = _customPrompt.asStateFlow()

    private val _aspectRatio = MutableStateFlow("1:1")
    val aspectRatio: StateFlow<String> = _aspectRatio.asStateFlow()

    private val _resolution = MutableStateFlow("1K")
    val resolution: StateFlow<String> = _resolution.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _processingStage = MutableStateFlow("")
    val processingStage: StateFlow<String> = _processingStage.asStateFlow()

    private val _currentProjectId = MutableStateFlow<String?>(null)
    val currentProjectId: StateFlow<String?> = _currentProjectId.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private var activeJob: Job? = null

    init {
        // Automatically default selected tool when tools become available
        viewModelScope.launch {
            tools.collect { toolList ->
                if (_selectedTool.value == null && toolList.isNotEmpty()) {
                    _selectedTool.value = toolList.first()
                }
            }
        }
    }

    fun setSourceBitmap(bitmap: Bitmap) {
        _sourceBitmap.value = bitmap
        _resultBitmap.value = null
        _currentProjectId.value = null
    }

    fun loadFromUri(uri: Uri) {
        viewModelScope.launch {
            val bmp = storageManager.loadBitmapFromUri(uri)
            if (bmp != null) {
                setSourceBitmap(bmp)
            } else {
                _feedbackMessage.value = "ছবি লোড করা যায়নি (Failed to read image)"
            }
        }
    }

    fun loadSamplePortrait() {
        val bmp = storageManager.createSamplePortraitBitmap()
        setSourceBitmap(bmp)
        _feedbackMessage.value = "স্যাম্পল ছবি লোড করা হয়েছে (Sample loaded)"
    }

    fun selectTool(tool: ToolConfigEntity) {
        _selectedTool.value = tool
    }

    fun setCustomPrompt(prompt: String) {
        _customPrompt.value = prompt
    }

    fun setAspectRatio(ratio: String) {
        _aspectRatio.value = ratio
    }

    fun setResolution(res: String) {
        _resolution.value = res
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun executeGeneration() {
        val source = _sourceBitmap.value
        val tool = _selectedTool.value
        if (source == null) {
            _feedbackMessage.value = "প্রথমে একটি ছবি নির্বাচন বা আপলোড করুন (Please upload a photo first)"
            return
        }
        if (tool == null) {
            _feedbackMessage.value = "একটি এআই টুল নির্বাচন করুন (Please select a tool)"
            return
        }

        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            _isProcessing.value = true
            _processingStage.value = "১/৪: ছবি প্রস্তুতি চলছে..."

            val result = repository.executeEditJob(
                sourceBitmap = source,
                toolConfig = tool,
                customPrompt = _customPrompt.value,
                aspectRatio = _aspectRatio.value,
                resolution = _resolution.value,
                existingProjectId = _currentProjectId.value,
                onProgress = { stage -> _processingStage.value = stage }
            )

            _isProcessing.value = false

            if (result.isSuccess) {
                val (project, aiResult) = result.getOrThrow()
                _resultBitmap.value = aiResult.bitmap
                _currentProjectId.value = project.id
                _feedbackMessage.value = "সফলভাবে সম্পন্ন হয়েছে! (${aiResult.providerUsed})"
            } else {
                _feedbackMessage.value = result.exceptionOrNull()?.message ?: "প্রসেসিং ব্যর্থ হয়েছে। আপনার কোনো ক্রেডিট কাটা হয়নি।"
            }
        }
    }

    fun cancelProcessing() {
        activeJob?.cancel()
        _isProcessing.value = false
        _feedbackMessage.value = "প্রসেসিং বাতিল করা হয়েছে। ক্রেডিট রিফান্ড সম্পন্ন।"
    }

    fun saveToGallery() {
        val resultBmp = _resultBitmap.value ?: _sourceBitmap.value
        if (resultBmp == null) {
            _feedbackMessage.value = "সেভ করার মতো কোনো ছবি নেই (No image to save)"
            return
        }
        viewModelScope.launch {
            val success = storageManager.exportToGallery(resultBmp, "PhotoForge_${System.currentTimeMillis()}")
            _feedbackMessage.value = if (success) {
                "ছবিটি সফলভাবে আপনার গ্যালারিতে সেভ করা হয়েছে! (Saved to gallery)"
            } else {
                "গ্যালারিতে সেভ করা যায়নি (Failed to save)"
            }
        }
    }

    fun loadProject(project: ProjectEntity) {
        viewModelScope.launch {
            val origBmp = storageManager.loadBitmap(project.originalImagePath)
            val resultBmp = storageManager.loadBitmap(project.latestImagePath)
            _sourceBitmap.value = origBmp ?: resultBmp
            _resultBitmap.value = resultBmp
            _currentProjectId.value = project.id
            _customPrompt.value = project.promptUsed
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_currentProjectId.value == projectId) {
                _currentProjectId.value = null
            }
            _feedbackMessage.value = "প্রজেক্ট মুছে ফেলা হয়েছে (Project deleted)"
        }
    }

    fun rechargeCredits(amount: Int, method: String) {
        viewModelScope.launch {
            repository.rechargeCredits(amount, method)
            _feedbackMessage.value = "$amount ক্রেডিট রিচার্জ সফল হয়েছে ($method)!"
        }
    }

    fun upgradePlan(plan: String, bonus: Int) {
        viewModelScope.launch {
            repository.upgradePlan(plan, bonus)
            _feedbackMessage.value = "$plan প্ল্যান অ্যাক্টিভেট করা হয়েছে এবং $bonus ক্রেডিট যোগ হয়েছে!"
        }
    }

    fun toggleAdminMode() {
        viewModelScope.launch {
            repository.toggleAdminRole()
        }
    }

    fun adminAdjustCredits(delta: Int, note: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.adminAdjustCredits(user.uid, delta, note)
            _feedbackMessage.value = "Admin: ক্রেডিট অ্যাডজাস্ট সম্পন্ন ($delta)"
        }
    }

    fun adminUpdateToolCost(toolId: String, cost: Int) {
        viewModelScope.launch {
            repository.adminUpdateToolCost(toolId, cost)
        }
    }

    fun adminToggleTool(toolId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.adminToggleToolStatus(toolId, isEnabled)
        }
    }

    fun getProjectVersions(projectId: String) = repository.getProjectVersions(projectId)

    class Factory(
        private val repository: PhotoForgeRepository,
        private val storageManager: ImageStorageManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PhotoForgeViewModel(repository, storageManager) as T
        }
    }
}
