package com.example.ui.locale

object Strings {
    // Navigation
    fun home(isBn: Boolean) = if (isBn) "হোম" else "Home"
    fun tools(isBn: Boolean) = if (isBn) "টুলস" else "Tools"
    fun projects(isBn: Boolean) = if (isBn) "প্রজেক্ট" else "Projects"
    fun pricing(isBn: Boolean) = if (isBn) "প্ল্যান ও ক্রেডিট" else "Plans & Credits"
    fun profile(isBn: Boolean) = if (isBn) "প্রোফাইল" else "Profile"
    fun admin(isBn: Boolean) = if (isBn) "এডমিন প্যানেল" else "Admin Panel"

    // Home screen
    fun appTagline(isBn: Boolean) = if (isBn) "প্রো এআই ফটো স্টুডিও" else "Pro AI Photo Studio"
    fun uploadPhoto(isBn: Boolean) = if (isBn) "ছবি আপলোড করুন" else "Upload Photo"
    fun takePhoto(isBn: Boolean) = if (isBn) "ক্যামেরা" else "Camera"
    fun samplePhoto(isBn: Boolean) = if (isBn) "স্যাম্পল ছবি ব্যবহার করুন" else "Use Sample Portrait"
    fun galleryPhoto(isBn: Boolean) = if (isBn) "গ্যালারি থেকে নিন" else "Choose from Gallery"
    fun quickTools(isBn: Boolean) = if (isBn) "জনপ্রিয় এআই টুলস" else "Popular AI Tools"
    fun recentProjects(isBn: Boolean) = if (isBn) "সাম্প্রতিক এডিটস" else "Recent Edits"
    fun creditsLabel(isBn: Boolean) = if (isBn) "ক্রেডিট" else "Credits"
    fun freeBadge(isBn: Boolean) = if (isBn) "ফ্রি" else "FREE"
    fun proBadge(isBn: Boolean) = if (isBn) "প্রো" else "PRO"

    // Editor screen
    fun beforeLabel(isBn: Boolean) = if (isBn) "আসল ছবি" else "Original"
    fun afterLabel(isBn: Boolean) = if (isBn) "এআই আউটপুট" else "AI Result"
    fun promptHint(isBn: Boolean) = if (isBn) "কাস্টম নির্দেশনা (যেমন: ফেস ঠিক রেখে স্টুডিও লাইটিং দিন)..." else "Custom prompt (e.g. Preserve face, studio lighting)..."
    fun quickPromptsTitle(isBn: Boolean) = if (isBn) "স্মার্ট প্রম্পট সহায়ক" else "Smart Prompt Suggestions"
    fun generateButton(isBn: Boolean, cost: Int) = if (isBn) "এআই দিয়ে রূপান্তর করুন ($cost ক্রেডিট)" else "Transform with AI ($cost Credits)"
    fun saveVersion(isBn: Boolean) = if (isBn) "নতুন ভার্সন সেভ" else "Save Version"
    fun download(isBn: Boolean) = if (isBn) "গ্যালারিতে সেভ" else "Download to Gallery"
    fun share(isBn: Boolean) = if (isBn) "শেয়ার করুন" else "Share"
    fun processingTitle(isBn: Boolean) = if (isBn) "এআই প্রসেসিং চলছে" else "AI Processing"
    fun cancel(isBn: Boolean) = if (isBn) "বাতিল" else "Cancel"

    // Categories
    fun catAll(isBn: Boolean) = if (isBn) "সকল" else "All"
    fun catPortrait(isBn: Boolean) = if (isBn) "পোর্ট্রেট" else "Portrait"
    fun catEnhance(isBn: Boolean) = if (isBn) "এনহ্যান্স" else "Enhance"
    fun catBackground(isBn: Boolean) = if (isBn) "ব্যাকগ্রাউন্ড" else "Background"
    fun catRestore(isBn: Boolean) = if (isBn) "স্মৃতি পুনরুদ্ধার" else "Restoration"
    fun catProduct(isBn: Boolean) = if (isBn) "প্রোডাক্ট" else "Product"
    fun catCreative(isBn: Boolean) = if (isBn) "ক্রিয়েটিভ" else "Creative"

    // Pricing & Plans
    fun plansTitle(isBn: Boolean) = if (isBn) "প্রিমিয়াম প্যাকেজ ও ক্রেডিট রিচার্জ" else "Premium Packages & Credit Top-up"
    fun freePlanTitle(isBn: Boolean) = if (isBn) "স্ট্যান্ডার্ড ফ্রি" else "Standard Free"
    fun proMonthlyTitle(isBn: Boolean) = if (isBn) "প্রো মান্থলি" else "Pro Monthly"
    fun proYearlyTitle(isBn: Boolean) = if (isBn) "প্রো ইয়ারলি" else "Pro Yearly"
    fun freePlanDesc(isBn: Boolean) = if (isBn) "দৈনিক ফ্রি ক্রেডিট • স্ট্যান্ডার্ড কোয়ালিটি • বেসিক টুলস" else "Daily Free Credits • Standard Quality • Basic Tools"
    fun proMonthlyDesc(isBn: Boolean) = if (isBn) "৳৩৯৯/মাস • ২০০ মাসিক ক্রেডিট • আল্ট্রা ২K রেজোলিউশন • নো ওয়াটারমার্ক" else "৳399/mo • 200 Monthly Credits • Ultra 2K Quality • No Watermark"
    fun proYearlyDesc(isBn: Boolean) = if (isBn) "৳২,৯৯৯/বছর • আনলিমিটেড প্রায়রিটি • ৪K মোড • ফুল কমার্শিয়াল লাইসেন্স" else "৳2,999/yr • Priority Processing • 4K Mode • Full Commercial License"
    fun rechargeTitle(isBn: Boolean) = if (isBn) "তাৎক্ষণিক ক্রেডিট রিচার্জ (bKash / Nagad / Card)" else "Instant Credit Recharge (bKash / Nagad / Card)"
}
