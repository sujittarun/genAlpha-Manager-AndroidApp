package com.genalpha.cricketacademy.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genalpha.cricketacademy.R
import com.genalpha.cricketacademy.data.AdmissionDraft
import com.genalpha.cricketacademy.data.ManagerSession
import com.genalpha.cricketacademy.data.OperationResult
import com.genalpha.cricketacademy.data.Student
import com.genalpha.cricketacademy.data.StudentDraft
import com.genalpha.cricketacademy.data.calculateAgeFromDate
import com.genalpha.cricketacademy.data.cardTimelineLabel
import com.genalpha.cricketacademy.data.currentDatePickerValues
import com.genalpha.cricketacademy.data.displayDate
import com.genalpha.cricketacademy.data.isActive
import com.genalpha.cricketacademy.data.isFeesPending
import com.genalpha.cricketacademy.data.isRenewalPending
import com.genalpha.cricketacademy.data.latestRenewal
import com.genalpha.cricketacademy.data.renewalStatus
import com.genalpha.cricketacademy.data.studentType
import com.genalpha.cricketacademy.data.todayIsoDate
import com.genalpha.cricketacademy.data.toDraft
import com.genalpha.cricketacademy.data.trackingCaption
import kotlinx.coroutines.launch
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
private val DarkAttentionBorder = Color(0x66D7A12B)
private val AlertBlue = Color(0xFF2266C9)
private const val MANAGER_VIEW_PIN = "290326"

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
private val JerseySizeOptions = listOf("22", "24", "26", "28", "30", "32", "34", "36", "38", "40", "42")

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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ViewSwitcherSection(
    selectedView: AppView,
    onSelected: (AppView) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppView.entries.forEach { view ->
                FilterChip(
                    selected = selectedView == view,
                    onClick = { onSelected(view) },
                    label = { Text(view.label) },
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
private fun PublicViewHeader(
    title: String,
    subtitle: String,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 28.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }
            BrandPosterCard(
                modifier = Modifier.widthIn(min = 84.dp, max = 92.dp),
                imageModifier = Modifier.height(116.dp),
            )
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
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Player Check-In",
                        color = BrandBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Today's attendance",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 28.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    FilledTonalIconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(42.dp),
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh attendance")
                        }
                    }
                    BrandPosterCard(
                        modifier = Modifier.widthIn(min = 78.dp, max = 86.dp),
                        imageModifier = Modifier.height(92.dp),
                    )
                }
            }
            Text(
                text = "Tap your name once to mark present. The button turns green after today's check-in.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CompactStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Present",
                    value = presentCount,
                    accent = BrandGreen,
                )
                CompactStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Remaining",
                    value = (totalCount - presentCount).coerceAtLeast(0),
                    accent = BrandGold,
                )
            }
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
    val snackbarHostState = remember { SnackbarHostState() }
    val filteredKids = remember(uiState.kids, uiState.selectedSlotFilter, uiState.searchQuery) { viewModel.filteredKids() }
    val stats = remember(uiState.kids) { viewModel.stats() }
    val slotSummary = remember(uiState.kids, uiState.selectedSlotFilter) { viewModel.slotSummary() }
    val alertKids = remember(uiState.kids) { viewModel.alertKids() }
    val rosterSections = remember(filteredKids) { buildRosterSections(filteredKids) }
    val activePlayers = remember(uiState.kids) { uiState.kids.filter { it.isActive() } }

    var selectedView by rememberSaveable { mutableStateOf(AppView.Admission) }
    var showManagerPinSheet by rememberSaveable { mutableStateOf(false) }
    var showLoginSheet by rememberSaveable { mutableStateOf(false) }
    var showAdmissionSheet by rememberSaveable { mutableStateOf(false) }
    var showScanSourceSheet by rememberSaveable { mutableStateOf(false) }
    var attachedAdmissionDocumentLabel by rememberSaveable { mutableStateOf<String?>(null) }
    var isScanLoading by rememberSaveable { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var showEditorSheet by rememberSaveable { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var showDetailSheet by rememberSaveable { mutableStateOf(false) }
    var showAttendanceHistory by rememberSaveable { mutableStateOf(false) }
    var attendanceHistoryCache by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    var attendanceHistoryLoadingId by rememberSaveable { mutableStateOf<String?>(null) }
    var playerSearchQuery by rememberSaveable { mutableStateOf("") }
    var playerSlotFilter by rememberSaveable { mutableStateOf("") }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isScanLoading = true
            attachedAdmissionDocumentLabel =
                uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { null }
                    ?: "Imported document attached"
            showAdmissionSheet = true
            isScanLoading = false
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap == null) return@rememberLauncherForActivityResult
        scope.launch {
            isScanLoading = true
            attachedAdmissionDocumentLabel = "Camera scan attached"
            showAdmissionSheet = true
            isScanLoading = false
        }
    }

    val playerFiltered = remember(activePlayers, playerSearchQuery, playerSlotFilter) {
        activePlayers.filter { student ->
            val slotMatch = playerSlotFilter.isBlank() || student.timeSlot == playerSlotFilter
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
    }

    val colorScheme = if (uiState.darkModeEnabled) AcademyDarkScheme else AcademyLightScheme

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                AppBottomBar(
                    selectedView = selectedView,
                    onSelected = { view ->
                        if (view == AppView.Manager && selectedView != AppView.Manager) {
                            showManagerPinSheet = true
                        } else {
                            selectedView = view
                        }
                    },
                )
            },
            floatingActionButton = {
                if (selectedView == AppView.Manager && viewModel.canEdit()) {
                    FloatingActionButton(
                        onClick = {
                            editingStudent = null
                            showEditorSheet = true
                        },
                        containerColor = BrandGold,
                        contentColor = BrandBlueDeep,
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add player")
                    }
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = innerPadding.calculateTopPadding() + 12.dp,
                        bottom = innerPadding.calculateBottomPadding() + 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
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
                        AlertSection(
                            alertKids = alertKids,
                        )
                    }

                    item {
                        StatsSection(
                            joined = stats.joinedCount,
                            active = stats.activeCount,
                            paid = stats.paidCount,
                            returning = stats.returningCount,
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
                            visibleCount = filteredKids.size,
                            totalCount = uiState.kids.size,
                            isRefreshing = uiState.isRefreshing,
                            onSearchChange = viewModel::setSearchQuery,
                            onRefresh = {
                                scope.launch {
                                    viewModel.loadKids()
                                }
                            },
                        )
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
                                    isManager = viewModel.canEdit(),
                                    onOpen = {
                                        selectedStudent = student
                                        showDetailSheet = true
                                    },
                                    onEdit = {
                                        editingStudent = student
                                        selectedStudent = student
                                        showEditorSheet = true
                                    },
                                )
                            }
                        }
                    }
                    } else if (selectedView == AppView.Admission) {
                        item {
                            PublicViewHeader(
                                title = "Parent Admission",
                                subtitle = "Share this view with parents for first-time admission. Staff-only roster data stays hidden until a manager opens the staff dashboard.",
                            )
                        }
                        item {
                            AdmissionActionsSection(
                                isScanLoading = isScanLoading,
                                onOpen = {
                                    attachedAdmissionDocumentLabel = null
                                    showAdmissionSheet = true
                                },
                                onScan = { showScanSourceSheet = true },
                            )
                        }
                        item {
                            EmptyPanel(
                                message = "Open a fresh form for manual entry, or attach a scan/photo so parents can keep the document alongside the form while entering details."
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
                                    }
                                },
                                isRefreshing = uiState.isAttendanceRefreshing,
                            )
                        }
                        item {
                            PlayerAttendanceToolbar(
                                searchQuery = playerSearchQuery,
                                onSearchChange = { playerSearchQuery = it },
                                selectedSlot = playerSlotFilter,
                                onSlotSelected = { selected ->
                                    playerSlotFilter = if (playerSlotFilter == selected) "" else selected
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
                                    AttendancePlayerCard(
                                        student = student,
                                        isPresent = uiState.todayAttendanceIds.contains(student.id),
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
                                selectedView = AppView.Manager
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

                if (showScanSourceSheet) {
                    ScanSourceSheet(
                        isLoading = isScanLoading,
                        onDismiss = { showScanSourceSheet = false },
                        onCamera = {
                            showScanSourceSheet = false
                            cameraLauncher.launch(null)
                        },
                        onPhotos = {
                            showScanSourceSheet = false
                            imagePickerLauncher.launch("image/*")
                        },
                    )
                }

                if (showAdmissionSheet) {
                    AdmissionFormSheet(
                        attachedDocumentLabel = attachedAdmissionDocumentLabel,
                        onLoadRegNo = { viewModel.peekNextAdmissionRegNo() },
                        onDismiss = {
                            showAdmissionSheet = false
                            attachedAdmissionDocumentLabel = null
                        },
                        onSubmit = { draft ->
                            viewModel.submitAdmission(draft).also { result ->
                                if (result.success) {
                                    showAdmissionSheet = false
                                    attachedAdmissionDocumentLabel = null
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
                            viewModel.saveStudent(draft, editingStudent).also { result ->
                                if (result.success) {
                                    showEditorSheet = false
                                    editingStudent = null
                                    selectedStudent = null
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
                        attendanceCount = uiState.attendanceCounts[selectedStudent!!.id] ?: 0,
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
                        onRenew = {
                            selectedStudent?.let { activeStudent ->
                                val result = viewModel.renewStudent(activeStudent)
                                if (result.success) {
                                    showDetailSheet = false
                                    selectedStudent = null
                                }
                                scope.launch {
                                    snackbarHostState.showSnackbar(result.message)
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
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                        )
                        Text(
                            text = "Staff Dashboard",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 31.sp,
                        )
                        Text(
                            text = "Track admissions, fees, renewals, jersey orders, and coaching batches in one place.",
                            color = Color.White.copy(alpha = 0.86f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                        )
                    }

                    BrandPosterCard(
                        modifier = Modifier.widthIn(min = 92.dp, max = 108.dp),
                        imageModifier = Modifier.height(124.dp),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledTonalIconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier.size(42.dp),
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
                            shape = RoundedCornerShape(18.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = BrandGold,
                                contentColor = BrandBlueDeep,
                            ),
                        ) {
                            Icon(Icons.Outlined.Person, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Staff Login")
                        }
                        AssistChip(
                            onClick = {},
                            label = { Text("Read only") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White.copy(alpha = 0.14f),
                                labelColor = Color.White,
                            ),
                        )
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
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Staff signed in",
                                        color = Color.White.copy(alpha = 0.72f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = session.email,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                IconButton(onClick = onLogout) {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.Logout,
                                        contentDescription = "Logout",
                                        tint = Color.White,
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
private fun AlertSection(
    alertKids: List<Student>,
) {
    val feesPendingKids = alertKids.filter { it.isFeesPending() }
    val renewalPendingKids = alertKids.filter { it.isRenewalPending() }
    val alertCount = alertKids.size
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
                text = "30-Day Alerts",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.9.sp,
            )
            Text(
                text = if (alertCount == 1) "1 player needs attention" else "$alertCount players need attention",
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
            } else {
                if (feesPendingKids.isNotEmpty()) {
                    AlertNameSection(
                        title = "Fees to collect",
                        students = feesPendingKids,
                    )
                }
                if (renewalPendingKids.isNotEmpty()) {
                    AlertNameSection(
                        title = "Renewal follow-up",
                        students = renewalPendingKids,
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertNameSection(
    title: String,
    students: List<Student>,
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

@Composable
private fun AdmissionActionsSection(
    isScanLoading: Boolean,
    onOpen: () -> Unit,
    onScan: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onOpen,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(20.dp),
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
                Text("New Admission Form", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        OutlinedButton(
            onClick = onScan,
            enabled = !isScanLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
        ) {
            if (isScanLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Reading scanned form")
            } else {
                Icon(Icons.Outlined.Search, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Scan / Import Document", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    selectedView: AppView,
    onSelected: (AppView) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        AppView.entries.forEach { view ->
            val icon = when (view) {
                AppView.Admission -> Icons.Outlined.Description
                AppView.Player -> Icons.Outlined.Person
                AppView.Manager -> Icons.Outlined.Lock
            }
            NavigationBarItem(
                selected = selectedView == view,
                onClick = { onSelected(view) },
                icon = { Icon(icon, contentDescription = view.label) },
                label = { Text(view.label) },
            )
        }
    }
}

@Composable
private fun ScanSourceSheet(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onPhotos: () -> Unit,
) {
    FormDialog(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Admission Scan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                IconButton(onClick = onDismiss, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                }
            }
            Text(
                "Choose how to capture the document",
                fontSize = 24.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "You can scan with the camera or import an image from photos. The document will stay attached to the admission form for reference while parents fill the details manually.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
            Button(
                onClick = onCamera,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text("Scan with Camera")
            }
            OutlinedButton(
                onClick = onPhotos,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text("Import from Photos")
            }
        }
    }
}

@Composable
private fun FormDialog(
    onDismiss: () -> Unit,
    expanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            contentAlignment = Alignment.BottomCenter,
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
                            else Modifier.heightIn(max = 720.dp)
                        )
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun StatsSection(
    joined: Int,
    active: Int,
    paid: Int,
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
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompactStatCard(
                modifier = Modifier.weight(1f),
                title = "Fees Paid",
                value = paid,
                accent = BrandGold,
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
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.9.sp,
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

@Composable
private fun RosterToolbar(
    searchQuery: String,
    visibleCount: Int,
    totalCount: Int,
    isRefreshing: Boolean,
    onSearchChange: (String) -> Unit,
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
                UiTimeSlots.forEach { slot ->
                    val count = activePlayers.count { it.timeSlot == slot }
                    FilterChip(
                        selected = selectedSlot == slot,
                        onClick = { onSlotSelected(slot) },
                        label = { Text("$slot ($count)") },
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

@Composable
private fun RosterRow(
    student: Student,
    isManager: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
) {
    val needsAttention = student.isFeesPending() || student.isRenewalPending()
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val slotTone = themedBadgeTone(Color(0xFFEAF2FF), BrandBlueDeep, DarkInfoContainer, DarkInfoText)
    val activeTone = themedBadgeTone(Color(0xFFEAF8F2), BrandGreen, DarkSuccessContainer, DarkSuccessText)
    val discontinuedTone = themedBadgeTone(Color(0xFFEAEFF6), Color(0xFF5D7399), DarkMutedContainer, DarkMutedText)
    val feePaidTone = themedBadgeTone(Color(0xFFEAF2FF), BrandBlue, DarkInfoContainer, DarkInfoText)
    val feePendingTone = themedBadgeTone(Color(0xFFFFE8E8), BrandRed, DarkDangerContainer, DarkDangerText)
    val renewalOkTone = themedBadgeTone(Color(0xFFEAF8F2), BrandGreen, DarkSuccessContainer, DarkSuccessText)
    val renewalPendingTone = themedBadgeTone(Color(0xFFFFF2D8), Color(0xFF8F6500), DarkWarningContainer, DarkWarningText)

    Card(
        modifier = Modifier.clickable(onClick = onOpen),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (needsAttention && isDarkTheme) DarkAttentionCard else if (needsAttention) Color(0xFFFFFAEC) else MaterialTheme.colorScheme.surface
        ),
        border = if (needsAttention) BorderStroke(1.dp, if (isDarkTheme) DarkAttentionBorder else Color(0x33F4BE2E)) else null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = student.name,
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Age ${student.age}  •  Joined ${displayDate(student.joinDate)}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    fontSize = 13.sp,
                )
                Text(
                    text = if (student.jerseySize.isBlank() && student.jerseyPairs <= 0) {
                        "Jersey not set"
                    } else {
                        "Jersey ${student.jerseySize.ifBlank { "TBD" }}  •  ${student.jerseyPairs} pair${if (student.jerseyPairs == 1) "" else "s"}"
                    },
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                student.cardTimelineLabel()?.let { timeline ->
                    Text(
                        text = timeline,
                        color = if (student.discontinued) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f)
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Badge(
                        label = student.timeSlot.ifBlank { "Not set" },
                        container = slotTone.container,
                        color = slotTone.text,
                    )
                    Badge(
                        label = if (student.discontinued) "Discontinued" else "Active",
                        container = if (student.discontinued) discontinuedTone.container else activeTone.container,
                        color = if (student.discontinued) discontinuedTone.text else activeTone.text,
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Badge(
                    label = if (student.feesPaid) "Fees paid" else "Fees pending",
                    container = if (student.feesPaid) feePaidTone.container else feePendingTone.container,
                    color = if (student.feesPaid) feePaidTone.text else feePendingTone.text,
                )
                Badge(
                    label = student.renewalStatus(),
                    container = when {
                        student.discontinued -> discontinuedTone.container
                        student.isRenewalPending() -> renewalPendingTone.container
                        else -> renewalOkTone.container
                    },
                    color = when {
                        student.discontinued -> discontinuedTone.text
                        student.isRenewalPending() -> renewalPendingTone.text
                        else -> renewalOkTone.text
                    },
                )
                if (isManager) {
                    TextButton(onClick = onEdit) {
                        Text("Edit")
                    }
                } else {
                    Text(
                        text = "Tap for details",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PlayerDetailSheet(
    student: Student,
    attendanceCount: Int,
    isManager: Boolean,
    onDismiss: () -> Unit,
    onShowAttendanceHistory: () -> Unit,
    onEdit: () -> Unit,
    onDelete: suspend () -> Unit,
    onRenew: suspend () -> Unit,
    onToggleStatus: suspend () -> Unit,
) {
    var actionInProgress by rememberSaveable(student.id) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val slotTone = themedBadgeTone(Color(0xFFEAF2FF), BrandBlueDeep, DarkInfoContainer, DarkInfoText)
    val newTone = themedBadgeTone(Color(0xFFEAF2FF), BrandBlueDeep, DarkInfoContainer, DarkInfoText)
    val returningTone = themedBadgeTone(Color(0xFFFFF2D8), Color(0xFF8F6500), DarkWarningContainer, DarkWarningText)
    val activeTone = themedBadgeTone(Color(0xFFEAF8F2), BrandGreen, DarkSuccessContainer, DarkSuccessText)
    val discontinuedTone = themedBadgeTone(Color(0xFFEAEFF6), Color(0xFF5D7399), DarkMutedContainer, DarkMutedText)
    val renewalOkTone = themedBadgeTone(Color(0xFFEAF8F2), BrandGreen, DarkSuccessContainer, DarkSuccessText)
    val renewalPendingTone = themedBadgeTone(Color(0xFFFFF2D8), Color(0xFF8F6500), DarkWarningContainer, DarkWarningText)
    val feesPaidAccent = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) DarkInfoText else BrandBlue

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
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Player Profile",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlue,
            )
            Text(
                text = student.name,
                fontSize = 28.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Badge(student.timeSlot.ifBlank { "Not set" }, slotTone.container, slotTone.text)
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
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DataTileContent(
                    modifier = Modifier.weight(1f),
                    label = "Reg No",
                    value = student.regNo?.toString() ?: "Manual",
                    accent = MaterialTheme.colorScheme.onSurface,
                )
                DataTileContent(
                    modifier = Modifier.weight(1f),
                    label = "Age",
                    value = student.age.toString(),
                    accent = MaterialTheme.colorScheme.onSurface,
                )
                DataTileContent(
                    modifier = Modifier.weight(1f),
                    label = "Days Present",
                    value = attendanceCount.toString(),
                    accent = BrandBlue,
                    onClick = onShowAttendanceHistory,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DataTileContent(
                    modifier = Modifier.weight(1f),
                    label = "Amount",
                    value = "Rs ${String.format(Locale.US, "%.2f", student.amountPaid)}",
                    accent = MaterialTheme.colorScheme.onSurface,
                )
                DataTileContent(
                    modifier = Modifier.weight(1f),
                    label = "Join Date",
                    value = displayDate(student.joinDate),
                    accent = MaterialTheme.colorScheme.onSurface,
                )
                DataTileContent(
                    modifier = Modifier.weight(1f),
                    label = "Jersey",
                    value = if (student.jerseySize.isBlank() && student.jerseyPairs <= 0) {
                        "Not set"
                    } else {
                        "${student.jerseySize.ifBlank { "TBD" }} • ${student.jerseyPairs} pair${if (student.jerseyPairs == 1) "" else "s"}"
                    },
                    accent = MaterialTheme.colorScheme.onSurface,
                )
            }

            DataTile(
                label = if (student.discontinued) "Discontinued" else "Latest Renewal",
                value = if (student.discontinued) {
                    displayDate(student.discontinuedAt)
                } else {
                    displayDate(student.latestRenewal())
                },
                accent = MaterialTheme.colorScheme.onSurface,
            )

            DataTile(
                label = "Fees",
                value = if (student.feesPaid) "Paid" else "Not paid",
                accent = if (student.feesPaid) feesPaidAccent else BrandRed,
            )

            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.background.copy(alpha = 0.84f)) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Badge(
                        label = student.renewalStatus(),
                        container = when {
                            student.discontinued -> discontinuedTone.container
                            student.isRenewalPending() -> renewalPendingTone.container
                            else -> renewalOkTone.container
                        },
                        color = when {
                            student.discontinued -> discontinuedTone.text
                            student.isRenewalPending() -> renewalPendingTone.text
                            else -> renewalOkTone.text
                        },
                    )
                    Text(
                        text = student.trackingCaption(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    )
                }
            }

            Text(
                text = "Last updated by ${student.updatedBy}",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                fontSize = 12.sp,
            )

            if (isManager) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        enabled = actionInProgress == null,
                        onClick = onEdit,
                    ) { Text("Edit") }
                    OutlinedButton(
                        enabled = actionInProgress == null,
                        onClick = {
                            scope.launch {
                                actionInProgress = "status"
                                onToggleStatus()
                                actionInProgress = null
                            }
                        }
                    ) {
                        if (actionInProgress == "status") {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                        Text(if (student.discontinued) "Mark active" else "Discontinue")
                    }
                    if (student.isRenewalPending() && student.isActive()) {
                        ElevatedButton(
                            enabled = actionInProgress == null,
                            onClick = {
                                scope.launch {
                                    actionInProgress = "renew"
                                    onRenew()
                                    actionInProgress = null
                                }
                            }
                        ) {
                            if (actionInProgress == "renew") {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White,
                                )
                            } else {
                                Icon(Icons.Outlined.Refresh, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Renew")
                        }
                    }
                    TextButton(
                        enabled = actionInProgress == null,
                        onClick = {
                            scope.launch {
                                actionInProgress = "delete"
                                onDelete()
                                actionInProgress = null
                            }
                        }
                    ) {
                        if (actionInProgress == "delete") {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = BrandRed,
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                        Text("Delete", color = BrandRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun Badge(label: String, container: Color, color: Color) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
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
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
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
    Surface(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.84f),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label.uppercase(Locale.getDefault()),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
            Text(
                text = value,
                color = accent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            if (onClick != null) {
                Text(
                    text = "Tap to view",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.48f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
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

private fun buildRosterSections(students: List<Student>): List<RosterSection> {
    val slotOrder = listOf("6AM", "7:30AM", "4PM", "5:30PM", "7PM")
    val sections = mutableListOf<RosterSection>()

    slotOrder.forEach { slot ->
        val matches = students
            .filter { it.isActive() && it.timeSlot == slot }
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
        if (matches.isNotEmpty()) {
            sections += RosterSection(slot, "$slot Batch", matches)
        }
    }

    val unassigned = students
        .filter { it.isActive() && it.timeSlot.isBlank() }
        .sortedBy { it.name.lowercase(Locale.getDefault()) }
    if (unassigned.isNotEmpty()) {
        sections += RosterSection("not-set", "Time Slot Not Set", unassigned)
    }

    val discontinued = students
        .filter { it.discontinued }
        .sortedBy { it.name.lowercase(Locale.getDefault()) }
    if (discontinued.isNotEmpty()) {
        sections += RosterSection("discontinued", "Discontinued", discontinued)
    }

    val ungrouped = students
        .filter { it.isActive() && it.timeSlot.isNotBlank() && it.timeSlot !in slotOrder }
        .sortedBy { it.name.lowercase(Locale.getDefault()) }
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
                Text("Manager Access", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                IconButton(onClick = onDismiss, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                }
            }
            Text("Secure editing access", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "Sign in with your Supabase manager account to unlock editing features in the app.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                lineHeight = 20.sp,
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
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    LaunchedEffect(pin, isChecking) {
        if (pin.length == 6 && !isChecking) {
            isChecking = true
            val result = onUnlock(pin)
            if (!result.success) {
                inlineMessage = result.message
                pin = ""
                focusRequester.requestFocus()
                keyboardController?.show()
            }
            isChecking = false
        }
    }

    FormDialog(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Manager View", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                IconButton(onClick = onDismiss, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                }
            }
            Text("Enter 6-digit PIN", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "This lock keeps dashboard stats and player management away from parents and player attendance mode.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                lineHeight = 20.sp,
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicTextField(
                    value = pin,
                    onValueChange = {
                        pin = it.filter(Char::isDigit).take(6)
                        inlineMessage = ""
                    },
                    modifier = Modifier
                        .size(1.dp)
                        .focusRequester(focusRequester),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done,
                    ),
                    cursorBrush = SolidColor(Color.Transparent),
                    decorationBox = {},
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        },
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                                    .height(60.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = char,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                            }
                        }
                    }
                }
            }
            Text(
                text = if (isChecking) "Checking PIN..." else "Enter all 6 digits to open Manager View",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                fontSize = 12.sp,
            )
            if (inlineMessage.isNotBlank()) {
                Text(inlineMessage, color = BrandRed, fontSize = 13.sp)
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
    var amountPaid by rememberSaveable(editingStudent?.id) { mutableStateOf(if (editingStudent == null) "0" else editingStudent.toDraft().amountPaid) }
    var jerseySize by rememberSaveable(editingStudent?.id) { mutableStateOf(editingStudent?.jerseySize.orEmpty()) }
    var jerseyPairs by rememberSaveable(editingStudent?.id) { mutableStateOf(editingStudent?.jerseyPairs?.toString() ?: "0") }
    var inlineMessage by rememberSaveable { mutableStateOf("") }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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

            OutlinedTextField(
                value = joinDate,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .then(rememberBringIntoViewOnFocusModifier()),
                label = { Text("Join date") },
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { openDatePicker.value.invoke() }) {
                        Text("Pick")
                    }
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Fees paid?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("If unpaid, amount is locked to 0.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
                }
                Switch(
                    checked = feesPaid,
                    onCheckedChange = {
                        feesPaid = it
                        if (!it) amountPaid = "0"
                    }
                )
            }

            OutlinedTextField(
                value = amountPaid,
                onValueChange = { amountPaid = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(rememberBringIntoViewOnFocusModifier()),
                label = { Text("Amount paid") },
                enabled = feesPaid,
                singleLine = true,
            )

            AdmissionSectionCard(title = "Jersey details") {
                AdmissionDropdownField(
                    label = "Jersey size",
                    value = jerseySize,
                    options = JerseySizeOptions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    onSelect = { jerseySize = it },
                )
                OutlinedTextField(
                    value = jerseyPairs,
                    onValueChange = { jerseyPairs = it.filter(Char::isDigit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = { Text("Jersey pairs") },
                    singleLine = true,
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
                                    amountPaid = amountPaid,
                                    jerseySize = jerseySize,
                                    jerseyPairs = jerseyPairs.ifBlank { "0" },
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
    attachedDocumentLabel: String?,
    onLoadRegNo: suspend () -> Long,
    onDismiss: () -> Unit,
    onSubmit: suspend (AdmissionDraft) -> OperationResult,
) {
    var applicantName by rememberSaveable { mutableStateOf("") }
    var nationality by rememberSaveable { mutableStateOf("Indian") }
    var birthDay by rememberSaveable { mutableStateOf("") }
    var birthMonth by rememberSaveable { mutableStateOf("") }
    var birthYear by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("") }
    var fatherGuardianName by rememberSaveable { mutableStateOf("") }
    var alternateContactNo by rememberSaveable { mutableStateOf("") }
    var parentContactNo by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var schoolCollege by rememberSaveable { mutableStateOf("") }
    var parentAadhaarNo by rememberSaveable { mutableStateOf("") }
    var timeSlot by rememberSaveable { mutableStateOf("") }
    var joinDate by rememberSaveable { mutableStateOf(todayIsoDate()) }
    var feesPaid by rememberSaveable { mutableStateOf(false) }
    var amountPaid by rememberSaveable { mutableStateOf("0") }
    var jerseySize by rememberSaveable { mutableStateOf("") }
    var jerseyPairs by rememberSaveable { mutableStateOf("0") }
    var paymentReference by rememberSaveable { mutableStateOf("") }
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
    val clipboardManager = LocalClipboardManager.current
    val upiId = remember { context.getString(R.string.academy_upi_id) }
    val upiName = remember { context.getString(R.string.academy_upi_name) }
    // Let parents generate a payment link/QR before toggling "Fees paid".
    // We only enable UPI actions when a positive amount is entered.
    val upiAmount = remember(amountPaid) {
        amountPaid.toDoubleOrNull()?.takeIf { it > 0.0 }
    }
    val upiUri = remember(upiId, upiName, upiAmount, applicantName) {
        if (upiId.isBlank() || upiAmount == null) "" else buildUpiPayUri(
            upiId = upiId,
            payeeName = upiName,
            amount = upiAmount,
            note = "Gen Alpha admission - ${applicantName.ifBlank { "New player" }}",
        )
    }
    var showUpiQr by rememberSaveable { mutableStateOf(false) }
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
            if (!attachedDocumentLabel.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = BrandBlue.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.12f)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.Description,
                            contentDescription = null,
                            tint = BrandBlue,
                            modifier = Modifier.size(20.dp),
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                "Attached document",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlue,
                            )
                            Text(
                                attachedDocumentLabel,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

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
                    value = city,
                    onValueChange = { city = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = "Grade (optional)",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Fees paid?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("If unpaid, amount stays at 0.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
                    }
                    Switch(
                        checked = feesPaid,
                        onCheckedChange = {
                            feesPaid = it
                            if (!it) amountPaid = "0"
                        }
                    )
                }
                AdmissionTextField(
                    value = amountPaid,
                    onValueChange = { amountPaid = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(rememberBringIntoViewOnFocusModifier()),
                    label = "Amount paid",
                    enabled = feesPaid,
                    singleLine = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdmissionDropdownField(
                        label = "Jersey size",
                        value = jerseySize,
                        options = JerseySizeOptions,
                        modifier = Modifier
                            .weight(1f)
                            .then(rememberBringIntoViewOnFocusModifier()),
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

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.84f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "UPI payment",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        if (upiId.isBlank()) {
                            Text(
                                text = "UPI payment is not configured yet. Please ask academy staff for payment details.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                            )
                        } else if (upiAmount == null) {
                            Text(
                                text = "Enter the amount above to enable the UPI QR and Pay button.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "UPI ID",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = upiId,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                TextButton(
                                    onClick = { clipboardManager.setText(AnnotatedString(upiId)) },
                                ) {
                                    Text("Copy")
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = { showUpiQr = !showUpiQr },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                ) {
                                    Text(if (showUpiQr) "Hide QR" else "Show QR")
                                }
                                Button(
                                    enabled = upiUri.isNotBlank(),
                                    onClick = {
                                        runCatching {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiUri))
                                            context.startActivity(intent)
                                        }.onFailure {
                                            inlineMessage = "Unable to open a UPI app on this device."
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = BrandBlue,
                                        contentColor = Color.White,
                                    ),
                                ) {
                                    Text("Pay in UPI app")
                                }
                            }
                            if (showUpiQr && upiUri.isNotBlank()) {
                                val qrBitmap = remember(upiUri) { generateQrBitmap(upiUri, 720) }
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Image(
                                            bitmap = qrBitmap.asImageBitmap(),
                                            contentDescription = "UPI QR",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 320.dp),
                                            contentScale = ContentScale.Fit,
                                        )
                                        Text(
                                            text = "Scan this QR in any UPI app",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            fontSize = 12.sp,
                                        )
                                    }
                                }
                            }
                        }

                        AdmissionTextField(
                            value = paymentReference,
                            onValueChange = { paymentReference = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(rememberBringIntoViewOnFocusModifier()),
                            label = "UPI reference / UTR (optional)",
                            singleLine = true,
                        )
                    }
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
                            val result = onSubmit(
                                AdmissionDraft(
                                    applicantName = applicantName,
                                    nationality = nationality,
                                    dateOfBirth = dateOfBirth,
                                    gender = gender,
                                    fatherGuardianName = fatherGuardianName,
                                    alternateContactNo = alternateContactNo,
                                    parentContactNo = parentContactNo,
                                    city = city,
                                    address = address,
                                    schoolCollege = schoolCollege,
                                    parentAadhaarNo = parentAadhaarNo,
                                    timeSlot = timeSlot,
                                    joinDate = joinDate,
                                    feesPaid = feesPaid,
                                    amountPaid = amountPaid,
                                    jerseySize = jerseySize,
                                    jerseyPairs = jerseyPairs.ifBlank { "0" },
                                    paymentMethod = "UPI",
                                    paymentUpiId = upiId,
                                    paymentReference = paymentReference,
                                    comments = comments,
                                    batsmanStyle = batsmanStyle,
                                    bowlingStyles = bowlingStyles.toList(),
                                    readyToStartNow = readyToStartNow,
                                    consentAccepted = consentAccepted,
                                    termsAccepted = termsAccepted,
                                )
                            )
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
                    text = value.ifBlank { "Select" },
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
                    text = { Text(option) },
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
