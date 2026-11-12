package net.prototux.aare.playlists

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.util.Log
import net.prototux.aare.media.SessionTracker

/** What [PlaylistPlayer.play] managed to do. None of these proves that music is playing. */
enum class PlayResult {
    /** The app's own media session accepted a play-from-URI request. */
    REQUESTED,

    /** The app was asked to open the link (it may need a tap on the phone to start playing). */
    OPENED_APP,

    /** Nothing could be sent (app not installed, link refused). */
    FAILED,
}

/**
 * Starts a pinned playlist, with what a normal app can do without the music app's cooperation:
 *
 * 1. If the owning app already has a media session that advertises play-from-URI, ask it to play.
 * 2. Otherwise open the link in that app (an ordinary view intent). Android may refuse to start an
 *    activity from the background, and a music app may open the page without playing it.
 *
 * Neither path is verified on a device.
 */
object PlaylistPlayer {
    private const val TAG = "AarePlaylists"

    fun play(context: Context, shortcut: PlaylistShortcut): PlayResult {
        val uri = Playlists.normalize(shortcut.uri)
        val pkg = Playlists.packageFor(uri)
        if (pkg != null && playFromSession(context, pkg, uri)) return PlayResult.REQUESTED
        return if (open(context, uri, pkg)) PlayResult.OPENED_APP else PlayResult.FAILED
    }

    private fun playFromSession(context: Context, pkg: String, uri: String): Boolean = try {
        val manager = context.getSystemService(MediaSessionManager::class.java)
        val controller = manager.getActiveSessions(ComponentName(context, SessionTracker.LISTENER))
            .firstOrNull { it.packageName == pkg }
        val actions = controller?.playbackState?.actions ?: 0L
        if (controller != null && (actions and PlaybackState.ACTION_PLAY_FROM_URI) != 0L) {
            controller.transportControls.playFromUri(Uri.parse(uri), Bundle())
            true
        } else {
            false
        }
    } catch (e: SecurityException) {
        // No notification access: the system won't list sessions.
        false
    }

    private fun open(context: Context, uri: String, pkg: String?): Boolean = try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (pkg != null) intent.setPackage(pkg)
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        Log.w(TAG, "can't open $uri: ${e.message}")
        false
    }
}
