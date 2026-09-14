package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String = "usr_default_guest",
    val name: String = "Guest User",
    val email: String = "guest@photoforge.ai",
    val plan: String = "FREE", // FREE, PRO_MONTHLY, PRO_YEARLY
    val creditsBalance: Int = 15,
    val role: String = "USER", // USER, ADMIN
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val toolId: String,
    val originalImagePath: String,
    val latestImagePath: String,
    val promptUsed: String,
    val versionCount: Int = 1,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "project_versions")
data class ProjectVersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: String,
    val versionNumber: Int,
    val imagePath: String,
    val prompt: String,
    val toolId: String,
    val resolution: String = "1K",
    val creditCost: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "credit_transactions")
data class CreditTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: String,
    val type: String, // CREDIT_CONSUME, CREDIT_REFUND, PLAN_GRANT, ADMIN_ADJUST, RECHARGE
    val amount: Int, // positive for grants/refunds, negative for consumes
    val toolId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val idempotencyKey: String,
    val note: String
)

@Entity(tableName = "tool_configs")
data class ToolConfigEntity(
    @PrimaryKey val toolId: String,
    val nameBn: String,
    val nameEn: String,
    val descriptionBn: String,
    val descriptionEn: String,
    val category: String, // PORTRAIT, ENHANCE, BACKGROUND, RESTORE, PRODUCT, CREATIVE, SOCIAL
    val creditCost: Int = 1,
    val isPro: Boolean = false,
    val iconName: String,
    val promptTemplate: String,
    val negativeConstraint: String,
    val isEnabled: Boolean = true
)

@Entity(tableName = "generation_jobs")
data class JobEntity(
    @PrimaryKey val jobId: String,
    val toolId: String,
    val status: String, // PENDING, PROCESSING, COMPLETED, FAILED
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val creditCost: Int,
    val errorMessage: String? = null
)
