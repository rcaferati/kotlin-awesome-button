package dev.caferati.awesomebutton.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.caferati.awesomebutton.AwesomeButtonStyle
import dev.caferati.awesomebutton.ButtonSize
import dev.caferati.awesomebutton.ButtonVariant
import dev.caferati.awesomebutton.ThemeName

@Composable
internal fun SizeChangesScreen(modifier: Modifier = Modifier) {
    val themeSizes = remember {
        listOf(
            ButtonSize.Small,
            ButtonSize.Medium,
            ButtonSize.Large,
        )
    }
    var isLongLabel by rememberSaveable { mutableStateOf(false) }
    var sizeIndex by rememberSaveable { mutableIntStateOf(1) }

    val autoWidthLabel = if (isLongLabel) "View analytics dashboard" else "Launch"
    val currentThemeSize = themeSizes[sizeIndex]
    val currentThemeSizeLabel = currentThemeSize.name.lowercase().replaceFirstChar { it.uppercase() }

    DemoContainer(modifier = modifier) {
        DemoSection("Auto Width String Change") {
            DemoCaption("Evaluates how the button reacts when a plain string label switches between short and long content.")

            SectionButton {
                DemoThemedTextButton(
                    "Toggle Label Length",
                    name = ThemeName.Bruce,
                    type = ButtonVariant.Secondary,
                    size = ButtonSize.Small,
                    autoWidth = true,
                    style = AwesomeButtonStyle(raiseAmount = 0.dp),
                    activeOpacity = 0.6f,
                    onPress = { isLongLabel = !isLongLabel },
                )
            }

            SizingVariantLabel("Animated with text transition")
            SectionButton {
                TextDemoButton(autoWidthLabel, textTransition = true)
            }

            SizingVariantLabel("Animated without text transition")
            SectionButton {
                TextDemoButton(autoWidthLabel)
            }

            SizingVariantLabel("Instant opt-out")
            SectionButton {
                TextDemoButton(autoWidthLabel, animateSize = false)
            }
        }

        DemoSection("Themed Fixed Size Change") {
            DemoCaption("Evaluates how a themed button behaves when its built-in size preset changes between fixed widths.")

            SectionButton {
                DemoThemedTextButton(
                    "Cycle Theme Size",
                    name = ThemeName.Bruce,
                    type = ButtonVariant.Secondary,
                    size = ButtonSize.Small,
                    autoWidth = true,
                    style = AwesomeButtonStyle(raiseAmount = 0.dp),
                    activeOpacity = 0.6f,
                    onPress = { sizeIndex = (sizeIndex + 1) % themeSizes.size },
                )
            }

            SizingVariantLabel("Animated with text transition")
            SectionButton {
                DemoThemedTextButton(
                    currentThemeSizeLabel,
                    name = ThemeName.Bruce,
                    type = ButtonVariant.Danger,
                    size = currentThemeSize,
                    textTransition = true,
                )
            }

            SizingVariantLabel("Animated without text transition")
            SectionButton {
                DemoThemedTextButton(
                    currentThemeSizeLabel,
                    name = ThemeName.Bruce,
                    type = ButtonVariant.Danger,
                    size = currentThemeSize,
                )
            }

            SizingVariantLabel("Instant opt-out")
            SectionButton {
                DemoThemedTextButton(
                    currentThemeSizeLabel,
                    name = ThemeName.Bruce,
                    type = ButtonVariant.Danger,
                    size = currentThemeSize,
                    animateSize = false,
                )
            }
        }
    }
}
