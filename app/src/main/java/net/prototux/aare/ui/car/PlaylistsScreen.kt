package net.prototux.aare.ui.car

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.prototux.aare.R
import net.prototux.aare.host.CarRuntime
import net.prototux.aare.playlists.PlayResult
import net.prototux.aare.playlists.PlaylistPlayer
import net.prototux.aare.playlists.Playlists

/**
 * The playlists the user pinned on the phone, as big buttons, above a small now-playing line with
 * previous / play-pause / next for whatever is playing. Tapping one asks its music app to play it
 * (see [PlaylistPlayer]). While driving the list is hidden, like the media browse screens: only
 * the transport buttons stay.
 */
@Composable
internal fun PlaylistsScreen(runtime: CarRuntime, driving: Boolean, modifier: Modifier) {
    val context = LocalContext.current
    val shortcuts = remember { Playlists.parse(runtime.settings.playlistsRaw) }
    val playback by runtime.media.playback.collectAsState()
    var status by remember { mutableStateOf<String?>(null) }

    Column(modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                val info = playback
                Text(info?.title.orEmpty(), style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(info?.artist.orEmpty(), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = { runtime.media.previous() }, modifier = Modifier.size(64.dp)) {
                Icon(Icons.Rounded.SkipPrevious, stringResource(R.string.media_action_previous), Modifier.size(36.dp))
            }
            IconButton(onClick = { runtime.media.playPause() }, modifier = Modifier.size(64.dp)) {
                Icon(if (playback?.playing == true) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    stringResource(R.string.media_action_play_pause), Modifier.size(40.dp))
            }
            IconButton(onClick = { runtime.media.next() }, modifier = Modifier.size(64.dp)) {
                Icon(Icons.Rounded.SkipNext, stringResource(R.string.media_action_next), Modifier.size(36.dp))
            }
            Spacer(Modifier.width(4.dp))
        }
        Spacer(Modifier.height(12.dp))

        val requested = stringResource(R.string.playlists_status_requested)
        val opened = stringResource(R.string.playlists_status_opened)
        val failed = stringResource(R.string.playlists_status_failed)
        status?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp))
        }

        if (driving) {
            Text(stringResource(R.string.playlists_paused_driving), style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 220.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(shortcuts) { p ->
                    FilledTonalButton(
                        onClick = {
                            status = when (PlaylistPlayer.play(context, p)) {
                                PlayResult.REQUESTED -> "$requested ${p.name}"
                                PlayResult.OPENED_APP -> "$opened ${p.name}"
                                PlayResult.FAILED -> "$failed ${p.name}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(88.dp),
                    ) {
                        Text(p.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
