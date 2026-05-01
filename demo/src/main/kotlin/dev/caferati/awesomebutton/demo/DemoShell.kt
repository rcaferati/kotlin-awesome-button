package dev.caferati.awesomebutton.demo

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.caferati.awesomebutton.AwesomeButtonStyle
import dev.caferati.awesomebutton.ButtonSize
import dev.caferati.awesomebutton.ButtonVariant
import dev.caferati.awesomebutton.RegisteredThemeDefinition
import dev.caferati.awesomebutton.ThemedButton
import dev.caferati.awesomebutton.getTheme
import kotlinx.coroutines.delay

private enum class DemoTab(
    val label: String,
    val icon: ImageVector,
) {
    Themed("Themed", Icons.Filled.Home),
    Progress("Progress", Icons.Filled.Refresh),
    Social("Social", Icons.Filled.Share),
    SizeChanges("Size Changes", Icons.Filled.Settings),
}

private enum class ThemedHeaderDirection {
    Forward,
    Backward,
}

private const val ThemedStackTransitionDurationMillis = 240
private const val ThemedStackBehindOffsetDivisor = 4
private val SecondaryHeaderColor = Color(0xFF4F6FC4)

@Composable
internal fun DemoShell() {
    var selectedTab by rememberSaveable { mutableStateOf(DemoTab.Themed) }
    var themeStack by rememberSaveable { mutableStateOf(listOf(0)) }
    var direction by rememberSaveable { mutableStateOf(ThemedHeaderDirection.Forward) }
    var themeTransitionRunning by remember { mutableStateOf(false) }

    val currentThemeIndex = themeStack.lastOrNull() ?: 0
    val currentTheme = getTheme(currentThemeIndex)

    fun pushNextTheme() {
        if (themeTransitionRunning || !currentTheme.next) return
        direction = ThemedHeaderDirection.Forward
        themeTransitionRunning = true
        themeStack = themeStack + (currentThemeIndex + 1)
    }

    fun popTheme() {
        if (themeTransitionRunning || themeStack.size <= 1) return
        direction = ThemedHeaderDirection.Backward
        themeTransitionRunning = true
        themeStack = themeStack.dropLast(1)
    }

    LaunchedEffect(currentThemeIndex, themeTransitionRunning) {
        if (themeTransitionRunning) {
            delay(ThemedStackTransitionDurationMillis.toLong())
            themeTransitionRunning = false
        }
    }

    BackHandler(enabled = selectedTab == DemoTab.Themed && themeStack.size > 1 && !themeTransitionRunning) {
        popTheme()
    }

    Surface(color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {
            when (selectedTab) {
                DemoTab.Themed -> ThemedHeaderBar(
                    theme = currentTheme,
                    themeIndex = currentThemeIndex,
                    direction = direction,
                    transitionRunning = themeTransitionRunning,
                    onPrev = ::popTheme,
                    onNext = ::pushNextTheme,
                )
                DemoTab.Progress -> StaticHeaderBar("Progress Buttons")
                DemoTab.Social -> StaticHeaderBar("Social Buttons")
                DemoTab.SizeChanges -> StaticHeaderBar("Size Changes")
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when (selectedTab) {
                    DemoTab.Themed -> ThemedStackHost(
                        themeIndex = currentThemeIndex,
                        direction = direction,
                        modifier = Modifier.fillMaxSize(),
                    )
                    DemoTab.Progress -> ProgressScreen(Modifier.fillMaxSize())
                    DemoTab.Social -> SocialScreen(Modifier.fillMaxSize())
                    DemoTab.SizeChanges -> SizeChangesScreen(Modifier.fillMaxSize())
                }
            }

            NavigationBar {
                DemoTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemedStackHost(
    themeIndex: Int,
    direction: ThemedHeaderDirection,
    modifier: Modifier = Modifier,
) {
    val stateHolder = rememberSaveableStateHolder()

    AnimatedContent(
        targetState = themeIndex,
        transitionSpec = { themedStackContentTransform(direction) },
        label = "themed-stack-host",
        modifier = modifier.background(Color.White),
    ) { targetThemeIndex ->
        stateHolder.SaveableStateProvider("theme-$targetThemeIndex") {
            ThemedButtonsScreen(
                index = targetThemeIndex,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
            )
        }
    }
}

@Composable
private fun StaticHeaderBar(title: String) {
    val density = LocalDensity.current
    val topInset = with(density) { WindowInsets.statusBars.getTop(density).toDp() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SecondaryHeaderColor),
    ) {
        Spacer(Modifier.height(topInset))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ThemedHeaderBar(
    theme: RegisteredThemeDefinition,
    themeIndex: Int,
    direction: ThemedHeaderDirection,
    transitionRunning: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = theme.background,
        animationSpec = tween(durationMillis = 240),
        label = "themed-header-background",
    )
    val foregroundColor by animateColorAsState(
        targetValue = theme.color,
        animationSpec = tween(durationMillis = 240),
        label = "themed-header-foreground",
    )
    val density = LocalDensity.current
    val topInset = with(density) { WindowInsets.statusBars.getTop(density).toDp() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor),
    ) {
        Spacer(Modifier.height(topInset))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            AnimatedContent(
                targetState = themeIndex,
                transitionSpec = { themedHeaderTitleTransform(direction) },
                label = "themed-header-title",
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 96.dp),
            ) { targetThemeIndex ->
                Text(
                    text = getTheme(targetThemeIndex).title,
                    color = foregroundColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeaderButtonSlot {
                    ThemeHeaderButton(
                        label = "Prev",
                        theme = theme,
                        foregroundColor = foregroundColor,
                        disabled = transitionRunning || !theme.prev,
                        onPress = onPrev,
                    )
                }

                Spacer(Modifier.fillMaxHeight())

                HeaderButtonSlot {
                    ThemeHeaderButton(
                        label = "Next",
                        theme = theme,
                        foregroundColor = foregroundColor,
                        disabled = transitionRunning || !theme.next,
                        onPress = onNext,
                    )
                }
            }
        }
    }
}

private fun themedStackContentTransform(direction: ThemedHeaderDirection): ContentTransform {
    val animationSpec = tween<IntOffset>(durationMillis = ThemedStackTransitionDurationMillis)
    val transform =
        when (direction) {
            ThemedHeaderDirection.Forward ->
                slideInHorizontally(animationSpec = animationSpec) { width -> width } +
                    fadeIn(animationSpec = tween(ThemedStackTransitionDurationMillis), initialAlpha = 0.98f) togetherWith
                    slideOutHorizontally(animationSpec = animationSpec) { width -> -width / ThemedStackBehindOffsetDivisor } +
                    fadeOut(animationSpec = tween(ThemedStackTransitionDurationMillis), targetAlpha = 0.92f)
            ThemedHeaderDirection.Backward ->
                slideInHorizontally(animationSpec = animationSpec) { width -> -width / ThemedStackBehindOffsetDivisor } +
                    fadeIn(animationSpec = tween(ThemedStackTransitionDurationMillis), initialAlpha = 0.92f) togetherWith
                    slideOutHorizontally(animationSpec = animationSpec) { width -> width } +
                    fadeOut(animationSpec = tween(ThemedStackTransitionDurationMillis), targetAlpha = 0.98f)
        }

    return transform.apply {
        targetContentZIndex = if (direction == ThemedHeaderDirection.Forward) 1f else -1f
    }
}

private fun themedHeaderTitleTransform(direction: ThemedHeaderDirection): ContentTransform {
    val animationSpec = tween<IntOffset>(durationMillis = ThemedStackTransitionDurationMillis)
    val titlePushDistance = 40
    return when (direction) {
        ThemedHeaderDirection.Forward ->
            slideInHorizontally(animationSpec = animationSpec) { titlePushDistance } +
                fadeIn(animationSpec = tween(ThemedStackTransitionDurationMillis)) togetherWith
                slideOutHorizontally(animationSpec = animationSpec) { -titlePushDistance } +
                fadeOut(animationSpec = tween(ThemedStackTransitionDurationMillis))
        ThemedHeaderDirection.Backward ->
            slideInHorizontally(animationSpec = animationSpec) { -titlePushDistance } +
                fadeIn(animationSpec = tween(ThemedStackTransitionDurationMillis)) togetherWith
                slideOutHorizontally(animationSpec = animationSpec) { titlePushDistance } +
                fadeOut(animationSpec = tween(ThemedStackTransitionDurationMillis))
    }
}

@Composable
private fun HeaderButtonSlot(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.width(88.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun ThemeHeaderButton(
    label: String,
    theme: RegisteredThemeDefinition,
    foregroundColor: Color,
    disabled: Boolean,
    onPress: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val visualForegroundColor = if (disabled) {
        foregroundColor.copy(alpha = 0.45f)
    } else {
        foregroundColor
    }

    Box {
        ThemedButton(
            child = label,
            config = theme,
            type = ButtonVariant.Flat,
            size = ButtonSize.Small,
            width = 80.dp,
            transparent = true,
            activeOpacity = 0.6f,
            debouncedPressTimeMillis = 875,
            style = AwesomeButtonStyle(
                raiseAmount = 0.dp,
                backgroundActive = if (disabled) Color.Transparent else Color.Black.copy(alpha = 0.05f),
                foregroundColor = visualForegroundColor,
            ),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) {
                    if (!disabled) onPress()
                },
        )
    }
}
