package net.prototux.aare.playlists

/** A playlist (or album, podcast…) the user pinned: a [name] and the [uri] that opens it in its app. */
data class PlaylistShortcut(val name: String, val uri: String)

/**
 * The pinned playlists, kept in one preference string, one `name<TAB>uri` per line. Pure logic,
 * no Android: see [PlaylistPlayer] for what happens when one is tapped.
 */
object Playlists {
    private const val SEP = '\t'

    private val SPOTIFY_WEB = Regex(
        """https?://open\.spotify\.com/(?:intl-[a-z]+/)?(playlist|album|artist|show|episode|track)/([A-Za-z0-9]+)(?:[?#].*)?""",
    )

    /** Reads the stored string back; lines without a name or without a link are dropped. */
    fun parse(raw: String): List<PlaylistShortcut> = raw.lineSequence().mapNotNull { line ->
        val i = line.indexOf(SEP)
        if (i <= 0) return@mapNotNull null
        val name = line.substring(0, i).trim()
        val uri = line.substring(i + 1).trim()
        if (name.isEmpty() || uri.isEmpty()) null else PlaylistShortcut(name, uri)
    }.toList()

    fun serialize(list: List<PlaylistShortcut>): String =
        list.joinToString("\n") { "${clean(it.name)}$SEP${clean(it.uri)}" }

    /** A tab or a line break in a field would break the one-line format. */
    private fun clean(s: String) = s.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ').trim()

    /**
     * A pasted Spotify web link (`https://open.spotify.com/playlist/ID?si=…`) as the `spotify:playlist:ID`
     * URI; anything else is returned trimmed and unchanged.
     */
    fun normalize(input: String): String {
        val s = input.trim()
        val m = SPOTIFY_WEB.matchEntire(s) ?: return s
        return "spotify:${m.groupValues[1]}:${m.groupValues[2]}"
    }

    /** The package of the app that owns [uri], when it is one we know; null otherwise. */
    fun packageFor(uri: String): String? = when {
        uri.startsWith("spotify:") || "open.spotify.com" in uri -> "com.spotify.music"
        uri.startsWith("deezer:") || "deezer.com" in uri || "deezer.page.link" in uri -> "deezer.android.app"
        else -> null
    }
}
