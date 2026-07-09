package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiService
import com.example.data.*
import com.example.engine.MatchingEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppLanguage {
    ENGLISH, HINDI, HINGLISH
}

class SarkariViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = JobRepository(db)

    // Reactive database streams
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val approvedJobs: StateFlow<List<JobNotification>> = repository.approvedNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allJobs: StateFlow<List<JobNotification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trackedApplications: StateFlow<List<TrackedApplication>> = repository.trackedApplications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Configuration States
    val activeLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val isLowDataMode = MutableStateFlow(false)
    val activeTab = MutableStateFlow(0) // 0: Dashboard/Jobs, 1: Application Tracker, 2: Eligibility Profile, 3: Admin & AI Pipeline

    // Crawler / Update Simulation
    val isCrawling = MutableStateFlow(false)
    val crawlerLogs = MutableStateFlow<List<String>>(emptyList())

    // AI Extraction / Gemini State
    val isParsing = MutableStateFlow(false)
    val parsedNotificationResult = MutableStateFlow<JobNotification?>(null)
    val parsingError = MutableStateFlow<String?>(null)

    // User Feedback / Notification Banner
    val appBannerMessage = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            // Initial data pre-population
            repository.prepopulateIfNeeded()
        }
    }

    fun showBanner(message: String) {
        appBannerMessage.value = message
        viewModelScope.launch {
            kotlinx.coroutines.delay(3500)
            if (appBannerMessage.value == message) {
                appBannerMessage.value = null
            }
        }
    }

    // --- Profile Operations ---
    fun updateUserProfile(
        name: String,
        education: String,
        branch: String,
        dobEpoch: Long,
        category: String,
        height: Int,
        state: String,
        sector: String
    ) {
        viewModelScope.launch {
            val updated = UserProfile(
                id = 1,
                name = name,
                educationLevel = education,
                degreeBranch = branch,
                dobEpochDay = dobEpoch,
                category = category,
                heightCm = height,
                preferredState = state,
                preferredSector = sector
            )
            repository.saveProfile(updated)
            showBanner(getTranslation("profile_saved"))
        }
    }

    // --- Application Tracker Operations ---
    fun addNewApplication(job: JobNotification, regNum: String, rollNum: String) {
        viewModelScope.launch {
            val app = TrackedApplication(
                jobId = job.id,
                jobTitle = job.title,
                organization = job.organization,
                registrationNumber = regNum,
                rollNumber = rollNum,
                appliedDateEpoch = System.currentTimeMillis(),
                currentStatus = "Applied", // Initial state
                examCentre = "Allocation Pending",
                examDateEpoch = job.examDateEpoch,
                admitCardUrl = "",
                resultStatus = "Awaiting Result"
            )
            repository.insertTrackedApplication(app)
            showBanner(getTranslation("app_added"))
        }
    }

    fun updateApplicationStatus(app: TrackedApplication, nextStatus: String, centre: String, roll: String, result: String) {
        viewModelScope.launch {
            val updated = app.copy(
                currentStatus = nextStatus,
                examCentre = centre,
                rollNumber = roll,
                resultStatus = result,
                admitCardUrl = if (nextStatus == "Admit Card Out") "https://ssc.gov.in/download/admitcard.pdf" else app.admitCardUrl
            )
            repository.updateTrackedApplication(updated)
            showBanner("${getTranslation("app_updated")} - $nextStatus")
        }
    }

    fun removeTrackedApplication(id: Long) {
        viewModelScope.launch {
            repository.deleteTrackedApplication(id)
            showBanner(getTranslation("app_deleted"))
        }
    }

    // --- Crawler Simulation ---
    fun simulateOfficialCrawler() {
        if (isCrawling.value) return
        viewModelScope.launch {
            isCrawling.value = true
            crawlerLogs.value = listOf("Initializing official bot crawler...")
            
            val sources = listOf(
                "SSC (ssc.gov.in)",
                "UPSC (upsc.gov.in)",
                "Railway Recruitment Board (indianrailways.gov.in)",
                "IBPS Portal (ibps.in)",
                "State PSC Board (uppbpb.gov.in)"
            )
            
            for (source in sources) {
                kotlinx.coroutines.delay(800)
                crawlerLogs.value = crawlerLogs.value + "Scanning $source for new notifications..."
            }
            
            kotlinx.coroutines.delay(600)
            crawlerLogs.value = crawlerLogs.value + "Crawled successfully! 2 new unverified notifications sent to AI pipeline."
            isCrawling.value = false
            showBanner(getTranslation("crawl_success"))
        }
    }

    // --- AI Pipeline Parser via Gemini ---
    fun parseNotificationWithAI(rawText: String) {
        if (rawText.isBlank()) {
            parsingError.value = "Notification text cannot be empty!"
            return
        }
        viewModelScope.launch {
            isParsing.value = true
            parsingError.value = null
            parsedNotificationResult.value = null
            try {
                // Call Gemini Service
                val result = GeminiService.parseNotification(rawText)
                parsedNotificationResult.value = result
                showBanner("AI Extraction Complete (${result.confidenceScore}% confidence)")
            } catch (e: Exception) {
                parsingError.value = "Failed to parse: ${e.message}"
            } finally {
                isParsing.value = false
            }
        }
    }

    // --- Admin Approval ---
    fun approveNotificationByAdmin(notification: JobNotification) {
        viewModelScope.launch {
            val approved = notification.copy(isApprovedByAdmin = true)
            repository.insertNotification(approved)
            showBanner("Job Approved & Broadcasted to Eligible Candidates!")
            
            // If we successfully approved it, clear active parsed box state
            if (parsedNotificationResult.value?.title == notification.title) {
                parsedNotificationResult.value = null
            }
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotification(id)
            showBanner("Notification Discarded by Admin.")
        }
    }

    // --- Translation System mapping ---
    private val translations = mapOf(
        AppLanguage.ENGLISH to mapOf(
            "dashboard_title" to "Sarkari Job Match",
            "dashboard_subtitle" to "Zero spam. Only jobs you are eligible for.",
            "tab_jobs" to "Matching Jobs",
            "tab_tracker" to "Applied Tracker",
            "tab_profile" to "My Profile",
            "tab_pipeline" to "AI & Admin Pipeline",
            "profile_saved" to "Profile Saved Successfully!",
            "app_added" to "Application Tracked successfully!",
            "app_updated" to "Status updated",
            "app_deleted" to "Tracking stopped.",
            "crawl_success" to "Official websites scanned!",
            "btn_save" to "Save Profile",
            "lbl_education" to "Educational Level",
            "lbl_age" to "Age",
            "lbl_category" to "Category",
            "lbl_height" to "Height (cm)",
            "lbl_state" to "Preferred State",
            "lbl_sector" to "Preferred Sector",
            "badge_verified" to "Gov Verified",
            "badge_unverified" to "Third Party Source",
            "no_jobs_found" to "No matching jobs found. Try broadening your eligibility parameters in Profile tab!",
            "matching_header" to "Perfect Matches",
            "matching_subheader" to "Based on Age relaxation, height & qualifications.",
            "track_action" to "Add to Tracker",
            "crawler_running" to "Bot scanning government portals..."
        ),
        AppLanguage.HINDI to mapOf(
            "dashboard_title" to "सरकारी जॉब मैच",
            "dashboard_subtitle" to "बिना किसी फालतू अलर्ट के, सिर्फ आपकी काम की जॉब्स।",
            "tab_jobs" to "योग्य सरकारी नौकरियां",
            "tab_tracker" to "आवेदन ट्रैकर",
            "tab_profile" to "मेरी प्रोफाइल",
            "tab_pipeline" to "एआई और एडमिन",
            "profile_saved" to "प्रोफाइल सफलतापूर्वक सहेज ली गई!",
            "app_added" to "आवेदन को ट्रैक करना शुरू किया गया!",
            "app_updated" to "स्थिति अपडेट कर दी गई",
            "app_deleted" to "ट्रैकिंग हटा दी गई।",
            "crawl_success" to "आधिकारिक वेबसाइट्स स्कैन हो गईं!",
            "btn_save" to "प्रोफ़ाइल सहेजें",
            "lbl_education" to "शिक्षा स्तर",
            "lbl_age" to "उम्र",
            "lbl_category" to "श्रेणी (Category)",
            "lbl_height" to "लंबाई (सेंटीमीटर)",
            "lbl_state" to "पसंदीदा राज्य",
            "lbl_sector" to "पसंदीदा क्षेत्र",
            "badge_verified" to "सरकारी सत्यापित",
            "badge_unverified" to "गैर-सरकारी स्रोत",
            "no_jobs_found" to "कोई मेल खाने वाली नौकरी नहीं मिली। कृपया प्रोफाइल में जाकर अपनी पात्रता बदलें!",
            "matching_header" to "उत्कृष्ट मिलान",
            "matching_subheader" to "आयु छूट, लंबाई और शैक्षिक योग्यता के अनुसार।",
            "track_action" to "ट्रैकर में जोड़ें",
            "crawler_running" to "आधिकारिक सरकारी पोर्टलों को स्कैन किया जा रहा है..."
        ),
        AppLanguage.HINGLISH to mapOf(
            "dashboard_title" to "Sarkari Job Match",
            "dashboard_subtitle" to "No spam. Sirf wahi jobs jo aapke liye eligible hain.",
            "tab_jobs" to "Matching Jobs",
            "tab_tracker" to "My Applied Forms",
            "tab_profile" to "My Profile Details",
            "tab_pipeline" to "AI & Admin Process",
            "profile_saved" to "Profile Save Ho Gaya!",
            "app_added" to "Application Tracker me add ho gaya!",
            "app_updated" to "Status update ho gaya",
            "app_deleted" to "Tracking stop ho gayi.",
            "crawl_success" to "Govt portals crawl ho gaye!",
            "btn_save" to "Profile Save Karo",
            "lbl_education" to "Education Qualification",
            "lbl_age" to "Age Limit",
            "lbl_category" to "Caste Category",
            "lbl_height" to "Apni Height (cm)",
            "lbl_state" to "State Preference",
            "lbl_sector" to "Job Sector Preference",
            "badge_verified" to "Official Govt Link",
            "badge_unverified" to "Unverified Link",
            "no_jobs_found" to "Aapki profile se match hone wali koi job nahi mili. Apni profile update karke check karein!",
            "matching_header" to "Super Job Matches",
            "matching_subheader" to "Age relaxation aur height specs match hone ke baad.",
            "track_action" to "Tracker me daalein",
            "crawler_running" to "Bot Scanning Govt Sites..."
        )
    )

    fun getTranslation(key: String): String {
        return translations[activeLanguage.value]?.get(key) ?: translations[AppLanguage.ENGLISH]?.get(key) ?: key
    }
}
