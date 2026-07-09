package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.engine.MatchResult
import com.example.engine.MatchingEngine
import com.example.ui.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) { innerPadding ->
                    SarkariJobMatchApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun SarkariJobMatchApp(
    modifier: Modifier = Modifier,
    viewModel: SarkariViewModel = viewModel()
) {
    val activeTab by viewModel.activeTab.collectAsState()
    val activeLanguage by viewModel.activeLanguage.collectAsState()
    val isLowData by viewModel.isLowDataMode.collectAsState()
    val bannerMessage by viewModel.appBannerMessage.collectAsState()

    val profile by viewModel.userProfile.collectAsState()
    val approvedJobs by viewModel.approvedJobs.collectAsState()
    val allJobs by viewModel.allJobs.collectAsState()
    val trackedApps by viewModel.trackedApplications.collectAsState()

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // --- Top Bar Banner / Header ---
        TopBrandingHeader(
            activeLanguage = activeLanguage,
            isLowData = isLowData,
            profile = profile,
            onLanguageChange = { viewModel.activeLanguage.value = it },
            onToggleLowData = { viewModel.isLowDataMode.value = !isLowData },
            viewModel = viewModel
        )

        // --- Simulated Push Notification Banner ---
        AnimatedVisibility(
            visible = bannerMessage != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            bannerMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Notification",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.0f)
                        )
                    }
                }
            }
        }

        // --- Core Main Screen Body ---
        Box(modifier = Modifier.weight(1.0f)) {
            when (activeTab) {
                0 -> DashboardJobsScreen(
                    profile = profile,
                    jobs = approvedJobs,
                    trackedApps = trackedApps,
                    isLowData = isLowData,
                    viewModel = viewModel
                )
                1 -> ApplicationTrackerScreen(
                    trackedApps = trackedApps,
                    isLowData = isLowData,
                    viewModel = viewModel
                )
                2 -> ProfileScreen(
                    profile = profile,
                    viewModel = viewModel
                )
                3 -> AdminPipelineScreen(
                    allJobs = allJobs,
                    viewModel = viewModel
                )
            }
        }

        // --- Bottom Navigation Menu ---
        BottomNavBar(
            activeTab = activeTab,
            onTabSelected = { viewModel.activeTab.value = it },
            viewModel = viewModel
        )
    }
}

