package com.genalpha.cricketacademy.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genalpha.cricketacademy.R
import com.genalpha.cricketacademy.data.AdmissionDraft
import com.genalpha.cricketacademy.data.AcademyExpense
import com.genalpha.cricketacademy.data.ManagerSession
import com.genalpha.cricketacademy.data.OperationResult
import com.genalpha.cricketacademy.data.PaymentFollowUp
import com.genalpha.cricketacademy.data.PendingAdmission
import com.genalpha.cricketacademy.data.Student
import com.genalpha.cricketacademy.data.StudentDraft
import com.genalpha.cricketacademy.data.StudentPayment
import com.genalpha.cricketacademy.data.StudentTimelineItem
import com.genalpha.cricketacademy.data.addMonthsForPlan
import com.genalpha.cricketacademy.data.calculateAgeFromDate
import com.genalpha.cricketacademy.data.cardTimelineLabel
import com.genalpha.cricketacademy.data.currentDatePickerValues
import com.genalpha.cricketacademy.data.daysSince
import com.genalpha.cricketacademy.data.displayDate
import com.genalpha.cricketacademy.data.displayTimelineStamp
import com.genalpha.cricketacademy.data.feeStatusLabel
import com.genalpha.cricketacademy.data.isActive
import com.genalpha.cricketacademy.data.isSpecialTraining
import com.genalpha.cricketacademy.data.isFeesPending
import com.genalpha.cricketacademy.data.isPaymentPendingVerification
import com.genalpha.cricketacademy.data.isRenewalPending
import com.genalpha.cricketacademy.data.latestRenewal
import com.genalpha.cricketacademy.data.membershipDateLabel
import com.genalpha.cricketacademy.data.nextRenewalCycleDate
import com.genalpha.cricketacademy.data.renewalStatus
import com.genalpha.cricketacademy.data.studentType
import com.genalpha.cricketacademy.data.tenureBadgeLabel
import com.genalpha.cricketacademy.data.todayIsoDate
import com.genalpha.cricketacademy.data.toDraft
import com.genalpha.cricketacademy.data.trackingCaption
import com.genalpha.cricketacademy.data.trainingDurationLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

private val BrandBlue = Color(0xFF1551AD)
private val BrandBlueDeep = Color(0xFF08142F)
private val BrandGold = Color(0xFFF4BE2E)
private val BrandGreen = Color(0xFF1A7D56)
private val BrandRed = Color(0xFFC43A3A)
private val SurfaceTint = Color(0xFFF4F8FF)
private val DarkInfoContainer = Color(0xFF1A2740)
private val DarkInfoText = Color(0xFF9BC7FF)
private val DarkSuccessContainer = Color(0xFF173527)
private val DarkSuccessText = Color(0xFF99E2B8)
private val DarkWarningContainer = Color(0xFF433112)
private val DarkWarningText = Color(0xFFFFD875)
private val DarkDangerContainer = Color(0xFF462028)
private val DarkDangerText = Color(0xFFFFB0BB)
private val DarkMutedContainer = Color(0xFF273141)
private val DarkMutedText = Color(0xFFBCC7DA)
private val DarkAttentionCard = Color(0xFF211A12)

private fun adaptiveSp(base: Float, fontScale: Float, minRatio: Float = 0.72f): TextUnit {
    return (base / fontScale.coerceAtLeast(1f))
        .coerceAtLeast(base * minRatio)
        .sp
}
private val DarkAttentionBorder = Color(0x66D7A12B)
private val AlertBlue = Color(0xFF2266C9)
private const val MANAGER_VIEW_PIN = "290326"
private const val CompactMaxFontScale = 0.90f

private fun compactDensityScale(density: Density): Float = when {
    density.density >= 3.2f -> 0.84f
    density.density >= 2.8f -> 0.88f
    else -> 0.92f
}

private data class SlotOption(
    val value: String,
    val label: String,
)

private val AdmissionSlotOptions = listOf(
    SlotOption("6AM", "6:00 - 7:30 AM"),
    SlotOption("7:30AM", "7:30 - 9:00 AM"),
    SlotOption("4PM", "4:00 - 5:30 PM"),
    SlotOption("5:30PM", "5:30 - 7:00 PM"),
    SlotOption("7PM", "7:00 - 8:30 PM"),
)
private const val AdmissionOneTimeFee = 500.0
private const val JerseyExtraPairFee = 750.0
private val AdmissionFeePlanOptions = listOf(
    SlotOption("monthly", "Monthly"),
    SlotOption("quarterly", "3 months - 5% off"),
    SlotOption("halfyearly", "6 months - 10% off"),
    SlotOption("special", "Special training"),
    SlotOption("custom", "Custom amount"),
)
private val AdmissionFilledByOptions = listOf("Parent / Guardian", "Coach", "Manager")

private fun admissionPlanBase(plan: String): Double = when (plan) {
    "quarterly" -> 9975.0
    "halfyearly" -> 18900.0
    "special" -> 10000.0
    "custom" -> 0.0
    else -> 3500.0
}

private fun chargeableJerseyPairs(pairText: String): Int =
    (pairText.toIntOrNull() ?: 0).coerceAtLeast(0)

private fun extraJerseyAmount(pairText: String): Double =
    chargeableJerseyPairs(pairText) * JerseyExtraPairFee

private fun admissionAmountLabel(amount: Double): String =
    "Rs ${String.format(Locale.US, "%,d", amount.toInt())}"

private data class JoiningFeeSplit(
    val coachingFee: Double,
    val admissionFee: Double,
    val jerseyAmount: Double,
    val totalFeeAmount: Double,
)

private fun joiningFeeSplitForPlan(student: Student, plan: String): JoiningFeeSplit {
    val base = when (plan) {
        "quarterly" -> 9975.0
        "halfyearly" -> 18900.0
        "special" -> 10000.0
        "custom" -> 0.0
        else -> 3500.0
    }
    val admissionFee = if (plan == "special" || plan == "custom") 0.0 else AdmissionOneTimeFee
    val jerseyAmount = student.jerseyPairs.coerceAtLeast(0) * JerseyExtraPairFee
    return JoiningFeeSplit(
        coachingFee = base,
        admissionFee = admissionFee,
        jerseyAmount = jerseyAmount,
        totalFeeAmount = base + admissionFee + jerseyAmount,
    )
}

private fun joiningPaymentAmountForPlan(student: Student, plan: String): Double {
    return joiningFeeSplitForPlan(student, plan).totalFeeAmount
}

private fun planDiscountLabel(plan: String): String = when (plan) {
    "quarterly" -> "5% discount applied"
    "halfyearly" -> "10% discount applied"
    else -> ""
}

private val BatsmanOptions = listOf("Right-handed batsman", "Left-handed batsman")
private val BowlingOptions = listOf(
    "Right-arm fast bowler",
    "Left-arm fast bowler",
    "Right-arm off spinner",
    "Left-arm leg spinner",
)
private val AdmissionYears = ((Calendar.getInstance().get(Calendar.YEAR) - 4) downTo 2010).toList()
private val AdmissionMonths = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)
private val UiTimeSlots = listOf("6AM", "7:30AM", "4PM", "5:30PM", "7PM")
private val JerseySizeOptions = listOf("", "22", "24", "26", "28", "30", "32", "34", "36", "38", "40", "42")
private fun jerseySizeLabel(size: String): String = when (size) {
    "" -> "Not set"
    "38" -> "38 - Medium"
    else -> size
}

private val AcademyLightScheme = lightColorScheme(
    primary = BrandBlue,
    secondary = BrandGold,
    tertiary = BrandGreen,
    background = Color(0xFFEDF3FB),
    surface = Color.White,
    onSurface = Color(0xFF102547),
    onBackground = Color(0xFF102547),
)

private val AcademyDarkScheme = darkColorScheme(
    primary = Color(0xFF7FB2FF),
    secondary = BrandGold,
    tertiary = Color(0xFF57C497),
    background = Color(0xFF08111F),
    surface = Color(0xFF111D31),
    onSurface = Color(0xFFF3F7FF),
    onBackground = Color(0xFFF3F7FF),
)

private data class BadgeTone(
    val container: Color,
    val text: Color,
)

private enum class AppView(val label: String) {
    Admission("Admission"),
    Player("Attendance"),
    Manager("Staff"),
    Finance("Finance"),
}

@Composable
private fun appBackgroundBrush(): Brush {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF050B16),
                Color(0xFF0C1A2C),
                Color(0xFF07101D),
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFEAF2FB),
                Color(0xFFFFF7DD),
                Color(0xFFF7FAFF),
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)

@Composable
private fun PublicViewHeader(
    title: String,
    subtitle: String,
) {
    val fontScale = LocalDensity.current.fontScale
    val isAdmission = title.contains("Admission", ignoreCase = true)
    val isFinance = title.contains("Finance", ignoreCase = true)
    val accent = when {
        isFinance -> BrandGreen
        isAdmission -> BrandGold
        else -> BrandBlue
    }
    Surface(
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)),
        tonalElevation = 1.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = if (isAdmission) 0.16f else 0.10f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                        )
                    )
                )
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Text(
                        text = "GEN ALPHA CRICKET ACADEMY",
                        color = BrandBlue,
                        fontSize = adaptiveSp(10f, fontScale, minRatio = 0.78f),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.0.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = adaptiveSp(24f, fontScale, minRatio = 0.72f),
                        lineHeight = adaptiveSp(27f, fontScale, minRatio = 0.72f),
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                        fontSize = adaptiveSp(13f, fontScale, minRatio = 0.76f),
                        lineHeight = adaptiveSp(18f, fontScale, minRatio = 0.76f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BrandPosterCard(
                    modifier = Modifier.widthIn(min = 62.dp, max = 78.dp),
                    imageModifier = Modifier.height(82.dp),
                )
            }
        }
    }
}

