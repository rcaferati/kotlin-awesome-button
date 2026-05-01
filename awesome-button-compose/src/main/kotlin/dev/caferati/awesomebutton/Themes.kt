package dev.caferati.awesomebutton

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val defaultThemeName = ThemeName.Basic
private val themeOrder = listOf(
    ThemeName.Basic,
    ThemeName.Bojack,
    ThemeName.Cartman,
    ThemeName.Mysterion,
    ThemeName.C137,
    ThemeName.Rick,
    ThemeName.Summer,
    ThemeName.Bruce,
)

private fun flatStyle() = ThemeButtonStyle(
    backgroundColor = Color.Transparent,
    backgroundDarker = Color.Transparent,
    backgroundShadow = Color.Transparent,
    raiseLevel = 0.dp,
    borderRadius = 0.dp,
)

private fun socialTypes(common: ThemeButtonStyle): Map<ButtonVariant, ThemeButtonStyle> =
    mapOf(
        ButtonVariant.Twitter to common.merge(
            ThemeButtonStyle(backgroundColor = Color(0xFF00ACED), backgroundDarker = Color(0xFF0096CF)),
        ),
        ButtonVariant.X to common.merge(
            ThemeButtonStyle(backgroundColor = Color(0xFF171717), backgroundDarker = Color(0xFF050505)),
        ),
        ButtonVariant.Messenger to common.merge(
            ThemeButtonStyle(backgroundColor = Color(0xFF3186F6), backgroundDarker = Color(0xFF2566BC)),
        ),
        ButtonVariant.Facebook to common.merge(
            ThemeButtonStyle(backgroundColor = Color(0xFF4868AD), backgroundDarker = Color(0xFF325194)),
        ),
        ButtonVariant.Github to common.merge(
            ThemeButtonStyle(backgroundColor = Color(0xFF2C3036), backgroundDarker = Color(0xFF060708)),
        ),
        ButtonVariant.Linkedin to common.merge(
            ThemeButtonStyle(backgroundColor = Color(0xFF0077B5), backgroundDarker = Color(0xFF005885)),
        ),
        ButtonVariant.Whatsapp to common.merge(
            ThemeButtonStyle(backgroundColor = Color(0xFF25D366), backgroundDarker = Color(0xFF14A54B)),
        ),
        ButtonVariant.Reddit to common.merge(
            ThemeButtonStyle(backgroundColor = Color(0xFFFC461E), backgroundDarker = Color(0xFFD52802)),
        ),
        ButtonVariant.Pinterest to common.merge(
            ThemeButtonStyle(backgroundColor = Color(0xFFBD091C), backgroundDarker = Color(0xFF980313)),
        ),
        ButtonVariant.Youtube to common.merge(
            ThemeButtonStyle(backgroundColor = Color(0xFFCC181E), backgroundDarker = Color(0xFFAB0D12)),
        ),
    )

private fun standardSizes(
    icon: Int = 55,
    mediumHeight: Int = 55,
    largeHeight: Int = 60,
): Map<ButtonSize, ThemeSizeStyle> =
    mapOf(
        ButtonSize.Icon to ThemeSizeStyle(width = icon.dp, height = icon.dp, textSize = 12.sp, paddingHorizontal = 4.dp),
        ButtonSize.Small to ThemeSizeStyle(width = 120.dp, height = 42.dp, textSize = 12.sp),
        ButtonSize.Medium to ThemeSizeStyle(width = 200.dp, height = mediumHeight.dp),
        ButtonSize.Large to ThemeSizeStyle(width = 250.dp, height = largeHeight.dp, textSize = 16.sp),
    )

