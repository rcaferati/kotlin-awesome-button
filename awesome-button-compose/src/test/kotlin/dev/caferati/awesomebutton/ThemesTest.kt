package dev.caferati.awesomebutton

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    @Suppress("DEPRECATION")
    @Test
    fun twitterCompatibilityVariantResolvesToTheCanonicalXStyle() {
        ThemeName.entries.forEach { themeName ->
            val theme = getTheme(themeName)
            assertEquals(theme.buttons[ButtonVariant.X], theme.buttons[ButtonVariant.Twitter])
        }
    }

    @Test
    fun physicalThemeCornersRemainPhysicalInRtl() {
        val source =
            ThemeButtonStyle(
                borderTopLeftRadius = 1.dp,
                borderTopRightRadius = 2.dp,
                borderBottomLeftRadius = 3.dp,
                borderBottomRightRadius = 4.dp,
            )

        val rtl = source.toAwesomeButtonStyle(LayoutDirection.Rtl).cornerRadii!!

        assertEquals(2.dp, rtl.topStart)
        assertEquals(1.dp, rtl.topEnd)
        assertEquals(3.dp, rtl.bottomEnd)
        assertEquals(4.dp, rtl.bottomStart)
    }

    @Test
    fun progressTravelEntersFromTheLogicalLeadingEdge() {
        assertEquals(
            -75f,
            awesomeButtonProgressTranslationX(0.25f, 100f, LayoutDirection.Ltr),
        )
        assertEquals(
            75f,
            awesomeButtonProgressTranslationX(0.25f, 100f, LayoutDirection.Rtl),
        )
        assertEquals(
            0f,
            awesomeButtonProgressTranslationX(1f, 100f, LayoutDirection.Rtl),
        )
    }

    @Test
    fun flatStylingIsPreservedWhileDisabled() {
        val theme = getTheme(ThemeName.Basic)

        assertEquals(
            ButtonVariant.Flat,
            resolveButtonType(theme, disabled = true, flat = true, ButtonVariant.Danger),
        )
        assertEquals(
            ButtonVariant.Flat,
            resolveButtonType(theme, disabled = true, flat = false, ButtonVariant.Flat),
        )
        assertEquals(
            ButtonVariant.Disabled,
            resolveButtonType(theme, disabled = true, flat = false, ButtonVariant.Danger),
        )
        val disabledFlatStyle =
            theme.buttons.getValue(
                resolveButtonType(theme, disabled = true, flat = false, ButtonVariant.Flat),
            )
        assertEquals(0.dp, disabledFlatStyle.raiseLevel)
        assertEquals(Color.Transparent, disabledFlatStyle.backgroundDarker)
        assertEquals(Color.Transparent, disabledFlatStyle.backgroundShadow)
    }

    @Test
    fun canonicalDimensionPrecedenceAndInvalidInputsAreStable() {
        assertEquals(
            100.dp,
            resolveThemedWidth(false, 100.dp, true, 200.dp, 300.dp),
        )
        assertEquals(null, resolveThemedWidth(false, null, true, 200.dp, 300.dp))
        assertEquals(
            200.dp,
            resolveThemedWidth(false, null, false, 200.dp, 300.dp),
        )
        assertEquals(
            300.dp,
            resolveThemedWidth(false, null, false, null, 300.dp),
        )
        assertEquals(80.dp, resolveThemedHeight(80.dp, 60.dp, 44.dp))
        assertEquals(60.dp, resolveThemedHeight(null, 60.dp, 44.dp))
        assertEquals(44.dp, resolveThemedHeight(null, null, 44.dp))
        assertEquals(52.dp, resolveThemedHeight(null, null, null))
    }

    @Test
    fun variantStylePrecedesSizeAndInvalidExplicitValuesAreAbsent() {
        val resolved =
            resolveThemedRequestedStyle(
                buttonStyle = ThemeButtonStyle(textSize = 18.sp),
                sizeStyle = ThemeSizeStyle(textSize = 12.sp),
                explicitStyle =
                    AwesomeButtonStyle(
                        textSize = TextUnit(Float.NaN, TextUnitType.Sp),
                    ),
                layoutDirection = LayoutDirection.Ltr,
            )

        assertEquals(18.sp, resolved.textSize)
    }

    @Test
    fun themedTransitionDurationMatchesTheOwningChange() {
        assertEquals(
            200,
            resolveThemedStyleTransitionDurationMillis(
                sameThemeSource = true,
                sameTransparent = true,
                variantChanged = true,
                reduceMotion = false,
                styleDurationMillis = 173,
            ),
        )
        assertEquals(
            173,
            resolveThemedStyleTransitionDurationMillis(
                sameThemeSource = true,
                sameTransparent = true,
                variantChanged = false,
                reduceMotion = false,
                styleDurationMillis = 173,
            ),
        )
        assertEquals(
            0,
            resolveThemedStyleTransitionDurationMillis(
                sameThemeSource = false,
                sameTransparent = true,
                variantChanged = true,
                reduceMotion = false,
                styleDurationMillis = 173,
            ),
        )
    }

    @Test
    fun themedSourceIdentityKeepsBuiltInSelectionsStableAndCustomConfigsReferential() {
        assertTrue(
            isSameThemedSource(
                previousConfig = null,
                previousIndex = 0,
                previousName = null,
                config = null,
                index = 0,
                name = null,
            ),
        )
        assertFalse(
            isSameThemedSource(
                previousConfig = null,
                previousIndex = 0,
                previousName = null,
                config = null,
                index = 1,
                name = null,
            ),
        )

        val first = getTheme(0)
        val equalButReplaced = getTheme(0)
        assertFalse(
            isSameThemedSource(
                previousConfig = first,
                previousIndex = null,
                previousName = null,
                config = equalButReplaced,
                index = null,
                name = null,
            ),
        )
        assertTrue(
            isSameThemedSource(
                previousConfig = first,
                previousIndex = null,
                previousName = null,
                config = first,
                index = null,
                name = null,
            ),
        )
    }
}
