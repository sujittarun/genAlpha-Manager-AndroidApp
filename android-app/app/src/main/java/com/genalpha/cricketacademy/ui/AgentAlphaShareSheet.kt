package com.genalpha.cricketacademy.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.genalpha.cricketacademy.data.AgentAlphaAttachment
import com.genalpha.cricketacademy.data.AgentAlphaConfirmation
import com.genalpha.cricketacademy.data.AgentAlphaIntakeReview
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MAX_ATTACHMENT_BYTES = 15L * 1024 * 1024
private const val MAX_TOTAL_BYTES = 30L * 1024 * 1024

data class AgentAlphaShareItem(
    val uri: Uri,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
)

data class AgentAlphaShareRequest(
    val id: String,
    val text: String,
    val items: List<AgentAlphaShareItem>,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgentAlphaShareSheet(
    request: AgentAlphaShareRequest,
    viewModel: AcademyViewModel,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val correctionBringIntoViewRequester = remember { BringIntoViewRequester() }
    var notes by remember(request.id) { mutableStateOf(request.text) }
    var review by remember(request.id) { mutableStateOf<AgentAlphaIntakeReview?>(null) }
    var confirmation by remember(request.id) { mutableStateOf<AgentAlphaConfirmation?>(null) }
    var correction by remember(request.id) { mutableStateOf("") }
    var status by remember(request.id) { mutableStateOf("Nothing will be saved until you confirm the review.") }
    var error by remember(request.id) { mutableStateOf("") }
    var isWorking by remember(request.id) { mutableStateOf(false) }

    fun startExtraction() {
        if (isWorking) return
        focusManager.clearFocus()
        keyboardController?.hide()
        scope.launch {
            isWorking = true
            error = ""
            status = "Uploading securely and reading with AgentAlpha…"
            val slowNotice = launch {
                delay(15_000)
                if (isWorking) {
                    status = "AgentAlpha is reading the complete form. Detailed handwriting can take up to two minutes—keep this screen open."
                }
            }
            try {
                val attachments = context.readAgentAlphaAttachments(request.items)
                review = viewModel.createAgentAlphaIntake(notes, attachments)
                status = "Draft ready. Verify it below; nothing has been saved yet."
            } catch (throwable: Throwable) {
                error = throwable.message ?: "AgentAlpha could not read this share."
                status = "Nothing was saved."
            } finally {
                slowNotice.cancel()
                isWorking = false
            }
        }
    }

    fun applyCorrection() {
        val currentReview = review ?: return
        if (correction.isBlank() || isWorking) return
        focusManager.clearFocus()
        keyboardController?.hide()
        scope.launch {
            isWorking = true
            error = ""
            status = "Applying your correction and rebuilding the full review…"
            try {
                review = viewModel.correctAgentAlphaIntake(currentReview, correction)
                correction = ""
                status = "Correction applied. Check the complete updated review."
            } catch (throwable: Throwable) {
                error = throwable.message ?: "AgentAlpha could not apply this correction."
            } finally {
                isWorking = false
            }
        }
    }

    fun confirmReview() {
        val currentReview = review ?: return
        if (isWorking) return
        focusManager.clearFocus()
        keyboardController?.hide()
        scope.launch {
            isWorking = true
            error = ""
            status = if (currentReview.intakeType == "renewal") {
                "Recording the renewal and payment…"
            } else {
                "Creating the pending admission…"
            }
            try {
                confirmation = viewModel.confirmAgentAlphaIntake(currentReview)
                status = confirmation?.message.orEmpty()
            } catch (throwable: Throwable) {
                error = throwable.message ?: "AgentAlpha could not confirm this intake."
                status = "The review is still open. You can retry confirmation."
            } finally {
                isWorking = false
            }
        }
    }

    LaunchedEffect(review?.summary) {
        if (review != null) {
            delay(120)
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(confirmation) {
        if (confirmation != null) {
            delay(120)
            val lastItem = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
            listState.animateScrollToItem(lastItem)
        }
    }

    Dialog(
        onDismissRequest = { if (!isWorking) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (confirmation == null) Icons.Outlined.AutoAwesome else Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                    ) {
                        Text(
                            text = "Share to AgentAlpha",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = when {
                                confirmation != null -> "Saved successfully"
                                review != null -> "Review before saving"
                                else -> "Admission or renewal intake"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDismiss, enabled = !isWorking) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close")
                    }
                }
                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding(),
                    state = listState,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (request.items.isNotEmpty() && review == null) {
                        item {
                            Text(
                                text = "Shared attachments (${request.items.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        items(request.items, key = { it.uri.toString() }) { item ->
                            AgentAlphaAttachmentRow(item)
                        }
                    }

                    if (confirmation == null && review == null) {
                        item {
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Messages or instructions") },
                                placeholder = { Text("Example: Renewal for Arjun, ₹3,000 paid for one month") },
                                minLines = 3,
                                maxLines = 8,
                                enabled = !isWorking,
                            )
                        }
                    }

                    review?.let { currentReview ->
                        item {
                            AgentAlphaComparisonPanel(
                                items = request.items,
                                review = currentReview,
                            )
                        }

                        if (confirmation == null) {
                            item {
                                OutlinedTextField(
                                    value = correction,
                                    onValueChange = { correction = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bringIntoViewRequester(correctionBringIntoViewRequester)
                                        .onFocusEvent { focusState ->
                                            if (focusState.isFocused) {
                                                scope.launch {
                                                    delay(250)
                                                    correctionBringIntoViewRequester.bringIntoView()
                                                }
                                            }
                                        },
                                    label = { Text("Correction or missing detail") },
                                    placeholder = { Text("Example: Address is … and payment is pending") },
                                    minLines = 2,
                                    maxLines = 6,
                                    enabled = !isWorking,
                                )
                            }
                            item {
                                OutlinedButton(
                                    onClick = ::applyCorrection,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp),
                                    enabled = correction.isNotBlank() && !isWorking,
                                    shape = RoundedCornerShape(16.dp),
                                ) {
                                    Text("Apply correction and re-check")
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = if (error.isBlank()) {
                                MaterialTheme.colorScheme.surfaceVariant
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            },
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                if (isWorking) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                                Text(
                                    text = error.ifBlank { status },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (error.isBlank()) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    },
                                )
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (confirmation == null) {
                                Button(
                                    onClick = if (review == null) ::startExtraction else ::confirmReview,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 54.dp),
                                    enabled = !isWorking && (review != null || notes.isNotBlank() || request.items.isNotEmpty()),
                                    shape = RoundedCornerShape(16.dp),
                                ) {
                                    Text(
                                        text = if (review == null) {
                                            "Read with AgentAlpha"
                                        } else if (review?.intakeType == "renewal") {
                                            "Confirm renewal"
                                        } else {
                                            "Confirm admission"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                TextButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isWorking,
                                ) {
                                    Text("Cancel and close")
                                }
                            } else {
                                Button(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 54.dp),
                                    shape = RoundedCornerShape(16.dp),
                                ) {
                                    Text("Done", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class AgentAlphaReviewPresentation(
    val title: String,
    val caseId: String,
    val details: List<String>,
    val warnings: List<String>,
    val guidance: List<String>,
)

private data class AgentAlphaSourcePreviewState(
    val isLoading: Boolean = true,
    val bitmap: ImageBitmap? = null,
)

@Composable
private fun AgentAlphaComparisonPanel(
    items: List<AgentAlphaShareItem>,
    review: AgentAlphaIntakeReview,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.FactCheck,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Compare source with AgentAlpha",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "Check every value against the original before confirming.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val useSideBySide = maxWidth >= 700.dp && items.isNotEmpty()
            if (useSideBySide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    AgentAlphaSourceDocument(
                        items = items,
                        modifier = Modifier.weight(1f),
                        previewHeight = 560.dp,
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ComparisonPaneLabel("EXTRACTED DRAFT")
                        AgentAlphaReviewCard(review)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (items.isNotEmpty()) {
                        AgentAlphaSourceDocument(
                            items = items,
                            modifier = Modifier.fillMaxWidth(),
                            previewHeight = 340.dp,
                        )
                    }
                    ComparisonPaneLabel("EXTRACTED DRAFT")
                    AgentAlphaReviewCard(review)
                }
            }
        }
    }
}

@Composable
private fun ComparisonPaneLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun AgentAlphaSourceDocument(
    items: List<AgentAlphaShareItem>,
    modifier: Modifier = Modifier,
    previewHeight: androidx.compose.ui.unit.Dp,
) {
    val context = LocalContext.current
    var selectedIndex by remember(items) { mutableStateOf(0) }
    var showExpanded by remember { mutableStateOf(false) }
    val selected = items[selectedIndex.coerceIn(items.indices)]
    val previewState by produceState(
        initialValue = AgentAlphaSourcePreviewState(),
        selected.uri,
        selected.mimeType,
    ) {
        val bitmap = withContext(Dispatchers.IO) {
            context.decodeSourcePreview(selected, maxDimension = 1800)
        }
        value = AgentAlphaSourcePreviewState(isLoading = false, bitmap = bitmap)
    }
    val preview = previewState.bitmap

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 2.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(modifier = Modifier.weight(1f)) {
                    ComparisonPaneLabel("SOURCE DOCUMENT")
                    Text(
                        text = selected.fileName,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (items.size > 1) {
                    Text(
                        text = "${selectedIndex + 1}/${items.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(previewHeight)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .clickable(enabled = preview != null) { showExpanded = true },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    preview != null -> Image(
                        bitmap = preview,
                        contentDescription = "Original ${selected.fileName}",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentScale = ContentScale.Fit,
                    )
                    previewState.isLoading -> CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
                    else -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Outlined.Description, contentDescription = null)
                        Text(
                            text = "Preview unavailable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (items.size > 1) {
                    TextButton(
                        onClick = { selectedIndex = (selectedIndex - 1 + items.size) % items.size },
                    ) { Text("Previous") }
                    TextButton(
                        onClick = { selectedIndex = (selectedIndex + 1) % items.size },
                    ) { Text("Next") }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { showExpanded = true }, enabled = preview != null) {
                    Text("Open & zoom")
                }
            }
        }
    }

    if (showExpanded && preview != null) {
        AgentAlphaExpandedSource(
            preview = preview,
            fileName = selected.fileName,
            onDismiss = { showExpanded = false },
        )
    }
}

@Composable
private fun AgentAlphaExpandedSource(
    preview: ImageBitmap,
    fileName: String,
    onDismiss: () -> Unit,
) {
    var scale by remember(preview) { mutableStateOf(1f) }
    var offset by remember(preview) { mutableStateOf(Offset.Zero) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF111318),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Original document",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                        Text(
                            text = fileName,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }
                    TextButton(onClick = {
                        scale = 1f
                        offset = Offset.Zero
                    }) {
                        Text("Reset", color = Color.White)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close document", tint = Color.White)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(preview) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                offset = if (scale <= 1f) Offset.Zero else offset + pan
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        bitmap = preview,
                        contentDescription = "Original $fileName",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            },
                        contentScale = ContentScale.Fit,
                    )
                }
                Text(
                    text = "Pinch to zoom • drag to inspect handwriting",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.72f),
                )
            }
        }
    }
}

@Composable
private fun AgentAlphaReviewCard(review: AgentAlphaIntakeReview) {
    val presentation = remember(review.summary, review.intakeType) {
        buildAgentAlphaReviewPresentation(review)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 2.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.FactCheck,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = presentation.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    if (presentation.caseId.isNotBlank()) {
                        Text(
                            text = presentation.caseId,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                ) {
                    Text(
                        text = "DRAFT",
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                presentation.details.forEachIndexed { index, line ->
                    if (index == 0 && !line.contains(':')) {
                        Text(
                            text = styledReviewText(line),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            reviewDetailSectionTitle(line).takeIf(String::isNotBlank)?.let { sectionTitle ->
                                Text(
                                    text = sectionTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(13.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            ) {
                                Text(
                                    text = styledReviewText(line),
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }

                if (presentation.warnings.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Column(
                            modifier = Modifier.padding(15.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(9.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.WarningAmber,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                                Text(
                                    text = "Check before saving",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                            presentation.warnings.forEach { warning ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Text(
                                        text = "•",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                    Text(
                                        text = styledReviewText(warning),
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                }
                            }
                        }
                    }
                }

                if (presentation.guidance.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Text(
                                text = styledReviewText(
                                    presentation.guidance.joinToString("\n\n") { cleanReviewLine(it) },
                                ),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildAgentAlphaReviewPresentation(review: AgentAlphaIntakeReview): AgentAlphaReviewPresentation {
    val lines = review.summary.lineSequence().map(String::trim).filter(String::isNotBlank).toList()
    val caseId = Regex("\\bGA-[A-Za-z0-9-]+\\b", RegexOption.IGNORE_CASE)
        .find(review.summary)?.value.orEmpty()
    val title = when {
        review.intakeType == "renewal" -> "Renewal review"
        review.intakeType == "admission" -> "Admission review"
        else -> "More details needed"
    }
    val content = lines.drop(1).filterNot { line ->
        line.startsWith("ID:", ignoreCase = true) || line.equals(caseId, ignoreCase = true)
    }
    val warningHeaderIndex = content.indexOfFirst { line ->
        line.contains("need before saving", ignoreCase = true) ||
            line.contains("please check", ignoreCase = true) ||
            line.contains("more details needed", ignoreCase = true)
    }
    val guidanceStart = content.indexOfFirst { line ->
        line.startsWith("Send ", ignoreCase = true) ||
            line.startsWith("Reply ", ignoreCase = true)
    }
    val detailsEnd = listOf(warningHeaderIndex, guidanceStart)
        .filter { it >= 0 }
        .minOrNull() ?: content.size
    val details = content.take(detailsEnd)
    val warningEnd = if (guidanceStart >= 0) guidanceStart else content.size
    val warnings = if (warningHeaderIndex >= 0) {
        content.subList((warningHeaderIndex + 1).coerceAtMost(content.size), warningEnd.coerceAtLeast(warningHeaderIndex + 1))
            .map(::cleanWarningLine)
            .filter(String::isNotBlank)
    } else {
        emptyList()
    }
    val guidance = if (guidanceStart >= 0) content.drop(guidanceStart) else emptyList()
    return AgentAlphaReviewPresentation(
        title = title,
        caseId = caseId,
        details = details,
        warnings = warnings,
        guidance = guidance,
    )
}

private fun styledReviewText(line: String): AnnotatedString = buildAnnotatedString {
    val segments = cleanReviewLine(line).split(" • ")
    segments.forEachIndexed { index, rawSegment ->
        if (index > 0) append("  •  ")
        val segment = rawSegment.trim()
        val colon = segment.indexOf(':')
        if (colon in 1..24) {
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append(segment.substring(0, colon + 1))
            pop()
            val value = segment.substring(colon + 1)
            if (value.isNotBlank()) append(value)
        } else {
            appendAsteriskStyledText(segment)
        }
    }
}

private fun AnnotatedString.Builder.appendAsteriskStyledText(value: String) {
    var cursor = 0
    Regex("\\*([^*]+)\\*").findAll(value).forEach { match ->
        if (match.range.first > cursor) append(value.substring(cursor, match.range.first))
        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
        append(match.groupValues[1])
        pop()
        cursor = match.range.last + 1
    }
    if (cursor < value.length) append(value.substring(cursor))
}

private fun cleanReviewLine(line: String): String = line
    .replace("⚠️", "")
    .trim()

private fun cleanWarningLine(line: String): String = cleanReviewLine(line)
    .replace(Regex("^(?:[•*-]|\\d+[.)])\\s*"), "")
    .trim()

private fun reviewDetailSectionTitle(line: String): String {
    val normalized = cleanReviewLine(line).lowercase()
    return when {
        normalized.startsWith("guardian:") -> "CONTACT"
        normalized.startsWith("school:") -> "ACADEMY & EDUCATION"
        normalized.startsWith("address:") -> "ADDRESS"
        normalized.startsWith("joining:") || normalized.startsWith("paid through:") -> "SCHEDULE"
        normalized.startsWith("skills:") -> "CRICKET PROFILE"
        normalized.startsWith("payment:") || normalized.startsWith("*₹") -> "PAYMENT"
        normalized.startsWith("ref:") -> "PAYMENT REFERENCE"
        else -> "DETAILS"
    }
}

@Composable
private fun AgentAlphaAttachmentRow(item: AgentAlphaShareItem) {
    val context = LocalContext.current
    val preview by produceState<ImageBitmap?>(initialValue = null, item.uri, item.mimeType) {
        value = withContext(Dispatchers.IO) {
            context.decodeSourcePreview(item, maxDimension = 512)
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (preview != null) {
                Image(
                    bitmap = preview!!,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(Icons.Outlined.Description, contentDescription = null)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(item.fileName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                text = item.sizeBytes.takeIf { it >= 0 }?.let(::formatBytes)
                    ?: item.mimeType.ifBlank { "Shared file" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private suspend fun Context.readAgentAlphaAttachments(
    items: List<AgentAlphaShareItem>,
): List<AgentAlphaAttachment> = withContext(Dispatchers.IO) {
    val knownTotal = items.sumOf { it.sizeBytes.coerceAtLeast(0) }
    require(knownTotal <= MAX_TOTAL_BYTES) { "The combined attachments must be 30 MB or smaller." }
    var actualTotal = 0L
    items.map { item ->
        val mimeType = item.mimeType.ifBlank { contentResolver.getType(item.uri).orEmpty() }
        require(mimeType.startsWith("image/") || mimeType.equals("application/pdf", true)) {
            "${item.fileName} is not a supported image or PDF."
        }
        require(item.sizeBytes < 0 || item.sizeBytes <= MAX_ATTACHMENT_BYTES) {
            "${item.fileName} is larger than 15 MB."
        }
        val bytes = contentResolver.openInputStream(item.uri)?.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                output.write(buffer, 0, count)
                require(output.size().toLong() <= MAX_ATTACHMENT_BYTES) {
                    "${item.fileName} is larger than 15 MB."
                }
            }
            output.toByteArray()
        } ?: throw IllegalArgumentException("Could not open ${item.fileName}.")
        actualTotal += bytes.size
        require(actualTotal <= MAX_TOTAL_BYTES) { "The combined attachments must be 30 MB or smaller." }
        AgentAlphaAttachment(
            fileName = item.fileName,
            mimeType = mimeType,
            bytes = bytes,
        )
    }
}

private fun Context.decodeSourcePreview(
    item: AgentAlphaShareItem,
    maxDimension: Int,
): ImageBitmap? = if (
    item.mimeType.equals("application/pdf", ignoreCase = true) ||
    item.fileName.endsWith(".pdf", ignoreCase = true) ||
    contentResolver.getType(item.uri).equals("application/pdf", ignoreCase = true)
) {
    decodePdfPreview(item.uri, maxDimension)
} else {
    decodeImagePreview(item.uri, maxDimension)
}

private fun Context.decodeImagePreview(uri: Uri, maxDimension: Int): ImageBitmap? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (bounds.outWidth / sample > maxDimension || bounds.outHeight / sample > maxDimension) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        ?.asImageBitmap()
}.getOrNull()

private fun Context.decodePdfPreview(uri: Uri, maxDimension: Int): ImageBitmap? = runCatching {
    contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
        PdfRenderer(descriptor).use { renderer ->
            if (renderer.pageCount == 0) {
                null
            } else {
                renderer.openPage(0).use { page ->
                    val longestSide = maxOf(page.width, page.height).coerceAtLeast(1)
                    val renderScale = (maxDimension.toFloat() / longestSide).coerceAtMost(2f)
                    val width = (page.width * renderScale).toInt().coerceAtLeast(1)
                    val height = (page.height * renderScale).toInt().coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap.asImageBitmap()
                }
            }
        }
    }
}.getOrNull()

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    bytes >= 1024 -> "%.0f KB".format(bytes / 1024.0)
    else -> "$bytes bytes"
}