private val basicTheme: ThemeDefinition by lazy {
    val common = ThemeButtonStyle(borderRadius = 8.dp, height = 60.dp, raiseLevel = 12.dp)
    val primary = Color(0xFF4688C5)
    val anchor = Color(0xFF46C578)
    val danger = Color(0xFFB13A3A)

    ThemeDefinition(
        title = "Basic Theme",
        background = Color(0xFF1775C8),
        color = Color.White,
        buttons = mapOf(
            ButtonVariant.Primary to common.merge(
                ThemeButtonStyle(
                    backgroundColor = primary,
                    backgroundDarker = blendColors(-0.5, primary),
                    backgroundActive = blendColors(-0.3, primary),
                    backgroundProgress = blendColors(-0.65, primary),
                    textColor = Color.White,
                    activityColor = Color(0xFFB3E5E1),
                ),
            ),
            ButtonVariant.Secondary to common.merge(
                ThemeButtonStyle(
                    backgroundColor = Color.White,
                    backgroundDarker = blendColors(-0.1, primary),
                    backgroundActive = blendColors(0.85, primary),
                    backgroundProgress = Color(0xFFC8E3F5),
                    backgroundPlaceholder = Color(0xFF1E88E5),
                    textColor = Color(0xFF1E88E5),
                    borderWidth = 1.dp,
                    borderColor = Color(0xFF1E88E5),
                    activityColor = Color(0xFF1E88E5),
                ),
            ),
            ButtonVariant.Anchor to common.merge(
                ThemeButtonStyle(
                    backgroundColor = anchor,
                    backgroundDarker = blendColors(-0.5, anchor),
                    backgroundProgress = blendColors(-0.65, anchor),
                    textColor = Color.White,
                    activityColor = Color.White,
                ),
            ),
            ButtonVariant.Danger to common.merge(
                ThemeButtonStyle(
                    backgroundColor = danger,
                    backgroundDarker = blendColors(-0.5, danger),
                    backgroundProgress = blendColors(-0.65, danger),
                    textColor = Color.White,
                    activityColor = Color.White,
                ),
            ),
            ButtonVariant.Disabled to common.merge(disabledThemeStyle()),
            ButtonVariant.Flat to flatStyle(),
        ) + socialTypes(common),
        size = mapOf(
            ButtonSize.Icon to ThemeSizeStyle(width = 60.dp, height = 60.dp, textSize = 12.sp, paddingHorizontal = 4.dp),
            ButtonSize.Small to ThemeSizeStyle(width = 120.dp, height = 44.dp, textSize = 12.sp),
            ButtonSize.Medium to ThemeSizeStyle(width = 200.dp, height = 60.dp),
            ButtonSize.Large to ThemeSizeStyle(width = 250.dp, height = 60.dp, textSize = 16.sp),
        ),
    )
}

private val bojackTheme: ThemeDefinition by lazy {
    val blue = Color(0xFF6678C5)
    val grey = Color(0xFF848289)
    val pink = Color(0xFFEBA0BD)
    val teal = Color(0xFF3EB7B9)
    val common = ThemeButtonStyle(
        borderRadius = 4.dp,
        height = 55.dp,
        activityColor = Color.White,
        textColor = Color.White,
        raiseLevel = 6.dp,
        paddingHorizontal = 20.dp,
    )
    ThemeDefinition(
        title = "Bojack Theme",
        background = Color(0xFF4F6FC4),
        color = Color.White,
        buttons = mapOf(
            ButtonVariant.Primary to common.merge(
                ThemeButtonStyle(backgroundColor = blue, backgroundDarker = blendColors(-0.3, blue), backgroundProgress = Color(0xFF2A4284)),
            ),
            ButtonVariant.Secondary to common.merge(
                ThemeButtonStyle(backgroundColor = grey, backgroundDarker = blendColors(-0.3, grey), backgroundProgress = Color(0xFF3F3F3F)),
            ),
            ButtonVariant.Anchor to common.merge(
                ThemeButtonStyle(backgroundColor = teal, backgroundDarker = blendColors(-0.3, teal), backgroundProgress = blendColors(-0.6, teal)),
            ),
            ButtonVariant.Danger to common.merge(
                ThemeButtonStyle(backgroundColor = blendColors(-0.1, pink), backgroundDarker = blendColors(-0.3, pink), backgroundProgress = blendColors(-0.5, pink)),
            ),
            ButtonVariant.Disabled to common.merge(disabledThemeStyle()),
            ButtonVariant.Flat to flatStyle(),
        ) + socialTypes(common),
        size = standardSizes(),
    )
}

