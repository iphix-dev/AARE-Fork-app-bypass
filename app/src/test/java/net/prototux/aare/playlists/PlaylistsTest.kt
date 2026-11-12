package net.prototux.aare.playlists

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaylistsTest {
    @Test fun roundTrip() {
        val list = listOf(PlaylistShortcut("Route", "spotify:playlist:abc123"), PlaylistShortcut("Chill", "deezer://www.deezer.com/playlist/42"))
        assertEquals(list, Playlists.parse(Playlists.serialize(list)))
    }

    @Test fun emptyStringIsEmptyList() = assertEquals(emptyList<PlaylistShortcut>(), Playlists.parse(""))

    @Test fun malformedLinesAreDropped() {
        val raw = "no separator\n\tonly uri\nonly name\t\nGood\tspotify:playlist:x"
        assertEquals(listOf(PlaylistShortcut("Good", "spotify:playlist:x")), Playlists.parse(raw))
    }

    @Test fun tabsAndNewlinesInFieldsCannotBreakTheFormat() {
        val parsed = Playlists.parse(Playlists.serialize(listOf(PlaylistShortcut("A\tB\nC", "spotify:playlist:x"))))
        assertEquals(listOf(PlaylistShortcut("A B C", "spotify:playlist:x")), parsed)
    }

    @Test fun spotifyWebLinkBecomesUri() =
        assertEquals("spotify:playlist:37i9dQZF1DXcBWIGoYBM5M",
            Playlists.normalize("https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M?si=abcdef"))

    @Test fun spotifyLinkWithLocalePrefixBecomesUri() =
        assertEquals("spotify:album:xyz", Playlists.normalize(" https://open.spotify.com/intl-fr/album/xyz "))

    @Test fun otherLinksAreKept() {
        assertEquals("spotify:playlist:x", Playlists.normalize("spotify:playlist:x"))
        assertEquals("deezer://www.deezer.com/playlist/42", Playlists.normalize("deezer://www.deezer.com/playlist/42"))
    }

    @Test fun packageIsGuessedFromTheLink() {
        assertEquals("com.spotify.music", Playlists.packageFor("spotify:playlist:x"))
        assertEquals("deezer.android.app", Playlists.packageFor("deezer://www.deezer.com/playlist/42"))
        assertNull(Playlists.packageFor("https://example.com/list"))
    }
}
