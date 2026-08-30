package dev.caferati.awesomebutton

/** One-shot completion handle supplied to an accepted progress-button activation. */
public fun interface AwesomeButtonNext {
    /** Claims progress completion and snapshots [callback] for the completing transition. */
    public fun complete(callback: (() -> Unit)?)

    /** Convenience syntax equivalent to [complete]. */
    public operator fun invoke(callback: (() -> Unit)? = null) {
        complete(callback)
    }
}

/** Press handler used by [AwesomeButton] and [ThemedButton]. */
public typealias AwesomeButtonPressCallback = (AwesomeButtonNext?) -> Unit

/** Built-in theme names supported by [ThemedButton] and [getTheme]. */
public enum class ThemeName {
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
public enum class ButtonVariant {
    Primary,
    Secondary,
    Anchor,
    Danger,
    Disabled,
    Flat,

    @Deprecated("Use X", ReplaceWith("ButtonVariant.X"))
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
public enum class ButtonSize {
    Icon,
    Small,
    Medium,
    Large,
}
