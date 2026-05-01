package dev.caferati.awesomebutton.demo

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.caferati.awesomebutton.AwesomeButtonStyle
import dev.caferati.awesomebutton.ButtonSize
import dev.caferati.awesomebutton.ButtonVariant
import dev.caferati.awesomebutton.ThemeName
import dev.caferati.awesomebutton.ThemedButton

@Composable
internal fun ProgressScreen(modifier: Modifier = Modifier) {
    val scope = rememberDemoScope()
    val themeName = ThemeName.Mysterion

    DemoContainer(modifier = modifier) {
        DemoSection("Labeled Buttons") {
            SectionButton {
                DemoThemedTextButton(
                    "Progress",
                    name = themeName,
                    type = ButtonVariant.Primary,
                    width = 200.dp,
                    progress = true,
                    onPress = delayedCompletion(scope, 1000),
                )
            }
            SectionButton {
                DemoThemedTextButton(
                    "Slower",
                    name = themeName,
                    type = ButtonVariant.Secondary,
                    width = 200.dp,
                    progress = true,
                    progressLoadingTimeMillis = 6000,
                    onPress = delayedCompletion(scope, 1000),
                )
            }
            SectionButton {
                DemoThemedTextButton(
                    "No Bar",
                    name = themeName,
                    type = ButtonVariant.Anchor,
                    width = 200.dp,
                    progress = true,
                    showProgressBar = false,
                    onPress = delayedCompletion(scope, 1000),
                )
            }
            SectionButton {
                DemoThemedTextButton(
                    "Flat Progress",
                    name = themeName,
                    type = ButtonVariant.Danger,
                    width = 200.dp,
                    style = AwesomeButtonStyle(borderRadius = 0.dp, raiseAmount = 0.dp),
                    progress = true,
                    onPress = delayedCompletion(scope, 1000),
                )
            }
            SectionButton {
                ThemedButton(
                    name = themeName,
                    type = ButtonVariant.Anchor,
                    size = ButtonSize.Icon,
                    progress = true,
                    style = AwesomeButtonStyle(borderRadius = 30.dp, raiseAmount = 6.dp),
                    onPress = delayedCompletion(scope, 1000),
                    content = {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = null,
                            tint = buttonTextColor(themeName, ButtonVariant.Anchor),
                            modifier = Modifier.size(24.dp),
                        )
                    },
                )
            }
            SectionButton {
                ThemedButton(
                    name = themeName,
                    type = ButtonVariant.Secondary,
                    size = ButtonSize.Icon,
                    progress = true,
                    style = AwesomeButtonStyle(borderRadius = 30.dp, raiseAmount = 0.dp),
                    onPress = delayedCompletion(scope, 1000),
                    content = {
                        Text(
                            text = "f",
                            color = buttonTextColor(themeName, ButtonVariant.Secondary),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                )
            }
        }
    }
}
