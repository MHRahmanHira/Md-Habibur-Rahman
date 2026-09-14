package com.example.data.repository

import android.graphics.Bitmap
import com.example.ai.AIImageProvider
import com.example.ai.AIProcessResult
import com.example.ai.ImageStorageManager
import com.example.data.local.PhotoForgeDatabase
import com.example.data.model.CreditTransactionEntity
import com.example.data.model.JobEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectVersionEntity
import com.example.data.model.ToolConfigEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class PhotoForgeRepository(
    private val database: PhotoForgeDatabase,
    private val aiProvider: AIImageProvider,
    private val storageManager: ImageStorageManager
) {
    val currentUser: Flow<UserEntity?> = database.userDao().getCurrentUserFlow()
    val allProjects: Flow<List<ProjectEntity>> = database.projectDao().getAllProjects()
    val allTools: Flow<List<ToolConfigEntity>> = database.toolDao().getEnabledTools()
    val allToolsAdmin: Flow<List<ToolConfigEntity>> = database.toolDao().getAllTools()
    val creditTransactions: Flow<List<CreditTransactionEntity>> = database.creditTransactionDao().getAllTransactions()
    val recentJobs: Flow<List<JobEntity>> = database.jobDao().getRecentJobs()

    fun getProjectVersions(projectId: String): Flow<List<ProjectVersionEntity>> {
        return database.projectVersionDao().getVersionsForProject(projectId)
    }

    suspend fun executeEditJob(
        sourceBitmap: Bitmap,
        toolConfig: ToolConfigEntity,
        customPrompt: String,
        aspectRatio: String,
        resolution: String,
        existingProjectId: String? = null,
        onProgress: (String) -> Unit
    ): Result<Pair<ProjectEntity, AIProcessResult>> = withContext(Dispatchers.IO) {
        val user = database.userDao().getCurrentUserSync()
            ?: UserEntity(uid = "usr_default_guest", creditsBalance = 15).also {
                database.userDao().insertOrUpdate(it)
            }

        // Credit check
        val requiredCredits = toolConfig.creditCost
        if (user.creditsBalance < requiredCredits) {
            return@withContext Result.failure(
                IllegalStateException("অপর্যাপ্ত ক্রেডিট! আপনার অ্যাকাউন্টে ন্যূনতম $requiredCredits ক্রেডিট প্রয়োজন। (Insufficient credits)")
            )
        }

        val jobId = "job_${UUID.randomUUID()}"
        val idempotencyKey = "tx_${jobId}"

        // Step 1: Reserve credit idempotently & log job
        val pendingJob = JobEntity(
            jobId = jobId,
            toolId = toolConfig.toolId,
            status = "PROCESSING",
            startedAt = System.currentTimeMillis(),
            creditCost = requiredCredits
        )
        database.jobDao().insertJob(pendingJob)

        // Deduct temporarily
        val newBalance = user.creditsBalance - requiredCredits
        database.userDao().updateCredits(user.uid, newBalance)
        database.creditTransactionDao().insertTransaction(
            CreditTransactionEntity(
                transactionId = "tx_hold_${UUID.randomUUID()}",
                type = "CREDIT_CONSUME",
                amount = -requiredCredits,
                toolId = toolConfig.toolId,
                idempotencyKey = idempotencyKey,
                note = "${toolConfig.nameBn} ব্যবহারের জন্য ক্রেডিট কর্তন"
            )
        )

        // Step 2: Execute AI Provider
        val aiResult = aiProvider.processImage(
            originalBitmap = sourceBitmap,
            customPrompt = customPrompt,
            toolConfig = toolConfig,
            aspectRatio = aspectRatio,
            resolution = resolution,
            onProgress = onProgress
        )

        if (aiResult.isFailure) {
            // Automatic refund on failure (Rule 11/17)
            val refundBalance = newBalance + requiredCredits
            database.userDao().updateCredits(user.uid, refundBalance)
            database.creditTransactionDao().insertTransaction(
                CreditTransactionEntity(
                    transactionId = "tx_refund_${UUID.randomUUID()}",
                    type = "CREDIT_REFUND",
                    amount = requiredCredits,
                    toolId = toolConfig.toolId,
                    idempotencyKey = "refund_${idempotencyKey}",
                    note = "প্রসেসিং ব্যর্থ হওয়ায় স্বয়ংক্রিয় রিফান্ড (${requiredCredits} ক্রেডিট)"
                )
            )
            database.jobDao().updateJob(
                pendingJob.copy(
                    status = "FAILED",
                    completedAt = System.currentTimeMillis(),
                    errorMessage = aiResult.exceptionOrNull()?.localizedMessage ?: "Processing error"
                )
            )
            return@withContext Result.failure(
                aiResult.exceptionOrNull() ?: Exception("ছবিটি প্রসেস করা যায়নি। আপনার ক্রেডিট কাটা হয়নি।")
            )
        }

        val processSuccess = aiResult.getOrThrow()

        // Step 3: Save images and project version
        val originalPath = storageManager.saveBitmap(sourceBitmap, "orig")
        val resultPath = storageManager.saveBitmap(processSuccess.bitmap, "result")

        val projectId = existingProjectId ?: "proj_${UUID.randomUUID()}"
        val existingProject = if (existingProjectId != null) database.projectDao().getProjectById(existingProjectId) else null
        val nextVersionNumber = (existingProject?.versionCount ?: 0) + 1

        val project = ProjectEntity(
            id = projectId,
            title = existingProject?.title ?: "${toolConfig.nameEn} Project",
            toolId = toolConfig.toolId,
            originalImagePath = existingProject?.originalImagePath ?: originalPath,
            latestImagePath = resultPath,
            promptUsed = customPrompt.ifBlank { toolConfig.promptTemplate },
            versionCount = nextVersionNumber,
            isFavorite = existingProject?.isFavorite ?: false,
            createdAt = existingProject?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        database.projectDao().insertOrUpdate(project)

        val version = ProjectVersionEntity(
            projectId = projectId,
            versionNumber = nextVersionNumber,
            imagePath = resultPath,
            prompt = customPrompt.ifBlank { toolConfig.promptTemplate },
            toolId = toolConfig.toolId,
            resolution = resolution,
            creditCost = requiredCredits,
            timestamp = System.currentTimeMillis()
        )
        database.projectVersionDao().insertVersion(version)

        // Mark job as complete
        database.jobDao().updateJob(
            pendingJob.copy(
                status = "COMPLETED",
                completedAt = System.currentTimeMillis()
            )
        )

        Result.success(Pair(project, processSuccess))
    }

    suspend fun rechargeCredits(amount: Int, paymentProvider: String) = withContext(Dispatchers.IO) {
        val user = database.userDao().getCurrentUserSync() ?: return@withContext
        val newBalance = user.creditsBalance + amount
        database.userDao().updateCredits(user.uid, newBalance)
        database.creditTransactionDao().insertTransaction(
            CreditTransactionEntity(
                transactionId = "tx_recharge_${UUID.randomUUID()}",
                type = "RECHARGE",
                amount = amount,
                idempotencyKey = "recharge_${UUID.randomUUID()}",
                note = "$paymentProvider এর মাধ্যমে $amount ক্রেডিট রিচার্জ সম্পন্ন"
            )
        )
    }

    suspend fun upgradePlan(planName: String, bonusCredits: Int) = withContext(Dispatchers.IO) {
        val user = database.userDao().getCurrentUserSync() ?: return@withContext
        database.userDao().updatePlan(user.uid, planName, bonusCredits)
        database.creditTransactionDao().insertTransaction(
            CreditTransactionEntity(
                transactionId = "tx_plan_${UUID.randomUUID()}",
                type = "PLAN_GRANT",
                amount = bonusCredits,
                idempotencyKey = "plan_${UUID.randomUUID()}",
                note = "$planName প্ল্যান অ্যাক্টিভেশন এবং $bonusCredits বোনাস ক্রেডিট"
            )
        )
    }

    suspend fun toggleAdminRole() = withContext(Dispatchers.IO) {
        val user = database.userDao().getCurrentUserSync() ?: return@withContext
        val newRole = if (user.role == "ADMIN") "USER" else "ADMIN"
        database.userDao().updateRole(user.uid, newRole)
    }

    suspend fun adminAdjustCredits(targetUid: String, delta: Int, auditNote: String) = withContext(Dispatchers.IO) {
        val user = database.userDao().getCurrentUserSync() ?: return@withContext
        val newBalance = maxOf(0, user.creditsBalance + delta)
        database.userDao().updateCredits(targetUid, newBalance)
        database.creditTransactionDao().insertTransaction(
            CreditTransactionEntity(
                transactionId = "tx_admin_${UUID.randomUUID()}",
                type = "ADMIN_ADJUST",
                amount = delta,
                idempotencyKey = "admin_${UUID.randomUUID()}",
                note = "Admin Audit: $auditNote"
            )
        )
    }

    suspend fun adminUpdateToolCost(toolId: String, cost: Int) = withContext(Dispatchers.IO) {
        database.toolDao().updateToolCost(toolId, cost)
    }

    suspend fun adminToggleToolStatus(toolId: String, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        database.toolDao().updateToolStatus(toolId, isEnabled)
    }

    suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        database.projectVersionDao().deleteVersionsForProject(projectId)
        database.projectDao().deleteProject(projectId)
    }

    suspend fun loginAs(name: String, email: String) = withContext(Dispatchers.IO) {
        val newUser = UserEntity(
            uid = "usr_${name.lowercase().replace(" ", "_")}",
            name = name,
            email = email,
            plan = "FREE",
            creditsBalance = 20,
            role = if (email.contains("admin")) "ADMIN" else "USER"
        )
        database.userDao().insertOrUpdate(newUser)
    }
}
