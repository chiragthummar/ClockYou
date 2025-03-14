package com.bnyro.clock.ui.dialog

import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.extensions.getContentFileName
import com.bnyro.clock.ui.components.ClickableIcon
import com.bnyro.clock.ui.components.DialogButton
import com.bnyro.clock.util.PickPersistentFileContract
import com.bnyro.clock.util.RingtoneHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun RingtonePickerDialog(
    onDismissRequest: () -> Unit,
    onSelection: (String, Uri) -> Unit
) {
    val context = LocalContext.current

    val pickSoundFile = rememberLauncherForActivityResult(PickPersistentFileContract()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        onSelection.invoke(context.getContentFileName(uri).orEmpty(), uri)
        onDismissRequest.invoke()
    }

    var sounds by remember {
        mutableStateOf(emptyList<Pair<String, Uri>>())
    }
    var currentlyPlayingRingtone: Ringtone? = remember { null }
    var currentlyPlayingUri: Uri? = remember { null }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            sounds = RingtoneHelper.getAvailableSounds(context).toList().sortedBy { it.first }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            currentlyPlayingRingtone?.stop()
            currentlyPlayingRingtone = null
            currentlyPlayingUri = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(android.R.string.cancel, onDismissRequest)
        },
        dismissButton = {
            DialogButton(com.bnyro.clock.R.string.custom_file) {
                pickSoundFile.launch(arrayOf("audio/*"))
            }
        },
        title = {
            Text(stringResource(com.bnyro.clock.R.string.sound))
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(400.dp, 500.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (sounds.isEmpty()) {
                    CircularProgressIndicator()
                } else {
                    sounds.forEach { (title, uri) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSelection.invoke(title, uri)
                                    onDismissRequest.invoke()
                                }
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(title)
                            Spacer(modifier = Modifier.weight(1f))
                            ClickableIcon(imageVector = Icons.Default.NotificationsActive) {
                                currentlyPlayingRingtone?.stop()

                                // stop the ringtone at the second click
                                if (currentlyPlayingUri == uri) {
                                    currentlyPlayingUri = null
                                    currentlyPlayingRingtone = null
                                    return@ClickableIcon
                                }

                                // start playing the new selected ringtone
                                currentlyPlayingRingtone = RingtoneManager.getRingtone(context, uri)
                                currentlyPlayingRingtone?.play()
                                currentlyPlayingUri = uri
                            }
                        }
                    }
                }
            }
        }
    )
}
