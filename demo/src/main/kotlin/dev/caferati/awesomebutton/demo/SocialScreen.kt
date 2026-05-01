package dev.caferati.awesomebutton.demo

import androidx.compose.foundation.layout.size
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
                    style = AwesomeButtonStyle(borderRadius = 50.dp, raiseAmount = 8.dp),
                    progress = true,
                    onPress = delayedCompletion(scope, 1000),
                    before = {
                        SocialBrandIconPadded(
                            SocialBrand.Facebook,
                            color = buttonTextColor(themeName, ButtonVariant.Facebook),
                            size = 24.dp,
                            trailingPadding = 8.dp,
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
                    style = AwesomeButtonStyle(borderRadius = 8.dp, raiseAmount = 8.dp),
                    progress = true,
                    onPress = delayedCompletion(scope, 1000),
                    before = {
                        SocialBrandIconPadded(
                            SocialBrand.Linkedin,
                            color = buttonTextColor(themeName, ButtonVariant.Linkedin),
                            size = 22.dp,
                            trailingPadding = 8.dp,
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
                    style = AwesomeButtonStyle(borderRadius = 0.dp, raiseAmount = 6.dp),
                    progress = true,
                    onPress = delayedCompletion(scope, 1000),
                    before = {
                        SocialBrandIconPadded(
                            SocialBrand.Messenger,
                            color = buttonTextColor(themeName, ButtonVariant.Messenger),
                            size = 22.dp,
                            trailingPadding = 8.dp,
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
                    ),
                    progress = true,
                    onPress = delayedCompletion(scope, 1000),
                    before = {
                        SocialBrandIconPadded(
                            SocialBrand.Instagram,
                            color = Color.White,
                            size = 22.dp,
                            trailingPadding = 8.dp,
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
                    content = {
                        SocialBrandIcon(
                            brand = SocialBrand.Whatsapp,
                            color = buttonTextColor(themeName, ButtonVariant.Whatsapp),
                            size = 23.dp,
                            modifier = Modifier.size(23.dp),
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
                    content = {
                        SocialBrandIcon(
                            brand = SocialBrand.Youtube,
                            color = buttonTextColor(themeName, ButtonVariant.Youtube),
                            size = 23.dp,
                            modifier = Modifier.size(23.dp),
                        )
                    },
                )
            }
            SectionButton {
                ThemedButton(
                    name = themeName,
                    type = ButtonVariant.X,
                    width = 60.dp,
                    style = AwesomeButtonStyle(borderRadius = 8.dp, raiseAmount = 8.dp),
                    progress = true,
                    onPress = delayedCompletion(scope, 1000),
                    content = {
                        SocialBrandIcon(
                            brand = SocialBrand.X,
                            color = buttonTextColor(themeName, ButtonVariant.X),
                            size = 23.dp,
                            modifier = Modifier.size(23.dp),
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
                    content = {
                        SocialBrandIcon(
                            brand = SocialBrand.Pinterest,
                            color = buttonTextColor(themeName, ButtonVariant.Pinterest),
                            size = 23.dp,
                            modifier = Modifier.size(23.dp),
                        )
                    },
                )
            }
        }
    }
}
