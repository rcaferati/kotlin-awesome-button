package dev.caferati.awesomebutton

import androidx.compose.runtime.withFrameNanos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

internal const val DefaultTextTransitionSlotStaggerMillis = 7
internal const val DefaultTextTransitionRandomizeStartStaggerMillis = DefaultTextTransitionSlotStaggerMillis
internal const val DefaultTextTransitionExpandStaggerMillis = DefaultTextTransitionSlotStaggerMillis
internal const val TextTransitionPostRandomizeHoldMillis = 10
internal const val DefaultTextTransitionCollapseStaggerMillis = DefaultTextTransitionSlotStaggerMillis
internal const val TextTransitionRefreshMillis = 16

private enum class LetterWidthGroup {
    Narrow,
    Average,
    Wide,
}

private val narrowLowercaseLetters = "iljtfr".toList()
private val averageLowercaseLetters = "acesuvxznho".toList()
private val wideLowercaseLetters = "mwdbpqgy".toList()
private val lowercaseLetters = "abcdefghijklmnopqrstuvwxyz".toList()
private val uppercaseLetters = lowercaseLetters.map { it.uppercaseChar() }
private val narrowUppercaseLetters = narrowLowercaseLetters.map { it.uppercaseChar() }
private val averageUppercaseLetters = averageLowercaseLetters.map { it.uppercaseChar() }
private val wideUppercaseLetters = wideLowercaseLetters.map { it.uppercaseChar() }
private val digits = "0123456789".toList()
private val symbols = "#%&^+=-".toList()

internal data class TextTransitionTimeline(
    val sourceLength: Int,
    val targetLength: Int,
    val maxLength: Int,
    val slotStaggerMillis: Int,
    val lastSourceRandomizeStartMillis: Int,
    val lastRandomizeStartMillis: Int,
    val collapseStartMillis: Int,
    val totalDurationMillis: Int,
)

internal data class AutoWidthTextTransitionTiming(
    val widthDelayMillis: Int,
    val textDelayMillis: Int,
    val widthDurationMillis: Int,
)

internal sealed interface ButtonTextUpdatePlan {
    data class Assign(val text: String?) : ButtonTextUpdatePlan
    data object Keep : ButtonTextUpdatePlan
    data class Transition(val sourceText: String, val targetText: String) : ButtonTextUpdatePlan
}

internal enum class AutoWidthTextFlow {
    Initial,
    TextOnly,
    GrowFirst,
    ShrinkLast,
}

internal enum class ContentClipAlignment {
    Center,
    Leading,
}

internal enum class ButtonWidthMode {
    Auto,
    Fixed,
    Stretch,
}

internal sealed interface AutoWidthTextUpdatePlan {
    data object FallbackToTextSync : AutoWidthTextUpdatePlan

    data class Initial(
        val targetText: String,
        val targetWidthPx: Int,
    ) : AutoWidthTextUpdatePlan

    data class TextOnly(
        val sourceText: String,
        val targetText: String,
        val animateText: Boolean,
    ) : AutoWidthTextUpdatePlan

    data class GrowFirst(
        val sourceText: String,
        val targetText: String,
        val targetWidthPx: Int,
        val timing: AutoWidthTextTransitionTiming,
        val animateSize: Boolean,
        val animateText: Boolean,
    ) : AutoWidthTextUpdatePlan

    data class ShrinkLast(
        val sourceText: String,
        val targetText: String,
        val targetWidthPx: Int,
        val timing: AutoWidthTextTransitionTiming,
        val animateSize: Boolean,
        val animateText: Boolean,
    ) : AutoWidthTextUpdatePlan
}

internal fun normalizeTextTransitionSlotStaggerMillis(slotStaggerMillis: Int): Int =
    max(1, slotStaggerMillis)

internal fun getTextTransitionRandomizeStartMillis(
    index: Int,
    sourceLength: Int,
    targetLength: Int,
    slotStaggerMillis: Int,
): Int? {
    val normalizedSlotStaggerMillis = normalizeTextTransitionSlotStaggerMillis(slotStaggerMillis)

    if (index < sourceLength) {
        return index * normalizedSlotStaggerMillis
    }

    if (index < targetLength) {
        val lastSourceStartMillis =
            if (sourceLength > 0) {
                (sourceLength - 1) * normalizedSlotStaggerMillis
            } else {
                0
            }
        return lastSourceStartMillis + ((index - sourceLength + 1) * normalizedSlotStaggerMillis)
    }

    return null
}

