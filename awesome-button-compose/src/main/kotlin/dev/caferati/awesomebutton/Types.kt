package dev.caferati.awesomebutton

/** Completion handle used by progress buttons. */
fun interface AwesomeButtonNext {
    fun complete(callback: (() -> Unit)?)

    operator fun invoke(callback: (() -> Unit)? = null) {
        complete(callback)
    }
}

/** Press handler used by [AwesomeButton] and [ThemedButton]. */
typealias AwesomeButtonPressCallback = (AwesomeButtonNext?) -> Unit

/** Built-in theme names supported by [ThemedButton] and [getTheme]. */
enum class ThemeName {
    Basic,
    Bojack,
    Cartman,
    Mysterion,
    C137,
    Rick,
    Summer,
    Bruce,
}

/** Built-in button variants supported by themed buttons. */
enum class ButtonVariant {
    Primary,
    Secondary,
    Anchor,
    Danger,
    Disabled,
    Flat,
    Twitter,
    Messenger,
    Facebook,
    Github,
    Linkedin,
    Whatsapp,
    Reddit,
    Pinterest,
    Youtube,
    X,
}

/** Built-in size presets used by [ThemedButton]. */
enum class ButtonSize {
    Icon,
    Small,
    Medium,
    Large,
}