private val cartmanTheme: ThemeDefinition by lazy {
    val common = ThemeButtonStyle(borderRadius = 8.dp, height = 55.dp, activityColor = Color(0xFFFFE11D), raiseLevel = 8.dp)
    val blue = Color(0xFF00B8C4)
    val red = Color(0xFFDB4557)
    val yellow = Color(0xFFFDF353)
    val brown = Color(0xFF876753)
    val dark = Color(0xFF2D2D3A)
    ThemeDefinition(
        title = "Cartman Theme",
        background = Color(0xFFEE3253),
        color = yellow,
        buttons = mapOf(
            ButtonVariant.Primary to common.merge(
                ThemeButtonStyle(
                    backgroundColor = blue,
                    backgroundDarker = blendColors(-0.35, yellow),
                    textColor = yellow,
                    borderWidth = 2.dp,
                    borderColor = yellow,
                ),
            ),
            ButtonVariant.Secondary to common.merge(
                ThemeButtonStyle(
                    backgroundColor = red,
                    backgroundDarker = blendColors(-0.35, yellow),
                    textColor = yellow,
                    borderWidth = 2.dp,
                    borderColor = blendColors(-0.1, yellow),
                ),
            ),
            ButtonVariant.Anchor to common.merge(
                ThemeButtonStyle(
                    backgroundColor = dark,
                    backgroundDarker = blendColors(-0.3, brown),
                    textColor = blendColors(0.1, brown),
                    backgroundProgress = blendColors(0.025, dark),
                    borderWidth = 2.dp,
                    borderColor = brown,
                    activityColor = blendColors(0.1, brown),
                ),
            ),
            ButtonVariant.Danger to common.merge(
                ThemeButtonStyle(
                    backgroundColor = blendColors(-0.1, dark),
                    backgroundDarker = blendColors(-0.5, red),
                    backgroundProgress = blendColors(0.025, dark),
                    textColor = red,
                    borderColor = red,
                    borderWidth = 2.dp,
                    activityColor = blendColors(0.1, red),
                ),
            ),
            ButtonVariant.Disabled to common.merge(disabledThemeStyle()),
            ButtonVariant.Flat to flatStyle(),
        ) + socialTypes(common),
        size = standardSizes(),
    )
}

private val mysterionTheme: ThemeDefinition by lazy {
    val common = ThemeButtonStyle(borderRadius = 24.dp, height = 55.dp, activityColor = Color.White, raiseLevel = 8.dp)
    val primary = Color(0xFF463856)
    val secondary = Color(0xFF9A8C9D)
    val anchor = Color(0xFF749743)
    val anchorBorder = Color(0xFF678A37)
    val danger = Color(0xFFDB4557)
    val yellow = Color(0xFFFDF353)
    ThemeDefinition(
        title = "Mysterion Theme",
        background = primary,
        color = Color.White,
        buttons = mapOf(
            ButtonVariant.Primary to common.merge(
                ThemeButtonStyle(backgroundColor = primary, backgroundDarker = blendColors(-0.85, primary), textColor = Color.White, borderWidth = 1.dp, borderColor = primary),
            ),
            ButtonVariant.Secondary to common.merge(
                ThemeButtonStyle(backgroundColor = secondary, backgroundDarker = blendColors(-0.6218, secondary), textColor = Color.White, borderWidth = 1.dp, borderColor = secondary),
            ),
            ButtonVariant.Anchor to common.merge(
                ThemeButtonStyle(backgroundColor = anchor, backgroundDarker = blendColors(-0.6218, anchor), textColor = Color.White, borderWidth = 1.dp, borderColor = anchorBorder),
            ),
            ButtonVariant.Danger to common.merge(
                ThemeButtonStyle(backgroundColor = danger, backgroundDarker = blendColors(-0.5, danger), backgroundProgress = blendColors(-0.65, danger), textColor = yellow, activityColor = yellow),
            ),
            ButtonVariant.Disabled to common.merge(disabledThemeStyle()),
            ButtonVariant.Flat to flatStyle(),
        ) + socialTypes(common),
        size = standardSizes(),
    )
}

private val c137Theme: ThemeDefinition by lazy {
    val blue = Color(0xFF49536F)
    val yellow = Color(0xFFFEFC81)
    val green = Color(0xFF3DB64B)
    val skin = Color(0xFFECCAB1)
    val radioactive = Color(0xFFD2E054)
    val brown = Color(0xFF6D4B29)
    val common = ThemeButtonStyle(borderRadius = 25.dp, height = 55.dp, activityColor = Color(0xFFB3E5E1), raiseLevel = 6.dp)
    ThemeDefinition(
        title = "C-137 Theme",
        background = yellow,
        color = Color(0xFF535015),
        buttons = mapOf(
            ButtonVariant.Primary to common.merge(
                ThemeButtonStyle(backgroundColor = blue, backgroundDarker = blendColors(-0.3, blue), backgroundProgress = blendColors(-0.62, blue), textColor = blendColors(0.75, blue), activityColor = blendColors(0.75, blue)),
            ),
            ButtonVariant.Secondary to common.merge(
                ThemeButtonStyle(backgroundColor = yellow, backgroundDarker = blendColors(-0.3, yellow), backgroundProgress = blendColors(-0.62, yellow), textColor = blendColors(-0.9, yellow), activityColor = blendColors(-0.9, yellow)),
            ),
            ButtonVariant.Anchor to common.merge(
                ThemeButtonStyle(backgroundColor = skin, backgroundDarker = brown, backgroundProgress = blendColors(-0.5, skin), textColor = brown, activityColor = brown),
            ),
            ButtonVariant.Danger to common.merge(
                ThemeButtonStyle(backgroundColor = green, backgroundDarker = radioactive, backgroundProgress = blendColors(-0.62, green), textColor = radioactive, borderColor = radioactive, activityColor = radioactive),
            ),
            ButtonVariant.Disabled to common.merge(disabledThemeStyle()),
            ButtonVariant.Flat to flatStyle(),
        ) + socialTypes(common),
        size = standardSizes(),
    )
}