@Composable
fun TopBrandingHeader(
    activeLanguage: AppLanguage,
    isLowData: Boolean,
    profile: UserProfile?,
    onLanguageChange: (AppLanguage) -> Unit,
    onToggleLowData: () -> Unit,
    viewModel: SarkariViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, HighDensityBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand Name / Left Column
                Column {
                    Text(
                        text = viewModel.getTranslation("dashboard_title"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HighDensityPrimary,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-0.5).sp
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AccentGreen)
                        )
                        Text(
                            text = "LIVE UPDATES ACTIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Controls: Language selector + Low-Data Mode + Avatar initials
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Low Data Mode Icon Toggle
                    IconButton(
                        onClick = onToggleLowData,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(HighDensityBg)
                            .testTag("low_data_toggle")
                    ) {
                        Icon(
                            imageVector = if (isLowData) Icons.Default.Bolt else Icons.Default.CloudSync,
                            contentDescription = "Toggle Low Data",
                            tint = if (isLowData) AccentOrange else HighDensityTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Language Selector Dropdown / Row
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(HighDensityBg)
                              .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        AppLanguage.values().forEach { lang ->
                            val isSelected = activeLanguage == lang
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) HighDensityPrimary else Color.Transparent)
                                    .clickable { onLanguageChange(lang) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = when (lang) {
                                        AppLanguage.ENGLISH -> "EN"
                                        AppLanguage.HINDI -> "हि"
                                        AppLanguage.HINGLISH -> "Hng"
                                    },
                                    color = if (isSelected) Color.White else HighDensityTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Profile dynamic Avatar initials
                    val initials = profile?.name?.trim()?.split("\\s+".toRegex())
                        ?.mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
                        ?.take(2)
                        ?.joinToString("") ?: "RK"

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(HighDensityPrimary)
                            .clickable { viewModel.activeTab.value = 2 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
              }
        }
    }
}

// --- TAB 1: MATCHING JOBS ---
@Composable
fun DashboardJobsScreen(
    profile: UserProfile?,
    jobs: List<JobNotification>,
    trackedApps: List<TrackedApplication>,
    isLowData: Boolean,
    viewModel: SarkariViewModel
) {
    if (profile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Indigo Profile Eligibility Chip - High Density Design
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HighDensitySecondaryBg),
            border = BorderStroke(1.dp, HighDensitySecondary.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(HighDensitySecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Eligible Logo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Eligibility Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = HighDensitySecondary
                        )
                        val age = MatchingEngine.calculateAge(profile.dobEpochDay)
                        Text(
                            text = "${profile.educationLevel} • ${profile.category} • $age Yrs • ${profile.heightCm}cm",
                            fontSize = 11.sp,
                            color = HighDensitySecondary.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Button(
                    onClick = { viewModel.activeTab.value = 2 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, HighDensitySecondary.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp).testTag("edit_profile_dashboard_button")
                ) {
                    Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HighDensitySecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Evaluate all approved jobs for this profile
        val matchedJobPairs = jobs.map { job ->
            job to MatchingEngine.evaluateEligibility(profile, job)
        }

        // Filter and display matching jobs
        val eligibleJobs = matchedJobPairs.filter { it.second.isEligible }
        val ineligibleJobs = matchedJobPairs.filter { !it.second.isEligible }

        // Section Title: Matched For You
        Text(
            text = "MATCHED FOR YOU (${eligibleJobs.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = HighDensityTextSecondary,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        if (eligibleJobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Empty",
                        tint = HighDensityTextMuted,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.getTranslation("no_jobs_found"),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = HighDensityTextSecondary,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            eligibleJobs.forEach { (job, matchResult) ->
                JobCard(
                    job = job,
                    matchResult = matchResult,
                    isTracked = trackedApps.any { it.jobId == job.id },
                    isLowData = isLowData,
                    viewModel = viewModel
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Dashboard Tracker Widget Section
        if (trackedApps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "APPLICATION TRACKER",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = HighDensityTextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Grid of 2 columns
            val chunks = trackedApps.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                chunks.forEach { rowApps ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowApps.forEach { app ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.activeTab.value = 1 },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, HighDensityBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (app.currentStatus == "Admit Card Out" || app.currentStatus == "Result Declared") AccentOrange
                                                    else HighDensityPrimary
                                                )
                                        )
                                        Text(
                                            text = app.organization,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = HighDensityTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = app.currentStatus,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HighDensityTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val dateStr = if (app.examDateEpoch > 0) {
                                        val df = SimpleDateFormat("dd MMM", Locale.getDefault())
                                        df.format(Date(app.examDateEpoch))
                                    } else "TBD"
                                    Text(
                                        text = if (app.currentStatus == "Admit Card Out") "Exam: $dateStr" else "Status Updated",
                                        fontSize = 10.sp,
                                        color = HighDensityTextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        // Fill empty spot if odd count
                        if (rowApps.size < 2) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Non-eligible jobs (Aspirants like to see them too, but clearly separated & flagged!)
        if (ineligibleJobs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "OTHER ACTIVE NOTIFICATIONS (NOT RECOMMENDED)",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = HighDensityTextMuted,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ineligibleJobs.forEach { (job, matchResult) ->
                JobCard(
                    job = job,
                    matchResult = matchResult,
                    isTracked = trackedApps.any { it.jobId == job.id },
                    isLowData = isLowData,
                    viewModel = viewModel
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun JobCard(
    job: JobNotification,
    matchResult: MatchResult,
    isTracked: Boolean,
    isLowData: Boolean,
    viewModel: SarkariViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    var trackingDialogOpen by remember { mutableStateOf(false) }

    var regNumberInput by remember { mutableStateOf("") }
    var rollNumberInput by remember { mutableStateOf("") }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val endDateStr = dateFormat.format(Date(job.applicationEndDateEpoch))

    val daysLeft = ((job.applicationEndDateEpoch - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
    val urgencyText = if (daysLeft > 0) "CLOSING IN $daysLeft DAYS" else "CLOSING SOON"
    val urgencyBg = if (daysLeft > 0 && daysLeft <= 10) AccentOrangeBg else HighDensitySecondaryBg
    val urgencyColor = if (daysLeft > 0 && daysLeft <= 10) AccentOrange else HighDensitySecondary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("job_card_${job.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, HighDensityBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Organization, Tag + Match Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1.0f)) {
                    // Organization & Urgency Tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = job.organization,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensitySecondary,
                            letterSpacing = 0.5.sp
                        )
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(urgencyBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = urgencyText,
                                color = urgencyColor,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = job.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityTextPrimary,
                        lineHeight = 19.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // High Density Match Score
                Column(horizontalAlignment = Alignment.End) {
                    val percent = if (matchResult.isEligible) "98%" else "35%"
                    val matchColor = if (matchResult.isEligible) AccentGreen else Color.Red
                    Text(
                        text = "$percent Match",
                        color = matchColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Based on Profile",
                        color = HighDensityTextMuted,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle / Filter Tags Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Education Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(HighDensityBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = job.educationRequired,
                        color = HighDensityTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Age Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(HighDensityBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Age: ${job.minAge}-${job.maxAge}",
                        color = HighDensityTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Vacancy Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(HighDensityBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${job.vacancyCount}+ Vacancies",
                        color = HighDensityTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expand Criteria Dropdown link + Primary Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Check Criteria Details
                Row(
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Hide Criteria" else "Check Criteria",
                        fontSize = 12.sp,
                        color = HighDensityPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = HighDensityPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Action Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Apply Now Button
                    Button(
                        onClick = { /* Simulated browser redirect */ },
                        modifier = Modifier
                            .height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text("Apply Now", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Track / Bookmark Button
                    if (matchResult.isEligible) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isTracked) HighDensityPrimary.copy(alpha = 0.12f) else HighDensityBg)
                                .border(1.dp, if (isTracked) HighDensityPrimary else HighDensityBorder, RoundedCornerShape(10.dp))
                                .clickable { trackingDialogOpen = true }
                                .testTag("track_button_${job.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isTracked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Track Status",
                                tint = if (isTracked) HighDensityPrimary else HighDensityTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Expanded Match Details
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(HighDensityBg)
                        .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text("MATCH VERIFICATION LOG", fontSize = 10.sp, fontWeight = FontWeight.Black, color = HighDensityTextMuted, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Positive matches
                    matchResult.positiveMatches.forEach { reason ->
                        Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(reason, fontSize = 11.sp, color = HighDensityTextSecondary)
                        }
                    }

                    // Mismatch blocks
                    matchResult.negativeMatches.forEach { reason ->
                        Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(reason, fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("AI EXTRACTION REASONING:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HighDensityTextMuted)
                    Text(
                        text = job.aiReasoning,
                        fontSize = 11.sp,
                        color = HighDensityTextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    // Source Link Application button
                    OutlinedButton(
                        onClick = { /* Simulated browser open */ },
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HighDensityBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HighDensityPrimary),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verify Government PDF Source", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- DIALOG FOR TRACKING SETTINGS ---
    if (trackingDialogOpen) {
        AlertDialog(
            onDismissRequest = { trackingDialogOpen = false },
            title = { Text("Track Application Status", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HighDensityTextPrimary) },
            text = {
                Column {
                    Text(
                        text = "Track dates, admit cards, exam centers, and results automatically for: ${job.title}",
                        fontSize = 12.sp,
                        color = HighDensityTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = regNumberInput,
                        onValueChange = { regNumberInput = it },
                        label = { Text("Registration Number / Application ID") },
                        modifier = Modifier.fillMaxWidth().testTag("reg_number_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rollNumberInput,
                        onValueChange = { rollNumberInput = it },
                        label = { Text("Roll Number (Optional)") },
                        modifier = Modifier.fillMaxWidth().testTag("roll_number_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (regNumberInput.isNotBlank()) {
                            viewModel.addNewApplication(job, regNumberInput, rollNumberInput)
                            trackingDialogOpen = false
                        } else {
                            viewModel.showBanner("Registration number is required!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("submit_tracking_button")
                ) {
                    Text("Confirm Tracking", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { trackingDialogOpen = false }) {
                    Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HighDensityTextSecondary)
                }
            }
        )
    }
}

// --- TAB 2: APPLICATION TRACKER ---
@Composable
fun ApplicationTrackerScreen(
    trackedApps: List<TrackedApplication>,
    isLowData: Boolean,
    viewModel: SarkariViewModel
) {
    if (trackedApps.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No applications currently tracked.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Go to Matching Jobs tab and click 'Track Status' to automatically monitor exam dates and admit cards.",
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "My Tracked Applications",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Automatic reminders & status updates based on bot crawl scans.",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(trackedApps) { app ->
            TrackingCard(app = app, isLowData = isLowData, viewModel = viewModel)
        }
    }
}

@Composable
fun TrackingCard(
    app: TrackedApplication,
    isLowData: Boolean,
    viewModel: SarkariViewModel
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showStatusUpdateDialog by remember { mutableStateOf(false) }

    // Dialog form values
    val statuses = listOf("Applied", "Exam Scheduled", "Admit Card Out", "Answer Key Out", "Result Declared")
    var selectedStatus by remember { mutableStateOf(app.currentStatus) }
    var examCentreInput by remember { mutableStateOf(app.examCentre) }
    var rollNumberInput by remember { mutableStateOf(app.rollNumber) }
    var resultInput by remember { mutableStateOf(app.resultStatus) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tracker_card_${app.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, HighDensityBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(text = app.organization, fontSize = 11.sp, color = HighDensitySecondary, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Text(text = app.jobTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HighDensityTextPrimary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(HighDensityPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = app.currentStatus, color = HighDensityPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub details
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("REG NUMBER", fontSize = 9.sp, color = HighDensityTextMuted, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Text(app.registrationNumber, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HighDensityTextPrimary)
                }
                Column {
                    Text("ROLL NUMBER", fontSize = 9.sp, color = HighDensityTextMuted, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Text(app.rollNumber.ifEmpty { "Awaiting Allocation" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HighDensityTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontal status stepper timeline
            if (!isLowData) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val steps = listOf("Applied", "Admit Out", "Exam", "Result")
                    val currentStepIndex = when (app.currentStatus) {
                        "Applied" -> 0
                        "Admit Card Out" -> 1
                        "Exam Scheduled" -> 2
                        "Result Declared" -> 3
                        else -> 0
                    }

                    steps.forEachIndexed { idx, step ->
                        val isCompleted = idx <= currentStepIndex
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.0f)) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isCompleted) HighDensityPrimary else HighDensityBg)
                                    .border(1.dp, if (isCompleted) HighDensityPrimary else HighDensityBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = step,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) HighDensityPrimary else HighDensityTextMuted
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Alerts related to specific statuses (e.g., Admit Card release)
            if (app.currentStatus == "Admit Card Out") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AccentOrangeBg),
                    border = BorderStroke(1.dp, AccentOrange.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text("Admit Card is available!", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentOrange)
                            Text("Exam Center: ${app.examCentre}", fontSize = 11.sp, color = AccentOrange.copy(alpha = 0.85f))
                        }
                        IconButton(onClick = { /* Simulated open URL */ }, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = "Download", tint = AccentOrange)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Expand/Interact Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(if (isExpanded) "Hide Options" else "Manage Application", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HighDensityPrimary)
                }

                IconButton(
                    onClick = { viewModel.removeTrackedApplication(app.id) },
                    modifier = Modifier.size(36.dp).testTag("delete_tracked_button_${app.id}")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                }
            }

            // Management Expansion block (Simulate Admin updating or manual correction for testing)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "SIMULATE TRACKER SCAFFOLD (Sandbox)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Since external portals restrict live API hooks, test milestones manually below:",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { showStatusUpdateDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Simulate/Advance Progress Status")
                    }
                }
            }
        }
    }

    if (showStatusUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showStatusUpdateDialog = false },
            title = { Text("Simulate Status Progression") },
            text = {
                Column {
                    Text("Select a mock notification state representing crawler detection:")
                    Spacer(modifier = Modifier.height(8.dp))

                    // Simulated status row selection
                    statuses.forEach { st ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStatus = st }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedStatus == st, onClick = { selectedStatus = st })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(st)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = examCentreInput,
                        onValueChange = { examCentreInput = it },
                        label = { Text("Exam Center (e.g. Sector 62 Noida)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rollNumberInput,
                        onValueChange = { rollNumberInput = it },
                        label = { Text("Roll Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resultInput,
                        onValueChange = { resultInput = it },
                        label = { Text("Result (Awaiting / Passed / Failed)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateApplicationStatus(
                            app = app,
                            nextStatus = selectedStatus,
                            centre = examCentreInput,
                            roll = rollNumberInput,
                            result = resultInput
                        )
                        showStatusUpdateDialog = false
                    }
                ) {
                    Text("Apply Mock Milestones")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusUpdateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- TAB 3: ELIGIBILITY PROFILE ---
@Composable
fun ProfileScreen(
    profile: UserProfile?,
    viewModel: SarkariViewModel
) {
    if (profile == null) return

    // Form states
    var name by remember { mutableStateOf(profile.name) }
    var specialization by remember { mutableStateOf(profile.degreeBranch) }
    var heightInput by remember { mutableStateOf(profile.heightCm.toString()) }
    var stateInput by remember { mutableStateOf(profile.preferredState) }

    // Selection indicators
    val categories = listOf("General", "OBC", "SC", "ST", "EWS")
    var selectedCategory by remember { mutableStateOf(profile.category) }

    val eduLevels = listOf("10th Pass", "12th Pass", "Graduate", "Post Graduate")
    var selectedEdu by remember { mutableStateOf(profile.educationLevel) }

    val sectors = listOf("All", "SSC", "Banking", "Railways", "Defense", "State PSC")
    var selectedSector by remember { mutableStateOf(profile.preferredSector) }

    // DOB Spinner logic (extremely stable)
    val calendar = Calendar.getInstance().apply { timeInMillis = profile.dobEpochDay }
    var dobYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var dobMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) } // 0-indexed
    var dobDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "My Eligibility Profile",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = HighDensityTextPrimary
        )
        Text(
            text = "Accurate settings guarantee that you only receive alerts for positions you fully qualify for. Age relaxations are automatically evaluated based on caste category.",
            fontSize = 12.sp,
            color = HighDensityTextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Aspirant Name") },
            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = HighDensityPrimary) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_name_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Education Level Section
        Text("Educational Qualification", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HighDensityTextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            eduLevels.forEach { edu ->
                val isSel = selectedEdu == edu
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) HighDensityPrimary else Color.White)
                        .border(1.dp, if (isSel) HighDensityPrimary else HighDensityBorder, RoundedCornerShape(10.dp))
                        .clickable { selectedEdu = edu }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = edu,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else HighDensityTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Specialization
        OutlinedTextField(
            value = specialization,
            onValueChange = { specialization = it },
            label = { Text("Degree Branch / Stream (e.g. Science, Arts, ITI, B.Tech)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Date of Birth Spinners
        Text("Date of Birth (For Age Limit Calculation)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HighDensityTextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Day spinner
            OutlinedTextField(
                value = dobDay.toString(),
                onValueChange = {
                    val d = it.toIntOrNull()
                    if (d != null && d in 1..31) dobDay = d
                },
                label = { Text("Day") },
                modifier = Modifier.weight(1.0f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Month selection (simple text display, increments)
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .height(56.dp)
                    .background(Color.White)
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(8.dp))
                    .clickable {
                        dobMonth = (dobMonth + 1) % 12
                    }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                Text(text = "Month: ${months[dobMonth]}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HighDensityTextPrimary)
            }

            // Year spinner
            OutlinedTextField(
                value = dobYear.toString(),
                onValueChange = {
                    val y = it.toIntOrNull()
                    if (y != null && y in 1950..2020) dobYear = y
                },
                label = { Text("Year") },
                modifier = Modifier.weight(1.2f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category/Caste
        Text("Category (For Age Relaxation)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HighDensityTextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSel = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) HighDensityPrimary else Color.White)
                        .border(1.dp, if (isSel) HighDensityPrimary else HighDensityBorder, RoundedCornerShape(10.dp))
                        .clickable { selectedCategory = cat }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else HighDensityTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Height
        OutlinedTextField(
            value = heightInput,
            onValueChange = { heightInput = it },
            label = { Text("Height in CM (For Police/Defense posts)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // State Preference & Sector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = stateInput,
                onValueChange = { stateInput = it },
                label = { Text("Pref. State") },
                modifier = Modifier.weight(1.0f)
            )

            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .height(56.dp)
                    .background(Color.White)
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(8.dp))
                    .clickable {
                        val currentIdx = sectors.indexOf(selectedSector)
                        selectedSector = sectors[(currentIdx + 1) % sectors.size]
                    }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text("Pref. Sector", fontSize = 9.sp, color = HighDensityTextMuted)
                    Text(selectedSector, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HighDensityTextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = {
                val cal = Calendar.getInstance().apply {
                    set(dobYear, dobMonth, dobDay)
                }
                viewModel.updateUserProfile(
                    name = name,
                    education = selectedEdu,
                    branch = specialization,
                    dobEpoch = cal.timeInMillis,
                    category = selectedCategory,
                    height = heightInput.toIntOrNull() ?: 170,
                    state = stateInput,
                    sector = selectedSector
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_profile_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
        ) {
            Text("Save Profile Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
        }
    }
}

// --- TAB 4: ADMIN & AI PIPELINE ---
@Composable
fun AdminPipelineScreen(
    allJobs: List<JobNotification>,
    viewModel: SarkariViewModel
) {
    val isCrawling by viewModel.isCrawling.collectAsState()
    val crawlerLogs by viewModel.crawlerLogs.collectAsState()

    val isParsing by viewModel.isParsing.collectAsState()
    val parsedResult by viewModel.parsedNotificationResult.collectAsState()
    val parsingError by viewModel.parsingError.collectAsState()

    var customTextToParse by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Mock Text Presets
    val preset1 = """
        STAFF SELECTION COMMISSION (SSC)
        NOTICE: RECRUITMENT OF GD CONSTABLE IN BORDER SECURITY FORCE
        Total Vacancies: 35000 posts.
        Age Limits: 18 to 23 years. OBC candidates get up to 26 years.
        Minimum height required: 170 cm for male candidates, 157 cm for female.
        Required Qualification: Matriculation or 10th Class Pass from any Board.
        Apply online on ssc.gov.in before September 15th, 2026.
    """.trimIndent()

    val preset2 = """
        DELHI SUBORDINATE SERVICES SELECTION BOARD (DSSSB)
        ADVERTISEMENT 04/2026: ASSISTANT TEACHER (NURSERY)
        Total open vacancies: 1455.
        Age Eligibility: Candidate must not exceed 30 years. 
        Category relaxation: SC/ST candidates get 5 years extra.
        Essential Education: Senior Secondary School (Class 12th Pass) with at least 45% marks and Diploma in Nursery Teacher Education.
        Official Application Portal: dsssb.delhi.gov.in.
    """.trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Admin Portal & AI Data Pipeline",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = HighDensityTextPrimary
        )
        Text(
            text = "Indian government departments publish separate, unstructured notification PDFs. This pipeline crawls official sites and uses Gemini AI 1.5 Flash to extract eligibility fields for verified matching.",
            fontSize = 12.sp,
            color = HighDensityTextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        // --- SECTION 1: BOT CRAWLER SIMULATOR ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, HighDensityBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. Official Scraper / Bot Crawler",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = HighDensityTextPrimary
                )
                Text(
                    text = "Monitors UPSC, SSC, IBPS, Railways, and state PSC sites.",
                    fontSize = 11.sp,
                    color = HighDensityTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Log display terminal
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A)) // dark slate background
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        if (crawlerLogs.isEmpty()) {
                            Text("> Crawler idle. Click Scan to simulate scanning government portals...", color = Color(0xFF38BDF8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        } else {
                            crawlerLogs.forEach { log ->
                                Text("> $log", color = Color(0xFF34D399), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.simulateOfficialCrawler() },
                    modifier = Modifier.fillMaxWidth().height(40.dp).testTag("simulate_crawl_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                    enabled = !isCrawling
                ) {
                    if (isCrawling) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crawling...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan Official Portals", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECTION 2: GEMINI AI EXTRACTION LAYER ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, HighDensityBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Gemini AI Extraction Console",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = HighDensityTextPrimary
                )
                Text(
                    text = "Converts unstructured notice text into structured schemas.",
                    fontSize = 11.sp,
                    color = HighDensityTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Presets
                Text("Select Sample Gazette Text Preset:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HighDensityTextPrimary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { customTextToParse = preset1 },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensitySecondaryBg, contentColor = HighDensitySecondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.0f).height(34.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("SSC GD Notice", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Button(
                        onClick = { customTextToParse = preset2 },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensitySecondaryBg, contentColor = HighDensitySecondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.0f).height(34.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("DSSSB Teacher", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customTextToParse,
                    onValueChange = { customTextToParse = it },
                    label = { Text("Paste Raw Gazette / PDF Text") },
                    placeholder = { Text("E.g., Staff Selection Commission is recruiting...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("ai_text_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.parseNotificationWithAI(customTextToParse) },
                    modifier = Modifier.fillMaxWidth().height(40.dp).testTag("parse_ai_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                    enabled = !isParsing && customTextToParse.isNotBlank()
                ) {
                    if (isParsing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini AI Parsing...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Parse with Gemini 1.5 Flash", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // AI Parsing Error
                parsingError?.let { err ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Error: $err", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // AI Parsing Success Comparison Matrix (Original vs Extracted)
                parsedResult?.let { result ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = HighDensityBorder)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "EXTRACTED SCHEMA SIDE-BY-SIDE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HighDensityPrimary,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HighDensityBg)
                            .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SchemaRow(label = "Job Title", value = result.title)
                        SchemaRow(label = "Conducting Org", value = result.organization)
                        SchemaRow(label = "Total Vacancies", value = "${result.vacancyCount}+")
                        SchemaRow(label = "Age Constraints", value = "${result.minAge} - ${result.maxAge} Yrs")
                        SchemaRow(label = "Min Education", value = result.educationRequired)
                        SchemaRow(label = "Min Height Standard", value = if (result.minHeightCm > 0) "${result.minHeightCm} cm" else "None")
                        SchemaRow(
                            label = "Source Trust Score",
                            value = if (result.isGovSource) "100% Trusted (.gov.in)" else "70% (Non-govt domain)"
                        )
                        SchemaRow(label = "AI Confidence", value = "${result.confidenceScore}%")
                        SchemaRow(label = "AI Reasoning", value = result.aiReasoning)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.approveNotificationByAdmin(result) },
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("admin_approve_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen) // green success
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("One-Click Admin Approve & Broadcast", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECTION 3: ACTIVE PENDING QUEUE ---
        val pendingReview = allJobs.filter { !it.isApprovedByAdmin }
        if (pendingReview.isNotEmpty()) {
            Text(
                text = "Unapproved Submissions Queue (${pendingReview.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = HighDensityTextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            pendingReview.forEach { job ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, HighDensityBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = job.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HighDensityTextPrimary)
                        Text(text = "Extracted: ${job.organization} • Qual: ${job.educationRequired}", fontSize = 11.sp, color = HighDensityTextSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.approveNotificationByAdmin(job) },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.0f).height(32.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            OutlinedButton(
                                onClick = { viewModel.deleteNotification(job.id) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.0f).height(32.dp),
                                contentPadding = PaddingValues(0.dp),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Text("Discard", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun SchemaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(text = "$label:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = HighDensityTextMuted, modifier = Modifier.width(110.dp))
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HighDensityTextSecondary, modifier = Modifier.weight(1.0f))
    }
}

// --- BOTTOM NAVIGATION BAR ---
@Composable
fun BottomNavBar(
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    viewModel: SarkariViewModel
) {
    val items = listOf(
        Triple(0, Icons.Default.Work, "tab_jobs"),
        Triple(1, Icons.Default.CloudSync, "tab_tracker"),
        Triple(2, Icons.Default.Person, "tab_profile"),
        Triple(3, Icons.Default.Bolt, "tab_pipeline")
    )

    Column {
        Divider(color = HighDensityBorder, thickness = 1.dp)
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            items.forEach { (index, icon, transKey) ->
                val isSelected = activeTab == index
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(index) },
                    icon = { 
                        Icon(
                            imageVector = icon, 
                            contentDescription = viewModel.getTranslation(transKey),
                            modifier = Modifier.size(20.dp)
                        ) 
                    },
                    label = { 
                        Text(
                            text = viewModel.getTranslation(transKey), 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis, 
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = HighDensityPrimary,
                        selectedTextColor = HighDensityPrimary,
                        unselectedIconColor = HighDensityTextSecondary,
                        unselectedTextColor = HighDensityTextSecondary,
                        indicatorColor = HighDensityPrimary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_item_$index")
                )
            }
        }
    }
}
