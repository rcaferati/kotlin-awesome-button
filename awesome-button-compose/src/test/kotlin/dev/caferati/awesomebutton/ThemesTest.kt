package dev.caferati.awesomebutton

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemesTest {
    @Test
    fun indexLookupFallsBackToBasic() {
        assertEquals(ThemeName.Basic, getTheme(-1).name)
        assertEquals(ThemeName.Basic, getTheme(999).name)
    }

    @Test
    fun nameLookupResolvesRegisteredTheme() {
        val theme = getTheme(ThemeName.Rick)

        assertEquals("Rick Theme", theme.title)
        assertEquals(ThemeName.Rick, theme.name)
        assertTrue(theme.prev)
        assertTrue(theme.next)
    }

    @Test
    fun registryEdgesExposeNavigationFlags() {
        val first = getTheme(0)
        val last = getTheme(ThemeName.Bruce)

        assertFalse(first.prev)
        assertTrue(first.next)
        assertTrue(last.prev)
        assertFalse(last.next)
    }

    @Test
    fun everyThemeExposesXSocialVariant() {
        ThemeName.entries.forEach { themeName ->
            val style = getTheme(themeName).buttons[ButtonVariant.X]

            assertEquals("Missing X style for $themeName", Color(0xFF171717), style?.backgroundColor)
            assertEquals("Wrong X depth for $themeName", Color(0xFF050505), style?.backgroundDarker)
        }
    }
}
