package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class JobRepository(private val db: AppDatabase) {

    val userProfile: Flow<UserProfile?> = db.userProfileDao().getUserProfile()
    val allNotifications: Flow<List<JobNotification>> = db.jobNotificationDao().getAllNotifications()
    val approvedNotifications: Flow<List<JobNotification>> = db.jobNotificationDao().getApprovedNotifications()
    val trackedApplications: Flow<List<TrackedApplication>> = db.trackedApplicationDao().getAllTrackedApplications()

    suspend fun saveProfile(profile: UserProfile) {
        db.userProfileDao().insertOrUpdateProfile(profile)
    }

    suspend fun insertNotification(notification: JobNotification): Long {
        return db.jobNotificationDao().insertNotification(notification)
    }

    suspend fun updateNotification(notification: JobNotification) {
        db.jobNotificationDao().updateNotification(notification)
    }

    suspend fun deleteNotification(id: Long) {
        db.jobNotificationDao().deleteNotification(id)
    }

    suspend fun insertTrackedApplication(app: TrackedApplication): Long {
        return db.trackedApplicationDao().insertTrackedApplication(app)
    }

    suspend fun updateTrackedApplication(app: TrackedApplication) {
        db.trackedApplicationDao().updateTrackedApplication(app)
    }

    suspend fun deleteTrackedApplication(id: Long) {
        db.trackedApplicationDao().deleteTrackedApplication(id)
    }

    suspend fun prepopulateIfNeeded() {
        val currentProfile = db.userProfileDao().getUserProfile().firstOrNull()
        if (currentProfile == null) {
            // Create default profile for demonstration
            val defaultProfile = UserProfile(
                id = 1,
                name = "Aman Sharma",
                educationLevel = "12th Pass",
                degreeBranch = "Science",
                dobEpochDay = Calendar.getInstance().apply {
                    set(2002, Calendar.MAY, 15) // Age approx 24
                }.timeInMillis,
                category = "OBC",
                heightCm = 172,
                preferredState = "Delhi",
                preferredSector = "All"
            )
            db.userProfileDao().insertOrUpdateProfile(defaultProfile)
        }

        // Check if there are any jobs in database
        val jobs = db.jobNotificationDao().getAllNotifications().firstOrNull()
        if (jobs.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val thirtyDays = 30L * 24 * 60 * 60 * 1000

            val sampleJobs = listOf(
                JobNotification(
                    title = "SSC GD Constable 2026 Recruitment",
                    organization = "Staff Selection Commission (SSC)",
                    vacancyCount = 26146,
                    minAge = 18,
                    maxAge = 23,
                    educationRequired = "10th Pass",
                    minHeightCm = 170, // Aman is 172cm so eligible. If user is 168cm, ineligible!
                    applicationStartDateEpoch = now - (5L * 24 * 60 * 60 * 1000),
                    applicationEndDateEpoch = now + (20L * 24 * 60 * 60 * 1000),
                    examDateEpoch = now + (60L * 24 * 60 * 60 * 1000),
                    sourceLink = "https://ssc.gov.in/notifications/GD_Constable_2026.pdf",
                    isGovSource = true,
                    confidenceScore = 98,
                    aiReasoning = "Extracted perfectly from official gazette. Candidate must be 10th pass. Height requirement is 170cm for Male General/OBC/SC. Age limits: 18-23 years as of Aug 1, 2026.",
                    isApprovedByAdmin = true
                ),
                JobNotification(
                    title = "RRB Assistant Loco Pilot (ALP) Recruitment",
                    organization = "Railway Recruitment Board (RRB)",
                    vacancyCount = 5696,
                    minAge = 18,
                    maxAge = 30,
                    educationRequired = "10th Pass", // plus ITI (handled in text or detail)
                    minHeightCm = 0, // No height restriction
                    applicationStartDateEpoch = now - (12L * 24 * 60 * 60 * 1000),
                    applicationEndDateEpoch = now + (5L * 24 * 60 * 60 * 1000),
                    examDateEpoch = now + (45L * 24 * 60 * 60 * 1000),
                    sourceLink = "https://indianrailways.gov.in/rrb/ALP_Recruitment_01_2026.pdf",
                    isGovSource = true,
                    confidenceScore = 95,
                    aiReasoning = "Extracted from RRB CEN 01/2026. Age limit 18-30. Essential qualification: 10th Pass + ITI trade or Diploma in Engineering. Full medical standard A-1 is mandatory.",
                    isApprovedByAdmin = true
                ),
                JobNotification(
                    title = "SSC CHSL (10+2) Examination 2026",
                    organization = "Staff Selection Commission (SSC)",
                    vacancyCount = 3712,
                    minAge = 18,
                    maxAge = 27,
                    educationRequired = "12th Pass",
                    minHeightCm = 0,
                    applicationStartDateEpoch = now - (2L * 24 * 60 * 60 * 1000),
                    applicationEndDateEpoch = now + (28L * 24 * 60 * 60 * 1000),
                    examDateEpoch = now + (90L * 24 * 60 * 60 * 1000),
                    sourceLink = "https://ssc.gov.in/notifications/CHSL_10_2_2026.pdf",
                    isGovSource = true,
                    confidenceScore = 99,
                    aiReasoning = "Parsed from SSC official site. Requires 12th standard pass or equivalent from a recognized board. Age group: 18-27 years. standard age relaxations apply.",
                    isApprovedByAdmin = true
                ),
                JobNotification(
                    title = "IBPS PO / Management Trainee XV",
                    organization = "Institute of Banking Personnel Selection (IBPS)",
                    vacancyCount = 4455,
                    minAge = 20,
                    maxAge = 30,
                    educationRequired = "Graduate", // Eligible if user is Graduate, else ineligible!
                    minHeightCm = 0,
                    applicationStartDateEpoch = now + (5L * 24 * 60 * 60 * 1000),
                    applicationEndDateEpoch = now + (25L * 24 * 60 * 60 * 1000),
                    examDateEpoch = now + (110L * 24 * 60 * 60 * 1000),
                    sourceLink = "https://ibps.in/notifications/CWE_PO_XV.pdf",
                    isGovSource = false, // ibps.in is technically not gov.in (needs a soft flag, though trusted in banking)
                    confidenceScore = 92,
                    aiReasoning = "Extracted from IBPS PO notification. Essential: Degree (Graduation) in any discipline from a recognized University. Age: 20-30 years.",
                    isApprovedByAdmin = true
                ),
                JobNotification(
                    title = "UPSC Civil Services Prelims 2026",
                    organization = "Union Public Service Commission (UPSC)",
                    vacancyCount = 1056,
                    minAge = 21,
                    maxAge = 32,
                    educationRequired = "Graduate",
                    minHeightCm = 0,
                    applicationStartDateEpoch = now - (20L * 24 * 60 * 60 * 1000),
                    applicationEndDateEpoch = now - (1L * 24 * 60 * 60 * 1000), // Ended yesterday
                    examDateEpoch = now + (120L * 24 * 60 * 60 * 1000),
                    sourceLink = "https://upsc.gov.in/exams/CSE_2026_Prelims.pdf",
                    isGovSource = true,
                    confidenceScore = 100,
                    aiReasoning = "Official notification from upsc.gov.in. Minimum age 21, maximum 32. Any degree from a recognized university. Number of attempts restricted based on category.",
                    isApprovedByAdmin = true
                ),
                // --- PENDING REVIEW (AI-extracted, awaiting admin approval) ---
                JobNotification(
                    title = "UP Police Sub Inspector (Civil Police) Recruitment",
                    organization = "UP Police Recruitment Board (PRPB)",
                    vacancyCount = 921,
                    minAge = 21,
                    maxAge = 28,
                    educationRequired = "Graduate",
                    minHeightCm = 168,
                    applicationStartDateEpoch = now + (10L * 24 * 60 * 60 * 1000),
                    applicationEndDateEpoch = now + (40L * 24 * 60 * 60 * 1000),
                    examDateEpoch = now + (130L * 24 * 60 * 60 * 1000),
                    sourceLink = "https://uppbpb.gov.in/SI_Civil_Recruitment_2026.pdf",
                    isGovSource = true,
                    confidenceScore = 87, // Slightly lower, simulated extraction
                    aiReasoning = "AI extracted from state board PDF. Minimum height 168cm for General/OBC/SC male candidates. Requires Graduation in any stream. Age criteria: 21 to 28 years.",
                    isApprovedByAdmin = false // Awaiting review in Dashboard!
                ),
                JobNotification(
                    title = "Indian Army Agniveer GD (General Duty) 2026",
                    organization = "Indian Army",
                    vacancyCount = 25000,
                    minAge = 17, // 17.5 years actually, stored as 17
                    maxAge = 21,
                    educationRequired = "10th Pass",
                    minHeightCm = 170,
                    applicationStartDateEpoch = now + (2L * 24 * 60 * 60 * 1000),
                    applicationEndDateEpoch = now + (32L * 24 * 60 * 60 * 1000),
                    examDateEpoch = now + (80L * 24 * 60 * 60 * 1000),
                    sourceLink = "https://joinindianarmy.nic.in/Agniveer_GD_2026.pdf",
                    isGovSource = true,
                    confidenceScore = 89,
                    aiReasoning = "Extracted from joinindianarmy.nic.in site. Requires 10th pass/Matric with 45% marks in aggregate. Height 170cm standard for general region. Age: 17.5 to 21 years.",
                    isApprovedByAdmin = false // Awaiting review!
                )
            )

            for (job in sampleJobs) {
                db.jobNotificationDao().insertNotification(job)
            }
        }

        // Check if there are tracked applications
        val tracked = db.trackedApplicationDao().getAllTrackedApplications().firstOrNull()
        if (tracked.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            // Add a mock application
            val mockApp = TrackedApplication(
                jobId = 2, // ALP
                jobTitle = "RRB Assistant Loco Pilot (ALP) Recruitment",
                organization = "Railway Recruitment Board (RRB)",
                registrationNumber = "RRB-ALP-2026-9810",
                rollNumber = "RRB-109281-ALP",
                appliedDateEpoch = now - (8L * 24 * 60 * 60 * 1000),
                currentStatus = "Admit Card Out", // Showcase admit card status!
                examCentre = "Test Center 4, Sector 62, Noida, UP",
                examDateEpoch = now + (15L * 24 * 60 * 60 * 1000),
                admitCardUrl = "https://indianrailways.gov.in/rrb/admit_card_alp.pdf",
                resultStatus = "Awaiting Result"
            )
            db.trackedApplicationDao().insertTrackedApplication(mockApp)
        }
    }
}