internal fun getTextTransitionTimeline(
    fromText: String,
    targetText: String,
    slotStaggerMillis: Int = DefaultTextTransitionSlotStaggerMillis,
): TextTransitionTimeline {
    val sourceLength = fromText.length
    val targetLength = targetText.length
    val maxLength = max(sourceLength, targetLength)
    val normalizedSlotStaggerMillis = normalizeTextTransitionSlotStaggerMillis(slotStaggerMillis)
    val lastSourceRandomizeStartMillis =
        if (sourceLength > 0) {
            (sourceLength - 1) * normalizedSlotStaggerMillis
        } else {
            0
        }
    val lastRandomizeStartMillis =
        if (maxLength == 0) {
            0
        } else {
            getTextTransitionRandomizeStartMillis(
                index = maxLength - 1,
                sourceLength = sourceLength,
                targetLength = targetLength,
                slotStaggerMillis = normalizedSlotStaggerMillis,
            ) ?: 0
        }
    val collapseStartMillis = lastRandomizeStartMillis + TextTransitionPostRandomizeHoldMillis
    val totalDurationMillis =
        if (maxLength == 0) {
            0
        } else {
            collapseStartMillis + ((maxLength - 1) * normalizedSlotStaggerMillis)
        }

    return TextTransitionTimeline(
        sourceLength = sourceLength,
        targetLength = targetLength,
        maxLength = maxLength,
        slotStaggerMillis = normalizedSlotStaggerMillis,
        lastSourceRandomizeStartMillis = lastSourceRandomizeStartMillis,
        lastRandomizeStartMillis = lastRandomizeStartMillis,
        collapseStartMillis = collapseStartMillis,
        totalDurationMillis = totalDurationMillis,
    )
}

internal fun getTextTransitionCollapseMillis(
    index: Int,
    timeline: TextTransitionTimeline,
): Int {
    if (timeline.targetLength >= timeline.sourceLength) {
        return timeline.collapseStartMillis + (index * timeline.slotStaggerMillis)
    }

    val extraCount = timeline.sourceLength - timeline.targetLength

    if (index >= timeline.targetLength) {
        val extraIndex = index - timeline.targetLength
        val reverseExtraIndex = max(0, (extraCount - 1) - extraIndex)
        return timeline.collapseStartMillis + (reverseExtraIndex * timeline.slotStaggerMillis)
    }

    return timeline.collapseStartMillis + (extraCount * timeline.slotStaggerMillis) + (index * timeline.slotStaggerMillis)
}

private fun getLetterWidthGroup(character: Char): LetterWidthGroup? {
    val lowercasedCharacter = character.lowercaseChar()
    return when {
        narrowLowercaseLetters.contains(lowercasedCharacter) -> LetterWidthGroup.Narrow
        averageLowercaseLetters.contains(lowercasedCharacter) -> LetterWidthGroup.Average
        wideLowercaseLetters.contains(lowercasedCharacter) -> LetterWidthGroup.Wide
        else -> null
    }
}

internal fun getTextTransitionCharset(character: Char): List<Char>? {
    if (character.isWhitespace()) {
        return null
    }

    if (character.isDigit()) {
        return digits
    }

    if (character.isLetter() && character.isUpperCase()) {
        return when (getLetterWidthGroup(character)) {
            LetterWidthGroup.Narrow -> narrowUppercaseLetters
            LetterWidthGroup.Average -> averageUppercaseLetters
            LetterWidthGroup.Wide -> wideUppercaseLetters
            null -> uppercaseLetters
        }
    }

    if (character.isLetter() && character.isLowerCase()) {
        return when (getLetterWidthGroup(character)) {
            LetterWidthGroup.Narrow -> narrowLowercaseLetters
            LetterWidthGroup.Average -> averageLowercaseLetters
            LetterWidthGroup.Wide -> wideLowercaseLetters
            null -> lowercaseLetters
        }
    }

    return symbols
}

internal fun getRandomTransitionCharacter(
    character: Char,
    random: () -> Double = { Random.nextDouble() },
): Char {
    val charset = getTextTransitionCharset(character) ?: return character
    val index = (random() * charset.size).toInt().coerceIn(0, charset.lastIndex)
    return charset[index]
}

