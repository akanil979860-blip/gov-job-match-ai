package com.example.engine

import com.example.data.JobNotification
import com.example.data.UserProfile
import java.util.Calendar

data class MatchResult(
    val isEligible: Boolean,
    val userAge: Int,
    val maxAgeWithRelaxation: Int,
    val ageRelaxationYearsApplied: Int,
    val isEducationEligible: Boolean,
    val isHeightEligible: Boolean,
    val isAgeEligible: Boolean,
    val positiveMatches: List<String>,
    val negativeMatches: List<String>
)

object MatchingEngine {

    /**
     * Calculates the age in years based on Dob epoch milliseconds.
     */
    fun calculateAge(dobEpochMillis: Long): Int {
        val dob = Calendar.getInstance().apply { timeInMillis = dobEpochMillis }
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return if (age < 0) 0 else age
    }

    /**
     * Maps education string levels to integer ranks for easy hierarchy comparison.
     */
    private fun getEducationRank(level: String): Int {
        return when (level.trim()) {
            "10th Pass" -> 1
            "12th Pass" -> 2
            "Graduate" -> 3
            "Post Graduate" -> 4
            else -> 1
        }
    }

    /**
     * Evaluates a user's eligibility for a specific job notification.
     */
    fun evaluateEligibility(user: UserProfile, job: JobNotification): MatchResult {
        val userAge = calculateAge(user.dobEpochDay)

        // 1. Calculate Age Relaxation
        val ageRelaxation = when (user.category.uppercase()) {
            "OBC" -> 3
            "SC", "ST" -> 5
            "PWD" -> 10
            else -> 0 // General / EWS have 0 relaxation
        }
        val maxAgeWithRelaxation = job.maxAge + ageRelaxation
        val isAgeEligible = userAge in job.minAge..maxAgeWithRelaxation

        // 2. Evaluate Educational Ordinality
        val userEduRank = getEducationRank(user.educationLevel)
        val jobEduRank = getEducationRank(job.educationRequired)
        val isEducationEligible = userEduRank >= jobEduRank

        // 3. Evaluate Physical standards (Height)
        // If job min height is 0, physical standard doesn't apply.
        val isHeightEligible = job.minHeightCm == 0 || user.heightCm >= job.minHeightCm

        // Total eligibility is true only if all 3 criteria are met
        val isEligible = isAgeEligible && isEducationEligible && isHeightEligible

        // Construct explanations
        val positives = mutableListOf<String>()
        val negatives = mutableListOf<String>()

        // Age Details
        if (isAgeEligible) {
            val relaxationText = if (ageRelaxation > 0) " (includes +$ageRelaxation yrs relaxation for ${user.category})" else ""
            positives.add("Age Match: Your age is $userAge years, within the range ${job.minAge}-$maxAgeWithRelaxation years$relaxationText.")
        } else {
            val relaxationText = if (ageRelaxation > 0) " (includes +$ageRelaxation yrs relaxation for ${user.category})" else ""
            if (userAge < job.minAge) {
                negatives.add("Age Underlimit: You are $userAge years old. Minimum required age is ${job.minAge}.")
            } else {
                negatives.add("Age Overlimit: You are $userAge years old. Maximum allowed age with relaxation is $maxAgeWithRelaxation years$relaxationText.")
            }
        }

        // Education Details
        if (isEducationEligible) {
            positives.add("Education Match: You hold a ${user.educationLevel} degree which satisfies the minimum qualification of ${job.educationRequired}.")
        } else {
            negatives.add("Education Mismatch: Job requires ${job.educationRequired}, but your profile level is ${user.educationLevel}.")
        }

        // Physical Standards Details
        if (job.minHeightCm > 0) {
            if (isHeightEligible) {
                positives.add("Physical Standard Match: Your height is ${user.heightCm}cm, which meets the minimum physical height standard of ${job.minHeightCm}cm.")
            } else {
                negatives.add("Physical Standard Mismatch: Your height is ${user.heightCm}cm, which is below the physical requirement of ${job.minHeightCm}cm.")
            }
        } else {
            positives.add("Physical Standards: No specific physical standard criteria is mandated for this civil post.")
        }

        // Sector Preference Details
        val sectorLower = user.preferredSector.lowercase()
        val orgLower = job.organization.lowercase()
        val matchesPreferredSector = sectorLower == "all" || 
                (sectorLower == "banking" && (orgLower.contains("ibps") || orgLower.contains("bank") || orgLower.contains("sbi"))) ||
                (sectorLower == "ssc" && orgLower.contains("staff selection")) ||
                (sectorLower == "railways" && (orgLower.contains("railway") || orgLower.contains("rrb"))) ||
                (sectorLower == "defense" && (orgLower.contains("army") || orgLower.contains("police") || orgLower.contains("constable"))) ||
                (sectorLower == "state psc" && orgLower.contains("psc"))

        if (matchesPreferredSector) {
            positives.add("Career Sector Match: This post aligns with your preferred sector target (${user.preferredSector}).")
        }

        return MatchResult(
            isEligible = isEligible,
            userAge = userAge,
            maxAgeWithRelaxation = maxAgeWithRelaxation,
            ageRelaxationYearsApplied = ageRelaxation,
            isEducationEligible = isEducationEligible,
            isHeightEligible = isHeightEligible,
            isAgeEligible = isAgeEligible,
            positiveMatches = positives,
            negativeMatches = negatives
        )
    }
}
