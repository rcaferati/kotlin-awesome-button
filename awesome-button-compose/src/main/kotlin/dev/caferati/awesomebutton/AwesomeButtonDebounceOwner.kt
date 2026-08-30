package dev.caferati.awesomebutton

/** Owns the monotonic debounce history for one composed button instance. */
internal class AwesomeButtonDebounceOwner {
    private var lastAcceptedAtMillis: Long? = null

    fun tryAccept(
        nowMillis: Long,
        durationMillis: Long,
    ): Boolean {
        val previousAcceptance = lastAcceptedAtMillis
        if (
            durationMillis > 0 &&
            previousAcceptance != null &&
            nowMillis - previousAcceptance < durationMillis
        ) {
            return false
        }

        lastAcceptedAtMillis = nowMillis
        return true
    }
}
