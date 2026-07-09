package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Rajesh Kumar",
    val educationLevel: String = "12th Pass", // "10th Pass", "12th Pass", "Graduate", "Post Graduate"
    val degreeBranch: String = "Arts",
    val dobEpochDay: Long = 1022716800000L, // Default: 30 May 2002 (approx 24 yrs old)
    val category: String = "OBC", // "General", "OBC", "SC", "ST", "EWS"
    val heightCm: Int = 172,
    val preferredState: String = "Uttar Pradesh",
    val preferredSector: String = "Railways" // "SSC", "Banking", "Railways", "Defense", "State PSC", "All"
)

@Entity(tableName = "job_notifications")
data class JobNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val organization: String, // SSC, UPSC, IBPS, RRB, State PSC, etc.
    val vacancyCount: Int,
    val minAge: Int,
    val maxAge: Int,
    val educationRequired: String, // "10th Pass", "12th Pass", "Graduate", "Post Graduate"
    val minHeightCm: Int, // physical standard filter (0 if none)
    val applicationStartDateEpoch: Long,
    val applicationEndDateEpoch: Long,
    val examDateEpoch: Long,
    val sourceLink: String,
    val isGovSource: Boolean, // True if contains .gov.in or .nic.in
    val confidenceScore: Int, // AI extraction confidence percentage
    val aiReasoning: String, // Explanation of how requirements match profiles
    val isApprovedByAdmin: Boolean, // Approved notifications enter matching queue
    val rawDocumentText: String = "" // Simulation/Input raw PDF text
)

@Entity(tableName = "tracked_applications")
data class TrackedApplication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobId: Long,
    val jobTitle: String,
    val organization: String,
    val registrationNumber: String,
    val rollNumber: String = "",
    val appliedDateEpoch: Long,
    val currentStatus: String, // "Applied", "Exam Scheduled", "Admit Card Out", "Answer Key Out", "Result Declared"
    val examCentre: String = "",
    val examDateEpoch: Long = 0,
    val admitCardUrl: String = "",
    val resultStatus: String = "" // "Passed", "Failed", "Awaiting Result"
)