private val rickTheme: ThemeDefinition by lazy {
    val green = Color(0xFF3DB64B)
    val radioactive = Color(0xFFD2E054)
    val unityLight = Color(0xFF8B3357)
    val unityDark = Color(0xFF531849)
    val unityEyes = Color(0xFFE4E994)
    val common = ThemeButtonStyle(borderRadius = 25.dp, height = 55.dp, activityColor = Color.White, raiseLevel = 6.dp)
    ThemeDefinition(
        title = "Rick Theme",
        background = Color(0xFFAAD3EA),
        color = Color(0xFF2E84B1),
        buttons = mapOf(
            ButtonVariant.Primary to common.merge(
                ThemeButtonStyle(backgroundColor = Color(0xFFAAD3EA), backgroundDarker = Color(0xFF57A9D4), backgroundPlaceholder = Color(0xFF8DBDD9), textColor = Color(0xFF2E84B1), backgroundProgress = Color(0xFF57A9D4)),
            ),
            ButtonVariant.Secondary to common.merge(
                ThemeButtonStyle(backgroundColor = Color(0xFFFAFAFA), backgroundDarker = Color(0xFF67CBC3), backgroundActive = Color(0xFFE7FCFB), backgroundPlaceholder = Color(0xFFB3E5E1), textColor = Color(0xFF349890), backgroundProgress = Color(0xFFC5ECE8), borderWidth = 2.dp, borderColor = Color(0xFFB3E5E1), activityColor = Color(0xFF349890)),
            ),
            ButtonVariant.Anchor to common.merge(
                ThemeButtonStyle(backgroundColor = green, backgroundDarker = radioactive, backgroundProgress = blendColors(-0.62, green), textColor = radioactive, borderColor = radioactive, activityColor = radioactive, borderWidth = 2.dp),
            ),
            ButtonVariant.Danger to common.merge(
                ThemeButtonStyle(backgroundColor = unityLight, backgroundDarker = unityDark, backgroundProgress = unityDark, textColor = unityEyes, borderColor = unityDark, borderWidth = 2.dp, activityColor = unityEyes),
            ),
            ButtonVariant.Disabled to common.merge(
                ThemeButtonStyle(backgroundColor = Color(0xFFE8FCDA), backgroundDarker = Color(0xFFBDE1A2), textColor = Color(0xFFC7F2A9), borderWidth = 2.dp, borderColor = Color(0xFFC7E8AE)),
            ),
            ButtonVariant.Flat to flatStyle(),
        ) + socialTypes(common),
        size = standardSizes(),
    )
}

private val summerTheme: ThemeDefinition by lazy {
    val primary = Color(0xFFC77CB4)
    val secondary = Color(0xFFE6913E)
    val beth = Color(0xFFE36F5E)
    val common = ThemeButtonStyle(borderRadius = 24.dp, height = 55.dp, activityColor = Color.White, raiseLevel = 8.dp)
    ThemeDefinition(
        title = "Summer Theme",
        background = primary,
        color = Color.White,
        buttons = mapOf(
            ButtonVariant.Primary to common.merge(
                ThemeButtonStyle(backgroundColor = primary, backgroundDarker = blendColors(-0.38, primary), backgroundProgress = blendColors(-0.62, primary), textColor = Color.White, borderWidth = 0.dp, borderColor = primary),
            ),
            ButtonVariant.Secondary to common.merge(
                ThemeButtonStyle(backgroundColor = Color.White, backgroundDarker = blendColors(-0.6, primary), textColor = blendColors(-0.3, primary), borderWidth = 1.dp, borderColor = blendColors(-0.3, primary), activityColor = blendColors(-0.3, primary)),
            ),
            ButtonVariant.Anchor to common.merge(
                ThemeButtonStyle(backgroundColor = secondary, backgroundDarker = blendColors(-0.62, secondary), textColor = Color.White, borderWidth = 0.dp, borderColor = secondary),
            ),
            ButtonVariant.Danger to common.merge(
                ThemeButtonStyle(backgroundColor = beth, backgroundDarker = blendColors(-0.38, beth), backgroundProgress = blendColors(-0.62, beth), textColor = Color.White, borderWidth = 0.dp, borderColor = beth),
            ),
            ButtonVariant.Disabled to common.merge(disabledThemeStyle()),
            ButtonVariant.Flat to flatStyle(),
        ) + socialTypes(common),
        size = standardSizes(),
    )
}

