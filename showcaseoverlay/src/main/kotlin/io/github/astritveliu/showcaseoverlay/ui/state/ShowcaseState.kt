/*
 * Copyright (C) 2026 Astrit Veliu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.astritveliu.showcaseoverlay.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.astritveliu.showcaseoverlay.ui.model.ShowcaseTarget

/**
 * Stable state holder for a showcase sequence.
 *
 * Designed to minimise recomposition:
 * - [registry] is a snapshot-backed map; only consumers reading a specific
 *   key recompose when that key changes.
 * - [isVisible] and [currentIndex] are the only two observable fields that
 *   drive the overlay UI, so only the overlay recomposes on step changes.
 *
 * @param autoStartAfter When non-null the showcase starts automatically as
 *   soon as this many targets have registered with valid (non-zero, on-screen)
 *   bounds. Pass `null` to control the start manually via [start].
 * @param onStepChanged Called every time the visible step changes. Receives
 *   the new zero-based step index and the corresponding [ShowcaseTarget].
 * @param onFinished Called once when the showcase is dismissed — either by
 *   the user finishing/skipping the tour, or by calling [dismiss] programmatically.
 */
@Stable
class ShowcaseState(
    private val autoStartAfter: Int? = null,
    private val onStepChanged: ((step: Int, target: ShowcaseTarget) -> Unit)? = null,
    private val onFinished: (() -> Unit)? = null,
) {

    private val registry = mutableStateMapOf<Int, ShowcaseTarget>()

    var isVisible: Boolean by mutableStateOf(false)
        private set

    var currentIndex: Int by mutableIntStateOf(0)
        private set

    val currentTarget: ShowcaseTarget?
        get() = registry[currentIndex]

    val totalTargets: Int
        get() = registry.size

    private var hasAutoStarted = false

    /**
     * Called by [Modifier.showcaseTarget] on every layout pass.
     *
     * Skipped when:
     * - bounds are zero-sized (composable not yet measured)
     * - position.x is outside `[0, screenWidth]` (composable is off-screen,
     *   e.g. a pager page that is currently scrolled away). This avoids
     *   storing stale off-screen coordinates during pager swipe animations
     *   and prevents the flood of updates seen during horizontal scrolling.
     */
    internal fun registerTarget(target: ShowcaseTarget, screenWidth: Float = Float.MAX_VALUE) {
        val b = target.bounds

        if (b.width == 0f || b.height == 0f) return

        if (screenWidth != Float.MAX_VALUE) {
            if (b.right <= 0f || b.left >= screenWidth) return
        } else {
            if (b.right <= 0f) return
        }

        registry[target.index] = target

        autoStartAfter?.let { threshold ->
            if (!hasAutoStarted && !isVisible && registry.size >= threshold) {
                hasAutoStarted = true
                currentIndex = 0
                isVisible = true
                onStepChanged?.invoke(0, registry[0] ?: return@let)
            }
        }
    }

    /**
     * Manually start the showcase from step 0.
     * Does nothing if no targets are registered yet or all bounds are still zero.
     */
    fun start() {
        if (registry.isEmpty()) return
        if (registry.values.any { it.bounds.width == 0f || it.bounds.height == 0f }) return
        hasAutoStarted = true
        currentIndex = 0
        isVisible = true
        registry[0]?.let { onStepChanged?.invoke(0, it) }
    }

    /**
     * Advance to the next step. Calls [onFinished] and hides the overlay
     * when the last step is reached.
     */
    fun next() {
        if (currentIndex < registry.size - 1) {
            currentIndex++
            registry[currentIndex]?.let { onStepChanged?.invoke(currentIndex, it) }
        } else {
            finish()
        }
    }

    /**
     * Go back to the previous step. No-op when already on step 0.
     */
    fun previous() {
        if (currentIndex > 0) {
            currentIndex--
            registry[currentIndex]?.let { onStepChanged?.invoke(currentIndex, it) }
        }
    }

    /**
     * Dismiss the showcase immediately without completing the sequence.
     * Also fires [onFinished].
     */
    fun dismiss(): Unit = finish()

    /**
     * Reset state so the showcase can be shown again (e.g. after an app update).
     * Does not clear the target registry.
     */
    fun reset() {
        hasAutoStarted = false
        isVisible = false
        currentIndex = 0
    }

    private fun finish() {
        isVisible = false
        currentIndex = 0
        onFinished?.invoke()
    }
}

/**
 * Creates and remembers a [ShowcaseState].
 *
 * @param autoStartAfter Starts automatically once this many targets are on-screen.
 *   Pass `null` (default) to call [ShowcaseState.start] manually.
 * @param onStepChanged Invoked on every step change with the new index and target.
 * @param onFinished Invoked when the showcase completes or is dismissed.
 *
 * Usage:
 * ```
 * // Manual start
 * val showcase = rememberShowcaseState(
 *     onFinished = { prefs.markSeen() }
 * )
 * Button(onClick = { showcase.start() }) { Text("Start tour") }
 *
 * // Auto start as soon as 3 targets are laid out
 * val showcase = rememberShowcaseState(
 *     autoStartAfter = 3,
 *     onFinished = { prefs.markSeen() }
 * )
 * ```
 */
@Composable
fun rememberShowcaseState(
    autoStartAfter: Int? = null,
    onStepChanged: ((step: Int, target: ShowcaseTarget) -> Unit)? = null,
    onFinished: (() -> Unit)? = null,
): ShowcaseState = remember {
    ShowcaseState(
        autoStartAfter = autoStartAfter,
        onStepChanged = onStepChanged,
        onFinished = onFinished,
    )
}
