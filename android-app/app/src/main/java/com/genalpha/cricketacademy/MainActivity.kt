package com.genalpha.cricketacademy

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.genalpha.cricketacademy.data.SessionPrefs
import com.genalpha.cricketacademy.data.SupabaseRepository
import com.genalpha.cricketacademy.ui.AcademyApp
import com.genalpha.cricketacademy.ui.AcademyViewModel
import com.genalpha.cricketacademy.ui.AcademyViewModelFactory
import com.genalpha.cricketacademy.ui.AgentAlphaShareItem
import com.genalpha.cricketacademy.ui.AgentAlphaShareRequest
import java.util.UUID

class MainActivity : ComponentActivity() {
    private val repository by lazy { SupabaseRepository() }
    private val sessionPrefs by lazy { SessionPrefs(this) }
    private val viewModel: AcademyViewModel by viewModels {
        AcademyViewModelFactory(repository, sessionPrefs)
    }
    private var agentAlphaShareQueue by mutableStateOf<List<AgentAlphaShareRequest>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        captureAgentAlphaShare(intent)

        setContent {
            AcademyApp(
                viewModel = viewModel,
                agentAlphaShare = agentAlphaShareQueue.firstOrNull(),
                onAgentAlphaShareConsumed = {
                    agentAlphaShareQueue = agentAlphaShareQueue.drop(1)
                    if (agentAlphaShareQueue.isEmpty()) {
                        setIntent(Intent(this, MainActivity::class.java))
                    }
                },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        captureAgentAlphaShare(intent)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppForegrounded()
    }

    override fun onStop() {
        viewModel.onAppBackgrounded()
        super.onStop()
    }

    @Suppress("DEPRECATION")
    private fun captureAgentAlphaShare(intent: Intent?) {
        val shareIntent = intent ?: return
        if (shareIntent.action !in setOf(Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE)) return

        val streams = buildList {
            if (shareIntent.action == Intent.ACTION_SEND) {
                shareIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let(::add)
            } else {
                addAll(shareIntent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM).orEmpty())
            }
            val clip = shareIntent.clipData
            if (clip != null) {
                repeat(clip.itemCount) { index -> clip.getItemAt(index).uri?.let(::add) }
            }
        }.distinct()

        val items = streams.map { uri ->
            val metadata = queryShareMetadata(uri)
            AgentAlphaShareItem(
                uri = uri,
                fileName = metadata.first,
                mimeType = contentResolver.getType(uri).orEmpty().ifBlank { shareIntent.type.orEmpty() },
                sizeBytes = metadata.second,
            )
        }
        val text = sanitizeSharedText(
            shareIntent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString().orEmpty(),
        )
        val subject = sanitizeSharedText(
            shareIntent.getCharSequenceExtra(Intent.EXTRA_SUBJECT)?.toString().orEmpty(),
        )
        if (items.isEmpty() && text.isBlank() && subject.isBlank()) return

        agentAlphaShareQueue = agentAlphaShareQueue + AgentAlphaShareRequest(
            id = UUID.randomUUID().toString(),
            text = listOf(subject, text).filter { it.isNotBlank() }.distinct().joinToString("\n"),
            items = items,
        )
    }

    private fun sanitizeSharedText(value: String): String = value
        .lineSequence()
        .map(String::trim)
        .filter(String::isNotBlank)
        .filterNot { line ->
            GENERATED_WHATSAPP_MEDIA_CAPTION.matches(line)
        }
        .joinToString("\n")

    private fun queryShareMetadata(uri: Uri): Pair<String, Long> {
        var name = uri.lastPathSegment?.substringAfterLast('/').orEmpty().ifBlank { "shared-file" }
        var size = -1L
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null,
                null,
                null,
            )
            if (cursor?.moveToFirst() == true) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) name = cursor.getString(nameIndex).orEmpty().ifBlank { name }
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
            }
        } finally {
            cursor?.close()
        }
        return name to size
    }

    companion object {
        private val GENERATED_WHATSAPP_MEDIA_CAPTION =
            Regex("^(?:photos?|videos?|documents?|files?|audio) from\\s+.+$", RegexOption.IGNORE_CASE)
    }
}
