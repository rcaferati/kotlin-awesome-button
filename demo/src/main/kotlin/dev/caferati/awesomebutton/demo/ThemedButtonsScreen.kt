package dev.caferati.awesomebutton.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.caferati.awesomebutton.AwesomeButtonStyle
import dev.caferati.awesomebutton.ButtonSize
import dev.caferati.awesomebutton.ButtonVariant
import dev.caferati.awesomebutton.ThemeName
import dev.caferati.awesomebutton.ThemedButton
import dev.caferati.awesomebutton.getTheme

@Composable
internal fun ThemedButtonsScreen(
    index: Int,
    modifier: Modifier = Modifier,
) {
    val scope = rememberDemoScope()
    val transitionVariants = remember {
        listOf(
            ButtonVariant.Primary,
            ButtonVariant.Secondary,
            ButtonVariant.Anchor,
            ButtonVariant.Danger,
        )
    }
    val textTransitionLabels = remember { listOf("welcome", "Level 2", "Mission#42", "Go#3") }
    val sizeTransitionLabels = remember { listOf("Launch", "View analytics dashboard") }

    var transitionVariantIndex by rememberSaveable { mutableIntStateOf(0) }
    var textTransitionIndex by rememberSaveable { mutableIntStateOf(0) }
    var sizeTransitionIndex by rememberSaveable { mutableIntStateOf(0) }

    val theme = getTheme(index)
    val transitionVariant = transitionVariants[transitionVariantIndex]
    val primaryButtonColor = theme.buttons[ButtonVariant.Primary]?.backgroundColor ?: theme.color
    val sectionHeaderWidthFactor = if (theme.name == ThemeName.Basic) 0.6f else 0.5f

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        ThemeCharacterOverlay(theme.name)

        DemoContainer {
            DemoSection("Common", headerWidthFactor = sectionHeaderWidthFactor) {
                SectionButton { DemoThemedTextButton("Primary", config = theme, type = ButtonVariant.Primary) }
                SectionButton { DemoThemedTextButton("Secondary", config = theme, type = ButtonVariant.Secondary) }
                SectionButton { DemoThemedTextButton("Anchor", config = theme, type = ButtonVariant.Anchor) }
                SectionButton { DemoThemedTextButton("Danger", config = theme, type = ButtonVariant.Danger) }
                SectionButton { DemoThemedTextButton("Disabled", config = theme, type = ButtonVariant.Primary, disabled = true) }
            }

            DemoSection("Progress", headerWidthFactor = sectionHeaderWidthFactor) {
                SectionButton { DemoThemedTextButton("Primary", config = theme, type = ButtonVariant.Primary, progress = true, onPress = delayedCompletion(scope, 500)) }
                SectionButton { DemoThemedTextButton("Secondary", config = theme, type = ButtonVariant.Secondary, progress = true, onPress = delayedCompletion(scope, 500)) }
                SectionButton { DemoThemedTextButton("Anchor", config = theme, type = ButtonVariant.Anchor, progress = true, onPress = delayedCompletion(scope, 500)) }
                SectionButton { DemoThemedTextButton("Danger", config = theme, type = ButtonVariant.Danger, progress = true, onPress = delayedCompletion(scope, 500)) }
            }

            DemoSection("Variant Transition", headerWidthFactor = sectionHeaderWidthFactor) {
                SectionButton {
                    DemoInlineAction(
                        main = {
                            DemoThemedTextButton(
                                child = transitionVariant.demoLabel(),
                                name = theme.name,
                                type = transitionVariant,
                            )
                        },
                        action = {
                            FlatIconButton(
                                themeName = theme.name,
                                color = primaryButtonColor,
                                asset = DemoIconAsset.RightLeft,
                                accessibilityLabel = "Cycle variant",
                            ) {
                                transitionVariantIndex = (transitionVariantIndex + 1) % transitionVariants.size
                            }
                        },
                    )
                }
            }

            DemoSection("Text Transition", headerWidthFactor = sectionHeaderWidthFactor) {
                SectionButton {
                    DemoInlineAction(
                        main = {
                            DemoThemedTextButton(
                                child = textTransitionLabels[textTransitionIndex],
                                config = theme,
                                type = ButtonVariant.Primary,
                                textTransition = true,
                            )
                        },
                        action = {
                            FlatIconButton(
                                themeName = theme.name,
                                color = primaryButtonColor,
                                asset = DemoIconAsset.ForwardStep,
                                accessibilityLabel = "Cycle text",
                            ) {
                                textTransitionIndex = (textTransitionIndex + 1) % textTransitionLabels.size
                            }
                        },
                    )
                }
            }

            DemoSection("Size Transition", headerWidthFactor = sectionHeaderWidthFactor) {
                SectionButton {
                    DemoInlineAction(
                        main = {
                            DemoThemedTextButton(
                                child = sizeTransitionLabels[sizeTransitionIndex],
                                config = theme,
                                type = ButtonVariant.Primary,
                                autoWidth = true,
                                textTransition = true,
                            )
                        },
                        action = {
                            FlatIconButton(
                                themeName = theme.name,
                                color = primaryButtonColor,
                                asset = DemoIconAsset.ForwardStep,
                                accessibilityLabel = "Cycle size",
                            ) {
                                sizeTransitionIndex = (sizeTransitionIndex + 1) % sizeTransitionLabels.size
                            }
                        },
                    )
                }
            }

            DemoSection("Empty Placeholder", headerWidthFactor = sectionHeaderWidthFactor) {
                SectionButton { ThemedButton(config = theme, type = ButtonVariant.Primary) }
                SectionButton { ThemedButton(config = theme, type = ButtonVariant.Secondary) }
                SectionButton { ThemedButton(config = theme, type = ButtonVariant.Anchor) }
                SectionButton { ThemedButton(config = theme, type = ButtonVariant.Danger) }
            }

            DemoSection("Flat Buttons", headerWidthFactor = sectionHeaderWidthFactor) {
                SectionButton { DemoThemedTextButton("Primary", config = theme, type = ButtonVariant.Primary, style = AwesomeButtonStyle(raiseAmount = 0.dp), activeOpacity = 0.75f) }
                SectionButton { DemoThemedTextButton("Secondary", config = theme, type = ButtonVariant.Secondary, style = AwesomeButtonStyle(raiseAmount = 0.dp), activeOpacity = 0.75f) }
                SectionButton { DemoThemedTextButton("Anchor", config = theme, type = ButtonVariant.Anchor, style = AwesomeButtonStyle(raiseAmount = 0.dp), activeOpacity = 0.75f) }
                SectionButton { DemoThemedTextButton("Danger", config = theme, type = ButtonVariant.Danger, style = AwesomeButtonStyle(raiseAmount = 0.dp), activeOpacity = 0.75f, progress = true, onPress = delayedCompletion(scope, 500)) }
            }

            DemoSection("Before / After / Icon", headerWidthFactor = sectionHeaderWidthFactor) {
                SectionButton {
                    DemoThemedTextButton(
                        "Button Icon",
                        config = theme,
                        type = ButtonVariant.Primary,
                        style = AwesomeButtonStyle(contentGap = 8.dp),
                        before = {
                            DemoIcon(
                                asset = DemoIconAsset.Bars,
                                tint = buttonTextColor(theme.name, ButtonVariant.Primary),
                                size = 24.dp,
                            )
                        },
                    )
                }
                SectionButton {
                    DemoThemedTextButton(
                        "Button Icon",
                        config = theme,
                        type = ButtonVariant.Anchor,
                        style = AwesomeButtonStyle(contentGap = 8.dp),
                        after = {
                            DemoIcon(
                                asset = DemoIconAsset.TableCellsLarge,
                                tint = buttonTextColor(theme.name, ButtonVariant.Anchor),
                                size = 24.dp,
                            )
                        },
                    )
                }
                SectionButton {
                    DemoThemedTextButton(
                        "Button Icon",
                        config = theme,
                        type = ButtonVariant.Danger,
                        progress = true,
                        onPress = delayedCompletion(scope, 500),
                        style = AwesomeButtonStyle(contentGap = 8.dp),
                        before = {
                            DemoIcon(
                                asset = DemoIconAsset.TrashCan,
                                tint = buttonTextColor(theme.name, ButtonVariant.Danger),
                                size = 24.dp,
                            )
                        },
                    )
                }
                SectionButton {
                    ThemedButton(
                        config = theme,
                        type = ButtonVariant.Primary,
                        size = ButtonSize.Icon,
                        accessibilityLabel = "Add",
                        content = {
                            DemoIcon(
                                asset = DemoIconAsset.SquarePlus,
                                tint = buttonTextColor(theme.name, ButtonVariant.Primary),
                                size = 24.dp,
                            )
                        },
                    )
                }
                SectionButton {
                    ThemedButton(
                        config = theme,
                        type = ButtonVariant.Anchor,
                        size = ButtonSize.Icon,
                        accessibilityLabel = "Add user",
                        content = {
                            DemoIcon(
                                asset = DemoIconAsset.UserPlus,
                                tint = buttonTextColor(theme.name, ButtonVariant.Anchor),
                                size = 24.dp,
                            )
                        },
                    )
                }
                SectionButton {
                    ThemedButton(
                        config = theme,
                        type = ButtonVariant.Danger,
                        size = ButtonSize.Icon,
                        progress = true,
                        onPress = delayedCompletion(scope, 500),
                        accessibilityLabel = "Delete",
                        content = {
                            DemoIcon(
                                asset = DemoIconAsset.TrashCan,
                                tint = buttonTextColor(theme.name, ButtonVariant.Danger),
                                size = 24.dp,
                            )
                        },
                    )
                }
            }

            DemoSection("With auto and stretch", headerWidthFactor = sectionHeaderWidthFactor) {
                SectionButton { DemoThemedTextButton("Primary Auto", config = theme, type = ButtonVariant.Primary, autoWidth = true) }
                SectionButton { DemoThemedTextButton("Secondary Small Auto", config = theme, type = ButtonVariant.Secondary, size = ButtonSize.Small, autoWidth = true) }
                SectionButton { DemoThemedTextButton("Anchor Large Auto", config = theme, type = ButtonVariant.Anchor, size = ButtonSize.Large, autoWidth = true) }
                SectionButton { DemoThemedTextButton("Primary Large Stretch", config = theme, type = ButtonVariant.Danger, size = ButtonSize.Large, stretch = true) }
                SectionButton {
                    DemoThemedTextButton(
                        "Stretch Progress + Async Task",
                        config = theme,
                        type = ButtonVariant.Primary,
                        size = ButtonSize.Large,
                        stretch = true,
                        progress = true,
                        onPress = asyncLoadCompletion(scope, 900),
                    )
                }
                Text(
                    text = "This demo keeps the long-running progress example app-local, so the package remains focused on the button itself.",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

private fun ButtonVariant.demoLabel(): String =
    name.lowercase().replaceFirstChar { it.uppercase() }
