package dev.caferati.awesomebutton.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.caferati.awesomebutton.AwesomeButtonPressCallback
import dev.caferati.awesomebutton.AwesomeButtonStyle
import dev.caferati.awesomebutton.ButtonSize
import dev.caferati.awesomebutton.ButtonVariant
import dev.caferati.awesomebutton.ThemeDefinition
import dev.caferati.awesomebutton.ThemeName
import dev.caferati.awesomebutton.ThemedButton
import dev.caferati.awesomebutton.getTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun DemoContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .padding(top = 30.dp, bottom = 50.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            content()
        }
    }
}

@Composable
internal fun DemoSection(
    title: String,
    modifier: Modifier = Modifier,
    headerWidthFactor: Float = 0.6f,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(headerWidthFactor)
                .padding(vertical = 4.dp),
        ) {
            Text(
                text = title.uppercase(),
                color = Color(0xFF444444),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp,
            )
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth()
                    .background(Color(0x80444444))
                    .padding(top = 0.5.dp),
            )
        }
        Box(Modifier.padding(bottom = 8.dp))
        content()
    }
}

@Composable
internal fun SectionButton(content: @Composable () -> Unit) {
    Box(Modifier.padding(vertical = 8.dp)) {
        content()
    }
}

@Composable
internal fun DemoInlineAction(
    main: @Composable RowScope.() -> Unit,
    action: @Composable RowScope.() -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        main()
        action()
    }
}

@Composable
internal fun SizingVariantLabel(label: String) {
    Text(
        text = label.uppercase(),
        color = Color(0xFF6B7280),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.2.sp,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
    )
}

@Composable
internal fun DemoCaption(text: String) {
    Text(
        text = text,
        color = Color(0xFF5B6472),
        fontSize = 13.sp,
        lineHeight = 20.sp,
        modifier = Modifier.padding(bottom = 10.dp),
    )
}

@Composable
internal fun DemoThemedTextButton(
    child: String,
    name: ThemeName? = null,
    config: ThemeDefinition? = null,
    type: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    flat: Boolean = false,
    textTransition: Boolean = false,
    textTransitionSlotStaggerMillis: Int = 7,
    onPress: AwesomeButtonPressCallback? = null,
    disabled: Boolean = false,
    width: Dp? = null,
    height: Dp? = null,
    autoWidth: Boolean = false,
    stretch: Boolean = false,
    style: AwesomeButtonStyle? = null,
    activeOpacity: Float = 1f,
    debouncedPressTimeMillis: Long = 0,
    progress: Boolean = false,
    showProgressBar: Boolean = true,
    progressLoadingTimeMillis: Int = 3000,
    animateSize: Boolean = true,
    before: (@Composable RowScope.() -> Unit)? = null,
    after: (@Composable RowScope.() -> Unit)? = null,
    extra: (@Composable androidx.compose.foundation.layout.BoxScope.() -> Unit)? = null,
) {
    ThemedButton(
        child = child,
        name = name,
        config = config,
        type = type,
        size = size,
        flat = flat,
        textTransition = textTransition,
        textTransitionSlotStaggerMillis = textTransitionSlotStaggerMillis,
        onPress = onPress,
        disabled = disabled,
        width = width,
        height = height,
        autoWidth = autoWidth,
        stretch = stretch,
        style = style,
        activeOpacity = activeOpacity,
        debouncedPressTimeMillis = debouncedPressTimeMillis,
        progress = progress,
        showProgressBar = showProgressBar,
        progressLoadingTimeMillis = progressLoadingTimeMillis,
        animateSize = animateSize,
        before = before,
        after = after,
        extra = extra,
    )
}

@Composable
internal fun FlatIconButton(
    themeName: ThemeName,
    color: Color,
    imageVector: ImageVector = Icons.Filled.SwapHoriz,
    action: () -> Unit,
) {
    ThemedButton(
        name = themeName,
        type = ButtonVariant.Flat,
        size = ButtonSize.Icon,
        onPress = { action() },
        content = {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = color,
            )
        },
    )
}

internal fun delayedCompletion(
    scope: CoroutineScope,
    millis: Long,
): AwesomeButtonPressCallback =
    { next ->
        scope.launch {
            delay(millis)
            next?.invoke()
        }
    }

internal fun asyncLoadCompletion(
    scope: CoroutineScope,
    millis: Long,
): AwesomeButtonPressCallback =
    { next ->
        scope.launch {
            withContext(Dispatchers.Default) {
                performHeavyLoad(millis)
            }
            next?.invoke()
        }
    }

internal fun buttonTextColor(
    themeName: ThemeName,
    type: ButtonVariant,
): Color {
    val theme = getTheme(themeName)
    return theme.buttons[type]?.textColor ?: theme.color
}

@Composable
internal fun TextDemoButton(
    child: String,
    textTransition: Boolean = false,
    textTransitionSlotStaggerMillis: Int = 7,
    animateSize: Boolean = true,
) {
    DemoThemedTextButton(
        child = child,
        name = ThemeName.Bruce,
        type = ButtonVariant.Anchor,
        textTransition = textTransition,
        textTransitionSlotStaggerMillis = textTransitionSlotStaggerMillis,
        autoWidth = true,
        animateSize = animateSize,
    )
}

internal enum class SocialBrand(
    val drawableRes: Int,
) {
    Facebook(R.drawable.ic_brand_facebook),
    X(R.drawable.ic_brand_x),
    Messenger(R.drawable.ic_brand_messenger),
    Instagram(R.drawable.ic_brand_instagram),
    Whatsapp(R.drawable.ic_brand_whatsapp),
    Youtube(R.drawable.ic_brand_youtube),
    Linkedin(R.drawable.ic_brand_linkedin),
    Pinterest(R.drawable.ic_brand_pinterest),
}

@Composable
internal fun SocialBrandIcon(
    brand: SocialBrand,
    color: Color = Color.White,
    size: Dp = 21.dp,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(brand.drawableRes),
        contentDescription = null,
        tint = color,
        modifier = modifier.size(size),
    )
}

@Composable
internal fun SocialBrandIconPadded(
    brand: SocialBrand,
    color: Color = Color.White,
    size: Dp = 21.dp,
    trailingPadding: Dp = 0.dp,
) {
    SocialBrandIcon(
        brand = brand,
        color = color,
        size = size,
        modifier = Modifier.padding(end = trailingPadding),
    )
}

@Composable
internal fun InstagramGradient() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF4C63D2),
                        Color(0xFFBC3081),
                        Color(0xFFF47133),
                        Color(0xFFFED576),
                    ),
                ),
            ),
    )
}

@Composable
internal fun rememberDemoScope(): CoroutineScope = rememberCoroutineScope()

private fun performHeavyLoad(durationMillis: Long): Double {
    val startedAt = android.os.SystemClock.elapsedRealtime()
    var accumulator = 0.0
    var iteration = 1

    while (android.os.SystemClock.elapsedRealtime() - startedAt < durationMillis) {
        val operand = (iteration % 360) + 1
        accumulator += (operand * operand).toDouble() / ((iteration % 97) + 1)
        iteration += 1
    }

    return accumulator
}
