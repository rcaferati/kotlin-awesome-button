package dev.caferati.awesomebutton.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.caferati.awesomebutton.AwesomeButtonStyle
import dev.caferati.awesomebutton.ButtonSize
import dev.caferati.awesomebutton.ButtonVariant
import dev.caferati.awesomebutton.ThemeName
import dev.caferati.awesomebutton.ThemedButton

@Composable
internal fun SocialScreen(modifier: Modifier = Modifier) {
    val scope = rememberDemoScope()
    val themeName = ThemeName.Bojack

    DemoContainer(modifier = modifier) {
        DemoSection("Labeled Buttons") {
            SectionButton {
                DemoThemedTextButton(
                    "Facebook",
                    name = themeName,
                    type = ButtonVariant.Facebook,
                    width = 180.dp,
                    style = AwesomeButtonStyle(
                        borderRadius = 50.dp,
                        raiseAmount = 8.dp,
                        contentGap = 8.dp,
                    ),
                    progress = true,
                    onPress = delayedCompletion(scope, 1000),
                    before = {
                        DemoIcon(
                            asset = DemoIconAsset.Facebook,
                            tint = buttonTextColor(themeName, ButtonVariant.Facebook),
                            size = 24.dp,
                        )
                    },
                )
            }
            SectionButton {
                DemoThemedTextButton(
                    "LinkedIn",
                    name = themeName,
                    type = ButtonVariant.Linkedin,
                    width = 180.dp,
                    style = AwesomeButtonStyle(
                        borderRadius = 8.dp,
                        raiseAmount = 8.dp,
                        contentGap = 8.dp,
                    ),
                    progress = true,
                    onPress = delayedCompletion(scope, 1000),
                    before = {
                        DemoIcon(
                            asset = DemoIconAsset.Linkedin,
                            tint = buttonTextColor(themeName, ButtonVariant.Linkedin),
                            size = 24.dp,
                        )
                    },
                )
            }
            SectionButton {
                DemoThemedTextButton(
                    "Messenger",
                    name = themeName,
                    type = ButtonVariant.Messenger,
                    width = 180.dp,
                    style = AwesomeButtonStyle(
                        borderRadius = 0.dp,
                        raiseAmount = 6.dp,
                        contentGap = 8.dp,
                    ),
                    progress = true,
                    onPress = delayedCompletion(scope, 1000),
                    before = {
                        DemoIcon(
                            asset = DemoIconAsset.Messenger,
                            tint = buttonTextColor(themeName, ButtonVariant.Messenger),
                            size = 24.dp,
                        )
                    },
                )
            }
            SectionButton {
                DemoThemedTextButton(
                    "Instagram",
                    name = themeName,
                    width = 180.dp,
                    style = AwesomeButtonStyle(
                        backgroundActive = Color.Black.copy(alpha = 0.15f),
                        backgroundProgress = Color.Black.copy(alpha = 0.15f),
                        depthColor = Color(0xFFEAAC1E),
                        shadowColor = Color.Black.copy(alpha = 0.15f),
                        contentGap = 8.dp,
                    ),
                    progress = true,
                    onPress = delayedCompletion(scope, 1000),
                    before = {
                        DemoIcon(
                            asset = DemoIconAsset.Instagram,
                            tint = Color.White,
                            size = 24.dp,
                        )
                    },
                    extra = {
                        InstagramGradient()
                    },
                )
            }
        }

        DemoSection("Iconed Buttons") {
            SectionButton {
                ThemedButton(
                    name = themeName,
                    type = ButtonVariant.Whatsapp,
                    width = 60.dp,
                    style = AwesomeButtonStyle(borderRadius = 0.dp, raiseAmount = 0.dp),
                    progress = true,
                    onPress = delayedCompletion(scope, 1000),
                    accessibilityLabel = "WhatsApp",
                    content = {
                        DemoIcon(
                            asset = DemoIconAsset.Whatsapp,
                            tint = buttonTextColor(themeName, ButtonVariant.Whatsapp),
                            size = 24.dp,
                        )
                    },
                )
            }
            SectionButton {
                ThemedButton(
                    name = themeName,
                    type = ButtonVariant.Youtube,
                    width = 60.dp,
                    style = AwesomeButtonStyle(borderRadius = 0.dp, raiseAmount = 8.dp),
                    progress = true,
                    onPress = delayedCompletion(scope, 1000),
                    accessibilityLabel = "YouTube",
                    content = {
                        DemoIcon(
                            asset = DemoIconAsset.Youtube,
                            tint = buttonTextColor(themeName, ButtonVariant.Youtube),
                            size = 24.dp,
                        )
                    },
                )
            }
            SectionButton {
                ThemedButton(
                    name = themeName,
                    type = ButtonVariant.X,
                    width = 60.dp,
                    pressInAnimationDurationMillis = 140,
                    accessibilityLabel = "X",
                    style = AwesomeButtonStyle(borderRadius = 8.dp, raiseAmount = 8.dp),
                    progress = true,
                    onPress = delayedCompletion(scope, 1000),
                    content = {
                        DemoIcon(
                            asset = DemoIconAsset.X,
                            tint = buttonTextColor(themeName, ButtonVariant.X),
                            size = 24.dp,
                        )
                    },
                )
            }
            SectionButton {
                ThemedButton(
                    name = themeName,
                    type = ButtonVariant.Pinterest,
                    width = 60.dp,
                    height = 60.dp,
                    style = AwesomeButtonStyle(borderRadius = 80.dp, raiseAmount = 8.dp),
                    progress = true,
                    onPress = delayedCompletion(scope, 1000),
                    accessibilityLabel = "Pinterest",
                    content = {
                        DemoIcon(
                            asset = DemoIconAsset.Pinterest,
                            tint = buttonTextColor(themeName, ButtonVariant.Pinterest),
                            size = 24.dp,
                        )
                    },
                )
            }
        }
    }
}