internal fun buildTextTransitionFrame(
    fromText: String,
    targetText: String,
    elapsedMillis: Int,
    slotStaggerMillis: Int = DefaultTextTransitionSlotStaggerMillis,
    random: () -> Double = { Random.nextDouble() },
): String {
    if (fromText.isEmpty()) {
        return targetText
    }

    if (fromText == targetText) {
        return targetText
    }

    if (elapsedMillis <= 0) {
        return fromText
    }

    val timeline = getTextTransitionTimeline(
        fromText = fromText,
        targetText = targetText,
        slotStaggerMillis = slotStaggerMillis,
    )
    if (elapsedMillis >= timeline.totalDurationMillis) {
        return targetText
    }

    return buildString {
        for (index in 0 until timeline.maxLength) {
            val randomizeStartMillis =
                getTextTransitionRandomizeStartMillis(
                    index = index,
                    sourceLength = timeline.sourceLength,
                    targetLength = timeline.targetLength,
                    slotStaggerMillis = timeline.slotStaggerMillis,
                )
            val collapseMillis = getTextTransitionCollapseMillis(index, timeline)
            val sourceCharacter = fromText.getOrNull(index)
            val targetCharacter = targetText.getOrNull(index)
            val randomSourceCharacter =
                if (index >= timeline.sourceLength) {
                    targetCharacter
                } else {
                    sourceCharacter
                }

            when {
                randomizeStartMillis == null || elapsedMillis < randomizeStartMillis -> {
                    if (sourceCharacter != null) append(sourceCharacter)
                }
                elapsedMillis >= collapseMillis -> {
                    if (targetCharacter != null) append(targetCharacter)
                }
                randomSourceCharacter != null -> {
                    append(getRandomTransitionCharacter(randomSourceCharacter, random))
                }
            }
        }
    }
}

internal fun resolveButtonTextUpdatePlan(
    textTransitionEnabled: Boolean,
    nextText: String?,
    currentTarget: String?,
    displayedText: String?,
): ButtonTextUpdatePlan {
    val previousText = displayedText ?: currentTarget

    if (!textTransitionEnabled || nextText == null || nextText.isEmpty()) {
        return ButtonTextUpdatePlan.Assign(nextText)
    }

    if (nextText == currentTarget) {
        return ButtonTextUpdatePlan.Keep
    }

    if (previousText.isNullOrEmpty()) {
        return ButtonTextUpdatePlan.Assign(nextText)
    }

    return ButtonTextUpdatePlan.Transition(previousText, nextText)
}

internal fun resolveAutoWidthTextFlow(
    currentWidthPx: Int?,
    targetWidthPx: Int,
): AutoWidthTextFlow {
    currentWidthPx ?: return AutoWidthTextFlow.Initial

    if (kotlin.math.abs(currentWidthPx - targetWidthPx) < 1) {
        return AutoWidthTextFlow.TextOnly
    }

    return if (targetWidthPx > currentWidthPx) {
        AutoWidthTextFlow.GrowFirst
    } else {
        AutoWidthTextFlow.ShrinkLast
    }
}

internal fun resolveAutoWidthTextTransitionTiming(
    fromText: String,
    targetText: String,
    flow: AutoWidthTextFlow,
    slotStaggerMillis: Int = DefaultTextTransitionSlotStaggerMillis,
): AutoWidthTextTransitionTiming {
    val timeline =
        getTextTransitionTimeline(
            fromText = fromText,
            targetText = targetText,
            slotStaggerMillis = slotStaggerMillis,
        )
    val startOffsetMillis = (timeline.totalDurationMillis * 0.3).roundToInt()

    return when (flow) {
        AutoWidthTextFlow.GrowFirst ->
            AutoWidthTextTransitionTiming(
                widthDelayMillis = 0,
                textDelayMillis = startOffsetMillis,
                widthDurationMillis = timeline.totalDurationMillis,
            )
        AutoWidthTextFlow.ShrinkLast ->
            AutoWidthTextTransitionTiming(
                widthDelayMillis = startOffsetMillis,
                textDelayMillis = 0,
                widthDurationMillis = timeline.totalDurationMillis,
            )
        AutoWidthTextFlow.Initial,
        AutoWidthTextFlow.TextOnly ->
            AutoWidthTextTransitionTiming(
                widthDelayMillis = 0,
                textDelayMillis = 0,
                widthDurationMillis = 0,
            )
    }
}

