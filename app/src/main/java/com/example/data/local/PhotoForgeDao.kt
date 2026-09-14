package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CreditTransactionEntity
import com.example.data.model.JobEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectVersionEntity
import com.example.data.model.ToolConfigEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun getUser(uid: String): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUserSync(): UserEntity?

    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserEntity)

    @Query("UPDATE users SET creditsBalance = :newBalance WHERE uid = :uid")
    suspend fun updateCredits(uid: String, newBalance: Int)

    @Query("UPDATE users SET plan = :plan, creditsBalance = creditsBalance + :creditsBonus WHERE uid = :uid")
    suspend fun updatePlan(uid: String, plan: String, creditsBonus: Int)

    @Query("UPDATE users SET role = :role WHERE uid = :uid")
    suspend fun updateRole(uid: String, role: String)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    fun getProjectFlow(id: String): Flow<ProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: String)
}

@Dao
interface ProjectVersionDao {
    @Query("SELECT * FROM project_versions WHERE projectId = :projectId ORDER BY versionNumber ASC")
    fun getVersionsForProject(projectId: String): Flow<List<ProjectVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: ProjectVersionEntity): Long

    @Query("DELETE FROM project_versions WHERE projectId = :projectId")
    suspend fun deleteVersionsForProject(projectId: String)
}

@Dao
interface CreditTransactionDao {
    @Query("SELECT * FROM credit_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<CreditTransactionEntity>>

    @Query("SELECT * FROM credit_transactions WHERE idempotencyKey = :key LIMIT 1")
    suspend fun getTransactionByIdempotencyKey(key: String): CreditTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(transaction: CreditTransactionEntity): Long
}

@Dao
interface ToolDao {
    @Query("SELECT * FROM tool_configs WHERE isEnabled = 1")
    fun getEnabledTools(): Flow<List<ToolConfigEntity>>

    @Query("SELECT * FROM tool_configs")
    fun getAllTools(): Flow<List<ToolConfigEntity>>

    @Query("SELECT * FROM tool_configs WHERE toolId = :toolId LIMIT 1")
    suspend fun getToolById(toolId: String): ToolConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTools(tools: List<ToolConfigEntity>)

    @Update
    suspend fun updateTool(tool: ToolConfigEntity)

    @Query("UPDATE tool_configs SET creditCost = :cost WHERE toolId = :toolId")
    suspend fun updateToolCost(toolId: String, cost: Int)

    @Query("UPDATE tool_configs SET isEnabled = :isEnabled WHERE toolId = :toolId")
    suspend fun updateToolStatus(toolId: String, isEnabled: Boolean)
}

@Dao
interface JobDao {
    @Query("SELECT * FROM generation_jobs ORDER BY startedAt DESC LIMIT 20")
    fun getRecentJobs(): Flow<List<JobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity)

    @Update
    suspend fun updateJob(job: JobEntity)
}
