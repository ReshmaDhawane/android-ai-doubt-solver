package com.reshma.aidoubtsolver.ui.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reshma.aidoubtsolver.domain.ChatMessage
import com.reshma.aidoubtsolver.domain.MessageStatus
import com.reshma.aidoubtsolver.domain.Subject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var input by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) { viewModel.errors.collect { snackbar.showSnackbar(it) } }
    LaunchedEffect(state.messages.size, state.isThinking) {
        val count = state.messages.size + if (state.isThinking) 1 else 0
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("AI Doubt Solver") },
                    actions = {
                        IconButton(onClick = viewModel::clear) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Clear chat")
                        }
                    },
                )
                LazyRow(Modifier.padding(horizontal = 8.dp)) {
                    items(Subject.entries) { s ->
                        FilterChip(
                            selected = s == state.subject,
                            onClick = { viewModel.selectSubject(s) },
                            label = { Text(s.label) },
                            modifier = Modifier.padding(4.dp),
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            Box(Modifier.weight(1f)) {
                if (state.messages.isEmpty() && !state.isThinking) {
                    Text(
                        "Ask your first ${state.subject.label} doubt",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(state.messages, key = { it.id }) { m ->
                        MessageBubble(m, onRetry = { viewModel.retry(m) })
                    }
                    if (state.isThinking) {
                        item {
                            CircularProgressIndicator(Modifier.padding(16.dp).width(24.dp))
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Type your doubt...") },
                    maxLines = 4,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    enabled = input.isNotBlank() && !state.isThinking,
                    onClick = {
                        viewModel.send(input)
                        input = ""
                    },
                ) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send") }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(message: ChatMessage, onRetry: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val failed = message.status == MessageStatus.FAILED
    val colors = MaterialTheme.colorScheme
    val bg = when {
        failed -> colors.errorContainer
        message.isUser -> colors.primary
        else -> colors.surfaceVariant
    }
    val fg = if (message.isUser && !failed) colors.onPrimary else colors.onSurface

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            color = bg,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = { clipboard.setText(AnnotatedString(message.text)) },
                ),
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(message.text, color = fg)
                if (failed) TextButton(onClick = onRetry) { Text("Retry") }
            }
        }
    }
}