internal fun resolveAutoWidthTextUpdatePlan(
    isEligible: Boolean,
    targetText: String?,
    currentWidthPx: Int?,
    targetWidthPx: Int?,
    displayedText: String?,
    animateSize: Boolean,
    textTransition: Boolean,
    slotStaggerMillis: Int = DefaultTextTransitionSlotStaggerMillis,
): AutoWidthTextUpdatePlan {
    if (!isEligible || targetText == null || targetWidthPx == null) {
        return AutoWidthTextUpdatePlan.FallbackToTextSync
    }

    val sourceText = displayedText ?: targetText

    return when (val flow = resolveAutoWidthTextFlow(currentWidthPx, targetWidthPx)) {
        AutoWidthTextFlow.Initial ->
            AutoWidthTextUpdatePlan.Initial(
                targetText = targetText,
                targetWidthPx = targetWidthPx,
            )

        AutoWidthTextFlow.TextOnly ->
            AutoWidthTextUpdatePlan.TextOnly(
                sourceText = sourceText,
                targetText = targetText,
                animateText = textTransition,
            )

        AutoWidthTextFlow.GrowFirst ->
            AutoWidthTextUpdatePlan.GrowFirst(
                sourceText = sourceText,
                targetText = targetText,
                targetWidthPx = targetWidthPx,
                timing = resolveAutoWidthTextTransitionTiming(
                    fromText = sourceText,
                    targetText = targetText,
                    flow = flow,
                    slotStaggerMillis = slotStaggerMillis,
                ),
                animateSize = animateSize,
                animateText = textTransition,
            )

        AutoWidthTextFlow.ShrinkLast ->
            AutoWidthTextUpdatePlan.ShrinkLast(
                sourceText = sourceText,
                targetText = targetText,
                targetWidthPx = targetWidthPx,
                timing = resolveAutoWidthTextTransitionTiming(
                    fromText = sourceText,
                    targetText = targetText,
                    flow = flow,
                    slotStaggerMillis = slotStaggerMillis,
                ),
                animateSize = animateSize,
                animateText = textTransition,
            )
    }
}

internal fun shouldSnapWidthBridge(
    previous: ButtonWidthMode?,
    next: ButtonWidthMode,
): Boolean = previous == null || previous != next

internal fun shouldDeferReleaseAutoWidthTransition(
    isReleaseActive: Boolean,
    previousWidthMode: ButtonWidthMode?,
    nextWidthMode: ButtonWidthMode,
    currentAutoWidthTextEligible: Boolean,
    nextAutoWidthTextEligible: Boolean,
    currentTextTransition: Boolean,
    nextTextTransition: Boolean,
    nextAnimateSize: Boolean,
    sizeSignatureUnchanged: Boolean,
    currentText: String?,
    nextText: String?,
    currentWidthPx: Int?,
    targetWidthPx: Int?,
): Boolean {
    if (!isReleaseActive ||
        previousWidthMode != ButtonWidthMode.Auto ||
        nextWidthMode != ButtonWidthMode.Auto ||
        !currentAutoWidthTextEligible ||
        !nextAutoWidthTextEligible ||
        !currentTextTransition ||
        !nextTextTransition ||
        !nextAnimateSize ||
        !sizeSignatureUnchanged ||
        currentText == nextText ||
        targetWidthPx == null
    ) {
        return false
    }

    return when (resolveAutoWidthTextFlow(currentWidthPx, targetWidthPx)) {
        AutoWidthTextFlow.GrowFirst,
        AutoWidthTextFlow.ShrinkLast,
        -> true
        AutoWidthTextFlow.Initial,
        AutoWidthTextFlow.TextOnly,
        -> false
    }
}

internal suspend fun runFrameTextTransition(
    fromText: String,
    targetText: String,
    slotStaggerMillis: Int = DefaultTextTransitionSlotStaggerMillis,
    random: () -> Double = { Random.nextDouble() },
    onUpdate: (String) -> Unit,
    onComplete: (() -> Unit)? = null,
) {
    val timeline =
        getTextTransitionTimeline(
            fromText = fromText,
            targetText = targetText,
            slotStaggerMillis = slotStaggerMillis,
        )
    if (fromText.isEmpty() || targetText.isEmpty() || fromText == targetText) {
        onUpdate(targetText)
        onComplete?.invoke()
        return
    }

    var startNanos: Long? = null
    var lastPublishedValue: String? = null

    while (true) {
        val frameNanos = withFrameNanos { it }
        val runStartNanos = startNanos ?: frameNanos.also { startNanos = it }
        val elapsedMillis =
            minOf(
                timeline.totalDurationMillis,
                ((frameNanos - runStartNanos) / 1_000_000L).toInt(),
            )
        val nextValue =
            buildTextTransitionFrame(
                fromText = fromText,
                targetText = targetText,
                elapsedMillis = elapsedMillis,
                slotStaggerMillis = timeline.slotStaggerMillis,
                random = random,
            )

        if (nextValue != lastPublishedValue) {
            lastPublishedValue = nextValue
            onUpdate(nextValue)
        }

        if (elapsedMillis >= timeline.totalDurationMillis) {
            onComplete?.invoke()
            return
        }
    }
}