private val bruceTheme: ThemeDefinition by lazy {
    val dark = Color(0xFF3A3A3A)
    val white = Color(0xFFFBFBFB)
    val purple = Color(0xFF733086)
    val green = Color(0xFF77CD38)
    val yellow = Color(0xFFFFE727)
    val common = ThemeButtonStyle(borderRadius = 8.dp, height = 62.dp, raiseLevel = 10.dp, borderWidth = 2.dp)
    ThemeDefinition(
        title = "Bruce Theme",
        background = Color(0xFF2F2F2F),
        color = Color.White,
        buttons = mapOf(
            ButtonVariant.Primary to common.merge(
                ThemeButtonStyle(backgroundColor = dark, backgroundDarker = blendColors(-0.38, dark), backgroundProgress = blendColors(-0.62, dark), borderColor = blendColors(-0.38, dark), textColor = white, activityColor = white),
            ),
            ButtonVariant.Secondary to common.merge(
                ThemeButtonStyle(backgroundColor = white, backgroundDarker = dark, backgroundProgress = blendColors(-0.38, white), backgroundPlaceholder = dark, textColor = dark, borderColor = blendColors(-0.38, dark), activityColor = dark),
            ),
            ButtonVariant.Anchor to common.merge(
                ThemeButtonStyle(backgroundColor = yellow, backgroundDarker = blendColors(-0.38, dark), backgroundProgress = Color(0xFF404040), textColor = blendColors(-0.38, dark), borderColor = dark, borderWidth = 2.dp, activityColor = dark),
            ),
            ButtonVariant.Danger to common.merge(
                ThemeButtonStyle(backgroundColor = purple, backgroundDarker = blendColors(-0.62, purple), backgroundProgress = blendColors(-0.62, purple), textColor = green, borderColor = blendColors(-0.62, purple), activityColor = green),
            ),
            ButtonVariant.Disabled to common.merge(
                ThemeButtonStyle(backgroundColor = blendColors(0.38, dark), backgroundDarker = blendColors(0.13, dark), textColor = blendColors(0.13, dark), borderColor = blendColors(0.13, dark)),
            ),
            ButtonVariant.Flat to flatStyle(),
        ) + socialTypes(common),
        size = standardSizes(icon = 60, mediumHeight = 60),
    )
}

private fun disabledThemeStyle() = ThemeButtonStyle(
    backgroundColor = Color(0xFFDFDFDF),
    backgroundDarker = Color(0xFFCACACA),
    textColor = Color(0xFFB6B6B6),
)

private val themes: Map<ThemeName, ThemeDefinition> by lazy {
    mapOf(
        ThemeName.Basic to basicTheme,
        ThemeName.Bojack to bojackTheme,
        ThemeName.Cartman to cartmanTheme,
        ThemeName.Mysterion to mysterionTheme,
        ThemeName.C137 to c137Theme,
        ThemeName.Rick to rickTheme,
        ThemeName.Summer to summerTheme,
        ThemeName.Bruce to bruceTheme,
    )
}

private fun registeredThemeAtIndex(index: Int): RegisteredThemeDefinition {
    val safeIndex = index.coerceIn(0, themeOrder.lastIndex)
    val themeName = themeOrder[safeIndex]
    val theme = themes[themeName] ?: themes[defaultThemeName]!!
    return RegisteredThemeDefinition(
        title = theme.title,
        background = theme.background,
        color = theme.color,
        buttons = theme.buttons,
        size = theme.size,
        name = themeName,
        next = safeIndex + 1 < themeOrder.size,
        prev = safeIndex - 1 >= 0,
    )
}

fun getTheme(index: Int = 0): RegisteredThemeDefinition =
    registeredThemeAtIndex(if (index in themeOrder.indices) index else 0)

fun getTheme(name: ThemeName): RegisteredThemeDefinition =
    registeredThemeAtIndex(themeOrder.indexOf(name).takeIf { it >= 0 } ?: 0)