@Composable
private fun PlayerViewHeader(
    presentCount: Int,
    totalCount: Int,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
) {
    val fontScale = LocalDensity.current.fontScale
    val remaining = (totalCount - presentCount).coerceAtLeast(0)
    Surface(
        shape = RoundedCornerShape(30.dp),
        color = Color.Transparent,
        tonalElevation = 1.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0B2A5E), BrandBlue, Color(0xFF09204B))
                    ),
                    RoundedCornerShape(30.dp),
                )
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(
                            text = "PLAYER CHECK-IN",
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = adaptiveSp(10f, fontScale, minRatio = 0.78f),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.0.sp,
                        )
                        Text(
                            text = "Tap once. Done.",
                            color = Color.White,
                            fontSize = adaptiveSp(26f, fontScale, minRatio = 0.70f),
                            lineHeight = adaptiveSp(28f, fontScale, minRatio = 0.70f),
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Young players can mark attendance safely with large cards below.",
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = adaptiveSp(12f, fontScale, minRatio = 0.78f),
                            lineHeight = adaptiveSp(17f, fontScale, minRatio = 0.78f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.14f),
                            contentColor = Color.White,
                        ),
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh attendance")
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AttendanceHeroMetric(
                        modifier = Modifier.weight(1f),
                        title = "Present",
                        value = presentCount,
                        accent = BrandGreen,
                    )
                    AttendanceHeroMetric(
                        modifier = Modifier.weight(1f),
                        title = "Pending",
                        value = remaining,
                        accent = BrandGold,
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendanceHeroMetric(
    modifier: Modifier,
    title: String,
    value: Int,
    accent: Color,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title.uppercase(Locale.getDefault()),
                color = Color.White.copy(alpha = 0.70f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
            )
            Text(
                value.toString(),
                color = accent,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun BrandPosterCard(
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.68f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
    ) {
        Image(
            painter = painterResource(id = R.drawable.gen_alpha_badge_transparent),
            contentDescription = "Gen Alpha Cricket Academy logo",
            contentScale = ContentScale.Fit,
            modifier = imageModifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun rememberBringIntoViewOnFocusModifier(): Modifier {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    return Modifier
        .bringIntoViewRequester(bringIntoViewRequester)
        .onFocusEvent { focusState ->
            if (focusState.isFocused) {
                scope.launch {
                    bringIntoViewRequester.bringIntoView()
                }
            }
        }
}

@Composable
private fun themedBadgeTone(
    lightContainer: Color,
    lightText: Color,
    darkContainer: Color,
    darkText: Color,
): BadgeTone {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDarkTheme) {
        BadgeTone(darkContainer, darkText)
    } else {
        BadgeTone(lightContainer, lightText)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademyApp(viewModel: AcademyViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var rosterMovementMonthKey by rememberSaveable { mutableStateOf<String?>(null) }
    var rosterMovementType by rememberSaveable { mutableStateOf<String?>(null) }
    val baseFilteredKids = remember(
        uiState.kids,
        uiState.selectedSlotFilter,
        uiState.searchQuery,
        uiState.rosterSortKey,
        uiState.rosterSortAscending,
        uiState.payments,
        uiState.rosterStatusFilter,
        uiState.rosterJerseyFilter,
        uiState.rosterTypeFilter,
        uiState.rosterFeePaidFilter,
        uiState.rosterFeeDueFilter,
    ) { viewModel.filteredKids() }
    val filteredKids = remember(baseFilteredKids, rosterMovementMonthKey, rosterMovementType) {
        if (rosterMovementMonthKey.isNullOrBlank() || rosterMovementType.isNullOrBlank()) {
            baseFilteredKids
        } else {
            baseFilteredKids.filter { student ->
                studentMatchesMovementFilter(student, rosterMovementMonthKey.orEmpty(), rosterMovementType.orEmpty())
            }
        }
    }
    val rosterMovementLabel = remember(rosterMovementMonthKey, rosterMovementType) {
        movementFilterDisplayLabel(rosterMovementMonthKey, rosterMovementType)
    }
    val stats = remember(uiState.kids) { viewModel.stats() }
    val slotSummary = remember(uiState.kids, uiState.selectedSlotFilter) { viewModel.slotSummary() }
    val alertKids = remember(uiState.kids, uiState.payments) { viewModel.alertKids() }
    val rosterSections = remember(filteredKids, uiState.rosterSortKey) { buildRosterSections(filteredKids, uiState.rosterSortKey) }
    val activePlayers = remember(uiState.kids) { uiState.kids.filter { it.isActive() } }

    var selectedView by rememberSaveable { mutableStateOf(AppView.Admission) }
    val mainListState = rememberLazyListState()
    val financePullRefreshState = rememberPullToRefreshState(
        positionalThreshold = 150.dp,
        enabled = {
            selectedView == AppView.Finance &&
                mainListState.firstVisibleItemIndex == 0 &&
                mainListState.firstVisibleItemScrollOffset == 0 &&
                !uiState.isFinanceLoading
        },
    )
    var pendingProtectedView by rememberSaveable { mutableStateOf(AppView.Manager) }
    var showManagerPinSheet by rememberSaveable { mutableStateOf(false) }
    var showLoginSheet by rememberSaveable { mutableStateOf(false) }
    var showAdmissionSheet by rememberSaveable { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var showEditorSheet by rememberSaveable { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var showDetailSheet by rememberSaveable { mutableStateOf(false) }
    var rosterScrollTargetId by rememberSaveable { mutableStateOf<String?>(null) }
    var highlightedRosterStudentId by rememberSaveable { mutableStateOf<String?>(null) }
    var renewalStudent by remember { mutableStateOf<Student?>(null) }
    var showAttendanceHistory by rememberSaveable { mutableStateOf(false) }
    var attendanceHistoryCache by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    var timelineCache by remember { mutableStateOf<Map<String, List<StudentTimelineItem>>>(emptyMap()) }
    var attendanceHistoryLoadingId by rememberSaveable { mutableStateOf<String?>(null) }
    var timelineLoadingId by rememberSaveable { mutableStateOf<String?>(null) }
    var playerSearchQuery by rememberSaveable { mutableStateOf("") }
    var playerSlotFilter by rememberSaveable { mutableStateOf("") }
    val playerFiltered = remember(activePlayers, playerSearchQuery, playerSlotFilter) {
        activePlayers.filter { student ->
            val slotMatch = when {
                playerSlotFilter.isBlank() -> true
                playerSlotFilter == "not-set" -> student.timeSlot.isBlank()
                else -> student.timeSlot == playerSlotFilter
            }
            val search = playerSearchQuery.trim().lowercase()
            val searchMatch = search.isBlank() ||
                student.name.lowercase().contains(search) ||
                (student.regNo?.toString()?.contains(search) == true) ||
                student.timeSlot.lowercase().contains(search)
            slotMatch && searchMatch
        }
    }
    val playerSections = remember(playerFiltered) { buildAttendanceSections(playerFiltered) }
    val todaysPresentCount = remember(uiState.todayAttendanceIds, activePlayers) {
        activePlayers.count { uiState.todayAttendanceIds.contains(it.id) }
    }
    val attendanceFollowUps = remember(activePlayers, uiState.recentAttendanceDates, uiState.todayAttendanceIds) {
        buildAttendanceFollowUps(activePlayers, uiState.recentAttendanceDates, uiState.todayAttendanceIds)
    }
    val attendanceStreaks = remember(activePlayers, uiState.recentAttendanceDates, uiState.todayAttendanceIds) {
        buildAttendanceStreaks(activePlayers, uiState.recentAttendanceDates, uiState.todayAttendanceIds)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(selectedView) {
        if (selectedView == AppView.Player) {
            viewModel.loadTodayAttendance()
        }
        if (!(selectedView == AppView.Manager && rosterScrollTargetId != null)) {
            mainListState.scrollToItem(0)
        }
    }

    LaunchedEffect(rosterScrollTargetId, selectedView, rosterSections, rosterMovementLabel) {
        val targetId = rosterScrollTargetId ?: return@LaunchedEffect
        if (selectedView != AppView.Manager) return@LaunchedEffect
        val targetIndex = rosterLazyListIndexForStudent(
            sections = rosterSections,
            studentId = targetId,
            hasMovementBanner = rosterMovementLabel != null,
        ) ?: return@LaunchedEffect
        mainListState.animateScrollToItem(targetIndex)
        delay(2400)
        if (highlightedRosterStudentId == targetId) {
            highlightedRosterStudentId = null
        }
        if (rosterScrollTargetId == targetId) {
            rosterScrollTargetId = null
        }
    }

    LaunchedEffect(uiState.session?.accessToken) {
        if (uiState.session != null) {
            viewModel.loadFinance()
            viewModel.loadPendingAdmissions()
        }
    }

    LaunchedEffect(financePullRefreshState.isRefreshing) {
        if (financePullRefreshState.isRefreshing) {
            viewModel.loadFinance()
            financePullRefreshState.endRefresh()
        }
    }

    LaunchedEffect(showDetailSheet, selectedStudent?.id) {
        val studentId = selectedStudent?.id
        if (showDetailSheet && studentId != null && studentId !in attendanceHistoryCache && attendanceHistoryLoadingId != studentId) {
            attendanceHistoryLoadingId = studentId
            runCatching { viewModel.attendanceHistory(studentId) }
                .onSuccess { history ->
                    attendanceHistoryCache = attendanceHistoryCache + (studentId to history)
                }
            attendanceHistoryLoadingId = null
        }
        if (showDetailSheet && studentId != null && studentId !in timelineCache && timelineLoadingId != studentId) {
            timelineLoadingId = studentId
            runCatching { viewModel.studentTimeline(studentId) }
                .onSuccess { timeline ->
                    timelineCache = timelineCache + (studentId to timeline)
                }
            timelineLoadingId = null
        }
    }

    val jumpToRosterStudent: (Student) -> Unit = { student ->
        selectedView = AppView.Manager
        showDetailSheet = false
        showEditorSheet = false
        viewModel.setSearchQuery("")
        viewModel.setSlotFilter("all")
        viewModel.setRosterStatusFilter("active")
        viewModel.setRosterJerseyFilter("all")
        viewModel.setRosterTypeFilter("all")
        viewModel.setRosterFeePaidFilter("all")
        viewModel.setRosterFeeDueFilter("all")
        rosterMovementMonthKey = null
        rosterMovementType = null
        highlightedRosterStudentId = student.id
        rosterScrollTargetId = student.id
    }

    fun resetStaffRosterFilters() {
        viewModel.setRosterStatusFilter("active")
        viewModel.setRosterFeePaidFilter("all")
    }

    val colorScheme = if (uiState.darkModeEnabled) AcademyDarkScheme else AcademyLightScheme
    val density = LocalDensity.current
    val safeDensity = remember(density) {
        Density(
            density = density.density * compactDensityScale(density),
            fontScale = density.fontScale.coerceAtMost(CompactMaxFontScale),
        )
    }

    CompositionLocalProvider(LocalDensity provides safeDensity) {
    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                AppBottomBar(
                    selectedView = selectedView,
                    showFinance = uiState.session != null,
                    onSelected = { view ->
                        if (view == AppView.Finance && uiState.session != null) {
                            selectedView = view
                        } else if (view == AppView.Manager && selectedView != view) {
                            resetStaffRosterFilters()
                            pendingProtectedView = view
                            showManagerPinSheet = true
                        } else {
                            if (view == AppView.Manager) {
                                resetStaffRosterFilters()
                            }
                            selectedView = view
                        }
                    },
                )
            },
            floatingActionButton = {
                if (selectedView == AppView.Manager && viewModel.canEdit()) {
                    val scope = rememberCoroutineScope()
                    val showScrollTop = mainListState.firstVisibleItemIndex > 2
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AnimatedVisibility(
                            visible = showScrollTop,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            SmallFloatingActionButton(
                                onClick = {
                                    scope.launch { mainListState.animateScrollToItem(0) }
                                },
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll to top", modifier = Modifier.size(20.dp))
                            }
                        }
                        SmallFloatingActionButton(
                            onClick = {
                                scope.launch {
                                    viewModel.loadKids()
                                    mainListState.animateScrollToItem(0)
                                }
                            },
                            containerColor = BrandGold,
                            contentColor = BrandBlueDeep,
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh roster", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(appBackgroundBrush())
            ) {
                LazyColumn(
                    state = mainListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .nestedScroll(financePullRefreshState.nestedScrollConnection),
                    contentPadding = PaddingValues(
                        start = 14.dp,
                        end = 14.dp,
                        top = innerPadding.calculateTopPadding() + 8.dp,
                        bottom = innerPadding.calculateBottomPadding() + 196.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (selectedView == AppView.Manager) {
                    item {
                            HeaderSection(
                                session = uiState.session,
                                darkModeEnabled = uiState.darkModeEnabled,
                            onToggleDarkMode = viewModel::toggleDarkMode,
                            onLogin = { showLoginSheet = true },
                            onLogout = {
                                viewModel.logout()
                                selectedView = AppView.Admission
                                scope.launch {
                                    snackbarHostState.showSnackbar("Staff logged out.")
                                }
                            },
                        )
                    }

                    item {
                        CriticalAlertSection(
                            alertKids = alertKids,
                            payments = uiState.payments,
                            onStudentClick = jumpToRosterStudent,
                        )
                    }

                    item {
                        AlertSection(
                            alertKids = alertKids,
                            payments = uiState.payments,
                            onStudentClick = jumpToRosterStudent,
                        )
                    }

                    item {
                        StatsSection(
                            joined = stats.joinedCount,
                            active = stats.activeCount,
                            returning = stats.returningCount,
                        )
                    }

                    if (uiState.session != null && (uiState.pendingAdmissions.isNotEmpty() || uiState.isAdmissionReviewLoading)) {
                        item {
                            AdmissionReviewSection(
                                admissions = uiState.pendingAdmissions,
                                isLoading = uiState.isAdmissionReviewLoading,
                                onApprove = viewModel::approveAdmission,
                                onReject = viewModel::rejectAdmission,
                                onRemind = viewModel::sendAdmissionReminder,
                                onMessage = { message ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(message)
                                    }
                                },
                            )
                        }
                    }

                    item {
                        StudentMovementSection(
                            students = uiState.kids,
                            onMovementClick = { month, type ->
                                viewModel.setSearchQuery("")
                                viewModel.setSlotFilter("all")
                                viewModel.setRosterStatusFilter(if (type == "left") "all" else "active")
                                viewModel.setRosterJerseyFilter("all")
                                viewModel.setRosterTypeFilter("all")
                                viewModel.setRosterFeePaidFilter("all")
                                viewModel.setRosterFeeDueFilter("all")
                                rosterMovementMonthKey = month.key
                                rosterMovementType = type
                            },
                        )
                    }

                    item {
                        SlotsSection(
                            summaries = slotSummary,
                            selected = uiState.selectedSlotFilter,
                            onSelected = viewModel::setSlotFilter,
                        )
                    }

                    item {
                        SectionHeader(
                            title = "Academy roster",
                            subtitle = if (viewModel.canEdit()) {
                                "Search quickly, scan by batch, and tap any player to manage renewals or status."
                            } else {
                                "Public users can review the register. Manager login unlocks edit controls."
                            },
                        )
                    }

                    item {
                        RosterToolbar(
                            searchQuery = uiState.searchQuery,
                            sortKey = uiState.rosterSortKey,
                            sortAscending = uiState.rosterSortAscending,
                            statusFilter = uiState.rosterStatusFilter,
                            jerseyFilter = uiState.rosterJerseyFilter,
                            typeFilter = uiState.rosterTypeFilter,
                            feeDueFilter = uiState.rosterFeeDueFilter,
                            visibleCount = filteredKids.size,
                            totalCount = uiState.kids.size,
                            isRefreshing = uiState.isRefreshing,
                            onSearchChange = viewModel::setSearchQuery,
                            onSortChange = viewModel::setRosterSort,
                            onStatusFilterChange = viewModel::setRosterStatusFilter,
                            onJerseyFilterChange = viewModel::setRosterJerseyFilter,
                            onTypeFilterChange = viewModel::setRosterTypeFilter,
                            onFeeDueFilterChange = viewModel::setRosterFeeDueFilter,
                            onRefresh = {
                                scope.launch {
                                    viewModel.loadKids()
                                }
                            },
                        )
                    }

                    if (rosterMovementLabel != null) {
                        item {
                            MovementRosterFilterBanner(
                                label = rosterMovementLabel,
                                onClear = {
                                    rosterMovementMonthKey = null
                                    rosterMovementType = null
                                },
                            )
                        }
                    }

                    if (uiState.isLoading) {
                        item {
                            LoadingPanel()
                        }
                    } else if (filteredKids.isEmpty()) {
                        item {
                            EmptyPanel(message = viewModel.emptyStateMessage())
                        }
                    } else {
                        rosterSections.forEach { section ->
                            item(key = "section-${section.key}") {
                                RosterSectionHeader(title = section.title, count = section.students.size)
                            }
                            items(section.students, key = { it.id }) { student ->
                                RosterRow(
                                    student = student,
                                    payments = uiState.payments,
                                    paymentFollowUp = uiState.paymentFollowUps.firstOrNull { it.studentId == student.id },
                                    isManager = viewModel.canEdit(),
                                    highlighted = highlightedRosterStudentId == student.id,
                                    onOpen = {
                                        selectedStudent = student
                                        showDetailSheet = true
                                    },
                                    onEdit = {
                                        editingStudent = student
                                        selectedStudent = student
                                        showEditorSheet = true
                                    },
                                    onRenew = if ((student.isFeesPending() || student.isRenewalPending(uiState.payments)) && student.isActive()) {
                                        {
                                            renewalStudent = student
                                            selectedStudent = student
                                        }
                                    } else null,
                                    onSendReminder = if ((student.isFeesPending() || student.isRenewalPending(uiState.payments)) && student.isActive()) {
                                        {
                                            scope.launch {
                                                val result = if (student.isFeesPending()) {
                                                    viewModel.sendAdmissionReminder(
                                                        PendingAdmission(
                                                            id = student.id,
                                                            regNo = student.regNo,
                                                            applicantName = student.name,
                                                            age = student.age,
                                                            joinDate = student.joinDate,
                                                            feesPaid = student.feesPaid,
                                                            amountPaid = student.amountPaid,
                                                            timeSlot = student.timeSlot,
                                                            parentContactNo = student.parentContactNo,
                                                            alternateContactNo = student.alternateContactNo,
                                                            fatherGuardianName = student.fatherGuardianName,
                                                            schoolCollege = student.schoolCollege
                                                        )
                                                    )
                                                } else {
                                                    viewModel.sendRenewalReminder(student)
                                                }
                                                snackbarHostState.showSnackbar(result.message)
                                            }
                                        }
                                    } else null,
                                    onToggleStatus = {
                                        scope.launch {
                                            val result = viewModel.toggleStatus(student)
                                            snackbarHostState.showSnackbar(result.message)
                                        }
                                    },
                                    onJerseyPairsChange = if (viewModel.canEdit()) {
                                        { nextPairs, amount ->
                                            scope.launch {
                                                val result = viewModel.updateJerseyPairs(student, nextPairs, amount)
                                                snackbarHostState.showSnackbar(result.message)
                                            }
                                        }
                                    } else null,
                                )
                            }
                        }
                    }
                    } else if (selectedView == AppView.Finance) {
                        item {
                            PublicViewHeader(
                                title = "Finance",
                                subtitle = "Track collected fees and simple academy expenses.",
                            )
                        }
                        item {
                            FinancePanel(
                                uiState = uiState,
                                onAddExpense = viewModel::addExpense,
                                onDeleteExpense = viewModel::deleteExpense,
                            )
                        }
                    } else if (selectedView == AppView.Admission) {
                        item {
                            PublicViewHeader(
                                title = "Parent Admission",
                                subtitle = "Open the online form for first-time player admission.",
                            )
                        }
                        item {
                            AdmissionActionsSection(
                                onOpen = {
                                    showAdmissionSheet = true
                                },
                            )
                        }
                    } else {
                        item {
                            PlayerViewHeader(
                                presentCount = todaysPresentCount,
                                totalCount = activePlayers.size,
                                onRefresh = {
                                    scope.launch {
                                        viewModel.loadTodayAttendance()
                                        viewModel.loadRecentAttendanceDates()
                                    }
                                },
                                isRefreshing = uiState.isAttendanceRefreshing,
                            )
                        }
                        if (attendanceFollowUps.isNotEmpty()) {
                            item {
                                AttendanceFollowUpNudge(followUps = attendanceFollowUps)
                            }
                        }
                        item {
                            PlayerAttendanceToolbar(
                                searchQuery = playerSearchQuery,
                                onSearchChange = { playerSearchQuery = it },
                                selectedSlot = playerSlotFilter,
                                onSlotSelected = { selected ->
                                    playerSlotFilter = if (selected == "all" || playerSlotFilter == selected) "" else selected
                                },
                                activePlayers = activePlayers,
                            )
                        }
                        if (activePlayers.isEmpty()) {
                            item {
                                EmptyPanel(message = "No active players are available for attendance yet.")
                            }
                        } else if (playerFiltered.isEmpty()) {
                            item {
                                EmptyPanel(message = "No players match the current attendance filter.")
                            }
                        } else {
                            playerSections.forEach { section ->
                                item(key = "attendance-section-${section.key}") {
                                    RosterSectionHeader(title = section.title, count = section.students.size)
                                }
                                items(section.students, key = { it.id }) { student ->
                                    val streak = attendanceStreaks.firstOrNull { it.student.id == student.id }
                                    AttendancePlayerCard(
                                        student = student,
                                        isPresent = uiState.todayAttendanceIds.contains(student.id),
                                        streak = streak,
                                        onMarkPresent = {
                                            scope.launch {
                                                val result = viewModel.markAttendance(student)
                                                snackbarHostState.showSnackbar(result.message)
                                            }
                                        },
                                        onUndoPresent = {
                                            scope.launch {
                                                val result = viewModel.unmarkAttendance(student)
                                                snackbarHostState.showSnackbar(result.message)
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                if (selectedView == AppView.Finance) {
                    PullToRefreshContainer(
                        state = financePullRefreshState,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding(),
                    )
                }

                if (showLoginSheet) {
                    LoginSheet(
                        lastEmail = uiState.lastEmail,
                        lastPassword = uiState.lastPassword,
                        isLoading = uiState.isAuthLoading,
                        onDismiss = { showLoginSheet = false },
                        onSubmit = { email, password ->
                            viewModel.login(email, password).also { result ->
                                if (result.success) {
                                    showLoginSheet = false
                                }
                                scope.launch {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                            }
                        },
                    )
                }

                if (showManagerPinSheet) {
                    ManagerPinSheet(
                        onDismiss = { showManagerPinSheet = false },
                        onUnlock = { pin ->
                            if (pin == MANAGER_VIEW_PIN) {
                                if (pendingProtectedView == AppView.Manager) {
                                    resetStaffRosterFilters()
                                }
                                selectedView = pendingProtectedView
                                showManagerPinSheet = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Staff dashboard unlocked.")
                                }
                                OperationResult(true, "Staff dashboard unlocked.")
                            } else {
                                OperationResult(false, "Incorrect manager PIN.")
                            }
                        },
                    )
                }

                if (showAdmissionSheet) {
                    AdmissionFormSheet(
                        onLoadRegNo = { viewModel.peekNextAdmissionRegNo() },
                        onDismiss = {
                            showAdmissionSheet = false
                        },
                        onSubmit = { draft ->
                            viewModel.submitAdmission(draft).also { result ->
                                if (result.success) {
                                    showAdmissionSheet = false
                                }
                                if (result.success && draft.feesPaid && !draft.paymentPendingVerification) {
                                    context.shareReceiptPdf(buildAndroidAdmissionReceipt(draft, result.message))
                                }
                                scope.launch {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                            }
                        },
                    )
                }

                if (showEditorSheet) {
                    PlayerEditorSheet(
                        editingStudent = editingStudent,
                        onDismiss = { showEditorSheet = false },
                        onSubmit = { draft ->
                            val wasUnpaid = editingStudent?.feesPaid == false
                            viewModel.saveStudent(draft, editingStudent).also { result ->
                                val paidNow = draft.feesPaid
                                val studentForReceipt = editingStudent
                                if (result.success) {
                                    showEditorSheet = false
                                    editingStudent = null
                                    selectedStudent = null
                                }
                                if (result.success && wasUnpaid && paidNow && studentForReceipt != null) {
                                    context.shareReceiptPdf(buildAndroidPlayerPaidReceipt(studentForReceipt, draft))
                                }
                                scope.launch {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                            }
                        },
                    )
                }

                if (showDetailSheet && selectedStudent != null) {
                    PlayerDetailSheet(
                        student = selectedStudent!!,
                        payments = uiState.payments,
                        paymentFollowUp = uiState.paymentFollowUps.firstOrNull { it.studentId == selectedStudent!!.id },
                        attendanceCount = uiState.attendanceCounts[selectedStudent!!.id] ?: 0,
                        timeline = timelineCache[selectedStudent!!.id].orEmpty(),
                        isTimelineLoading = timelineLoadingId == selectedStudent!!.id,
                        isManager = viewModel.canEdit(),
                        onDismiss = {
                            showDetailSheet = false
                            selectedStudent = null
                            showAttendanceHistory = false
                        },
                        onShowAttendanceHistory = { showAttendanceHistory = true },
                        onEdit = {
                            editingStudent = selectedStudent
                            showDetailSheet = false
                            showEditorSheet = true
                        },
                        onDelete = {
                            selectedStudent?.let { activeStudent ->
                                val result = viewModel.deleteStudent(activeStudent)
                                if (result.success) {
                                    showDetailSheet = false
                                    selectedStudent = null
                                }
                                scope.launch {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                            }
                        },
                        onDeletePayment = { paymentId ->
                            val result = viewModel.deletePayment(paymentId, selectedStudent!!)
                            scope.launch {
                                snackbarHostState.showSnackbar(result.message)
                            }
                        },
                        onRenew = {
                            renewalStudent = selectedStudent
                        },
                        onSendReminder = {
                            selectedStudent?.let { activeStudent ->
                                val result = viewModel.sendRenewalReminder(activeStudent)
                                if (result.success) {
                                    val refreshed = runCatching { viewModel.studentTimeline(activeStudent.id) }.getOrDefault(emptyList())
                                    timelineCache = timelineCache + (activeStudent.id to refreshed)
                                }
                                scope.launch { snackbarHostState.showSnackbar(result.message) }
                            }
                        },
                        onConfirmPayment = {
                            selectedStudent?.let { activeStudent ->
                                val followUp = uiState.paymentFollowUps.firstOrNull { it.studentId == activeStudent.id }
                                if (followUp == null) {
                                    scope.launch { snackbarHostState.showSnackbar("No pending payment proof found.") }
                                } else {
                                    val result = viewModel.confirmPendingPayment(activeStudent, followUp)
                                    if (result.success) {
                                        val refreshed = runCatching { viewModel.studentTimeline(activeStudent.id) }.getOrDefault(emptyList())
                                        timelineCache = timelineCache + (activeStudent.id to refreshed)
                                    }
                                    scope.launch { snackbarHostState.showSnackbar(result.message) }
                                }
                            }
                        },
                        onToggleStatus = {
                            selectedStudent?.let { activeStudent ->
                                val result = viewModel.toggleStatus(activeStudent)
                                if (result.success) {
                                    showDetailSheet = false
                                    selectedStudent = null
                                }
                                scope.launch {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                            }
                        },
                        onSendAdmissionReminder = {
                            selectedStudent?.let { activeStudent ->
                                scope.launch {
                                    val result = viewModel.sendAdmissionReminder(
                                        PendingAdmission(
                                            id = activeStudent.id,
                                            regNo = activeStudent.regNo,
                                            applicantName = activeStudent.name,
                                            age = activeStudent.age,
                                            joinDate = activeStudent.joinDate,
                                            feesPaid = activeStudent.feesPaid,
                                            amountPaid = activeStudent.amountPaid,
                                            timeSlot = activeStudent.timeSlot,
                                            parentContactNo = activeStudent.parentContactNo,
                                            alternateContactNo = activeStudent.alternateContactNo,
                                            fatherGuardianName = activeStudent.fatherGuardianName,
                                            schoolCollege = activeStudent.schoolCollege
                                        )
                                    )
                                    snackbarHostState.showSnackbar(result.message)
                                }
                            }
                        }
                    )
                }

                if (showAttendanceHistory && selectedStudent != null) {
                    AttendanceHistoryDialog(
                        studentName = selectedStudent!!.name,
                        initialDates = attendanceHistoryCache[selectedStudent!!.id],
                        onDismiss = { showAttendanceHistory = false },
                        onLoadAttendanceHistory = {
                            val loaded = viewModel.attendanceHistory(selectedStudent!!.id)
                            attendanceHistoryCache = attendanceHistoryCache + (selectedStudent!!.id to loaded)
                            loaded
                        },
                    )
                }

                if (renewalStudent != null) {
                    RenewalPaymentDialog(
                        student = renewalStudent!!,
                        payments = uiState.payments,
                        onDismiss = { renewalStudent = null },
                        onSubmit = { plan, months, amount, comment, paymentDate, coachingFee, admissionFee, jerseyAmount, totalFeeAmount, jerseySize, jerseyPairs ->
                            val studentForReceipt = renewalStudent!!
                            val isJoiningFee = studentForReceipt.isFeesPending()
                            val result = viewModel.recordRenewalPayment(
                                student = studentForReceipt,
                                planType = plan,
                                monthsCovered = months,
                                amount = amount,
                                comment = comment,
                                cycleDateOverride = if (isJoiningFee) studentForReceipt.joinDate else null,
                                paidOn = paymentDate,
                                isJoiningFee = isJoiningFee,
                                coachingFee = if (isJoiningFee) coachingFee else 0.0,
                                admissionFee = if (isJoiningFee) admissionFee else 0.0,
                                jerseyAmount = if (isJoiningFee) jerseyAmount else 0.0,
                                totalFeeAmount = if (isJoiningFee) totalFeeAmount else 0.0,
                                jerseySize = if (isJoiningFee) jerseySize else studentForReceipt.jerseySize,
                                jerseyPairs = if (isJoiningFee) jerseyPairs else studentForReceipt.jerseyPairs,
                            )
                            if (result.success) {
                                context.shareReceiptPdf(
                                    buildAndroidRenewalReceipt(
                                        student = studentForReceipt,
                                        plan = plan,
                                        months = months,
                                        amount = amount,
                                        paidOn = paymentDate,
                                        isJoiningFee = isJoiningFee,
                                    )
                                )
                                renewalStudent = null
                                showDetailSheet = false
                                selectedStudent = null
                            }
                            scope.launch { snackbarHostState.showSnackbar(result.message) }
                            result
                        },
                    )
                }
            }
        }
    }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeaderSection(
    session: ManagerSession?,
    darkModeEnabled: Boolean,
    onToggleDarkMode: () -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
) {
    val fontScale = LocalDensity.current.fontScale

    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF0A1E47), BrandBlue, Color(0xFF103A82))
                    ),
                    shape = RoundedCornerShape(30.dp),
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "GEN ALPHA CRICKET ACADEMY",
                            color = Color.White.copy(alpha = 0.76f),
                            fontSize = adaptiveSp(11f, fontScale, minRatio = 0.78f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                        )
                        Text(
                            text = "Staff Dashboard",
                            color = Color.White,
                            fontSize = adaptiveSp(24f, fontScale, minRatio = 0.72f),
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = adaptiveSp(27f, fontScale, minRatio = 0.72f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Track admissions, fees, renewals, jersey orders, and coaching batches in one place.",
                            color = Color.White.copy(alpha = 0.86f),
                            fontSize = adaptiveSp(14f, fontScale, minRatio = 0.72f),
                            lineHeight = adaptiveSp(20f, fontScale, minRatio = 0.72f),
                        )
                    }

                    BrandPosterCard(
                        modifier = Modifier.widthIn(min = 72.dp, max = 84.dp),
                        imageModifier = Modifier.height(104.dp),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledTonalIconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.14f),
                            contentColor = Color.White,
                        ),
                    ) {
                        Icon(
                            imageVector = if (darkModeEnabled) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = if (darkModeEnabled) "Switch to light mode" else "Switch to dark mode",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    if (session == null) {
                        Button(
                            onClick = onLogin,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = BrandGold,
                                contentColor = BrandBlueDeep,
                            ),
                        ) {
                            Icon(Icons.Outlined.Person, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Staff Login", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    } else {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White.copy(alpha = 0.12f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Text(
                                        text = "Staff signed in",
                                        color = Color.White.copy(alpha = 0.72f),
                                        fontSize = adaptiveSp(11f, fontScale, minRatio = 0.78f),
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = session.email,
                                        color = Color.White,
                                        fontSize = adaptiveSp(14f, fontScale, minRatio = 0.74f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                TextButton(onClick = onLogout) {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.Logout,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Text(
                                        "Logout",
                                        color = Color.White,
                                        fontSize = adaptiveSp(11f, fontScale, minRatio = 0.78f),
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CriticalAlertSection(
    alertKids: List<Student>,
    payments: List<StudentPayment>,
    onStudentClick: (Student) -> Unit,
) {
    val criticalKids = alertKids.filter { it.isCriticalReminder(payments) }
    if (criticalKids.isEmpty()) return

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF7D302F)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Immediate follow-up",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.9.sp,
            )
            Text(
                text = if (criticalKids.size == 1) "1 player over 10 days" else "${criticalKids.size} players over 10 days",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 23.sp,
            )
            AlertNameSection(
                title = "Call parent today",
                students = criticalKids,
                onStudentClick = onStudentClick,
            )
        }
    }
}

@Composable
private fun AlertSection(
    alertKids: List<Student>,
    payments: List<StudentPayment>,
    onStudentClick: (Student) -> Unit,
) {
    val standardAlertKids = alertKids.filterNot { it.isCriticalReminder(payments) }
    val feesPendingKids = standardAlertKids.filter { it.isFeesPending() }
    val renewalPendingKids = standardAlertKids.filter { it.isRenewalPending(payments) }
    val alertCount = standardAlertKids.size
    val hasCriticalOnly = alertCount == 0 && alertKids.any { it.isCriticalReminder(payments) }
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2266C9)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Alert",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.9.sp,
            )
            Text(
                text = when (alertCount) {
                    0 -> "No regular alerts"
                    1 -> "1 regular alert"
                    else -> "$alertCount regular alerts"
                },
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 26.sp,
            )

            if (alertKids.isEmpty()) {
                Text(
                    text = "All current join fees and renewals are up to date.",
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                )
            } else if (hasCriticalOnly) {
                Text(
                    text = "Regular alerts are clear. Immediate follow-up is shown above.",
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                )
            } else {
                if (feesPendingKids.isNotEmpty()) {
                    AlertNameSection(
                        title = "Fees to collect",
                        students = feesPendingKids,
                        onStudentClick = onStudentClick,
                    )
                }
                if (renewalPendingKids.isNotEmpty()) {
                    AlertNameSection(
                        title = "Renewal follow-up",
                        students = renewalPendingKids,
                        onStudentClick = onStudentClick,
                    )
                }
            }
        }
    }
}

private fun Student.isCriticalReminder(payments: List<StudentPayment>): Boolean {
    if (!isFeesPending() && !isRenewalPending(payments)) return false
    val dueDate = if (isFeesPending()) joinDate else nextRenewalCycleDate(payments)
    return daysSince(dueDate) > 10
}

@Composable
private fun AlertNameSection(
    title: String,
    students: List<Student>,
    onStudentClick: (Student) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.76f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        students.forEach { student ->
            Row(
                modifier = Modifier.clickable { onStudentClick(student) },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "\u2022",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = student.name,
                    color = Color.White.copy(alpha = 0.94f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdmissionActionsSection(
    onOpen: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                BrandGold.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                                BrandBlue.copy(alpha = 0.07f),
                            )
                        )
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = BrandBlue.copy(alpha = 0.10f),
                    ) {
                        Icon(
                            Icons.Outlined.PersonAddAlt1,
                            contentDescription = null,
                            tint = BrandBlue,
                            modifier = Modifier.padding(14.dp).size(26.dp),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "First-time admission",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            lineHeight = 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Parent details, training slot, jersey info, consent, and payment info in one guided form.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MiniPromiseChip("Reg no auto")
                    MiniPromiseChip("Jersey size")
                    MiniPromiseChip("Consent")
                }
            }
        }
        Button(
            onClick = onOpen,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 62.dp),
            shape = RoundedCornerShape(24.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = BrandGold,
                contentColor = BrandBlueDeep,
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.PersonAddAlt1, contentDescription = null)
                Spacer(modifier = Modifier.size(10.dp))
                Text("Start Admission Form", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun MiniPromiseChip(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = BrandBlue.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.10f)),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = BrandBlueDeep,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun AppBottomBar(
    selectedView: AppView,
    showFinance: Boolean,
    onSelected: (AppView) -> Unit,
) {
    val views = AppView.entries.filter { view -> view != AppView.Finance || showFinance }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .animateContentSize(animationSpec = tween(durationMillis = 320)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 8.dp,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                views.forEach { view ->
                    AppBottomNavItem(
                        view = view,
                        selected = selectedView == view,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelected(view) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AppBottomNavItem(
    view: AppView,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val icon = when (view) {
        AppView.Admission -> Icons.Outlined.Description
        AppView.Player -> Icons.Outlined.Person
        AppView.Manager -> Icons.Outlined.Lock
        AppView.Finance -> Icons.Outlined.AttachMoney
    }
    val container = if (selected) {
        if (view == AppView.Admission) BrandGold.copy(alpha = 0.24f) else BrandBlue.copy(alpha = 0.14f)
    } else {
        Color.Transparent
    }
    val content = when {
        selected && view == AppView.Admission -> BrandBlueDeep
        selected -> BrandBlue
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
    }
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(20.dp),
        color = container,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = view.label, tint = content, modifier = Modifier.size(19.dp))
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                if (view == AppView.Player) "Attend" else view.label,
                color = content,
                fontSize = 9.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FinancePanel(
    uiState: AcademyUiState,
    onAddExpense: suspend (String, String, String, String, String) -> OperationResult,
    onDeleteExpense: suspend (String) -> OperationResult,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortKey by rememberSaveable { mutableStateOf("date") }
    var sortAscending by rememberSaveable { mutableStateOf(false) }
    var showExpenseForm by rememberSaveable { mutableStateOf(false) }
    var expenseType by rememberSaveable { mutableStateOf("Coach Fees") }
    var expenseAmount by rememberSaveable { mutableStateOf("") }
    var expensePaidBy by rememberSaveable { mutableStateOf("Sandeep") }
    var expenseComment by rememberSaveable { mutableStateOf("") }
    var expenseDate by rememberSaveable { mutableStateOf(todayIsoDate()) }
    var expenseMessage by remember { mutableStateOf<String?>(null) }
    var isAddingExpense by rememberSaveable { mutableStateOf(false) }
    var deletingExpenseId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedFinanceRange by rememberSaveable { mutableStateOf("month") }
    var customRangeStart by rememberSaveable { mutableStateOf(YearMonth.from(LocalDate.now()).atDay(1).toString()) }
    var customRangeEnd by rememberSaveable { mutableStateOf(YearMonth.from(LocalDate.now()).atEndOfMonth().toString()) }
    var selectedMonthDetailKey by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    fun calendarMonthKey(calendar: java.util.Calendar): String =
        String.format(Locale.US, "%04d-%02d", calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH) + 1)

    val nowCalendar = remember { java.util.Calendar.getInstance() }
    val selectedRange = remember(selectedFinanceRange, customRangeStart, customRangeEnd) {
        buildFinanceRangeSelection(selectedFinanceRange, customRangeStart, customRangeEnd)
    }

    val initialFees = uiState.kids
        .filter { it.feesPaid }
        .map { StudentPayment(id = "", studentId = it.id, amount = it.amountPaid, paidOn = it.joinDate) }

    val allFees = initialFees + uiState.payments

    fun sumFees(dateKey: String = ""): Double = allFees
        .filter { dateKey.isEmpty() || it.paidOn.startsWith(dateKey) }
        .sumOf { it.amount }

    val selectedFees = allFees
        .filter { isDateInFinanceRange(it.paidOn, selectedRange) }
        .sumOf { it.amount }
    fun sumExpenses(dateKey: String = ""): Double = uiState.expenses
        .filter { dateKey.isEmpty() || it.expenseDate.startsWith(dateKey) }
        .sumOf { it.amount }
    val selectedExpenses = uiState.expenses
        .filter { isDateInFinanceRange(it.expenseDate, selectedRange) }
        .sumOf { it.amount }
    val selectedNet = selectedFees - selectedExpenses
    val monthBuckets = (0..5).map { offset ->
        val month = (nowCalendar.clone() as java.util.Calendar).apply {
            add(java.util.Calendar.MONTH, -offset)
        }
        val key = calendarMonthKey(month)
        val monthName = month.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.SHORT, Locale.US).orEmpty()
        val fullMonthName = month.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, Locale.US).orEmpty()
        FinanceMonthSummary(
            key = key,
            label = monthName,
            fullLabel = "$fullMonthName ${month.get(java.util.Calendar.YEAR)}",
            fees = sumFees(key),
            expenses = sumExpenses(key),
        )
    }
    fun formatCurrency(value: Double): String = "Rs ${String.format(Locale.US, "%,.0f", value)}"

    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        if (uiState.isFinanceLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        FinanceRangeSelector(
            selected = selectedFinanceRange,
            period = selectedRange.period,
            onSelected = { selectedFinanceRange = it },
            customStart = customRangeStart,
            customEnd = customRangeEnd,
            onPickCustomStart = {
                selectedFinanceRange = "custom"
                showLocalDatePicker(context, customRangeStart) { customRangeStart = it }
            },
            onPickCustomEnd = {
                selectedFinanceRange = "custom"
                showLocalDatePicker(context, customRangeEnd) { customRangeEnd = it }
            },
        )

        FinanceOverviewCard(
            rangeLabel = selectedRange.label,
            fees = formatCurrency(selectedFees),
            expenses = formatCurrency(selectedExpenses),
            net = formatCurrency(selectedNet),
            isNetPositive = selectedNet >= 0,
            darkModeEnabled = uiState.darkModeEnabled,
        )

        FinanceMiniChart(
            months = monthBuckets,
            formatCurrency = { value -> formatCurrency(value) },
            onMonthClick = { selectedMonthDetailKey = it },
        )

        FinanceRecentLedgerSection(
            revenueRows = buildFinanceRevenueLines(uiState.kids, uiState.payments),
            expenses = uiState.expenses,
            searchQuery = searchQuery,
            sortKey = sortKey,
            sortAscending = sortAscending,
            deletingExpenseId = deletingExpenseId,
            message = expenseMessage,
            formatCurrency = { value -> formatCurrency(value) },
            onSearchChange = { searchQuery = it },
            onSortChange = { key ->
                if (sortKey == key) {
                    sortAscending = !sortAscending
                } else {
                    sortKey = key
                    sortAscending = key != "date"
                }
            },
            onAddExpense = {
                showExpenseForm = true
                expenseMessage = null
            },
            onDeleteExpense = { expense ->
                scope.launch {
                    deletingExpenseId = expense.id
                    val result = onDeleteExpense(expense.id)
                    expenseMessage = result.message
                    deletingExpenseId = null
                }
            },
        )
    }

    if (showExpenseForm) {
        val dialogDensity = LocalDensity.current
        Dialog(
            onDismissRequest = { if (!isAddingExpense) showExpenseForm = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            CompositionLocalProvider(LocalDensity provides dialogDensity) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                FinanceAddExpenseCard(
                    expenseType = expenseType,
                    amount = expenseAmount,
                    paidBy = expensePaidBy,
                    comment = expenseComment,
                    expenseDate = expenseDate,
                    isSaving = isAddingExpense,
                    message = expenseMessage,
                    onDismiss = { if (!isAddingExpense) showExpenseForm = false },
                    onTypeChange = { expenseType = it },
                    onAmountChange = { expenseAmount = it },
                    onPaidByChange = { expensePaidBy = it },
                    onCommentChange = { expenseComment = it },
                    onDateChange = { expenseDate = it },
                    onSubmit = {
                        scope.launch {
                            isAddingExpense = true
                            val result = onAddExpense(expenseType, expenseAmount, expensePaidBy, expenseComment, expenseDate)
                            isAddingExpense = false
                            expenseMessage = result.message
                            if (result.success) {
                                expenseAmount = ""
                                expenseComment = ""
                                expenseDate = todayIsoDate()
                                showExpenseForm = false
                            }
                        }
                    },
                )
            }
            }
        }
    }

    selectedMonthDetailKey?.let { monthKey ->
        FinanceMonthDetailDialog(
            monthKey = monthKey,
            students = uiState.kids,
            payments = uiState.payments,
            expenses = uiState.expenses,
            formatCurrency = { value -> formatCurrency(value) },
            onDismiss = { selectedMonthDetailKey = null },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FinanceRecentLedgerSection(
    revenueRows: List<FinanceRevenueLine>,
    expenses: List<AcademyExpense>,
    searchQuery: String,
    sortKey: String,
    sortAscending: Boolean,
    deletingExpenseId: String?,
    message: String?,
    formatCurrency: (Double) -> String,
    onSearchChange: (String) -> Unit,
    onSortChange: (String) -> Unit,
    onAddExpense: () -> Unit,
    onDeleteExpense: (AcademyExpense) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf("revenue") }
    val isRevenue = selectedTab == "revenue"
    val effectiveSortKey = when {
        isRevenue && sortKey == "paid_by" -> "date"
        else -> sortKey
    }
    val query = searchQuery.trim()
    val filteredRevenue = remember(revenueRows, query, effectiveSortKey, sortAscending) {
        revenueRows
            .filter {
                query.isBlank() ||
                    it.studentName.contains(query, ignoreCase = true) ||
                    it.type.contains(query, ignoreCase = true)
            }
            .sortedWith { a, b ->
                val result = when (effectiveSortKey) {
                    "amount" -> a.amount.compareTo(b.amount)
                    "type" -> a.type.compareTo(b.type)
                    "player" -> a.studentName.compareTo(b.studentName)
                    else -> normalizeDateForSort(a.date).compareTo(normalizeDateForSort(b.date))
                }
                if (sortAscending) result else -result
            }
    }
    val filteredExpenses = remember(expenses, query, effectiveSortKey, sortAscending) {
        expenses
            .filter {
                query.isBlank() ||
                    it.expenseType.contains(query, ignoreCase = true) ||
                    it.paidBy.contains(query, ignoreCase = true) ||
                    (it.comment?.contains(query, ignoreCase = true) == true)
            }
            .sortedWith { a, b ->
                val result = when (effectiveSortKey) {
                    "type" -> a.expenseType.compareTo(b.expenseType)
                    "amount" -> a.amount.compareTo(b.amount)
                    "paid_by" -> a.paidBy.compareTo(b.paidBy)
                    else -> normalizeDateForSort(a.expenseDate).compareTo(normalizeDateForSort(b.expenseDate))
                }
                if (sortAscending) result else -result
            }
    }
    val sortOptions = if (isRevenue) {
        listOf("date" to "Date", "amount" to "Amount", "type" to "Type", "player" to "Player")
    } else {
        listOf("date" to "Date", "amount" to "Amount", "type" to "Type", "paid_by" to "Paid by")
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Recent ledger",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        if (isRevenue) "Latest parent payments" else "Latest academy expenses",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                        fontSize = 12.sp,
                    )
                }
                if (!isRevenue) {
                    Button(
                        onClick = onAddExpense,
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add", maxLines = 1)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.70f), RoundedCornerShape(18.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FinanceLedgerTab(
                    label = "Revenue",
                    selected = isRevenue,
                    accent = BrandGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = "revenue" },
                )
                FinanceLedgerTab(
                    label = "Expenses",
                    selected = !isRevenue,
                    accent = BrandRed,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = "expenses" },
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(if (isRevenue) "Search player or payment type..." else "Search expense, paid by, comment...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                sortOptions.forEach { (key, label) ->
                    FilterChip(
                        selected = effectiveSortKey == key,
                        onClick = { onSortChange(key) },
                        label = {
                            Text(
                                if (effectiveSortKey == key) "$label ${if (sortAscending) "up" else "down"}" else label,
                                fontSize = 11.sp,
                                maxLines = 1,
                            )
                        },
                    )
                }
            }

            if (!message.isNullOrBlank()) {
                Text(
                    message,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            }

            if (isRevenue) {
                if (filteredRevenue.isEmpty()) {
                    FinanceEmptyLedgerMessage("No revenue found.")
                } else {
                    filteredRevenue.take(30).forEach { row ->
                        FinanceRevenueCard(row = row, formatCurrency = formatCurrency)
                    }
                }
            } else {
                if (filteredExpenses.isEmpty()) {
                    FinanceEmptyLedgerMessage("No expenses found.")
                } else {
                    filteredExpenses.take(30).forEach { expense ->
                        FinanceExpenseCard(
                            expense = expense,
                            isDeleting = deletingExpenseId == expense.id,
                            formatCurrency = formatCurrency,
                            onDelete = { onDeleteExpense(expense) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceLedgerTab(
    label: String,
    selected: Boolean,
    accent: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        color = if (selected) accent.copy(alpha = 0.14f) else Color.Transparent,
    ) {
        Text(
            label,
            modifier = Modifier.padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            color = if (selected) accent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun FinanceEmptyLedgerMessage(message: String) {
    Text(
        message,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
        fontSize = 13.sp,
    )
}

@Composable
private fun FinanceRevenueCard(
    row: FinanceRevenueLine,
    formatCurrency: (Double) -> String,
) {
    val accent = when {
        row.amount < 0 -> BrandRed
        row.type == "Joining" -> BrandBlue
        row.type == "Jersey" -> Color(0xFF9A6400)
        else -> BrandGreen
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.14f else 0.07f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.16f)),
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    row.studentName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    FinanceTypePill(row.type, accent)
                    Text(
                        displayDate(row.date),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                        fontSize = 11.sp,
                        maxLines = 1,
                    )
                }
            }
            Text(
                formatCurrency(row.amount),
                color = accent,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun FinanceExpenseCard(
    expense: AcademyExpense,
    isDeleting: Boolean,
    formatCurrency: (Double) -> String,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.80f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    expense.expenseType,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    expense.comment?.takeIf { it.isNotBlank() } ?: "${displayDate(expense.expenseDate)} • ${expense.paidBy}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!expense.comment.isNullOrBlank()) {
                    Text(
                        "${displayDate(expense.expenseDate)} • ${expense.paidBy}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    formatCurrency(expense.amount),
                    color = BrandRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                )
                TextButton(
                    enabled = !isDeleting,
                    onClick = onDelete,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.heightIn(min = 28.dp),
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Delete", color = BrandRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceTypePill(label: String, tint: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tint.copy(alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.22f else 0.12f),
    ) {
        Text(
            label,
            color = tint,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            maxLines = 1,
        )
    }
}

private data class FinanceMonthSummary(
    val key: String,
    val label: String,
    val fullLabel: String,
    val fees: Double,
    val expenses: Double,
)

private data class FinanceRevenueLine(
    val studentName: String,
    val type: String,
    val date: String,
    val amount: Double,
)

private data class PlayerPaymentLine(
    val id: String = "",
    val date: String,
    val title: String,
    val plan: String,
    val months: Int,
    val amount: Double,
)

private fun initialCoverageMonthsForAmount(amount: Double, feesPaid: Boolean, jerseyPairs: Int = 0): Int {
    if (!feesPaid || amount <= 0.0) return 0
    val feeOnlyAmount = (amount - (jerseyPairs.coerceAtLeast(0) * JerseyExtraPairFee)).coerceAtLeast(0.0)
    val withoutAdmissionFee = (feeOnlyAmount - 500.0).coerceAtLeast(0.0)
    val roundedAmount = kotlin.math.round(feeOnlyAmount).toInt()
    return when {
        roundedAmount == 10000 -> 1
        withoutAdmissionFee >= 18900.0 || roundedAmount in setOf(18900, 19400, 20000, 20500, 21000) -> 6
        roundedAmount in setOf(9000, 9500, 9975, 10475, 10500, 11000) ||
            withoutAdmissionFee in 9000.0..10500.0 -> 3
        else -> 1
    }
}

private fun paymentMonthsCovered(payment: StudentPayment): Int {
    val explicitMonths = (payment.monthsCovered ?: 1).coerceAtLeast(1)
    val planMonths = when (payment.planType) {
        "quarterly" -> 3
        "halfyearly" -> 6
        else -> 1
    }
    val amount = kotlin.math.round(payment.amount).toInt()
    val amountMonths = when (amount) {
        18900, 19400, 20000, 20500, 21000 -> 6
        9000, 9500, 9975, 10475, 10500, 11000 -> 3
        else -> 1
    }
    return maxOf(explicitMonths, planMonths, amountMonths)
}

private fun paymentPlanLabel(planType: String?, months: Int): String = when (planType) {
    "quarterly" -> "3 months"
    "halfyearly" -> "6 months"
    "special" -> "Special training"
    "jersey_pair" -> "Jersey pair"
    "custom" -> "Custom amount"
    else -> if (months > 1) "$months months" else "Monthly"
}

private fun paymentTypeLabel(paymentType: String?): String = when (paymentType.orEmpty().lowercase(Locale.US)) {
    "joining" -> "Joining"
    "jersey", "jersey_refund" -> "Jersey"
    else -> "Renewal"
}

private fun signedPaymentAmount(payment: StudentPayment): Double =
    if (payment.paymentType == "jersey_refund") -payment.amount else payment.amount

private fun buildPlayerPaymentRows(student: Student, payments: List<StudentPayment>): List<PlayerPaymentLine> {
    val rows = mutableListOf<PlayerPaymentLine>()
    val studentPayments = payments.filter { it.studentId == student.id }
    val hasJoiningPayment = studentPayments.any { it.paymentType == "joining" }

    if (student.feesPaid && student.amountPaid > 0.0 && !hasJoiningPayment) {
        val months = initialCoverageMonthsForAmount(student.amountPaid, student.feesPaid, student.jerseyPairs)
        rows += PlayerPaymentLine(
            date = student.joinDate,
            title = "Joining payment",
            plan = if (months > 1) "$months months + admission" else "Monthly + admission",
            months = months,
            amount = student.amountPaid,
        )
    }
    studentPayments.forEach { payment ->
        val typeLabel = paymentTypeLabel(payment.paymentType)
        val isJerseyPayment = payment.paymentType == "jersey" || payment.paymentType == "jersey_refund"
        val months = if (isJerseyPayment) 0 else paymentMonthsCovered(payment)
        rows += PlayerPaymentLine(
            id = payment.id,
            date = payment.paidOn,
            title = "$typeLabel payment",
            plan = if (isJerseyPayment) "Jersey pair" else paymentPlanLabel(payment.planType, months),
            months = months,
            amount = signedPaymentAmount(payment),
        )
    }
    return rows.sortedByDescending { it.date }
}

private data class FinanceRangeOption(
    val key: String,
    val label: String,
    val months: Long?,
    val type: String = "months",
)

private data class FinanceRangeSelection(
    val label: String,
    val period: String,
    val start: LocalDate?,
    val end: LocalDate?,
)

private val FinanceRangeOptions = listOf(
    FinanceRangeOption("month", "1 month", 1L),
    FinanceRangeOption("lastmonth", "Last month", 0L, "lastmonth"),
    FinanceRangeOption("3months", "3 months", 3L),
)

private fun showLocalDatePicker(context: Context, currentValue: String, onSelected: (String) -> Unit) {
    val current = runCatching { LocalDate.parse(currentValue) }.getOrElse { LocalDate.now() }
    DatePickerDialog(
        context,
        { _, year, month, day ->
            onSelected(LocalDate.of(year, month + 1, day).toString())
        },
        current.year,
        current.monthValue - 1,
        current.dayOfMonth,
    ).show()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FinanceRangeSelector(
    selected: String,
    period: String,
    customStart: String,
    customEnd: String,
    onPickCustomStart: () -> Unit,
    onPickCustomEnd: () -> Unit,
    onSelected: (String) -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val selectedContainer = if (isDark) Color(0xFF2D6CB8) else BrandBlue
    val selectedLabel = Color.White
    val unselectedContainer = if (isDark) Color(0xFF10243D) else Color(0xFFF2F6FC)
    val unselectedLabel = if (isDark) Color(0xFFDCEBFF) else BrandBlueDeep
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Finance range",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color(0xFFDCEBFF) else BrandBlue,
            )
            Text(
                period,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                fontSize = 11.sp,
                lineHeight = 15.sp,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FinanceRangeOptions.forEach { option ->
                    FilterChip(
                        selected = selected == option.key,
                        onClick = { onSelected(option.key) },
                        label = { Text(option.label, fontSize = 12.sp, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = selectedContainer,
                            selectedLabelColor = selectedLabel,
                            containerColor = unselectedContainer,
                            labelColor = unselectedLabel,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected == option.key,
                            borderColor = if (isDark) Color(0xFF25476D) else Color(0xFFD9E4F2),
                            selectedBorderColor = Color.Transparent,
                        ),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onPickCustomStart,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("From", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f))
                        Text(displayDate(customStart), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                OutlinedButton(
                    onClick = onPickCustomEnd,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("To", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f))
                        Text(displayDate(customEnd), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

private fun buildFinanceRangeSelection(key: String, customStart: String = "", customEnd: String = ""): FinanceRangeSelection {
    val option = FinanceRangeOptions.firstOrNull { it.key == key } ?: FinanceRangeOptions[1]
    if (option.months == null) {
        return FinanceRangeSelection(option.label, "All recorded finance data", null, null)
    }
    val today = LocalDate.now()
    val start: LocalDate
    val end: LocalDate
    when (option.type) {
        "lastmonth" -> {
            val previousMonth = YearMonth.from(today).minusMonths(1)
            start = previousMonth.atDay(1)
            end = previousMonth.atEndOfMonth()
        }
        "year" -> {
            start = LocalDate.of(today.year, 1, 1)
            end = LocalDate.of(today.year, 12, 31)
        }
        "custom" -> {
            val parsedStart = runCatching { LocalDate.parse(customStart) }.getOrElse { YearMonth.from(today).atDay(1) }
            val parsedEnd = runCatching { LocalDate.parse(customEnd) }.getOrElse { YearMonth.from(today).atEndOfMonth() }
            start = minOf(parsedStart, parsedEnd)
            end = maxOf(parsedStart, parsedEnd)
        }
        else -> {
            val endMonth = YearMonth.from(today)
            val startMonth = endMonth.minusMonths((option.months - 1).coerceAtLeast(0))
            start = startMonth.atDay(1)
            end = endMonth.atEndOfMonth()
        }
    }
    return FinanceRangeSelection(option.label, "${displayDate(start.toString())} to ${displayDate(end.toString())}", start, end)
}

private fun isDateInFinanceRange(value: String, range: FinanceRangeSelection): Boolean {
    if (range.start == null || range.end == null) return true
    val date = try {
        LocalDate.parse(value.take(10))
    } catch (_: Exception) {
        return false
    }
    return !date.isBefore(range.start) && !date.isAfter(range.end)
}

@Composable
private fun FinanceOverviewCard(
    rangeLabel: String,
    fees: String,
    expenses: String,
    net: String,
    isNetPositive: Boolean,
    darkModeEnabled: Boolean,
) {
    val containerColor = when {
        isNetPositive && darkModeEnabled -> Color(0xFF163F2D)
        isNetPositive -> Color(0xFFEAF8EF)
        darkModeEnabled -> Color(0xFF4A1F24)
        else -> Color(0xFFFFECEC)
    }
    val contentColor = when {
        isNetPositive && darkModeEnabled -> Color(0xFFC7F7D2)
        isNetPositive -> Color(0xFF146C43)
        darkModeEnabled -> Color(0xFFFFC9C9)
        else -> Color(0xFFB42318)
    }
    val metricColor = if (darkModeEnabled) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.72f)
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = containerColor,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("$rangeLabel net", color = contentColor.copy(alpha = 0.74f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(net, color = contentColor, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                FinanceGlassMetric("Fees", fees, contentColor, metricColor, Modifier.weight(1f))
                FinanceGlassMetric("Expense", expenses, contentColor, metricColor, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FinanceGlassMetric(
    label: String,
    value: String,
    contentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label.uppercase(Locale.getDefault()), color = contentColor.copy(alpha = 0.78f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            Text(value, color = contentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun FinanceAddExpenseCard(
    expenseType: String,
    amount: String,
    paidBy: String,
    comment: String,
    expenseDate: String,
    isSaving: Boolean,
    message: String?,
    onDismiss: () -> Unit,
    onTypeChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onPaidByChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val context = LocalContext.current
    val openDatePicker = rememberUpdatedState(newValue = {
        val (year, month, day) = currentDatePickerValues(expenseDate.ifBlank { null })
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                onDateChange(
                    String.format(
                        Locale.US,
                        "%04d-%02d-%02d",
                        selectedYear,
                        selectedMonth + 1,
                        selectedDay
                    )
                )
            },
            year,
            month,
            day,
        ).show()
    })

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Add expense", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Track academy costs", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f))
                }
                IconButton(onClick = onDismiss, enabled = !isSaving) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                }
            }
            AdmissionDropdownField(
                label = "Type",
                value = expenseType,
                options = listOf("Coach Fees", "Purchased accessories", "Transport", "Maid expense", "Ground maintenance", "Other"),
                onSelect = onTypeChange,
            )
            OutlinedTextField(
                value = displayDate(expenseDate),
                onValueChange = {},
                readOnly = true,
                label = { Text("Expense date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openDatePicker.value() },
                trailingIcon = {
                    IconButton(onClick = { openDatePicker.value() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pick date")
                    }
                },
                interactionSource = remember { MutableInteractionSource() }
                    .also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect { interaction ->
                                if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                    openDatePicker.value()
                                }
                            }
                        }
                    },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                Box(modifier = Modifier.weight(1f)) {
                    AdmissionDropdownField(
                        label = "Paid by",
                        value = paidBy,
                        options = listOf("Sandeep", "Srinivas", "Sujit"),
                        onSelect = onPaidByChange,
                    )
                }
            }
            OutlinedTextField(value = comment, onValueChange = onCommentChange, label = { Text("Comment") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Button(enabled = !isSaving, onClick = onSubmit, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("Save expense")
            }
            if (!message.isNullOrBlank()) Text(message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FinanceMiniChart(
    months: List<FinanceMonthSummary>,
    formatCurrency: (Double) -> String,
    onMonthClick: (String) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        "6 MONTH VIEW",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text("Monthly profit/loss", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                months.forEach { month ->
                    val net = month.fees - month.expenses
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (net >= 0) BrandGreen.copy(alpha = 0.1f) else BrandRed.copy(alpha = 0.1f),
                        modifier = Modifier
                            .width(168.dp)
                            .clickable { onMonthClick(month.key) },
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp),
                        ) {
                            Text(month.label, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                FinanceMiniBar(month.fees, maxOf(month.fees, month.expenses, 1.0), BrandBlue)
                                FinanceMiniBar(month.expenses, maxOf(month.fees, month.expenses, 1.0), BrandRed)
                            }
                            Text(
                                formatCurrency(net),
                                color = if (net >= 0) BrandGreen else BrandRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            FinanceMiniPill("Fees", formatCurrency(month.fees), BrandBlue)
                            FinanceMiniPill("Expense", formatCurrency(month.expenses), BrandRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceMiniBar(value: Double, maxValue: Double, tint: Color) {
    val fraction = (value / maxValue).toFloat().coerceIn(0.08f, 1f)
    Box(
        modifier = Modifier
            .width(18.dp)
            .height((42 * fraction).dp)
            .clip(RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp))
            .background(tint)
    )
}

@Composable
private fun FinanceMiniPill(
    label: String,
    value: String,
    tint: Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tint.copy(alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.18f else 0.08f),
    ) {
        Text(
            text = "$label $value",
            color = tint,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

private fun normalizeDateForSort(date: String): String {
    val parts = date.split("-", "/")
    if (parts.size < 3) return date
    // Try to handle both YYYY-MM-DD and DD-MM-YYYY if they exist
    val (y, m, d) = if (parts[0].length == 4) {
        Triple(parts[0], parts[1], parts[2])
    } else {
        Triple(parts[2], parts[1], parts[0])
    }
    return "%04d-%02d-%02d".format(Locale.US, y.toIntOrNull() ?: 0, m.toIntOrNull() ?: 0, d.toIntOrNull() ?: 0)
}

private fun buildFinanceRevenueLines(students: List<Student>, payments: List<StudentPayment>): List<FinanceRevenueLine> {
    val studentIdsWithJoiningPayment = payments.filter { it.paymentType == "joining" }.map { it.studentId }.toSet()
    val legacyJoiningRows = students
        .filter { it.feesPaid && it.amountPaid > 0.0 && it.id !in studentIdsWithJoiningPayment }
        .map {
            FinanceRevenueLine(
                studentName = it.name,
                type = "Joining",
                date = it.joinDate,
                amount = it.amountPaid,
            )
        }
    val ledgerRows = payments.map { payment ->
        val student = students.firstOrNull { it.id == payment.studentId }
        FinanceRevenueLine(
            studentName = student?.name ?: "Unknown player",
            type = paymentTypeLabel(payment.paymentType),
            date = payment.paidOn,
            amount = signedPaymentAmount(payment),
        )
    }
    return (legacyJoiningRows + ledgerRows).sortedByDescending { normalizeDateForSort(it.date) }
}

@Composable
private fun FinanceMonthDetailDialog(
    monthKey: String,
    students: List<Student>,
    payments: List<StudentPayment>,
    expenses: List<AcademyExpense>,
    formatCurrency: (Double) -> String,
    onDismiss: () -> Unit,
) {
    val revenueRows = remember(monthKey, students, payments) {
        buildFinanceRevenueLines(students, payments)
            .filter { it.date.startsWith(monthKey) }
            .sortedByDescending { it.date }
    }
    val expenseRows = remember(monthKey, expenses) {
        expenses.filter { it.expenseDate.startsWith(monthKey) }.sortedByDescending { normalizeDateForSort(it.expenseDate) }
    }
    val revenueTotal = revenueRows.sumOf { it.amount }
    val joiningCount = revenueRows.count { it.type == "Joining" }
    val renewalCount = revenueRows.count { it.type == "Renewal" }
    val jerseyCount = revenueRows.count { it.type == "Jersey" }
    val joiningTotal = revenueRows.filter { it.type == "Joining" }.sumOf { it.amount }
    val renewalTotal = revenueRows.filter { it.type == "Renewal" }.sumOf { it.amount }
    val jerseyTotal = revenueRows.filter { it.type == "Jersey" }.sumOf { it.amount }
    val expenseTotal = expenseRows.sumOf { it.amount }
    val totalRecordCount = revenueRows.size + expenseRows.size
    val net = revenueTotal - expenseTotal
    val title = remember(monthKey) {
        runCatching {
            val ym = YearMonth.parse(monthKey)
            "${ym.month.name.lowercase(Locale.US).replaceFirstChar { it.titlecase(Locale.US) }} ${ym.year}"
        }.getOrElse { monthKey }
    }
    val dialogDensity = LocalDensity.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        CompositionLocalProvider(LocalDensity provides dialogDensity) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().widthIn(max = 560.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            FinanceMonthSummaryStrip(
                                revenue = formatCurrency(revenueTotal),
                                joining = formatCurrency(joiningTotal),
                                joiningCount = joiningCount,
                                renewal = formatCurrency(renewalTotal),
                                renewalCount = renewalCount,
                                jersey = formatCurrency(jerseyTotal),
                                jerseyCount = jerseyCount,
                                expense = formatCurrency(expenseTotal),
                                expenseCount = expenseRows.size,
                                net = formatCurrency(net),
                                totalRecordCount = totalRecordCount,
                                isNetPositive = net >= 0,
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        FinanceMonthDetailList(
                            title = "Revenue",
                            count = revenueRows.size,
                            emptyText = "No revenue",
                            rows = revenueRows.map {
                                Triple(it.studentName, "${it.type} • ${displayDate(it.date)}", formatCurrency(it.amount))
                            },
                            modifier = Modifier.weight(1f),
                        )
                        FinanceMonthDetailList(
                            title = "Expenses",
                            count = expenseRows.size,
                            emptyText = "No expenses",
                            rows = expenseRows.map {
                                Triple(it.expenseType, "${it.paidBy} • ${displayDate(it.expenseDate)}", formatCurrency(it.amount))
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun FinanceMonthSummaryStrip(
    revenue: String,
    joining: String,
    joiningCount: Int,
    renewal: String,
    renewalCount: Int,
    jersey: String,
    jerseyCount: Int,
    expense: String,
    expenseCount: Int,
    net: String,
    totalRecordCount: Int,
    isNetPositive: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FinanceSummaryMiniCard(
                label = "Revenue",
                value = revenue,
                subText = "Joining $joiningCount / $joining • Renewal $renewalCount / $renewal • Jersey $jerseyCount / $jersey",
                modifier = Modifier.weight(1.35f),
            )
            FinanceSummaryMiniCard(
                label = "Expense",
                value = expense,
                subText = "$expenseCount record${if (expenseCount == 1) "" else "s"}",
                modifier = Modifier.weight(1f),
            )
        }
        FinanceSummaryMiniCard(
            label = "Net",
            value = net,
            subText = "$totalRecordCount total record${if (totalRecordCount == 1) "" else "s"}",
            valueColor = if (isNetPositive) BrandGreen else BrandRed,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun FinanceSummaryMiniCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subText: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
    ) {
        Column(modifier = Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label.uppercase(Locale.US), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = valueColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!subText.isNullOrBlank()) {
                Text(subText, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun FinanceMonthDetailList(
    title: String,
    count: Int,
    emptyText: String,
    rows: List<Triple<String, String, String>>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                ) {
                    Text(
                        "$count",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
            if (rows.isEmpty()) {
                Text(emptyText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f))
            } else {
                rows.forEach { (primary, secondary, amount) ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(secondary, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(amount, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                }
            }
        }
    }
}

@Composable
private fun RenewalPaymentDialog(
    student: Student,
    payments: List<StudentPayment>,
    onDismiss: () -> Unit,
    onSubmit: suspend (String, Int, Double, String, String, Double, Double, Double, Double, String, Int) -> OperationResult,
) {
    val isJoiningFee = student.isFeesPending()
    val context = LocalContext.current
    var plan by rememberSaveable(student.id) { mutableStateOf("monthly") }
    val initialJoiningSplit = remember(student.id) { joiningFeeSplitForPlan(student, "monthly") }
    var amount by rememberSaveable(student.id) {
        mutableStateOf(if (isJoiningFee) initialJoiningSplit.totalFeeAmount.toInt().toString() else "3500")
    }
    var coachingFee by rememberSaveable(student.id) { mutableStateOf(initialJoiningSplit.coachingFee.toInt().toString()) }
    var admissionFee by rememberSaveable(student.id) { mutableStateOf(initialJoiningSplit.admissionFee.toInt().toString()) }
    var jerseySize by rememberSaveable(student.id) { mutableStateOf(student.jerseySize) }
    var jerseyPairs by rememberSaveable(student.id) { mutableStateOf(student.jerseyPairs.coerceAtLeast(0).toString()) }
    var comment by rememberSaveable(student.id) { mutableStateOf("") }
    var paymentDate by rememberSaveable(student.id) { mutableStateOf(todayIsoDate()) }
    var isSaving by rememberSaveable(student.id) { mutableStateOf(false) }
    var inlineMessage by rememberSaveable(student.id) { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val planInfo = when (plan) {
        "quarterly" -> Triple("3 months - 5% off", 3, 9975.0)
        "halfyearly" -> Triple("6 months - 10% off", 6, 18900.0)
        "special" -> Triple("Special training", 1, 10000.0)
        "custom" -> Triple("Custom amount", 1, amount.toDoubleOrNull() ?: 0.0)
        else -> Triple("Monthly", 1, 3500.0)
    }
    val safeJerseyPairs = jerseyPairs.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val jerseyAmount = safeJerseyPairs * JerseyExtraPairFee
    val joiningSplitTotal = (coachingFee.toDoubleOrNull() ?: 0.0) +
        (admissionFee.toDoubleOrNull() ?: 0.0) +
        jerseyAmount
    val cycleDate = if (isJoiningFee) student.joinDate else student.nextRenewalCycleDate(payments)
    val openPaymentDatePicker = rememberUpdatedState(newValue = {
        val (year, month, day) = currentDatePickerValues(paymentDate.ifBlank { null })
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                paymentDate = String.format(
                    Locale.US,
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay,
                )
            },
            year,
            month,
            day,
        ).show()
    })

    FormDialog(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                if (isJoiningFee) "Record joining fee" else "Record renewal payment",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                if (isJoiningFee) {
                    "${student.name} first fee starts from ${displayDate(cycleDate)}. Payment date is used for finance reports."
                } else {
                    "${student.name} cycle starts ${displayDate(cycleDate)}. Late payment will not change the usual fee date."
                },
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
            AdmissionDropdownField(
                label = "Plan",
                value = planInfo.first,
                options = listOf("Monthly", "3 months - 5% off", "6 months - 10% off", "Special training (1 month)", "Custom amount"),
                onSelect = { selected ->
                    plan = when (selected) {
                        "3 months - 5% off" -> "quarterly"
                        "6 months - 10% off" -> "halfyearly"
                        "Special training (1 month)" -> "special"
                        "Custom amount" -> "custom"
                        else -> "monthly"
                    }
                    if (isJoiningFee) {
                        val split = joiningFeeSplitForPlan(student, plan)
                        coachingFee = split.coachingFee.toInt().toString()
                        admissionFee = split.admissionFee.toInt().toString()
                        jerseySize = student.jerseySize
                        jerseyPairs = student.jerseyPairs.coerceAtLeast(0).toString()
                        amount = if (plan == "custom") "" else split.totalFeeAmount.toInt().toString()
                    } else {
                        amount = when (plan) {
                            "quarterly" -> "9975"
                            "halfyearly" -> "18900"
                            "special" -> "10000"
                            "custom" -> ""
                            else -> "3500"
                        }
                    }
                },
            )
            planDiscountLabel(plan).takeIf { it.isNotBlank() }?.let { discount ->
                Text(
                    text = discount,
                    color = BrandGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            if (!isJoiningFee) {
                AdmissionTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = "Amount paid",
                    singleLine = true,
                )
            }
            if (isJoiningFee) {
                AdmissionSectionCard(title = "Joining fee split") {
                    AdmissionTextField(
                        value = coachingFee,
                        onValueChange = { coachingFee = it.filter { char -> char.isDigit() || char == '.' } },
                        label = "Coaching fee",
                        singleLine = true,
                    )
                    AdmissionTextField(
                        value = admissionFee,
                        onValueChange = { admissionFee = it.filter { char -> char.isDigit() || char == '.' } },
                        label = "Admission fee",
                        singleLine = true,
                    )
                    AdmissionDropdownField(
                        label = "Jersey size",
                        value = jerseySizeLabel(jerseySize),
                        options = JerseySizeOptions.map(::jerseySizeLabel),
                        displayText = { it },
                        onSelect = { selectedLabel ->
                            jerseySize = JerseySizeOptions.firstOrNull { jerseySizeLabel(it) == selectedLabel }.orEmpty()
                        },
                    )
                    AdmissionTextField(
                        value = jerseyPairs,
                        onValueChange = { jerseyPairs = it.filter(Char::isDigit) },
                        label = "Jersey pairs",
                        singleLine = true,
                    )
                    Text(
                        text = "Jersey amount: ${admissionAmountLabel(jerseyAmount)} (${safeJerseyPairs} x ${admissionAmountLabel(JerseyExtraPairFee)})",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                    Text(
                        text = "Total due saved on player profile: ${admissionAmountLabel(joiningSplitTotal)}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                }
            }
            AdmissionTextField(
                value = paymentDate,
                onValueChange = {},
                label = "Payment date",
                singleLine = true,
                readOnly = true,
                trailing = {
                    TextButton(onClick = { openPaymentDatePicker.value.invoke() }) {
                        Text("Pick")
                    }
                },
            )
            AdmissionTextField(
                value = comment,
                onValueChange = { comment = it },
                label = "Comment",
                singleLine = true,
            )
            if (inlineMessage.isNotBlank()) {
                Text(
                    text = inlineMessage,
                    color = BrandRed,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }
            Button(
                enabled = !isSaving,
                onClick = {
                    scope.launch {
                        try {
                            isSaving = true
                            inlineMessage = ""
                            val finalAmount = if (isJoiningFee) {
                                joiningSplitTotal
                            } else if (plan == "custom") {
                                amount.toDoubleOrNull() ?: 0.0
                            } else {
                                amount.toDoubleOrNull() ?: planInfo.third
                            }
                            val result = onSubmit(
                                plan,
                                planInfo.second,
                                finalAmount,
                                comment,
                                paymentDate,
                                coachingFee.toDoubleOrNull() ?: 0.0,
                                admissionFee.toDoubleOrNull() ?: 0.0,
                                jerseyAmount,
                                joiningSplitTotal,
                                jerseySize,
                                safeJerseyPairs,
                            )
                            if (!result.success) {
                                inlineMessage = result.message
                            }
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(if (isJoiningFee) "Save joining payment" else "Save renewal payment")
            }
        }
    }
}

@Composable
private fun FormDialog(
    onDismiss: () -> Unit,
    expanded: Boolean = false,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit,
) {
    val dialogDensity = LocalDensity.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        CompositionLocalProvider(LocalDensity provides dialogDensity) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                contentAlignment = contentAlignment,
            ) {
                Surface(
                    modifier = Modifier
                        .then(
                            if (expanded) {
                                Modifier.fillMaxSize()
                            } else {
                                Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 560.dp)
                            }
                        ),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (expanded) Modifier.fillMaxSize()
                                else Modifier
                            )
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsSection(
    joined: Int,
    active: Int,
    returning: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompactStatCard(
                modifier = Modifier.weight(1f),
                title = "Joined",
                value = joined,
                accent = BrandBlue,
            )
            CompactStatCard(
                modifier = Modifier.weight(1f),
                title = "Active",
                value = active,
                accent = BrandGreen,
            )
            CompactStatCard(
                modifier = Modifier.weight(1f),
                title = "Returning",
                value = returning,
                accent = BrandBlueDeep,
            )
        }
    }
}

@Composable
private fun AdmissionReviewSection(
    admissions: List<PendingAdmission>,
    isLoading: Boolean,
    onApprove: suspend (PendingAdmission) -> OperationResult,
    onReject: suspend (PendingAdmission) -> OperationResult,
    onRemind: suspend (PendingAdmission) -> OperationResult,
    onMessage: (String) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.14f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        "Parent Submission Review",
                        color = BrandBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.7.sp,
                    )
                    Text(
                        "Approve admissions before they enter the roster",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = BrandGold.copy(alpha = 0.18f),
                ) {
                    Text(
                        "${admissions.size} pending",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = BrandBlueDeep,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }

            if (isLoading && admissions.isEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Loading submissions...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontSize = 13.sp)
                }
            }

            admissions.forEach { admission ->
                AdmissionReviewCard(
                    admission = admission,
                    onApprove = onApprove,
                    onReject = onReject,
                    onRemind = onRemind,
                    onMessage = onMessage,
                )
            }
        }
    }
}

@Composable
private fun AdmissionReviewCard(
    admission: PendingAdmission,
    onApprove: suspend (PendingAdmission) -> OperationResult,
    onReject: suspend (PendingAdmission) -> OperationResult,
    onRemind: suspend (PendingAdmission) -> OperationResult,
    onMessage: (String) -> Unit,
) {
    var actionInProgress by rememberSaveable(admission.id) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val paymentPending = admission.isPaymentPendingVerification()
    val feeText = when {
        admission.feesPaid -> "Paid Rs ${String.format(Locale.US, "%,.0f", admission.amountPaid)}"
        paymentPending -> "Pending verification Rs ${String.format(Locale.US, "%,.0f", admission.amountPaid)}"
        else -> "Fees not paid"
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.62f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Reg ${admission.regNo ?: "-"} • ${admission.filledBy.orEmpty().ifBlank { "Parent / Guardian" }}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                        fontSize = 11.sp,
                        maxLines = 1,
                    )
                    Text(
                        admission.applicantName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        "${admission.age} yrs • ${admission.timeSlot.orEmpty().ifBlank { "Slot not set" }} • ${displayDate(admission.joinDate)}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                }
                Badge(
                    label = feeText,
                    container = when {
                        admission.feesPaid -> BrandGreen.copy(alpha = 0.12f)
                        paymentPending -> Color(0xFFFFF2D8)
                        else -> BrandRed.copy(alpha = 0.1f)
                    },
                    color = when {
                        admission.feesPaid -> BrandGreen
                        paymentPending -> Color(0xFF8F6500)
                        else -> BrandRed
                    },
                )
            }

            Text(
                "Parent: ${admission.fatherGuardianName.orEmpty().ifBlank { "-" }} • ${admission.parentContactNo.orEmpty().ifBlank { admission.alternateContactNo.orEmpty().ifBlank { "No phone" } }}",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
            Text(
                "School: ${admission.schoolCollege.orEmpty().ifBlank { "-" }} • Jersey ${admission.jerseySize.orEmpty().ifBlank { "not set" }} / ${admission.jerseyPairs ?: 0} pair${if ((admission.jerseyPairs ?: 0) == 1) "" else "s"}",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                fontSize = 11.sp,
                lineHeight = 15.sp,
            )
            if (!admission.comments.isNullOrBlank()) {
                Text(
                    admission.comments,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = actionInProgress == null,
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        scope.launch {
                            actionInProgress = "approve"
                            val result = onApprove(admission)
                            onMessage(result.message)
                            actionInProgress = null
                        }
                    },
                ) {
                    if (actionInProgress == "approve") {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text("Approve", fontSize = 12.sp, maxLines = 1)
                }

                if (!admission.feesPaid && !paymentPending) {
                    OutlinedButton(
                        modifier = Modifier.weight(0.9f),
                        enabled = actionInProgress == null,
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            scope.launch {
                                actionInProgress = "remind"
                                val result = onRemind(admission)
                                onMessage(result.message)
                                actionInProgress = null
                            }
                        },
                    ) {
                        if (actionInProgress == "remind") {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text("Remind", fontSize = 12.sp, maxLines = 1)
                    }
                }

                OutlinedButton(
                    modifier = Modifier.weight(0.9f),
                    enabled = actionInProgress == null,
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        scope.launch {
                            actionInProgress = "reject"
                            val result = onReject(admission)
                            onMessage(result.message)
                            actionInProgress = null
                        }
                    },
                ) {
                    if (actionInProgress == "reject") {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text("Reject", color = BrandRed, fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun CompactStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: Int,
    accent: Color,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title.uppercase(Locale.getDefault()),
                color = accent,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 28.sp,
            )
        }
    }
}

private data class StudentMovementMonth(
    val key: String,
    val label: String,
    val joined: Int,
    val continuing: Int,
    val discontinued: Int,
)

@Composable
private fun StudentMovementSection(
    students: List<Student>,
    onMovementClick: (StudentMovementMonth, String) -> Unit,
) {
    val movement = remember(students) { buildStudentMovement(students) }
    val maxValue = movement.flatMap { listOf(it.joined, it.continuing, it.discontinued) }.maxOrNull()?.coerceAtLeast(1) ?: 1

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Student Movement", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Joined, continuing from previous month, and discontinued.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                )
            }
            LazyRowLikeMovement(months = movement, maxValue = maxValue, onMovementClick = onMovementClick)
        }
    }
}

@Composable
private fun LazyRowLikeMovement(
    months: List<StudentMovementMonth>,
    maxValue: Int,
    onMovementClick: (StudentMovementMonth, String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        months.forEach { month ->
            StudentMovementCard(month = month, maxValue = maxValue, onMovementClick = onMovementClick)
        }
    }
}

@Composable
private fun StudentMovementCard(
    month: StudentMovementMonth,
    maxValue: Int,
    onMovementClick: (StudentMovementMonth, String) -> Unit,
) {
    val activeCount = (month.continuing + month.joined - month.discontinued).coerceAtLeast(0)

    Surface(
        modifier = Modifier.width(156.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.76f),
        border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.10f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(month.label, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = BrandBlue.copy(alpha = 0.10f),
                ) {
                    Text(
                        text = "$activeCount active",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = BrandBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .height(76.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Bottom,
            ) {
                MovementBar(month.joined, maxValue, BrandBlue)
                MovementBar(month.continuing, maxValue, BrandGreen)
                MovementBar(month.discontinued, maxValue, BrandRed)
            }
            MovementLegendLine("Joined", month.joined, BrandBlue) { onMovementClick(month, "joined") }
            MovementLegendLine("Continuing", month.continuing, BrandGreen) { onMovementClick(month, "continuing") }
            MovementLegendLine("Left", month.discontinued, BrandRed) { onMovementClick(month, "left") }
        }
    }
}

@Composable
private fun MovementBar(value: Int, maxValue: Int, color: Color) {
    Box(
        modifier = Modifier
            .width(20.dp)
            .height(((value.toFloat() / maxValue.toFloat()) * 68f).coerceAtLeast(8f).dp)
            .background(
                Brush.verticalGradient(
                    listOf(color.copy(alpha = 0.92f), color)
                ),
                RoundedCornerShape(topStart = 99.dp, topEnd = 99.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
            )
    )
}

@Composable
private fun MovementLegendLine(label: String, value: Int, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(enabled = value > 0, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(modifier = Modifier.size(7.dp).background(color, CircleShape))
        Text("$value $label", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f), maxLines = 1)
    }
}

private fun buildStudentMovement(students: List<Student>): List<StudentMovementMonth> {
    val currentMonth = YearMonth.now()
    return (0..5).map { offset ->
        val month = currentMonth.minusMonths(offset.toLong())
        val monthStart = month.atDay(1)
        val monthEnd = month.atEndOfMonth()
        val previousMonthEnd = monthStart.minusDays(1)
        StudentMovementMonth(
            key = "${month.year}-${month.monthValue.toString().padStart(2, '0')}",
            label = month.month.name.take(3).lowercase(Locale.US).replaceFirstChar { it.titlecase(Locale.US) } + " '${month.year.toString().takeLast(2)}",
            joined = students.count { parseStudentDate(it.joinDate)?.let { date -> !date.isBefore(monthStart) && !date.isAfter(monthEnd) } == true },
            continuing = students.count {
                val joined = parseStudentDate(it.joinDate)
                val discontinued = studentDiscontinuedMovementDate(it)
                joined != null && !joined.isAfter(previousMonthEnd) &&
                    (!it.discontinued || (discontinued != null && !discontinued.isBefore(monthStart)))
            },
            discontinued = students.count { studentDiscontinuedMovementDate(it)?.let { date -> !date.isBefore(monthStart) && !date.isAfter(monthEnd) } == true },
        )
    }
}

private fun parseStudentDate(value: String?): LocalDate? = try {
    val datePart = value?.take(10)
    if (datePart.isNullOrBlank()) null else LocalDate.parse(datePart)
} catch (_: Exception) {
    null
}

private fun studentDiscontinuedMovementDate(student: Student): LocalDate? {
    parseStudentDate(student.discontinuedAt)?.let { return it }
    if (!student.discontinued) return null
    return parseStudentDate(student.updatedAt)
        ?: parseStudentDate(student.createdAt)
        ?: parseStudentDate(student.joinDate)
}

private fun movementYearMonth(monthKey: String?): YearMonth? = try {
    if (monthKey.isNullOrBlank()) null else YearMonth.parse(monthKey)
} catch (_: Exception) {
    null
}

private fun studentMatchesMovementFilter(student: Student, monthKey: String, type: String): Boolean {
    val month = movementYearMonth(monthKey) ?: return true
    val monthStart = month.atDay(1)
    val monthEnd = month.atEndOfMonth()
    val previousMonthEnd = monthStart.minusDays(1)
    val joinDate = parseStudentDate(student.joinDate)
    val discontinuedAt = studentDiscontinuedMovementDate(student)
    return when (type) {
        "joined" -> joinDate != null && !joinDate.isBefore(monthStart) && !joinDate.isAfter(monthEnd)
        "left" -> discontinuedAt != null && !discontinuedAt.isBefore(monthStart) && !discontinuedAt.isAfter(monthEnd)
        else -> joinDate != null && !joinDate.isAfter(previousMonthEnd) &&
            (!student.discontinued || (discontinuedAt != null && !discontinuedAt.isBefore(monthStart)))
    }
}

private fun reminderOverdueDays(student: Student, payments: List<StudentPayment>): Int {
    val dueDate = when {
        student.isFeesPending() -> student.joinDate
        student.isRenewalPending(payments) -> student.nextRenewalCycleDate(payments)
        else -> return 0
    }
    return runCatching {
        ChronoUnit.DAYS.between(LocalDate.parse(dueDate), LocalDate.now()).toInt().coerceAtLeast(0)
    }.getOrDefault(0)
}

private fun movementFilterDisplayLabel(monthKey: String?, type: String?): String? {
    val month = movementYearMonth(monthKey) ?: return null
    val typeLabel = when (type) {
        "joined" -> "Joined"
        "left" -> "Left"
        "continuing" -> "Continuing"
        else -> return null
    }
    val monthLabel = month.month.name.take(3).lowercase(Locale.US).replaceFirstChar { it.titlecase(Locale.US) } +
        " ${month.year}"
    return "$typeLabel · $monthLabel"
}

@Composable
private fun MovementRosterFilterBanner(label: String, onClear: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BrandBlue.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.16f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Roster filtered by $label",
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TextButton(onClick = onClear) {
                Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RosterToolbar(
    searchQuery: String,
    sortKey: String,
    sortAscending: Boolean,
    statusFilter: String,
    jerseyFilter: String,
    typeFilter: String,
    feeDueFilter: String,
    visibleCount: Int,
    totalCount: Int,
    isRefreshing: Boolean,
    onSearchChange: (String) -> Unit,
    onSortChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onJerseyFilterChange: (String) -> Unit,
    onTypeFilterChange: (String) -> Unit,
    onFeeDueFilterChange: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        )
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isBlank()) {
                                        Text(
                                            text = "Search player or slot",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                            fontSize = 14.sp,
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                )
                            }
                        }
                    }
                }
                FilledTonalIconButton(
                    onClick = onRefresh,
                    shape = RoundedCornerShape(18.dp),
                    colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (isRefreshing) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null)
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "name" to "Name",
                    "joinDate" to "Join",
                    "nextDue" to "Due",
                    "amount" to "Amount",
                    "slot" to "Slot",
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = sortKey == key,
                        onClick = { onSortChange(key) },
                        label = {
                            Text(
                                if (sortKey == key) "$label ${if (sortAscending) "up" else "down"}" else label,
                                fontSize = 12.sp,
                                maxLines = 1,
                            )
                        },
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactRosterFilter(
                    label = "Status",
                    selectedValue = statusFilter,
                    options = RosterStatusFilterOptions,
                    onSelected = onStatusFilterChange,
                )
                CompactRosterFilter(
                    label = "Jersey",
                    selectedValue = jerseyFilter,
                    options = RosterJerseyFilterOptions,
                    onSelected = onJerseyFilterChange,
                )
                CompactRosterFilter(
                    label = "Type",
                    selectedValue = typeFilter,
                    options = RosterTypeFilterOptions,
                    onSelected = onTypeFilterChange,
                )
                CompactRosterFilter(
                    label = "Due",
                    selectedValue = feeDueFilter,
                    options = RosterFeeDueFilterOptions,
                    onSelected = onFeeDueFilterChange,
                )
            }
            Text(
                text = if (searchQuery.isBlank()) {
                    "Showing $visibleCount of $totalCount registered players."
                } else {
                    "$visibleCount matches for \"$searchQuery\"."
                },
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                fontSize = 13.sp,
            )
        }
    }
}

private val RosterStatusFilterOptions = listOf(
    "all" to "All",
    "active" to "Active",
    "discontinued" to "Discontinued",
)

private val RosterJerseyFilterOptions = listOf(
    "all" to "All",
    "not-set" to "Not set",
    "22" to "22",
    "24" to "24",
    "26" to "26",
    "28" to "28",
    "30" to "30",
    "32" to "32",
    "34" to "34",
    "36" to "36",
    "38" to "38",
)

private val RosterTypeFilterOptions = listOf(
    "all" to "All",
    "new" to "New",
    "returning" to "Returning",
)

private val RosterFeeDueFilterOptions = listOf(
    "all" to "All",
    "joining-pending" to "Joining pending",
    "overdue" to "Overdue",
)

@Composable
private fun CompactRosterFilter(
    label: String,
    selectedValue: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedValue }?.second ?: "All"

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(999.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        ) {
            Text(
                text = "$label: $selectedLabel",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (value, optionLabel) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = optionLabel,
                            fontSize = 13.sp,
                            fontWeight = if (value == selectedValue) FontWeight.Bold else FontWeight.Medium,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelected(value)
                    },
                )
            }
        }
    }
}

private data class AttendanceFollowUp(
    val student: Student,
    val absentDays: Long,
    val lastPresentDate: String?,
)

private data class AttendanceStreak(
    val student: Student,
    val days: Int,
    val badgeLabel: String,
    val tier: String,
)

private const val AttendanceFollowUpDays = 5L

private fun isAttendanceWeekday(date: LocalDate): Boolean {
    return date.dayOfWeek.value in 1..5
}

private fun previousAttendanceWeekday(date: LocalDate): LocalDate {
    var cursor = date.minusDays(1)
    while (!isAttendanceWeekday(cursor)) {
        cursor = cursor.minusDays(1)
    }
    return cursor
}

private fun attendanceDateSet(
    studentId: String,
    recentAttendanceDates: Map<String, List<String>>,
    todayAttendanceIds: Set<String>,
): Set<String> {
    val today = LocalDate.now()
    return buildSet {
        recentAttendanceDates[studentId].orEmpty().forEach { date ->
            runCatching { LocalDate.parse(date.take(10)) }.getOrNull()
                ?.takeIf(::isAttendanceWeekday)
                ?.toString()
                ?.let(::add)
        }
        if (todayAttendanceIds.contains(studentId) && isAttendanceWeekday(today)) add(today.toString())
    }
}

private fun attendanceStreakCount(
    studentId: String,
    recentAttendanceDates: Map<String, List<String>>,
    todayAttendanceIds: Set<String>,
): Int {
    val dates = attendanceDateSet(studentId, recentAttendanceDates, todayAttendanceIds)
    val today = LocalDate.now()
    var cursor = if (todayAttendanceIds.contains(studentId) && isAttendanceWeekday(today)) {
        today
    } else {
        previousAttendanceWeekday(today)
    }
    var streak = 0
    while (dates.contains(cursor.toString())) {
        streak += 1
        cursor = previousAttendanceWeekday(cursor)
    }
    return streak
}

private fun attendanceStreakBadge(streak: Int): Pair<String, String>? = when {
    streak >= 30 -> "30d Legend" to "legend"
    streak >= 15 -> "15d Gold" to "gold"
    streak >= 7 -> "7d Star" to "star"
    else -> null
}

private fun buildAttendanceStreaks(
    activePlayers: List<Student>,
    recentAttendanceDates: Map<String, List<String>>,
    todayAttendanceIds: Set<String>,
): List<AttendanceStreak> {
    return activePlayers.mapNotNull { student ->
        val streak = attendanceStreakCount(student.id, recentAttendanceDates, todayAttendanceIds)
        val badge = attendanceStreakBadge(streak) ?: return@mapNotNull null
        AttendanceStreak(
            student = student,
            days = streak,
            badgeLabel = badge.first,
            tier = badge.second,
        )
    }.sortedWith(compareByDescending<AttendanceStreak> { it.days }.thenBy { it.student.name.lowercase() })
}

private fun buildAttendanceFollowUps(
    activePlayers: List<Student>,
    recentAttendanceDates: Map<String, List<String>>,
    todayAttendanceIds: Set<String>,
): List<AttendanceFollowUp> {
    val today = LocalDate.now()
    return activePlayers.mapNotNull { student ->
        val presentDates = recentAttendanceDates[student.id].orEmpty()
            .mapNotNull { runCatching { LocalDate.parse(it.take(10)) }.getOrNull() }
            .filter { !it.isAfter(today) }
        val lastPresent = when {
            todayAttendanceIds.contains(student.id) -> today
            else -> presentDates.maxOrNull()
        }
        val joined = runCatching { LocalDate.parse(student.joinDate.take(10)) }.getOrNull() ?: return@mapNotNull null
        val streakStart = lastPresent ?: joined
        val absentDays = ChronoUnit.DAYS.between(streakStart, today).coerceAtLeast(0)
        if (absentDays < AttendanceFollowUpDays) return@mapNotNull null
        AttendanceFollowUp(
            student = student,
            absentDays = absentDays,
            lastPresentDate = lastPresent?.takeIf { it != today }?.toString(),
        )
    }.sortedWith(compareByDescending<AttendanceFollowUp> { it.absentDays }.thenBy { it.student.name.lowercase() })
}

@Composable
private fun AttendanceStreakBadge(streak: AttendanceStreak, compact: Boolean = false) {
    val (container, textColor) = when (streak.tier) {
        "legend" -> Color(0xFFECFDF5) to Color(0xFF047857)
        "gold" -> Color(0xFFFFF7ED) to Color(0xFFB45309)
        "star" -> Color(0xFFEEF2FF) to Color(0xFF4338CA)
        else -> Color(0xFFF8FAFC) to Color(0xFF475569)
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = container,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.16f)),
    ) {
        Text(
            "★ ${streak.badgeLabel}",
            modifier = Modifier.padding(horizontal = if (compact) 8.dp else 10.dp, vertical = if (compact) 4.dp else 6.dp),
            color = textColor,
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AttendanceFollowUpNudge(followUps: List<AttendanceFollowUp>) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
        border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = BrandBlue,
                    modifier = Modifier.size(18.dp),
                )
                Column {
                    Text(
                        "${followUps.size} player${if (followUps.size == 1) "" else "s"} need attendance follow-up",
                        color = BrandBlueDeep,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                    )
                    Text(
                        "No attendance marked for 5+ days. Review before marking discontinued.",
                        color = Color(0xFF416184),
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                followUps.take(6).forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.75f),
                        border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.16f)),
                    ) {
                        Text(
                            text = "${item.student.name} • ${item.absentDays}d${item.lastPresentDate?.let { " • last ${displayDate(it)}" } ?: " • never marked"}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = BrandBlueDeep,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                val remaining = followUps.size - 6
                if (remaining > 0) {
                    Text("+$remaining more", color = Color(0xFF416184), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerAttendanceToolbar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedSlot: String,
    onSlotSelected: (String) -> Unit,
    activePlayers: List<Student>,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier = Modifier.size(18.dp),
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isBlank()) {
                                Text(
                                    "Search by name or reg no",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                    fontSize = 14.sp,
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedSlot.isBlank(),
                    onClick = { onSlotSelected("all") },
                    label = { Text("All (${activePlayers.size})", fontSize = 12.sp, maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBlue,
                        selectedLabelColor = Color.White,
                    ),
                )
                UiTimeSlots.forEach { slot ->
                    val count = activePlayers.count { it.timeSlot == slot }
                    FilterChip(
                        selected = selectedSlot == slot,
                        onClick = { onSlotSelected(slot) },
                        label = { Text("$slot ($count)", fontSize = 12.sp, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandBlue,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
                val notSetCount = activePlayers.count { it.timeSlot.isBlank() }
                if (notSetCount > 0) {
                    FilterChip(
                        selected = selectedSlot == "not-set",
                        onClick = { onSlotSelected("not-set") },
                        label = { Text("Not set ($notSetCount)", fontSize = 12.sp, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandBlue,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendancePlayerCard(
    student: Student,
    isPresent: Boolean,
    streak: AttendanceStreak?,
    onMarkPresent: () -> Unit,
    onUndoPresent: () -> Unit,
) {
    val slotTone = themedBadgeTone(Color(0xFFEAF2FF), BrandBlueDeep, DarkInfoContainer, DarkInfoText)
    val presentTone = themedBadgeTone(Color(0xFFEAF8F2), BrandGreen, DarkSuccessContainer, DarkSuccessText)
    val pendingTone = themedBadgeTone(Color(0xFFFFF2D8), Color(0xFF8F6500), DarkWarningContainer, DarkWarningText)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPresent) presentTone.container.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isPresent) presentTone.text.copy(alpha = 0.25f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = student.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 22.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = student.timeSlot.ifBlank { "Slot not set" },
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    fontSize = 14.sp,
                )
                streak?.let {
                    AttendanceStreakBadge(it, compact = true)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Badge(
                    label = student.timeSlot.ifBlank { "Not set" },
                    container = slotTone.container,
                    color = slotTone.text,
                )
                Badge(
                    label = if (isPresent) "Present today" else "Attendance pending",
                    container = if (isPresent) presentTone.container else pendingTone.container,
                    color = if (isPresent) presentTone.text else pendingTone.text,
                )
            }
            if (isPresent) {
                OutlinedButton(
                    onClick = onUndoPresent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, presentTone.text.copy(alpha = 0.45f)),
                ) {
                    Text(
                        text = "Revert Present",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = presentTone.text,
                    )
                }
            } else {
                Button(
                    onClick = onMarkPresent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        text = "Mark Present",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SlotsSection(
    summaries: List<com.genalpha.cricketacademy.data.SlotSummary>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Time Slots",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "Check active batch occupancy or filter the roster instantly.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                fontSize = 13.sp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                summaries.forEach { summary ->
                    val selectedChip = (summary.value == "all" && selected.isBlank()) || selected == summary.value
                    CompactSlotChip(
                        label = summary.label,
                        count = summary.count,
                        selected = selectedChip,
                        onClick = { onSelected(summary.value) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactSlotChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) BrandBlue else MaterialTheme.colorScheme.background.copy(alpha = 0.82f),
        tonalElevation = if (selected) 0.dp else 0.5.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = count.toString(),
                color = if (selected) Color.White else BrandBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
private fun LoadingPanel() {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(color = BrandBlue)
            Column {
                Text("Loading academy register", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Fetching the latest student records from Supabase.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
            }
        }
    }
}

@Composable
private fun EmptyPanel(message: String) {
    OutlinedCard(shape = RoundedCornerShape(24.dp), colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Text(
            text = message,
            modifier = Modifier.padding(20.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            fontSize = 15.sp,
            lineHeight = 22.sp,
        )
    }
}

@Composable
private fun RosterSectionHeader(
    title: String,
    count: Int,
) {
    val countTone = themedBadgeTone(
        lightContainer = Color(0xFFEAF2FF),
        lightText = BrandBlueDeep,
        darkContainer = DarkInfoContainer,
        darkText = DarkInfoText,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Badge(
            label = count.toString(),
            container = countTone.container,
            color = countTone.text,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RosterRow(
    student: Student,
    payments: List<StudentPayment>,
    paymentFollowUp: PaymentFollowUp?,
    isManager: Boolean,
    highlighted: Boolean = false,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onRenew: (() -> Unit)? = null,
    onSendReminder: (() -> Unit)? = null,
    onToggleStatus: (() -> Unit)? = null,
    onJerseyPairsChange: ((Int, Double) -> Unit)? = null,
) {
    val needsAttention = student.isFeesPending() || student.isRenewalPending(payments)
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val highlightedContainer = if (isDarkTheme) Color(0xFF18314F) else Color(0xFFEAF4FF)
    val highlightedBorder = if (isDarkTheme) Color(0xFFFFD86B) else BrandBlue
    val slotTone = themedBadgeTone(Color(0xFFEAF2FF), BrandBlueDeep, DarkInfoContainer, DarkInfoText)
    val activeTone = themedBadgeTone(Color(0xFFEAF8F2), BrandGreen, DarkSuccessContainer, DarkSuccessText)
    val discontinuedTone = themedBadgeTone(Color(0xFFEAEFF6), Color(0xFF5D7399), DarkMutedContainer, DarkMutedText)
    val feePaidTone = themedBadgeTone(Color(0xFFEAF2FF), BrandBlue, DarkInfoContainer, DarkInfoText)
    val feePendingTone = themedBadgeTone(Color(0xFFFFE8E8), BrandRed, DarkDangerContainer, DarkDangerText)
    val feeVerificationTone = themedBadgeTone(Color(0xFFFFF2D8), Color(0xFF8F6500), DarkWarningContainer, DarkWarningText)
    val feeReminderTone = themedBadgeTone(Color(0xFFEAF2FF), BrandBlueDeep, DarkInfoContainer, DarkInfoText)
    val feeFailedTone = themedBadgeTone(Color(0xFFEAEFF6), BrandRed, DarkMutedContainer, BrandRed)
    val renewalOkTone = themedBadgeTone(Color(0xFFEAF8F2), BrandGreen, DarkSuccessContainer, DarkSuccessText)
    val renewalPendingTone = themedBadgeTone(Color(0xFFFFF2D8), Color(0xFF8F6500), DarkWarningContainer, DarkWarningText)
    val feeLabel = student.feeStatusLabel(paymentFollowUp, payments)
    val renewalStatusLabel = student.renewalStatus(payments)
    var showingActions by rememberSaveable(student.id) { mutableStateOf(false) }
    var pendingJerseyPairs by rememberSaveable(student.id) { mutableStateOf<Int?>(null) }
    var jerseyAdjustmentAmount by rememberSaveable(student.id) { mutableStateOf("") }
    val previousJerseyPairs = student.jerseyPairs.coerceAtLeast(0)
    val nextJerseyPairs = pendingJerseyPairs
    val jerseyPairDelta = (nextJerseyPairs ?: previousJerseyPairs) - previousJerseyPairs
    val jerseyAdjustmentAmountValue = jerseyAdjustmentAmount.toDoubleOrNull()
    val rotation by animateFloatAsState(
        targetValue = if (showingActions) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "roster-card-flip",
    )
    val showBack = isManager && rotation > 90f
    val density = LocalDensity.current
    val baseContainer = when {
        highlighted -> highlightedContainer
        needsAttention && isDarkTheme -> DarkAttentionCard
        needsAttention -> Color(0xFFFFFCF3)
        else -> MaterialTheme.colorScheme.surface
    }
    if (nextJerseyPairs != null) {
        val confirmedNextPairs = nextJerseyPairs
        AlertDialog(
            onDismissRequest = { pendingJerseyPairs = null },
            title = {
                Text(if (jerseyPairDelta >= 0) "Record jersey amount" else "Record jersey adjustment")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Jersey pairs: $previousJerseyPairs to $confirmedNextPairs",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        fontSize = 13.sp,
                    )
                    OutlinedTextField(
                        value = jerseyAdjustmentAmount,
                        onValueChange = { value ->
                            jerseyAdjustmentAmount = value.filter { it.isDigit() || it == '.' }
                        },
                        label = { Text(if (jerseyPairDelta >= 0) "Amount received" else "Amount adjusted/refunded") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = {
                            Text("Use 0 for complimentary jersey. Default is ${admissionAmountLabel(JerseyExtraPairFee)} per pair.")
                        },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = jerseyAdjustmentAmountValue != null && jerseyAdjustmentAmountValue >= 0.0,
                    onClick = {
                        onJerseyPairsChange?.invoke(confirmedNextPairs, jerseyAdjustmentAmountValue ?: 0.0)
                        pendingJerseyPairs = null
                    },
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingJerseyPairs = null }) {
                    Text("Cancel")
                }
            },
        )
    }
    val baseBorder = when {
        highlighted -> BorderStroke(2.dp, highlightedBorder)
        needsAttention -> BorderStroke(1.dp, if (isDarkTheme) DarkAttentionBorder else Color(0x33F4BE2E))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
    }

    LaunchedEffect(isManager) {
        if (!isManager) showingActions = false
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        if (showBack) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationY = rotation - 180f
                        cameraDistance = 18f * density.density
                    },
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BrandBlue.copy(alpha = if (isDarkTheme) 0.45f else 0.22f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    BrandBlue.copy(alpha = if (isDarkTheme) 0.22f else 0.10f),
                                    BrandGold.copy(alpha = if (isDarkTheme) 0.10f else 0.06f),
                                    MaterialTheme.colorScheme.surface,
                                )
                            )
                        )
                        .padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Manage player",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                                Text(
                                    text = student.name,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            TextButton(onClick = { showingActions = false }) {
                                Text("Back")
                            }
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            RosterActionButton(
                                label = "Profile",
                                tint = BrandBlue,
                                onClick = {
                                    showingActions = false
                                    onOpen()
                                },
                            )
                            RosterActionButton(
                                label = "Edit details",
                                tint = BrandBlue,
                                onClick = {
                                    showingActions = false
                                    onEdit()
                                },
                            )
                            onRenew?.let { renew ->
                                RosterActionButton(
                                    label = if (student.isFeesPending()) "Record joining fee" else "Renew payment",
                                    tint = BrandGreen,
                                    onClick = {
                                        showingActions = false
                                        renew()
                                    },
                                )
                            }
                            onSendReminder?.let { sendReminder ->
                                RosterActionButton(
                                    label = "Send reminder",
                                    tint = Color(0xFF9A6400),
                                    onClick = {
                                        showingActions = false
                                        sendReminder()
                                    },
                                )
                            }
                            onToggleStatus?.let { toggle ->
                                RosterActionButton(
                                    label = if (student.discontinued) "Mark active" else "Discontinue",
                                    tint = if (student.discontinued) BrandGreen else BrandRed,
                                    onClick = {
                                        showingActions = false
                                        toggle()
                                    },
                                )
                            }
                        }

                        Text(
                            text = "Delete stays inside the player profile so it cannot be tapped by mistake.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 18f * density.density
                    }
                    .clickable(onClick = onOpen),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = baseContainer),
                border = baseBorder,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = student.name,
                                    fontSize = 18.sp,
                                    lineHeight = 21.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                                if (student.isSpecialTraining(payments)) {
                                    Surface(
                                        color = Color(0xFFFFF9E6),
                                        shape = RoundedCornerShape(999.dp),
                                        border = BorderStroke(1.dp, Color(0xFFF4BE2E))
                                    ) {
                                        Text(
                                            text = "SPECIAL",
                                            color = Color(0xFF8F6500),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${student.tenureBadgeLabel()} training  •  Joined ${displayDate(student.joinDate)}",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Badge(
                            label = if (student.discontinued) "Discontinued" else "Active",
                            container = if (student.discontinued) discontinuedTone.container else activeTone.container,
                            color = if (student.discontinued) discontinuedTone.text else activeTone.text,
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Badge(
                            label = student.timeSlot.ifBlank { "Not set" },
                            container = slotTone.container,
                            color = slotTone.text,
                        )
                        Badge(
                            label = feeLabel,
                            container = when {
                                feeLabel == "Reminder failed" -> feeFailedTone.container
                                feeLabel == "Retry scheduled" -> feeReminderTone.container
                                feeLabel == "Reminder sent" -> feeReminderTone.container
                                student.feesPaid -> feePaidTone.container
                                feeLabel == "Pending verification" -> feeVerificationTone.container
                                else -> feePendingTone.container
                            },
                            color = when {
                                feeLabel == "Reminder failed" -> feeFailedTone.text
                                feeLabel == "Retry scheduled" -> feeReminderTone.text
                                feeLabel == "Reminder sent" -> feeReminderTone.text
                                student.feesPaid -> feePaidTone.text
                                feeLabel == "Pending verification" -> feeVerificationTone.text
                                else -> feePendingTone.text
                            },
                        )
                        Badge(
                            label = renewalStatusLabel,
                            container = when {
                                student.discontinued -> discontinuedTone.container
                                student.isFeesPending() -> feePendingTone.container
                                student.isRenewalPending(payments) -> renewalPendingTone.container
                                else -> renewalOkTone.container
                            },
                            color = when {
                                student.discontinued -> discontinuedTone.text
                                student.isFeesPending() -> feePendingTone.text
                                student.isRenewalPending(payments) -> renewalPendingTone.text
                                else -> renewalOkTone.text
                            },
                        )
                    }

                    student.cardTimelineLabel(payments)?.let { timeline ->
                        Text(
                            text = timeline,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (onRenew != null && isManager) {
                        OutlinedButton(
                            onClick = {
                                showingActions = false
                                onRenew()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, BrandGreen.copy(alpha = 0.35f)),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                containerColor = BrandGreen.copy(alpha = if (isDarkTheme) 0.14f else 0.08f),
                                contentColor = BrandGreen,
                            ),
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (student.isFeesPending()) "Record Joining Fee" else "Renew Payment",
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (student.jerseySize.isBlank() && student.jerseyPairs <= 0) {
                                "Jersey not set"
                            } else {
                                "Jersey ${student.jerseySize.ifBlank { "TBD" }} • ${student.jerseyPairs} pair${if (student.jerseyPairs == 1) "" else "s"}"
                            },
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (isManager) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                onJerseyPairsChange?.let {
                                    JerseyPairStepper(
                                        count = student.jerseyPairs.coerceAtLeast(0),
                                        onDecrease = {
                                            val nextPairs = (student.jerseyPairs - 1).coerceAtLeast(0)
                                            pendingJerseyPairs = nextPairs
                                            jerseyAdjustmentAmount = String.format(Locale.US, "%.0f", JerseyExtraPairFee)
                                        },
                                        onIncrease = {
                                            val nextPairs = student.jerseyPairs + 1
                                            pendingJerseyPairs = nextPairs
                                            jerseyAdjustmentAmount = String.format(Locale.US, "%.0f", JerseyExtraPairFee)
                                        },
                                    )
                                }
                                Surface(
                                    onClick = { showingActions = true },
                                    shape = RoundedCornerShape(999.dp),
                                    color = BrandBlue.copy(alpha = if (isDarkTheme) 0.22f else 0.10f),
                                    border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.20f)),
                                ) {
                                    Icon(
                                        Icons.Outlined.Edit,
                                        contentDescription = "Manage player",
                                        modifier = Modifier.padding(9.dp).size(15.dp),
                                        tint = BrandBlue,
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Tap for profile",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JerseyPairStepper(
    count: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = BrandBlue.copy(alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.18f else 0.08f),
        border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                enabled = count > 0,
                onClick = onDecrease,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Text(
                    text = "−",
                    color = if (count > 0) BrandBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
            Text(
                text = "$count",
                color = BrandBlueDeep,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.widthIn(min = 18.dp).wrapContentWidth(Alignment.CenterHorizontally),
            )
            Surface(
                onClick = onIncrease,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Text(
                    text = "+",
                    color = BrandBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun RosterActionButton(
    label: String,
    tint: Color,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .heightIn(min = 46.dp)
            .widthIn(min = 146.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.28f)),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = tint.copy(alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.16f else 0.07f),
            contentColor = tint,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PlayerDetailSheet(
    student: Student,
    payments: List<StudentPayment>,
    paymentFollowUp: PaymentFollowUp?,
    attendanceCount: Int,
    timeline: List<StudentTimelineItem>,
    isTimelineLoading: Boolean,
    isManager: Boolean,
    onDismiss: () -> Unit,
    onShowAttendanceHistory: () -> Unit,
    onEdit: () -> Unit,
    onDelete: suspend () -> Unit,
    onDeletePayment: suspend (String) -> Unit,
    onRenew: suspend () -> Unit,
    onSendReminder: suspend () -> Unit,
    onConfirmPayment: suspend () -> Unit,
    onToggleStatus: suspend () -> Unit,
    onSendAdmissionReminder: suspend () -> Unit,
) {
    var actionInProgress by rememberSaveable(student.id) { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by rememberSaveable(student.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val fontScale = LocalDensity.current.fontScale
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val slotTone = themedBadgeTone(Color(0xFFEAF2FF), BrandBlueDeep, DarkInfoContainer, DarkInfoText)
    val newTone = themedBadgeTone(Color(0xFFEAF2FF), BrandBlueDeep, DarkInfoContainer, DarkInfoText)
    val returningTone = themedBadgeTone(Color(0xFFFFF2D8), Color(0xFF8F6500), DarkWarningContainer, DarkWarningText)
    val activeTone = themedBadgeTone(Color(0xFFEAF8F2), BrandGreen, DarkSuccessContainer, DarkSuccessText)
    val discontinuedTone = themedBadgeTone(Color(0xFFEAEFF6), Color(0xFF5D7399), DarkMutedContainer, DarkMutedText)
    val renewalOkTone = themedBadgeTone(Color(0xFFEAF8F2), BrandGreen, DarkSuccessContainer, DarkSuccessText)
    val renewalPendingTone = themedBadgeTone(Color(0xFFFFF2D8), Color(0xFF8F6500), DarkWarningContainer, DarkWarningText)
    val feeReminderTone = themedBadgeTone(Color(0xFFEAF2FF), BrandBlueDeep, DarkInfoContainer, DarkInfoText)
    val feeFailedTone = themedBadgeTone(Color(0xFFEAEFF6), BrandRed, DarkMutedContainer, BrandRed)
    val feeVerificationTone = themedBadgeTone(Color(0xFFFFF2D8), Color(0xFF8F6500), DarkWarningContainer, DarkWarningText)
    val paymentRows = remember(student, payments) { buildPlayerPaymentRows(student, payments) }
    val totalPaid = paymentRows.sumOf { it.amount }
    val totalMonthsPaid = paymentRows.sumOf { it.months }
    val reminderDue = student.isRenewalPending(payments)
    val reminderOverdueDays = remember(student, payments) { reminderOverdueDays(student, payments) }
    val feeLabel = student.feeStatusLabel(paymentFollowUp, payments)
    val pendingFollowUp = paymentFollowUp?.takeIf { it.isPendingVerification() }
        ?: if (student.isPaymentPendingVerification()) {
            PaymentFollowUp(
                studentId = student.id,
                selectedPlan = "monthly",
                amount = student.amountPaid ?: 3500.0,
                monthsCovered = 1,
                cycleStartDate = student.joinDate,
                reminderType = "joining_fee"
            )
        } else null
    val pendingPlan = pendingFollowUp?.selectedPlan?.takeIf { it.isNotBlank() } ?: "monthly"
    val pendingMonths = pendingFollowUp?.monthsCovered?.takeIf { it > 0 } ?: when (pendingPlan) {
        "quarterly" -> 3
        "halfyearly" -> 6
        else -> 1
    }
    val pendingAmount = pendingFollowUp?.amount?.takeIf { it > 0.0 } ?: when (pendingPlan) {
        "quarterly" -> 9975.0
        "halfyearly" -> 18900.0
        "special" -> 10000.0
        else -> 3500.0
    }
    val pendingFromDate = pendingFollowUp?.cycleStartDate?.takeIf { it.isNotBlank() } ?: student.nextRenewalCycleDate(payments)
    val pendingToDate = addMonthsForPlan(pendingFromDate, pendingMonths)

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete player?") },
            text = {
                Text(
                    "Delete ${student.name}? This will permanently remove the player record.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            },
            confirmButton = {
                TextButton(
                    enabled = actionInProgress == null,
                    onClick = {
                        scope.launch {
                            showDeleteConfirmation = false
                            actionInProgress = "delete"
                            onDelete()
                            actionInProgress = null
                        }
                    },
                ) {
                    Text("Delete", color = BrandRed)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = actionInProgress == null,
                    onClick = { showDeleteConfirmation = false },
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Player Profile",
                        fontSize = adaptiveSp(11f, fontScale, minRatio = 0.78f),
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue,
                    )
                    Text(
                        text = student.name,
                        fontSize = adaptiveSp(26f, fontScale, minRatio = 0.70f),
                        lineHeight = adaptiveSp(29f, fontScale, minRatio = 0.70f),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    student.regNo?.let {
                        Text(
                            "Reg #$it",
                            fontSize = adaptiveSp(12f, fontScale, minRatio = 0.76f),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Badge(student.timeSlot.ifBlank { "No slot" }, slotTone.container, slotTone.text)
                Badge(
                    student.studentType(),
                    if (student.studentType() == "Returning") returningTone.container else newTone.container,
                    if (student.studentType() == "Returning") returningTone.text else newTone.text,
                )
                Badge(
                    if (student.discontinued) "Discontinued" else "Active",
                    if (student.discontinued) discontinuedTone.container else activeTone.container,
                    if (student.discontinued) discontinuedTone.text else activeTone.text,
                )
                Badge(
                    feeLabel,
                    when (feeLabel) {
                        "Reminder failed" -> feeFailedTone.container
                        "Retry scheduled" -> feeReminderTone.container
                        "Reminder sent" -> feeReminderTone.container
                        "Pending verification" -> feeVerificationTone.container
                        "Fees paid" -> renewalOkTone.container
                        else -> renewalPendingTone.container
                    },
                    when (feeLabel) {
                        "Reminder failed" -> feeFailedTone.text
                        "Retry scheduled" -> feeReminderTone.text
                        "Reminder sent" -> feeReminderTone.text
                        "Pending verification" -> feeVerificationTone.text
                        "Fees paid" -> renewalOkTone.text
                        else -> renewalPendingTone.text
                    },
                )
            }

            // ── Quick Actions (top) ──
            if (isManager) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(enabled = actionInProgress == null, onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp)); Text("Edit")
                    }
                    if ((student.isFeesPending() || student.isRenewalPending(payments)) && student.isActive()) {
                        ElevatedButton(
                            enabled = actionInProgress == null,
                            onClick = { scope.launch { actionInProgress = "renew"; onRenew(); actionInProgress = null } },
                        ) {
                            if (actionInProgress == "renew") CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                            else Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp)); Text(if (student.isFeesPending()) "Joining payment" else "Renew")
                        }
                    }
                    if (reminderDue && student.isActive()) {
                        OutlinedButton(
                            enabled = actionInProgress == null,
                            onClick = { scope.launch { actionInProgress = "reminder"; onSendReminder(); actionInProgress = null } },
                        ) {
                            if (actionInProgress == "reminder") CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp)); Text("Send reminder")
                        }
                    }
                    if (!student.feesPaid && student.isActive()) {
                        OutlinedButton(
                            enabled = actionInProgress == null,
                            onClick = { scope.launch { actionInProgress = "joining_reminder"; onSendAdmissionReminder(); actionInProgress = null } },
                        ) {
                            if (actionInProgress == "joining_reminder") CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp)); Text("Remind joining fee")
                        }
                    }
                }
            }

            // ── Quick Stats ──
            ProfileSectionCard(title = "Overview") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DataTileContent(modifier = Modifier.weight(1f), label = "Age", value = student.age.toString(), accent = MaterialTheme.colorScheme.onSurface)
                    DataTileContent(modifier = Modifier.weight(1f), label = "Days Present", value = attendanceCount.toString(), accent = BrandBlue, onClick = onShowAttendanceHistory)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DataTileContent(modifier = Modifier.weight(1f), label = "Training", value = student.trainingDurationLabel(), accent = MaterialTheme.colorScheme.onSurface)
                    DataTileContent(modifier = Modifier.weight(1f), label = "Membership Dates", value = student.membershipDateLabel(), accent = MaterialTheme.colorScheme.onSurface)
                }
                if (student.jerseySize.isNotBlank() || student.jerseyPairs > 0) {
                    DataTileContent(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Jersey",
                        value = "${student.jerseySize.ifBlank { "TBD" }} • ${student.jerseyPairs} pair${if (student.jerseyPairs == 1) "" else "s"}",
                        accent = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            // ── Fee & Renewal ──
            ProfileSectionCard(title = "Fee & Renewal") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DataTileContent(
                        modifier = Modifier.weight(1f),
                        label = "Initial Amount",
                        value = "Rs ${String.format(Locale.US, "%.0f", student.amountPaid)}",
                        accent = MaterialTheme.colorScheme.onSurface,
                    )
                    DataTileContent(
                        modifier = Modifier.weight(1f),
                        label = "Next Fee Due",
                        value = if (student.discontinued) "Paused" else displayDate(student.nextRenewalCycleDate(payments)),
                        accent = if (!student.discontinued && student.isRenewalPending(payments)) BrandRed else MaterialTheme.colorScheme.onSurface,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DataTileContent(
                        modifier = Modifier.weight(1f),
                        label = if (student.discontinued) "Discontinued" else "Latest Renewal",
                        value = if (student.discontinued) displayDate(student.discontinuedAt) else displayDate(student.latestRenewal(payments)),
                        accent = MaterialTheme.colorScheme.onSurface,
                    )
                    DataTileContent(
                        modifier = Modifier.weight(1f),
                        label = "Renewal Status",
                        value = student.renewalStatus(payments),
                        accent = when {
                            student.discontinued -> discontinuedTone.text
                            student.isFeesPending() -> BrandRed
                            student.isRenewalPending(payments) -> BrandRed
                            else -> BrandGreen
                        },
                    )
                }
                Text(
                    text = student.trackingCaption(payments),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                )
            }

            if (reminderOverdueDays > 10) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BrandRed.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, BrandRed.copy(alpha = 0.22f)),
                ) {
                    Text(
                        text = "⚠ Overdue for $reminderOverdueDays days. Follow up with parent today.",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        color = BrandRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }

            if (pendingFollowUp != null && isManager) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFFFF2D8).copy(alpha = if (isDarkTheme) 0.16f else 0.72f),
                    border = BorderStroke(1.dp, Color(0xFFF4BF2A).copy(alpha = 0.3f)),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Payment pending verification", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "${renewalPlanLabel(pendingPlan)} • Rs ${String.format(Locale.US, "%,.0f", pendingAmount)} • ${displayDate(pendingFromDate)} to ${displayDate(pendingToDate)}",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                            fontSize = 12.sp, lineHeight = 16.sp,
                        )
                        Button(
                            onClick = { scope.launch { actionInProgress = "confirm-payment"; onConfirmPayment(); actionInProgress = null } },
                            enabled = actionInProgress == null,
                            shape = RoundedCornerShape(15.dp),
                        ) {
                            if (actionInProgress == "confirm-payment") {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                                Spacer(Modifier.width(8.dp))
                            }
                            Text("Confirm payment received")
                        }
                    }
                }
            }

            // ── Payment History ──
            ProfileSectionCard(title = "Payment History") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DataTileContent(modifier = Modifier.weight(1f), label = "Total Paid", value = "Rs ${String.format(Locale.US, "%,.0f", totalPaid)}", accent = BrandGreen)
                    DataTileContent(modifier = Modifier.weight(1f), label = "Months Paid", value = totalMonthsPaid.toString(), accent = MaterialTheme.colorScheme.onSurface)
                }
                if (paymentRows.isEmpty()) {
                    Text("No paid fee records yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontSize = 12.sp)
                } else {
                    paymentRows.forEach { row ->
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(row.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                Text(
                                    "${displayDate(row.date)} • ${row.plan}${if (row.months > 0) " • ${row.months}m" else ""}",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                                    fontSize = 11.sp,
                                )
                            }
                            Text(
                                "Rs ${String.format(Locale.US, "%,.0f", row.amount)}",
                                color = if (row.amount < 0) BrandRed else BrandGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            if (isManager && row.id.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            actionInProgress = "delete-payment-${row.id}"
                                            onDeletePayment(row.id)
                                            actionInProgress = null
                                        }
                                    },
                                    modifier = Modifier.size(32.dp),
                                    enabled = actionInProgress == null
                                ) {
                                    if (actionInProgress == "delete-payment-${row.id}") {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = BrandRed)
                                    } else {
                                        Icon(Icons.Outlined.Close, contentDescription = "Delete payment", tint = BrandRed.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Parent & Contact ──
            ProfileSectionCard(title = "Parent & Contact") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DataTileContent(
                        modifier = Modifier.weight(1f),
                        label = "Guardian",
                        value = student.fatherGuardianName.ifBlank { "Not saved" },
                        accent = MaterialTheme.colorScheme.onSurface,
                    )
                    if (student.filledBy.isNotBlank()) {
                        DataTileContent(
                            modifier = Modifier.weight(1f),
                            label = "Form filled by",
                            value = student.filledBy,
                            accent = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                if (student.parentContactNo.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        DataTileContent(modifier = Modifier.weight(1f), label = "Phone", value = student.parentContactNo, accent = MaterialTheme.colorScheme.onSurface)
                        Button(
                            onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.parentContactNo}"))) },
                            shape = RoundedCornerShape(14.dp),
                        ) { Text("Call", fontSize = 12.sp) }
                    }
                }
                if (student.alternateContactNo.isNotBlank()) {
                    DataTileContent(modifier = Modifier.fillMaxWidth(), label = "Alternate Contact", value = student.alternateContactNo, accent = MaterialTheme.colorScheme.onSurface)
                }
                if (student.schoolCollege.isNotBlank() || student.grade.isNotBlank()) {
                    DataTileContent(
                        modifier = Modifier.fillMaxWidth(),
                        label = "School / Grade",
                        value = listOfNotNull(
                            student.schoolCollege.takeIf { it.isNotBlank() },
                            student.grade.takeIf { it.isNotBlank() }?.let { "Grade $it" },
                        ).joinToString(" • "),
                        accent = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (student.address.isNotBlank()) {
                    DataTileContent(modifier = Modifier.fillMaxWidth(), label = "Address", value = student.address, accent = MaterialTheme.colorScheme.onSurface)
                }
            }

            ProfileSectionCard(title = "Timeline") {
                PlayerTimelineList(
                    timeline = timeline.take(12),
                    isLoading = isTimelineLoading,
                )
            }

            // ── Footer ──
            Text(
                text = "Last updated by ${student.updatedBy}",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                fontSize = 11.sp,
            )

            // ── Danger Zone ──
            if (isManager) {
                HorizontalDivider(color = BrandRed.copy(alpha = 0.12f))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        enabled = actionInProgress == null,
                        onClick = { scope.launch { actionInProgress = "status"; onToggleStatus(); actionInProgress = null } },
                        border = BorderStroke(1.dp, BrandRed.copy(alpha = 0.3f)),
                    ) {
                        if (actionInProgress == "status") { CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = BrandRed); Spacer(Modifier.width(8.dp)) }
                        Text(if (student.discontinued) "Mark active" else "Discontinue", color = BrandRed.copy(alpha = 0.8f))
                    }
                    TextButton(
                        enabled = actionInProgress == null,
                        onClick = { showDeleteConfirmation = true },
                    ) {
                        if (actionInProgress == "delete") { CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = BrandRed); Spacer(Modifier.width(8.dp)) }
                        Text("Delete player", color = BrandRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PlayerTimelineList(
    timeline: List<StudentTimelineItem>,
    isLoading: Boolean,
) {
    when {
        isLoading -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
            Text("Loading timeline...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f), fontSize = 12.sp)
        }
        timeline.isEmpty() -> Text(
            "No timeline events recorded yet.",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            fontSize = 12.sp,
        )
        else -> Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            timeline.forEachIndexed { index, item ->
                PlayerTimelineEvent(
                    item = item,
                    isLast = index == timeline.lastIndex,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerTimelineEvent(
    item: StudentTimelineItem,
    isLast: Boolean,
) {
    val eventText = "${item.eventType} ${item.title} ${item.details.orEmpty()}".lowercase()
    val dotColor = when {
        eventText.contains("failed") || eventText.contains("error") -> BrandRed
        eventText.contains("confirmed") ||
            eventText.contains("paid") ||
            eventText.contains("payment") ||
            eventText.contains("renew") -> BrandGreen
        eventText.contains("reminder") ||
            eventText.contains("whatsapp") ||
            eventText.contains("message") -> BrandGold
        item.eventType in listOf("admission", "created") -> BrandBlue
        item.eventType == "discontinued" -> BrandRed
        item.eventType == "active" -> BrandGreen
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f)
    }
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.width(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(12.dp)
                    .background(dotColor.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(dotColor, CircleShape)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(58.dp)
                        .background(dotColor.copy(alpha = if (isDarkTheme) 0.30f else 0.18f), RoundedCornerShape(99.dp))
                )
            }
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            color = dotColor.copy(alpha = if (isDarkTheme) 0.13f else 0.07f),
            border = BorderStroke(1.dp, dotColor.copy(alpha = if (isDarkTheme) 0.24f else 0.14f)),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    item.title,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TimelineMetaChip(displayTimelineStamp(item.createdAt, item.eventDate), dotColor)
                    TimelineMetaChip(item.changedBy.orEmpty().ifBlank { "System" }, dotColor)
                }
                if (!item.details.isNullOrBlank()) {
                    Text(
                        item.details,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                    )
                }
                if (item.proofUrl.isNotBlank()) {
                    PaymentProofThumbnail(url = item.proofUrl)
                }
            }
        }
    }
}

@Composable
private fun TimelineMetaChip(
    label: String,
    tint: Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tint.copy(alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.16f else 0.09f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PaymentProofThumbnail(url: String) {
    var showViewer by rememberSaveable(url) { mutableStateOf(false) }
    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, key1 = url) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                URL(url).openStream().use { stream -> BitmapFactory.decodeStream(stream) }
            }.getOrNull()
        }
    }

    Surface(
        modifier = Modifier
            .padding(top = 4.dp)
            .clickable(enabled = bitmap != null) { showViewer = true },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Payment proof thumbnail",
                    modifier = Modifier
                        .size(width = 54.dp, height = 42.dp)
                        .clip(RoundedCornerShape(9.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 54.dp, height = 42.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }
            Text(
                "View proof",
                color = BrandBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }

    if (showViewer && bitmap != null) {
        Dialog(onDismissRequest = { showViewer = false }) {
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Payment proof",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val fontScale = LocalDensity.current.fontScale

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.84f),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                fontSize = adaptiveSp(12f, fontScale, minRatio = 0.78f),
                letterSpacing = 0.04.em,
            )
            content()
        }
    }
}

@Composable
private fun Badge(label: String, container: Color, color: Color) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val fontScale = LocalDensity.current.fontScale
    val resolvedContainer = if (isDarkTheme) {
        Color(container.red, container.green, container.blue, 0.22f)
    } else {
        container
    }
    val resolvedColor = if (isDarkTheme) color.copy(alpha = 0.96f) else color

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = resolvedContainer,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = resolvedColor,
            fontSize = adaptiveSp(12f, fontScale, minRatio = 0.78f),
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DataTile(label: String, value: String, accent: Color = BrandBlueDeep) {
    DataTileContent(
        modifier = Modifier.fillMaxWidth(),
        label = label,
        value = value,
        accent = accent,
    )
}

@Composable
private fun DataTileContent(
    modifier: Modifier,
    label: String,
    value: String,
    accent: Color,
    onClick: (() -> Unit)? = null,
) {
    val fontScale = LocalDensity.current.fontScale

    Surface(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.84f),
    ) {
        Column(
            modifier = Modifier
                .heightIn(min = 82.dp)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = if (onClick != null) "${label.uppercase(Locale.getDefault())} >" else label.uppercase(Locale.getDefault()),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                fontSize = adaptiveSp(10.5f, fontScale, minRatio = 0.76f),
                lineHeight = adaptiveSp(12.5f, fontScale, minRatio = 0.76f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                color = accent,
                fontSize = adaptiveSp(15f, fontScale, minRatio = 0.72f),
                lineHeight = adaptiveSp(18f, fontScale, minRatio = 0.72f),
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AttendanceHistoryDialog(
    studentName: String,
    initialDates: List<String>?,
    onDismiss: () -> Unit,
    onLoadAttendanceHistory: suspend () -> List<String>,
) {
    var dates by remember(initialDates) { mutableStateOf(initialDates) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(initialDates) {
        if (dates == null) {
            runCatching { onLoadAttendanceHistory() }
                .onSuccess {
                    dates = it
                    errorMessage = null
                }
                .onFailure { error ->
                    dates = emptyList()
                    errorMessage = error.message ?: "Unable to load attendance history."
                }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val dialogDensity = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides dialogDensity) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
                    .padding(horizontal = 20.dp, vertical = 40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 760.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Attendance", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                                Text(
                                    text = studentName,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            IconButton(onClick = onDismiss, modifier = Modifier.size(34.dp)) {
                                Icon(Icons.Outlined.Close, contentDescription = "Close")
                            }
                        }

                        when {
                            dates == null -> {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Text("Loading attendance days", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
                                }
                            }
                            errorMessage != null -> {
                                Text(errorMessage.orEmpty(), color = BrandRed, fontSize = 13.sp)
                            }
                            else -> {
                                val attendanceDates = dates.orEmpty()
                                val months = remember(attendanceDates) { buildAttendanceMonths(attendanceDates) }
                                val presentSet = remember(attendanceDates) { attendanceDates.toSet() }
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.84f),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Text(
                                            text = if (attendanceDates.isEmpty()) {
                                                "No attendance marked yet."
                                            } else {
                                                "${attendanceDates.size} training day${if (attendanceDates.size == 1) "" else "s"} marked"
                                            },
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                                            fontSize = 12.sp,
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        ) {
                                            months.forEach { month ->
                                                AttendanceMonthCard(
                                                    month = month,
                                                    attendanceDates = presentSet,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class CalendarMonth(
    val year: Int,
    val month: Int,
)

@Composable
private fun AttendanceMonthCard(
    month: CalendarMonth,
    attendanceDates: Set<String>,
) {
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
    val daysInMonth = remember(month) { monthDays(month.year, month.month) }
    val firstOffset = remember(month) { monthFirstDayOffset(month.year, month.month) }
    val totalCells = remember(daysInMonth, firstOffset) {
        val raw = firstOffset + daysInMonth
        if (raw % 7 == 0) raw else raw + (7 - (raw % 7))
    }

    Surface(
        modifier = Modifier.widthIn(min = 176.dp, max = 176.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = monthLabel(month.year, month.month),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = monthAttendanceCount(month, attendanceDates),
                    color = BrandBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                dayLabels.forEach { label ->
                    Text(
                        modifier = Modifier.weight(1f),
                        text = label,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (weekStart in 0 until totalCells step 7) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        for (column in 0 until 7) {
                            val cellIndex = weekStart + column
                            val dayNumber = cellIndex - firstOffset + 1
                            val validDay = dayNumber in 1..daysInMonth
                            val isoDate = if (validDay) {
                                String.format(Locale.US, "%04d-%02d-%02d", month.year, month.month + 1, dayNumber)
                            } else {
                                ""
                            }
                            val attended = validDay && attendanceDates.contains(isoDate)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(22.dp),
                                shape = RoundedCornerShape(7.dp),
                                color = when {
                                    attended -> BrandBlue.copy(alpha = 0.14f)
                                    validDay -> MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                                    else -> Color.Transparent
                                },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (validDay) dayNumber.toString() else "",
                                        color = when {
                                            attended -> BrandBlue
                                            validDay -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            else -> Color.Transparent
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = if (attended) FontWeight.Bold else FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildAttendanceMonths(attendanceDates: List<String>): List<CalendarMonth> {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val months = mutableListOf<CalendarMonth>()
    repeat(6) {
        months += CalendarMonth(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH),
        )
        calendar.add(Calendar.MONTH, -1)
    }
    return months
}

private fun monthDays(year: Int, month: Int): Int {
    return Calendar.getInstance().run {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
        getActualMaximum(Calendar.DAY_OF_MONTH)
    }
}

private fun monthFirstDayOffset(year: Int, month: Int): Int {
    return Calendar.getInstance().run {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
        get(Calendar.DAY_OF_WEEK) - 1
    }
}

private fun monthLabel(year: Int, month: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    return java.text.SimpleDateFormat("MMM yyyy", Locale.US).format(calendar.time)
}

private fun monthAttendanceCount(month: CalendarMonth, attendanceDates: Set<String>): String {
    val prefix = String.format(Locale.US, "%04d-%02d-", month.year, month.month + 1)
    val count = attendanceDates.count { it.startsWith(prefix) }
    return if (count == 0) "No sessions" else "$count present"
}

private data class RosterSection(
    val key: String,
    val title: String,
    val students: List<Student>,
)

private fun rosterLazyListIndexForStudent(
    sections: List<RosterSection>,
    studentId: String,
    hasMovementBanner: Boolean,
): Int? {
    var index = 8
    if (hasMovementBanner) index += 1

    sections.forEach { section ->
        index += 1
        section.students.forEach { student ->
            if (student.id == studentId) return index
            index += 1
        }
    }

    return null
}

private fun buildRosterSections(students: List<Student>, sortKey: String): List<RosterSection> {
    val sections = mutableListOf<RosterSection>()
    
    // If sorting by nextDue or name (not slot), show a flat list or special sections
    if (sortKey == "nextDue") {
        if (students.isNotEmpty()) {
            sections += RosterSection("all", "All Players (Sorted by Due Date)", students)
        }
        return sections
    }

    val slotOrder = listOf("6AM", "7:30AM", "4PM", "5:30PM", "7PM")
    slotOrder.forEach { slot ->
        val matches = students
            .filter { it.isActive() && it.timeSlot == slot }
        if (matches.isNotEmpty()) {
            sections += RosterSection(slot, "$slot Batch", matches)
        }
    }

    val unassigned = students
        .filter { it.isActive() && it.timeSlot.isBlank() }
    if (unassigned.isNotEmpty()) {
        sections += RosterSection("not-set", "Time Slot Not Set", unassigned)
    }

    val discontinued = students
        .filter { it.discontinued }
    if (discontinued.isNotEmpty()) {
        sections += RosterSection("discontinued", "Discontinued", discontinued)
    }

    val ungrouped = students
        .filter { it.isActive() && it.timeSlot.isNotBlank() && it.timeSlot !in slotOrder }
    if (ungrouped.isNotEmpty()) {
        sections += RosterSection("other", "Other Slots", ungrouped)
    }

    return sections
}

private fun buildAttendanceSections(students: List<Student>): List<RosterSection> {
    val slotOrder = listOf("6AM", "7:30AM", "4PM", "5:30PM", "7PM")
    val sections = mutableListOf<RosterSection>()

    slotOrder.forEach { slot ->
        val matches = students
            .filter { it.timeSlot == slot }
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
        if (matches.isNotEmpty()) {
            sections += RosterSection(slot, "$slot Check-In", matches)
        }
    }

    val unassigned = students
        .filter { it.timeSlot.isBlank() }
        .sortedBy { it.name.lowercase(Locale.getDefault()) }
    if (unassigned.isNotEmpty()) {
        sections += RosterSection("not-set", "Time Slot Not Set", unassigned)
    }

    val customSlots = students
        .filter { it.timeSlot.isNotBlank() && it.timeSlot !in slotOrder }
        .groupBy { it.timeSlot }
        .toSortedMap()
    customSlots.forEach { (slot, players) ->
        sections += RosterSection("custom-$slot", slot, players.sortedBy { it.name.lowercase(Locale.getDefault()) })
    }

    return sections
}

@Composable
private fun LoginSheet(
    lastEmail: String,
    lastPassword: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: suspend (String, String) -> OperationResult,
) {
    var email by rememberSaveable(lastEmail) { mutableStateOf(lastEmail) }
    var password by rememberSaveable(lastPassword) { mutableStateOf(lastPassword) }
    var inlineMessage by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    FormDialog(onDismiss = onDismiss, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Manager Access", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                IconButton(onClick = onDismiss, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Secure editing access", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Sign in to unlock editing features.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = { Text("Manager email") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                )
            }
            if (inlineMessage.isNotBlank()) {
                Text(inlineMessage, color = BrandRed, fontSize = 13.sp)
            }
            Button(
                enabled = !isLoading,
                onClick = {
                    scope.launch {
                        inlineMessage = ""
                        val result = onSubmit(email, password)
                        if (!result.success) {
                            inlineMessage = result.message
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Signing in")
                } else {
                    Icon(Icons.Outlined.PersonAddAlt1, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Login")
                }
            }
        }
    }
}

@Composable
private fun ManagerPinSheet(
    onDismiss: () -> Unit,
    onUnlock: (String) -> OperationResult,
) {
    var pin by rememberSaveable { mutableStateOf("") }
    var inlineMessage by rememberSaveable { mutableStateOf("") }
    var isChecking by rememberSaveable { mutableStateOf(false) }
    val dialogDensity = LocalDensity.current

    LaunchedEffect(pin, isChecking) {
        if (pin.length == 6 && !isChecking) {
            isChecking = true
            val result = onUnlock(pin)
            if (!result.success) {
                inlineMessage = result.message
                pin = ""
            }
            isChecking = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        CompositionLocalProvider(LocalDensity provides dialogDensity) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Staff PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Close, contentDescription = "Close")
                            }
                        }
                        Text(
                            "Enter 6-digit PIN",
                            fontSize = 22.sp,
                            lineHeight = 25.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "Unlock staff-only dashboard and player management.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            repeat(6) { index ->
                                val char = pin.getOrNull(index)?.toString().orEmpty()
                                val isActiveSlot = index == pin.length.coerceAtMost(5)
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        width = if (isActiveSlot || char.isNotEmpty()) 1.5.dp else 1.dp,
                                        color = when {
                                            char.isNotEmpty() -> BrandBlue.copy(alpha = 0.75f)
                                            isActiveSlot -> BrandBlue.copy(alpha = 0.42f)
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                        }
                                    ),
                                    tonalElevation = 0.dp,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = char,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                        )
                                    }
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                listOf("1", "2", "3"),
                                listOf("4", "5", "6"),
                                listOf("7", "8", "9"),
                                listOf("Clear", "0", "Del"),
                            ).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    row.forEach { key ->
                                        OutlinedButton(
                                            enabled = !isChecking,
                                            onClick = {
                                                inlineMessage = ""
                                                pin = when (key) {
                                                    "Clear" -> ""
                                                    "Del" -> pin.dropLast(1)
                                                    else -> (pin + key).take(6)
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp),
                                            shape = RoundedCornerShape(16.dp),
                                        ) {
                                            Text(
                                                text = key,
                                                fontSize = if (key.length == 1) 18.sp else 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (isChecking) {
                            Text(
                                text = "Checking PIN...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                                fontSize = 12.sp,
                            )
                        }
                        if (inlineMessage.isNotBlank()) {
                            Text(inlineMessage, color = BrandRed, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerEditorSheet(
    editingStudent: Student?,
    onDismiss: () -> Unit,
    onSubmit: suspend (StudentDraft) -> OperationResult,
) {
    var name by rememberSaveable(editingStudent?.id) { mutableStateOf(editingStudent?.name.orEmpty()) }
    var age by rememberSaveable(editingStudent?.id) { mutableStateOf(editingStudent?.age?.toString().orEmpty()) }
    var timeSlot by rememberSaveable(editingStudent?.id) { mutableStateOf(editingStudent?.timeSlot.orEmpty()) }
    var joinDate by rememberSaveable(editingStudent?.id) { mutableStateOf(editingStudent?.joinDate.orEmpty()) }
    var feesPaid by rememberSaveable(editingStudent?.id) { mutableStateOf(editingStudent?.feesPaid ?: true) }
    var jerseySize by rememberSaveable(editingStudent?.id) { mutableStateOf(editingStudent?.jerseySize.orEmpty()) }
    var jerseyPairs by rememberSaveable(editingStudent?.id) { mutableStateOf(editingStudent?.jerseyPairs?.toString() ?: "0") }
    var fatherGuardianName by rememberSaveable(editingStudent?.id) { mutableStateOf(editingStudent?.fatherGuardianName.orEmpty()) }
    var parentContactNo by rememberSaveable(editingStudent?.id) { mutableStateOf(editingStudent?.parentContactNo.orEmpty()) }
    var alternateContactNo by rememberSaveable(editingStudent?.id) { mutableStateOf(editingStudent?.alternateContactNo.orEmpty()) }
    var schoolCollege by rememberSaveable(editingStudent?.id) { mutableStateOf(editingStudent?.schoolCollege.orEmpty()) }
    var grade by rememberSaveable(editingStudent?.id) { mutableStateOf(editingStudent?.grade.orEmpty()) }
    var address by rememberSaveable(editingStudent?.id) { mutableStateOf(editingStudent?.address.orEmpty()) }
    var inlineMessage by rememberSaveable { mutableStateOf("") }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val editorCoachingFee = editingStudent?.coachingFee?.takeIf { it > 0.0 } ?: admissionPlanBase("monthly")
    val editorAdmissionFee = editingStudent?.admissionFee?.takeIf { it > 0.0 } ?: AdmissionOneTimeFee
    val editorJerseyAmount = extraJerseyAmount(jerseyPairs)
    val editorSuggestedTotal = editorCoachingFee + editorAdmissionFee + editorJerseyAmount
    val editorStoredAmountPaid = editingStudent?.amountPaid ?: 0.0
    val editorDerivedAmountPaid = when {
        !feesPaid -> 0.0
        editorStoredAmountPaid > 0.0 -> editorStoredAmountPaid
        else -> editorSuggestedTotal
    }

    val openDatePicker = rememberUpdatedState(newValue = {
        val (year, month, day) = currentDatePickerValues(joinDate)
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                joinDate = String.format(
                    Locale.US,
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )
            },
            year,
            month,
            day,
        ).show()
    })

    FormDialog(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Manager Workspace", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                IconButton(onClick = onDismiss, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                }
            }
            Text(
                if (editingStudent == null) "Add academy player" else "Edit academy player",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(rememberBringIntoViewOnFocusModifier()),
                label = { Text("Kid name") },
                singleLine = true,
            )
            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter(Char::isDigit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(rememberBringIntoViewOnFocusModifier()),
                label = { Text("Age") },
                singleLine = true,
            )

            TimeSlotSelector(selected = timeSlot, onSelected = { timeSlot = it })

            val hasRenewals = editingStudent?.renewals?.isNotEmpty() == true
            OutlinedTextField(
                value = joinDate,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .then(rememberBringIntoViewOnFocusModifier()),
                label = { Text("Join date") },
                readOnly = true,
                trailingIcon = {
                    TextButton(
                        onClick = { openDatePicker.value.invoke() },
                        enabled = !hasRenewals
                    ) {
                        Text(if (hasRenewals) "Locked" else "Pick")
                    }
                },
                supportingText = if (hasRenewals) {
                    { Text("Join date is locked because renewals are already recorded.") }
                } else null
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Fees paid?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("If paid, the total due is saved as the joining amount.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
                }
                Switch(
                    checked = feesPaid,
                    onCheckedChange = { feesPaid = it }
                )
            }

            AdmissionSectionCard(title = "Jersey details") {
                AdmissionDropdownField(
                    label = "Jersey size",
                    value = jerseySize,
                    options = JerseySizeOptions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    displayText = ::jerseySizeLabel,
                    onSelect = { jerseySize = it },
                )
                OutlinedTextField(
                    value = jerseyPairs,
                    onValueChange = { jerseyPairs = it.filter(Char::isDigit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = { Text("Jersey pairs") },
                    supportingText = { Text("Each selected jersey pair adds Rs 750.") },
                    singleLine = true,
                )
            }

            AdmissionSectionCard(title = "Joining fee split") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DataTileContent(
                        modifier = Modifier.weight(1f),
                        label = "Coaching",
                        value = admissionAmountLabel(editorCoachingFee),
                        accent = BrandBlue,
                    )
                    DataTileContent(
                        modifier = Modifier.weight(1f),
                        label = "Admission",
                        value = admissionAmountLabel(editorAdmissionFee),
                        accent = BrandBlue,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DataTileContent(
                        modifier = Modifier.weight(1f),
                        label = "Jersey",
                        value = admissionAmountLabel(editorJerseyAmount),
                        accent = BrandBlue,
                    )
                    DataTileContent(
                        modifier = Modifier.weight(1f),
                        label = "Total",
                        value = admissionAmountLabel(editorSuggestedTotal),
                        accent = BrandGold,
                    )
                }
                Text(
                    text = if (feesPaid) {
                        "Joining amount saved: ${admissionAmountLabel(editorDerivedAmountPaid)}. Use Renew/Record Joining Fee for custom payment entries."
                    } else {
                        "Unpaid records save Rs 0 until joining payment is recorded. Every jersey pair is Rs 750."
                    },
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }

            AdmissionSectionCard(title = "Parent and school details") {
                OutlinedTextField(
                    value = fatherGuardianName,
                    onValueChange = { fatherGuardianName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = { Text("Father / guardian name") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = parentContactNo,
                    onValueChange = { parentContactNo = it.filter(Char::isDigit).take(10) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = { Text("Mobile number") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = alternateContactNo,
                    onValueChange = { alternateContactNo = it.filter(Char::isDigit).take(10) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = { Text("Alternate mobile") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = schoolCollege,
                    onValueChange = { schoolCollege = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = { Text("School") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = { Text("Grade") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 86.dp)
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = { Text("Address") },
                    minLines = 2,
                )
            }

            if (inlineMessage.isNotBlank()) {
                Text(inlineMessage, color = BrandRed, fontSize = 13.sp, lineHeight = 18.sp)
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    enabled = !isSaving,
                    onClick = {
                        scope.launch {
                            isSaving = true
                                    val result = onSubmit(
                                        StudentDraft(
                                            name = name,
                                            age = age,
                                            timeSlot = timeSlot,
                                            joinDate = joinDate,
                                            feesPaid = feesPaid,
                                            amountPaid = String.format(Locale.US, "%.2f", editorDerivedAmountPaid),
                                            feePlan = editingStudent?.feePlan?.ifBlank { "monthly" } ?: "monthly",
                                            coachingFee = String.format(Locale.US, "%.2f", editorCoachingFee),
                                            admissionFee = String.format(Locale.US, "%.2f", editorAdmissionFee),
                                            jerseyAmount = String.format(Locale.US, "%.2f", editorJerseyAmount),
                                            totalFeeAmount = String.format(Locale.US, "%.2f", editorSuggestedTotal),
                                            jerseySize = jerseySize,
                                            jerseyPairs = jerseyPairs.ifBlank { "0" },
                                            paymentMethod = editingStudent?.paymentMethod.orEmpty(),
                                            paymentUpiId = editingStudent?.paymentUpiId.orEmpty(),
                                            paymentReference = editingStudent?.paymentReference.orEmpty(),
                                            comments = editingStudent?.comments.orEmpty(),
                                            fatherGuardianName = fatherGuardianName,
                                            parentContactNo = parentContactNo,
                                            alternateContactNo = alternateContactNo,
                                            schoolCollege = schoolCollege,
                                            grade = grade,
                                            address = address,
                                        )
                                    )
                            if (!result.success) {
                                inlineMessage = result.message
                            }
                            isSaving = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(if (editingStudent == null) "Saving" else "Updating")
                    } else {
                        Text(if (editingStudent == null) "Save player" else "Save changes")
                    }
                }

                OutlinedButton(
                    enabled = !isSaving,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdmissionFormSheet(
    onLoadRegNo: suspend () -> Long,
    onDismiss: () -> Unit,
    onSubmit: suspend (AdmissionDraft) -> OperationResult,
) {
    var applicantName by rememberSaveable { mutableStateOf("") }
    var filledBy by rememberSaveable { mutableStateOf("Parent / Guardian") }
    var nationality by rememberSaveable { mutableStateOf("Indian") }
    var birthDay by rememberSaveable { mutableStateOf("") }
    var birthMonth by rememberSaveable { mutableStateOf("") }
    var birthYear by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("") }
    var fatherGuardianName by rememberSaveable { mutableStateOf("") }
    var alternateContactNo by rememberSaveable { mutableStateOf("") }
    var parentContactNo by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var grade by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var schoolCollege by rememberSaveable { mutableStateOf("") }
    var parentAadhaarNo by rememberSaveable { mutableStateOf("") }
    var timeSlot by rememberSaveable { mutableStateOf("") }
    var joinDate by rememberSaveable { mutableStateOf(todayIsoDate()) }
    var feesPaid by rememberSaveable { mutableStateOf(false) }
    var feePlan by rememberSaveable { mutableStateOf("monthly") }
    var customAmount by rememberSaveable { mutableStateOf("") }
    var jerseySize by rememberSaveable { mutableStateOf("") }
    var jerseyPairs by rememberSaveable { mutableStateOf("") }
    var comments by rememberSaveable { mutableStateOf("") }
    var batsmanStyle by rememberSaveable { mutableStateOf("") }
    var bowlingStyles by rememberSaveable { mutableStateOf(emptySet<String>()) }
    var readyToStartNow by rememberSaveable { mutableStateOf(false) }
    var consentAccepted by rememberSaveable { mutableStateOf(false) }
    var termsAccepted by rememberSaveable { mutableStateOf(false) }
    var inlineMessage by rememberSaveable { mutableStateOf("") }
    var isSubmitting by rememberSaveable { mutableStateOf(false) }
    var previewRegNo by rememberSaveable { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val upiId = remember { context.getString(R.string.academy_upi_id) }
    val upiName = remember { context.getString(R.string.academy_upi_name) }
    val coachingFee = remember(feePlan, customAmount) {
        if (feePlan == "custom") customAmount.toDoubleOrNull() ?: 0.0 else admissionPlanBase(feePlan)
    }
    val admissionFee = remember(feePlan) {
        if (feePlan == "special") 0.0 else AdmissionOneTimeFee
    }
    val admissionExtraJerseyAmount = remember(jerseyPairs) { extraJerseyAmount(jerseyPairs) }
    val planAmount = remember(coachingFee, admissionFee, admissionExtraJerseyAmount) {
        coachingFee + admissionFee + admissionExtraJerseyAmount
    }
    val upiAmount = remember(planAmount) { planAmount.takeIf { it > 0.0 } }
    var showUpiWebDialog by rememberSaveable { mutableStateOf(false) }
    val dateOfBirth = remember(birthDay, birthMonth, birthYear) {
        if (birthDay.isBlank() || birthMonth.isBlank() || birthYear.isBlank()) {
            ""
        } else {
            val monthIndex = AdmissionMonths.indexOf(birthMonth) + 1
            String.format(Locale.US, "%s-%02d-%02d", birthYear, monthIndex, birthDay.toIntOrNull() ?: 1)
        }
    }
    val calculatedAge = remember(dateOfBirth) { calculateAgeFromDate(dateOfBirth) }

    LaunchedEffect(Unit) {
        runCatching { onLoadRegNo() }
            .onSuccess { previewRegNo = it }
            .onFailure { inlineMessage = it.message ?: "Unable to fetch registration number." }
    }

    val openJoinDatePicker = rememberUpdatedState(newValue = {
        val (year, month, day) = currentDatePickerValues(joinDate)
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                joinDate = String.format(
                    Locale.US,
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )
            },
            year,
            month,
            day,
        ).show()
    })

    FormDialog(
        onDismiss = onDismiss,
        expanded = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Parent Admission", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                    Text(
                        "New player admission form",
                        fontSize = 28.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                }
            }

            Text(
                "Fill this once for a first-time admission. A unique registration number will be generated automatically and the player card will appear on the dashboard.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
            DataTile(
                label = "Reg No",
                value = previewRegNo?.toString() ?: "Loading...",
                accent = BrandBlue,
            )
            AdmissionSectionCard(title = "Player details") {
                AdmissionTextField(
                    value = applicantName,
                    onValueChange = { applicantName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = "Applicant name and initial",
                    singleLine = true,
                )
                AdmissionDropdownField(
                    label = "Form filled by",
                    value = filledBy,
                    options = AdmissionFilledByOptions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    onSelect = { filledBy = it },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdmissionDropdownField(
                        label = "DD",
                        value = birthDay,
                        options = (1..31).map { it.toString() },
                        modifier = Modifier
                            .weight(1f)
                            .then(rememberBringIntoViewOnFocusModifier()),
                        onSelect = { birthDay = it },
                    )
                    AdmissionDropdownField(
                        label = "MON",
                        value = birthMonth,
                        options = AdmissionMonths,
                        modifier = Modifier
                            .weight(1f)
                            .then(rememberBringIntoViewOnFocusModifier()),
                        onSelect = { birthMonth = it },
                    )
                    AdmissionDropdownField(
                        label = "YYYY",
                        value = birthYear,
                        options = AdmissionYears.map { year -> year.toString() },
                        modifier = Modifier
                            .weight(1f)
                            .then(rememberBringIntoViewOnFocusModifier()),
                        onSelect = { birthYear = it },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DataTileContent(
                        modifier = Modifier.weight(1f),
                        label = "Age",
                        value = calculatedAge?.toString() ?: "Auto",
                        accent = MaterialTheme.colorScheme.onSurface,
                    )
                    DataTileContent(
                        modifier = Modifier.weight(1f),
                        label = "DOB",
                        value = if (dateOfBirth.isBlank()) "Select" else displayDate(dateOfBirth),
                        accent = MaterialTheme.colorScheme.onSurface,
                    )
                }
                AdmissionTextField(
                    value = nationality,
                    onValueChange = { nationality = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = "Nationality",
                    singleLine = true,
                )
                GenderSelector(selected = gender, onSelected = { gender = it })
                AdmissionTextField(
                    value = schoolCollege,
                    onValueChange = { schoolCollege = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = "School",
                    singleLine = true,
                )
            }

            AdmissionSectionCard(title = "Parent / guardian details") {
                AdmissionTextField(
                    value = fatherGuardianName,
                    onValueChange = { fatherGuardianName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = "Father's / Guardian's name",
                    singleLine = true,
                )
                AdmissionTextField(
                    value = parentContactNo,
                    onValueChange = { parentContactNo = it.filter(Char::isDigit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = "Parent contact number",
                    singleLine = true,
                )
                AdmissionTextField(
                    value = alternateContactNo,
                    onValueChange = { alternateContactNo = it.filter(Char::isDigit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = "Alternate contact number",
                    singleLine = true,
                )
                AdmissionTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = "Grade",
                    singleLine = true,
                )
                AdmissionTextField(
                    value = city,
                    onValueChange = { city = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = "City (optional)",
                    singleLine = true,
                )
                AdmissionTextField(
                    value = address,
                    onValueChange = { address = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = "Home address (student)",
                    minLines = 3,
                )
                AdmissionTextField(
                    value = parentAadhaarNo,
                    onValueChange = { parentAadhaarNo = it.filter(Char::isDigit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = "Parent Aadhaar (optional)",
                    singleLine = true,
                )
            }

            AdmissionSectionCard(title = "Training and payment") {
                AdmissionTimeSlotSelector(selected = timeSlot, onSelected = { timeSlot = it })
                AdmissionTextField(
                    value = joinDate,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = "Joining date",
                    readOnly = true,
                    trailing = {
                        TextButton(onClick = { openJoinDatePicker.value.invoke() }) {
                            Text("Pick")
                        }
                    },
                )
                AdmissionDropdownField(
                    label = "Fee plan",
                    value = AdmissionFeePlanOptions.firstOrNull { it.value == feePlan }?.label ?: "Monthly",
                    options = AdmissionFeePlanOptions.map { it.label },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    onSelect = { selectedLabel ->
                        feePlan = AdmissionFeePlanOptions.firstOrNull { it.label == selectedLabel }?.value ?: "monthly"
                    },
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DataTileContent(
                            modifier = Modifier.weight(1f),
                            label = "Coaching fee",
                            value = admissionAmountLabel(coachingFee),
                            accent = BrandBlue,
                        )
                        DataTileContent(
                            modifier = Modifier.weight(1f),
                            label = "Admission fee",
                            value = admissionAmountLabel(admissionFee),
                            accent = BrandBlue,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DataTileContent(
                            modifier = Modifier.weight(1f),
                            label = "Jersey amount",
                            value = admissionAmountLabel(admissionExtraJerseyAmount),
                            accent = BrandBlue,
                        )
                        DataTileContent(
                            modifier = Modifier.weight(1f),
                            label = "Total",
                            value = admissionAmountLabel(planAmount),
                            accent = BrandGold,
                        )
                    }
                    Text(
                        text = "Each jersey pair adds Rs 750. If payment is marked made, this total is submitted for manager verification.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                }
                if (feePlan == "custom") {
                    AdmissionTextField(
                        value = customAmount,
                        onValueChange = { customAmount = it.filter { char -> char.isDigit() || char == '.' } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(rememberBringIntoViewOnFocusModifier()),
                        label = "Custom amount",
                        singleLine = true,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdmissionDropdownField(
                        label = "Jersey size",
                        value = jerseySize,
                        options = JerseySizeOptions,
                        modifier = Modifier
                            .weight(1f)
                            .then(rememberBringIntoViewOnFocusModifier()),
                        displayText = ::jerseySizeLabel,
                        onSelect = { jerseySize = it },
                    )
                    AdmissionTextField(
                        value = jerseyPairs,
                        onValueChange = { jerseyPairs = it.filter(Char::isDigit) },
                        modifier = Modifier
                            .weight(1f)
                            .then(rememberBringIntoViewOnFocusModifier()),
                        label = "Jersey pairs",
                        singleLine = true,
                    )
                }
            }

            AdmissionSectionCard(title = "Skills and playing style") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = readyToStartNow,
                        onCheckedChange = { readyToStartNow = it },
                    )
                    Text(
                        "Kick start the journey now",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text("Batsman", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BatsmanOptions.forEach { option ->
                        FilterChip(
                            selected = batsmanStyle == option,
                            onClick = { batsmanStyle = option },
                            label = { Text(option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandBlue,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
                Text("Bowler", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BowlingOptions.forEach { option ->
                        val selected = option in bowlingStyles
                        FilterChip(
                            selected = selected,
                            onClick = {
                                bowlingStyles = if (selected) bowlingStyles - option else bowlingStyles + option
                            },
                            label = { Text(option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandBlue,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
            }

            AdmissionSectionCard(title = "Any comments or special requests? (Optional)") {
                AdmissionTextField(
                    value = comments,
                    onValueChange = { comments = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = "Comments (optional)",
                    minLines = 3,
                )
            }

            AdmissionSectionCard(title = "Parent consent") {
                Text(
                    "I, the parent or guardian of the applicant, request admission for my child to Gen Alpha Cricket Academy. I confirm that the information provided is correct. I understand that my child must follow academy rules, discipline, and coaching instructions. I understand that cricket training, fitness drills, matches, and travel may involve the risk of minor or major injuries, and I give consent for my child to participate. I also understand that the academy is not responsible for injuries caused by indiscipline or for the loss of personal items during training or travel.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Text(
                    "Terms and conditions",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                TermsBullet("Attend with discipline and punctuality.")
                TermsBullet("Co-operate during training and follow academy instructions.")
                TermsBullet("Monthly fees must be cleared within the first 10 days.")
                TermsBullet("Fees once paid are non-refundable.")
                TermsBullet("Proper cricket kit is required for training.")
                TermsBullet("Damage to academy property is the player's responsibility.")
                ConsentCheckboxRow(
                    checked = consentAccepted,
                    title = "I agree to the parent consent statement",
                    body = "This confirms the child may be enrolled and will follow academy rules.",
                    onCheckedChange = { consentAccepted = it },
                )
                ConsentCheckboxRow(
                    checked = termsAccepted,
                    title = "I accept the terms and conditions",
                    body = "This confirms the admission terms, fee rules, and discipline expectations.",
                    onCheckedChange = { termsAccepted = it },
                )
            }

            AdmissionSectionCard(title = "Payment details") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Payment made?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "UPI payments stay pending until manager verifies.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        )
                    }
                    Switch(
                        checked = feesPaid,
                        onCheckedChange = { feesPaid = it }
                    )
                }
                if (upiId.isNotBlank()) {
                    Button(
                        onClick = { showUpiWebDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    ) {
                        Text("Open UPI Payment Options")
                    }
                }
            }

            if (showUpiWebDialog) {
                Dialog(
                    onDismissRequest = { showUpiWebDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { showUpiWebDialog = false }) {
                                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                                }
                            }
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        webViewClient = object : WebViewClient() {
                                            private fun handleUrl(urlStr: String?): Boolean {
                                                urlStr ?: return false
                                                if (urlStr.startsWith("upi://")) {
                                                    runCatching {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlStr))
                                                        ctx.startActivity(intent)
                                                    }
                                                    return true
                                                }
                                                return false
                                            }
                                            
                                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                                return handleUrl(request?.url?.toString())
                                            }
                                            
                                            @Deprecated("Deprecated in Java")
                                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                                return handleUrl(url)
                                            }
                                        }
                                    }
                                },
                                update = { webView ->
                                    val safeName = Uri.encode(applicantName.ifBlank { "New player" })
                                    val safeUpiId = Uri.encode(upiId)
                                    val safePayeeName = Uri.encode(upiName)
                                    val safePlan = Uri.encode(feePlan)
                                    val url = "file:///android_asset/pay.html?a=${upiAmount ?: 0}&name=$safeName&p=$safePlan&upiId=$safeUpiId&payeeName=$safePayeeName"
                                    if (webView.url != url) {
                                        webView.loadUrl(url)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (inlineMessage.isNotBlank()) {
                Text(
                    text = inlineMessage,
                    color = BrandRed,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    enabled = !isSubmitting && consentAccepted && termsAccepted,
                    onClick = {
                        scope.launch {
                            isSubmitting = true
                            inlineMessage = ""
                            val draft = AdmissionDraft(
                                applicantName = applicantName,
                                filledBy = filledBy,
                                nationality = nationality,
                                dateOfBirth = dateOfBirth,
                                gender = gender,
                                fatherGuardianName = fatherGuardianName,
                                alternateContactNo = alternateContactNo,
                                parentContactNo = parentContactNo,
                                city = city,
                                grade = grade,
                                address = address,
                                schoolCollege = schoolCollege,
                                parentAadhaarNo = parentAadhaarNo,
                                timeSlot = timeSlot,
                                joinDate = joinDate,
                                feesPaid = false,
                                amountPaid = if (feesPaid) String.format(Locale.US, "%.2f", planAmount) else "0",
                                feePlan = feePlan,
                                coachingFee = String.format(Locale.US, "%.2f", coachingFee),
                                admissionFee = String.format(Locale.US, "%.2f", admissionFee),
                                jerseyAmount = String.format(Locale.US, "%.2f", admissionExtraJerseyAmount),
                                totalFeeAmount = String.format(Locale.US, "%.2f", planAmount),
                                jerseySize = jerseySize,
                                jerseyPairs = jerseyPairs.ifBlank { "0" },
                                paymentMethod = "UPI",
                                paymentUpiId = upiId,
                                paymentReference = "",
                                paymentPendingVerification = feesPaid,
                                comments = comments,
                                batsmanStyle = batsmanStyle,
                                bowlingStyles = bowlingStyles.toList(),
                                readyToStartNow = readyToStartNow,
                                consentAccepted = consentAccepted,
                                termsAccepted = termsAccepted,
                            )
                            val result = onSubmit(draft)
                            if (!result.success) {
                                inlineMessage = result.message
                            }
                            isSubmitting = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Submitting admission")
                    } else {
                        Text("Submit admission form")
                    }
                }
                if (!consentAccepted || !termsAccepted) {
                    Text(
                        text = "Please accept both consent checkboxes to submit the form.",
                        color = BrandRed,
                        fontSize = 12.sp,
                    )
                }

                OutlinedButton(
                    enabled = !isSubmitting,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun AdmissionSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    OutlinedCard(
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
            )
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenderSelector(
    selected: String,
    onSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Gender", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Male", "Female").forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelected(option) },
                    label = { Text(option) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBlue,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdmissionTimeSlotSelector(
    selected: String,
    onSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Choose time slot", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AdmissionSlotOptions.forEach { option ->
                FilterChip(
                    selected = selected == option.value,
                    onClick = { onSelected(option.value) },
                    label = {
                        Column {
                            Text(option.value)
                            Text(option.label, fontSize = 11.sp)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBlue,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ConsentCheckboxRow(
    checked: Boolean,
    title: String,
    body: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Text(
                text = body,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun TermsBullet(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text("\u2022", color = BrandBlue, fontWeight = FontWeight.Bold)
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun admissionTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandBlue,
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f),
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    cursorColor = BrandBlue,
)

@Composable
private fun AdmissionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    trailing: (@Composable (() -> Unit))? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        enabled = enabled,
        readOnly = readOnly,
        shape = RoundedCornerShape(18.dp),
        colors = admissionTextFieldColors(),
        trailingIcon = trailing,
    )
}

@Composable
private fun AdmissionDropdownField(
    label: String,
    value: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    displayText: (String) -> String = { it },
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (value.isBlank()) "Select" else displayText(value),
                    color = if (value.isBlank()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(displayText(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimeSlotSelector(
    selected: String,
    onSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Time slot", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("6AM", "7:30AM", "4PM", "5:30PM", "7PM").forEach { slot ->
                FilterChip(
                    selected = selected == slot,
                    onClick = { onSelected(slot) },
                    label = { Text(slot) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBlue,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }
    }
}


private fun Context.sharePlainText(title: String, text: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(sendIntent, title))
}

private data class ReceiptPdfRow(
    val label: String,
    val value: String,
)

private data class ReceiptPdfData(
    val receiptType: String,
    val receiptNo: String,
    val regNo: String,
    val playerName: String,
    val amountText: String,
    val parentContact: String,
    val status: String = "PAID",
    val rows: List<ReceiptPdfRow>,
    val footer: String,
)

private fun ReceiptPdfData.toShareText(): String = buildString {
    appendLine("*GEN ALPHA CRICKET ACADEMY*")
    appendLine("_${receiptType}_")
    appendLine()
    appendLine("Receipt No: $receiptNo")
    appendLine("Reg No: $regNo")
    appendLine("Player: $playerName")
    appendLine("Amount Paid: $amountText")
    rows.take(8).forEach { row ->
        if (row.value.isNotBlank()) appendLine("${row.label}: ${row.value}")
    }
    appendLine("Status: $status")
    appendLine()
    appendLine(footer)
}

private fun Context.shareReceiptPdf(receipt: ReceiptPdfData) {
    val pdfUri = createReceiptPdf(receipt)
    if (pdfUri == null) {
        sharePlainText("Share receipt", receipt.toShareText())
        return
    }

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, pdfUri)
        putExtra(Intent.EXTRA_SUBJECT, "${receipt.receiptType} - ${receipt.playerName}")
        putExtra(Intent.EXTRA_TEXT, receipt.toShareText())
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(sendIntent, "Share receipt PDF"))
}


private fun formatRupees(value: Double): String = "Rs ${String.format(Locale.US, "%,.0f", value)}"

private fun buildReceiptNo(prefix: String, regNo: String): String =
    "$prefix-${regNo.ifBlank { "NEW" }}-${LocalDate.now().toString().replace("-", "")}"

private fun buildAndroidAdmissionReceipt(draft: AdmissionDraft, resultMessage: String): ReceiptPdfData {
    val regNo = Regex("""Reg No\s+([A-Za-z0-9-]+)""")
        .find(resultMessage)
        ?.groupValues
        ?.getOrNull(1)
        ?: "Saved"
    val amount = draft.amountPaid.toDoubleOrNull() ?: 0.0
    val jersey = "${draft.jerseySize.ifBlank { "Not set" }}${
        if ((draft.jerseyPairs.toIntOrNull() ?: 0) > 0) {
            " (${draft.jerseyPairs} pair${if (draft.jerseyPairs == "1") "" else "s"})"
        } else {
            ""
        }
    }"
    return ReceiptPdfData(
        receiptType = "Joining Fee Receipt",
        receiptNo = buildReceiptNo("GACA", regNo),
        regNo = regNo,
        playerName = draft.applicantName,
        amountText = formatRupees(amount),
        parentContact = draft.parentContactNo,
        rows = listOf(
            ReceiptPdfRow("Parent / Guardian", draft.fatherGuardianName),
            ReceiptPdfRow("Contact", draft.parentContactNo),
            ReceiptPdfRow("Join Date", displayDate(draft.joinDate)),
            ReceiptPdfRow("Time Slot", draft.timeSlot.ifBlank { "Not set" }),
            ReceiptPdfRow("Jersey", jersey),
            ReceiptPdfRow("Paid On", displayDate(LocalDate.now().toString())),
        ),
        footer = "Thank you for choosing Gen Alpha Cricket Academy.",
    )
}

private fun buildAndroidPlayerPaidReceipt(student: Student, draft: StudentDraft): ReceiptPdfData {
    val amount = draft.amountPaid.toDoubleOrNull() ?: student.amountPaid
    val regNo = student.regNo?.toString() ?: "Saved"
    val jerseyPairs = draft.jerseyPairs.toIntOrNull() ?: student.jerseyPairs
    val jersey = "${draft.jerseySize.ifBlank { student.jerseySize.ifBlank { "Not set" } }}${
        if (jerseyPairs > 0) " ($jerseyPairs pair${if (jerseyPairs == 1) "" else "s"})" else ""
    }"
    return ReceiptPdfData(
        receiptType = "Joining Fee Receipt",
        receiptNo = buildReceiptNo("GACA", regNo),
        regNo = regNo,
        playerName = draft.name.ifBlank { student.name },
        amountText = formatRupees(amount),
        parentContact = student.parentContactNo,
        rows = listOf(
            ReceiptPdfRow("Parent / Guardian", student.fatherGuardianName.ifBlank { "Parent" }),
            ReceiptPdfRow("Contact", student.parentContactNo),
            ReceiptPdfRow("Join Date", displayDate(draft.joinDate.ifBlank { student.joinDate })),
            ReceiptPdfRow("Time Slot", draft.timeSlot.ifBlank { student.timeSlot.ifBlank { "Not set" } }),
            ReceiptPdfRow("Jersey", jersey),
            ReceiptPdfRow("Paid On", displayDate(LocalDate.now().toString())),
        ),
        footer = "Thank you for choosing Gen Alpha Cricket Academy.",
    )
}

private fun renewalPlanLabel(plan: String): String = when (plan) {
    "quarterly" -> "3 Months (Quarterly)"
    "halfyearly" -> "6 Months (Half-yearly)"
    "special" -> "Special Training"
    "custom" -> "Custom Renewal"
    else -> "Monthly"
}

private fun buildAndroidRenewalReceipt(
    student: Student,
    plan: String,
    months: Int,
    amount: Double,
    paidOn: String = LocalDate.now().toString(),
    isJoiningFee: Boolean = false,
): ReceiptPdfData {
    val regNo = student.regNo?.toString() ?: "Saved"
    return ReceiptPdfData(
        receiptType = if (isJoiningFee) "Joining Fee Receipt" else "Renewal Fee Receipt",
        receiptNo = buildReceiptNo(if (isJoiningFee) "GACA" else "GACA-REN", regNo),
        regNo = regNo,
        playerName = student.name,
        amountText = formatRupees(amount),
        parentContact = student.parentContactNo,
        rows = listOf(
            ReceiptPdfRow("Plan", renewalPlanLabel(plan)),
            ReceiptPdfRow("Covered", "$months month${if (months == 1) "" else "s"}"),
            ReceiptPdfRow("Paid On", displayDate(paidOn)),
            ReceiptPdfRow("Time Slot", student.timeSlot.ifBlank { "Not set" }),
        ),
        footer = if (isJoiningFee) {
            "Welcome to Gen Alpha Cricket Academy."
        } else {
            "Thank you for continuing with Gen Alpha Cricket Academy."
        },
    )
}

private fun Context.createReceiptPdf(receipt: ReceiptPdfData): Uri? {
    val document = PdfDocument()
    return try {
        val pageWidth = 794
        val pageHeight = 1123
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
        val canvas = page.canvas
        val deepBlue = 0xFF0D2D66.toInt()
        val accentBlue = 0xFF1F5FBF.toInt()
        val gold = 0xFFF4BE2E.toInt()
        val ink = 0xFF10264F.toInt()
        val muted = 0xFF66748C.toInt()
        val paleGold = 0xFFFFF7DC.toInt()
        val border = 0xFFD8E1EE.toInt()
        val rowBorder = 0xFFE2E9F5.toInt()
        val success = 0xFF178553.toInt()

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            color = border
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ink
            textSize = 31f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = muted
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ink
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val playerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ink
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val rowValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ink
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = muted
            textSize = 13f
        }

        canvas.drawColor(0xFFF3F6FB.toInt())
        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawRoundRect(RectF(92f, 70f, 702f, 1050f), 8f, 8f, fillPaint)
        canvas.drawRoundRect(RectF(92f, 70f, 702f, 1050f), 8f, 8f, strokePaint)
        fillPaint.color = deepBlue
        canvas.drawRect(92f, 70f, 702f, 82f, fillPaint)
        fillPaint.color = gold
        canvas.drawRect(92f, 82f, 702f, 86f, fillPaint)

        BitmapFactory.decodeResource(resources, R.drawable.gen_alpha_badge_transparent)?.let { logo ->
            canvas.drawBitmap(logo, null, RectF(120f, 112f, 196f, 224f), null)
        }
        labelPaint.color = accentBlue
        canvas.drawText("GEN ALPHA CRICKET ACADEMY", 218f, 142f, labelPaint)
        titlePaint.color = ink
        canvas.drawText("Fee Receipt", 218f, 176f, titlePaint)
        smallPaint.color = muted
        canvas.drawText("Official payment confirmation", 218f, 202f, smallPaint)

        val stampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        fillPaint.color = success
        canvas.drawRoundRect(RectF(584f, 126f, 662f, 160f), 17f, 17f, fillPaint)
        canvas.drawText(receipt.status, 623f, 148f, stampPaint)

        strokePaint.color = border
        strokePaint.strokeWidth = 2f
        canvas.drawLine(122f, 258f, 672f, 258f, strokePaint)
        strokePaint.strokeWidth = 1.5f

        labelPaint.color = muted
        canvas.drawText("RECEIPT NO", 122f, 310f, labelPaint)
        valuePaint.color = ink
        valuePaint.textAlign = Paint.Align.LEFT
        drawSingleLine(canvas, receipt.receiptNo, 122f, 339f, valuePaint, 250f)
        canvas.drawText("DATE", 474f, 310f, labelPaint)
        drawSingleLine(canvas, displayDate(LocalDate.now().toString()), 474f, 339f, valuePaint, 150f)

        fillPaint.color = 0xFFF8FBFF.toInt()
        strokePaint.color = rowBorder
        canvas.drawRoundRect(RectF(122f, 386f, 672f, 470f), 6f, 6f, fillPaint)
        canvas.drawRoundRect(RectF(122f, 386f, 672f, 470f), 6f, 6f, strokePaint)
        canvas.drawText("RECEIVED FROM", 146f, 419f, labelPaint)
        drawSingleLine(canvas, receipt.playerName, 146f, 450f, playerPaint, 280f)
        canvas.drawText("REG NO", 528f, 419f, labelPaint)
        val regPaint = Paint(valuePaint).apply {
            textSize = 20f
            color = ink
            textAlign = Paint.Align.LEFT
        }
        drawSingleLine(canvas, receipt.regNo, 528f, 450f, regPaint, 100f)

        labelPaint.color = deepBlue
        canvas.drawText("PAYMENT DETAILS", 122f, 534f, labelPaint)
        labelPaint.color = muted
        val priorityLabels = if (receipt.receiptType.startsWith("Joining")) {
            listOf("Time Slot", "Jersey")
        } else {
            listOf("Plan", "Covered")
        }
        val selectedRows = priorityLabels.mapNotNull { label ->
            receipt.rows.firstOrNull { it.label == label }
        }
        val paymentRows = listOf(ReceiptPdfRow("Payment Type", receipt.receiptType.removeSuffix(" Receipt"))) + selectedRows
        var y = 560f
        paymentRows.take(3).forEachIndexed { index, row ->
            val fill = if (index % 2 == 0) 0xFFFFFFFF.toInt() else 0xFFF8FBFF.toInt()
            drawReceiptLine(canvas, row, 122f, y, 550f, labelPaint, rowValuePaint, fillPaint, strokePaint, fill)
            y += 54f
        }

        fillPaint.color = paleGold
        strokePaint.color = 0xFFF0DF9B.toInt()
        canvas.drawRoundRect(RectF(122f, 756f, 672f, 834f), 6f, 6f, fillPaint)
        canvas.drawRoundRect(RectF(122f, 756f, 672f, 834f), 6f, 6f, strokePaint)
        labelPaint.color = muted
        canvas.drawText("AMOUNT PAID", 146f, 791f, labelPaint)
        val amountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = deepBlue
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(receipt.amountText, 650f, 808f, amountPaint)

        strokePaint.color = border
        strokePaint.strokeWidth = 2f
        canvas.drawLine(122f, 905f, 672f, 905f, strokePaint)
        strokePaint.strokeWidth = 1.5f
        smallPaint.color = muted
        canvas.drawText("Fees once paid are recorded against the player profile.", 122f, 947f, smallPaint)
        canvas.drawText("Please keep this receipt for academy reference.", 122f, 971f, smallPaint)
        val thanksPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ink
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("Thank you", 672f, 971f, thanksPaint)

        document.finishPage(page)
        val safeReceiptNo = receipt.receiptNo.replace(Regex("""[^A-Za-z0-9_-]"""), "-")
        val receiptDir = File(cacheDir, "receipts").apply { mkdirs() }
        val file = File(receiptDir, "$safeReceiptNo.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    } catch (_: Exception) {
        null
    } finally {
        document.close()
    }
}


private fun drawReceiptLine(
    canvas: android.graphics.Canvas,
    row: ReceiptPdfRow,
    x: Float,
    y: Float,
    width: Float,
    labelPaint: Paint,
    valuePaint: Paint,
    fillPaint: Paint,
    strokePaint: Paint,
    fillColor: Int,
) {
    fillPaint.color = fillColor
    canvas.drawRect(RectF(x, y, x + width, y + 54f), fillPaint)
    canvas.drawRect(RectF(x, y, x + width, y + 54f), strokePaint)
    canvas.drawText(row.label, x + 24f, y + 33f, labelPaint)
    val originalAlign = valuePaint.textAlign
    valuePaint.textAlign = Paint.Align.RIGHT
    drawSingleLine(canvas, row.value.ifBlank { "-" }, x + width - 22f, y + 33f, valuePaint, width - 220f)
    valuePaint.textAlign = originalAlign
}

private fun drawSingleLine(
    canvas: android.graphics.Canvas,
    text: String,
    x: Float,
    y: Float,
    paint: Paint,
    maxWidth: Float,
) {
    val ellipsis = "..."
    var output = text
    while (paint.measureText(output) > maxWidth && output.length > ellipsis.length + 1) {
        output = output.dropLast(1)
    }
    if (output != text) output = output.dropLast(ellipsis.length).trimEnd() + ellipsis
    canvas.drawText(output, x, y, paint)
}

private fun drawWrappedText(
    canvas: android.graphics.Canvas,
    text: String,
    x: Float,
    y: Float,
    paint: Paint,
    maxWidth: Float,
    lineHeight: Float,
    maxLines: Int,
): Float {
    val words = text.split(Regex("""\s+""")).filter { it.isNotBlank() }
    var line = ""
    var currentY = y
    var lineCount = 0
    words.forEach { word ->
        val candidate = if (line.isBlank()) word else "$line $word"
        if (paint.measureText(candidate) <= maxWidth) {
            line = candidate
        } else {
            if (line.isNotBlank() && lineCount < maxLines) {
                canvas.drawText(line, x, currentY, paint)
                currentY += lineHeight
                lineCount += 1
            }
            line = word
        }
    }
    if (line.isNotBlank() && lineCount < maxLines) {
        canvas.drawText(line, x, currentY, paint)
        currentY += lineHeight
    }
    return currentY
}

private fun buildAndroidMonthlyFinanceBackup(
    monthLabel: String,
    activeStudents: Int,
    discontinuedStudents: Int,
    fees: Double,
    expenses: Double,
    net: Double,
    expenseRows: List<AcademyExpense>,
): String {
    val expenseLines = expenseRows.take(20).map {
        "- ${displayDate(it.expenseDate)} | ${it.expenseType} | ${formatRupees(it.amount)} | ${it.paidBy}"
    }
    return buildString {
        appendLine("Gen Alpha Cricket Academy - Monthly Backup")
        appendLine("Month: $monthLabel")
        appendLine("Active students: $activeStudents")
        appendLine("Discontinued students: $discontinuedStudents")
        appendLine("Fees collected: ${formatRupees(fees)}")
        appendLine("Expenses: ${formatRupees(expenses)}")
        appendLine("Net: ${formatRupees(net)}")
        appendLine()
        appendLine("Expenses")
        if (expenseLines.isEmpty()) {
            appendLine("- No expenses recorded this month.")
        } else {
            expenseLines.forEach { appendLine(it) }
            if (expenseRows.size > expenseLines.size) {
                appendLine("- ${expenseRows.size - expenseLines.size} more expense rows not shown.")
            }
        }
    }
}
