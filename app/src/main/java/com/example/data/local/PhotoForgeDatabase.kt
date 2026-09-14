package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CreditTransactionEntity
import com.example.data.model.JobEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectVersionEntity
import com.example.data.model.ToolConfigEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ProjectEntity::class,
        ProjectVersionEntity::class,
        CreditTransactionEntity::class,
        ToolConfigEntity::class,
        JobEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PhotoForgeDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao
    abstract fun projectVersionDao(): ProjectVersionDao
    abstract fun creditTransactionDao(): CreditTransactionDao
    abstract fun toolDao(): ToolDao
    abstract fun jobDao(): JobDao

    companion object {
        @Volatile
        private var INSTANCE: PhotoForgeDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PhotoForgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhotoForgeDatabase::class.java,
                    "photoforge_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: PhotoForgeDatabase) {
            val userDao = database.userDao()
            val toolDao = database.toolDao()
            val transactionDao = database.creditTransactionDao()

            // Initialize default user with welcome credits
            val defaultUser = UserEntity(
                uid = "usr_default_guest",
                name = "Guest Creator",
                email = "creator@photoforge.ai",
                plan = "FREE",
                creditsBalance = 15,
                role = "USER",
                createdAt = System.currentTimeMillis()
            )
            userDao.insertOrUpdate(defaultUser)

            transactionDao.insertTransaction(
                CreditTransactionEntity(
                    transactionId = "tx_welcome_bonus",
                    type = "PLAN_GRANT",
                    amount = 15,
                    idempotencyKey = "key_init_welcome",
                    note = "Welcome free credits (১৫টি ফ্রি ক্রেডিট)"
                )
            )

            // Seed master tools
            val tools = listOf(
                ToolConfigEntity(
                    toolId = "ai_enhance",
                    nameBn = "এআই এনহ্যান্স ও ডিটেইল",
                    nameEn = "AI Enhance & Sharpen",
                    descriptionBn = "অস্পষ্ট ছবি পরিষ্কার, ডিটেইল রিকভারি এবং লাইটিং সংশোধন",
                    descriptionEn = "Sharpen, deblur, recover dynamic range, and exposure correction",
                    category = "ENHANCE",
                    creditCost = 1,
                    isPro = false,
                    iconName = "AutoAwesome",
                    promptTemplate = "Enhance the image clarity, sharpen edges, remove digital noise and optimize exposure naturally while preserving exact original identity.",
                    negativeConstraint = "Do not alter facial features or introduce artificial halos, plastic smoothing or unnatural artifacts."
                ),
                ToolConfigEntity(
                    toolId = "portrait_studio",
                    nameBn = "প্রফেশনাল হেডশট ও পোর্ট্রেট",
                    nameEn = "Studio Headshot & Portrait",
                    descriptionBn = "সিভি, লিংকডইন ও প্রফেশনাল ব্যবহারের জন্য প্রিমিয়াম স্টুডিও লুক",
                    descriptionEn = "Transform into studio portrait with natural skin texture and cinematic lighting",
                    category = "PORTRAIT",
                    creditCost = 2,
                    isPro = false,
                    iconName = "Face",
                    promptTemplate = "Transform into a high-end corporate studio portrait with balanced key lighting and professional depth of field, strictly preserving exact facial identity, skin tone and natural expression.",
                    negativeConstraint = "Avoid plastic skin, changing facial geometry, unnatural teeth or altering identity."
                ),
                ToolConfigEntity(
                    toolId = "bg_remover",
                    nameBn = "ব্যাকগ্রাউন্ড রিমুভার",
                    nameEn = "Background Remover",
                    descriptionBn = "১ ক্লিকে স্বচ্ছ ব্যাকগ্রাউন্ড অথবা সলিড স্টুডিও ব্যাকগ্রাউন্ড",
                    descriptionEn = "Clean edge-accurate subject isolation and background separation",
                    category = "BACKGROUND",
                    creditCost = 1,
                    isPro = false,
                    iconName = "CropFree",
                    promptTemplate = "Isolate the main subject with pixel-perfect hair and silhouette boundary, transparent clean background.",
                    negativeConstraint = "Do not crop out main subject limbs or distort edges."
                ),
                ToolConfigEntity(
                    toolId = "bg_replace",
                    nameBn = "ব্যাকগ্রাউন্ড পরিবর্তন",
                    nameEn = "Background Replace",
                    descriptionBn = "অফিস, প্রকৃতি, ক্যাফে বা কাস্টম স্টুডিও ব্যাকগ্রাউন্ড যোগ করুন",
                    descriptionEn = "Place subject into scenic outdoor, modern luxury office or studio",
                    category = "BACKGROUND",
                    creditCost = 2,
                    isPro = false,
                    iconName = "Wallpaper",
                    promptTemplate = "Replace the background with an elegant aesthetic setting, matching perspective and environmental shadows naturally on the subject.",
                    negativeConstraint = "Do not change subject posture, lighting mismatch or create floating edges."
                ),
                ToolConfigEntity(
                    toolId = "old_photo_restore",
                    nameBn = "পুরনো ছবি পুনরুদ্ধার",
                    nameEn = "Old Photo Restoration",
                    descriptionBn = "ছেঁড়া, দাগযুক্ত ও ঝাপসা পারিবারিক স্মৃতি পুনরুদ্ধার করুন",
                    descriptionEn = "Remove dust, scratches, tears and restore faded vintage photographs",
                    category = "RESTORE",
                    creditCost = 2,
                    isPro = false,
                    iconName = "HistoryEdu",
                    promptTemplate = "Restore faded vintage photograph, repair paper scratches, remove dust and restore facial detail faithfully.",
                    negativeConstraint = "Avoid over-smoothing, maintain authentic vintage grain without distortions."
                ),
                ToolConfigEntity(
                    toolId = "colorize_bw",
                    nameBn = "সাদাকালো ছবি রঙিন করুন",
                    nameEn = "Colorize Black & White",
                    descriptionBn = "ঐতিহাসিক ও ভিন্টেজ ছবিতে প্রাণবন্ত বাস্তবসম্মত রঙ আনুন",
                    descriptionEn = "Add historically accurate and realistic natural colors to B&W photos",
                    category = "RESTORE",
                    creditCost = 2,
                    isPro = false,
                    iconName = "ColorLens",
                    promptTemplate = "Colorize black and white photograph with lifelike skin tones, authentic clothing colors and natural atmospheric shades.",
                    negativeConstraint = "Avoid oversaturated neon tones, color bleeding or blotchy patches."
                ),
                ToolConfigEntity(
                    toolId = "product_ecommerce",
                    nameBn = "প্রোডাক্ট ফটো স্টুডিও",
                    nameEn = "E-Commerce Product Studio",
                    descriptionBn = "ফেসবুক ও অনলাইন শপের জন্য প্রফেশনাল ক্যাটালগ ফটো",
                    descriptionEn = "White studio background, realistic soft contact shadows and clean product highlights",
                    category = "PRODUCT",
                    creditCost = 2,
                    isPro = true,
                    iconName = "ShoppingBag",
                    promptTemplate = "Present product in premium commercial catalog studio lighting with soft contact ground shadows and pure minimal presentation.",
                    negativeConstraint = "Do not alter product branding, logo or physical proportion."
                ),
                ToolConfigEntity(
                    toolId = "passport_id_photo",
                    nameBn = "পাসপোর্ট ও আইডি সাইজ ফটো",
                    nameEn = "Passport & ID Photo Maker",
                    descriptionBn = "অফিসিয়াল ডকুমেন্ট, ভিসা ও আবেদনের জন্য নির্দিষ্ট ফরম্যাট",
                    descriptionEn = "Formal portrait with standard blue/white background and balanced lighting",
                    category = "PORTRAIT",
                    creditCost = 1,
                    isPro = false,
                    iconName = "Badge",
                    promptTemplate = "Format as official passport photo with neutral balanced lighting, sharp eyes and clean formal backdrop.",
                    negativeConstraint = "No tilted head, no harsh shadows, preserve natural facial dimensions."
                ),
                ToolConfigEntity(
                    toolId = "relight_golden_hour",
                    nameBn = "গোল্ডেন আওয়ার ও রি-লাইট",
                    nameEn = "Golden Hour & Relight",
                    descriptionBn = "সূর্যাস্তের মিষ্টি সোনালী আভা ও সিনেমাটিক লাইটিং",
                    descriptionEn = "Warm golden hour sunlight, dynamic directional lighting and rim highlights",
                    category = "CREATIVE",
                    creditCost = 1,
                    isPro = false,
                    iconName = "WbSunny",
                    promptTemplate = "Apply warm golden hour sunlight with soft ambient backlight and natural warm reflections on skin and hair.",
                    negativeConstraint = "Avoid blown out white highlights, artificial yellow casts or washed out shadows."
                ),
                ToolConfigEntity(
                    toolId = "anime_illustration",
                    nameBn = "এনিমে ও আর্ট স্টাইল",
                    nameEn = "Anime & Art Illustration",
                    descriptionBn = "ছবিকে আকর্ষণীয় এনিমেটেড বা চিত্রশিল্পের রূপ দিন",
                    descriptionEn = "Stylize into aesthetic modern anime or digital painting while maintaining recognizable features",
                    category = "CREATIVE",
                    creditCost = 2,
                    isPro = true,
                    iconName = "Palette",
                    promptTemplate = "Artistic modern digital anime illustration inspired by vibrant high-end animation aesthetics, retaining recognizable likeness.",
                    negativeConstraint = "Avoid grotesque distortions, disfigured limbs or unrecognizable likeness."
                ),
                ToolConfigEntity(
                    toolId = "vintage_film_look",
                    nameBn = "ভিন্টেজ ফিল্ম ও রেট্রো",
                    nameEn = "Vintage 35mm Film",
                    descriptionBn = "ক্লাসিক ৩৫ মিমি অ্যানালগ ক্যামেরা ও নস্টালজিক টোন",
                    descriptionEn = "Classic 35mm film grain, warm halation and nostalgic color grading",
                    category = "CREATIVE",
                    creditCost = 1,
                    isPro = false,
                    iconName = "CameraRoll",
                    promptTemplate = "Film photograph shot on 35mm Kodak Portra with subtle natural grain, rich organic tones and cinematic softness.",
                    negativeConstraint = "No digital noise artifacts, no harsh banding."
                ),
                ToolConfigEntity(
                    toolId = "ai_upscale_4k",
                    nameBn = "এআই আল্ট্রা আপস্কেল (২K/৪K)",
                    nameEn = "AI Ultra Upscale (2K/4K)",
                    descriptionBn = "প্রিন্টিং ও বড় ডিসপ্লের জন্য ছবির রেজোলিউশন বৃদ্ধি",
                    descriptionEn = "Super-resolution detail synthesis for high quality prints and high-res displays",
                    category = "ENHANCE",
                    creditCost = 2,
                    isPro = true,
                    iconName = "Hd",
                    promptTemplate = "Super-resolution AI upscale with micro-texture synthesis for skin, fabric and environment.",
                    negativeConstraint = "Avoid cartoonish sharpness or geometric pixelation."
                )
            )

            toolDao.insertTools(tools)
        }
    }
}
