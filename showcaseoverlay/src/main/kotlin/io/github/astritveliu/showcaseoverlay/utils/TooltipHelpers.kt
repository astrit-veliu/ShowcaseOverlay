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

package io.github.astritveliu.showcaseoverlay.utils

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.astritveliu.showcaseoverlay.ui.model.ShowcaseTarget

// Tooltip placement helpers
fun ShowcaseTarget.highlightRect(extra: Float = 0f): Rect = bounds.inflate(padding + extra)

fun ShowcaseTarget.highlightCircleRadius(pulse: Float = 0f): Float =
    maxOf(bounds.width, bounds.height) / 2f + padding + pulse


/**
 * Safely converts a raw pixel float to [Dp], clamped to a minimum of [min].
 *
 * Compose throws [IllegalArgumentException] if any padding value is negative.
 * This happens when the cutout travels near or beyond the screen edge during
 * animation, producing a computed edge offset that undershoots zero. Clamping
 * to `0.dp` (or a custom [min]) prevents the crash entirely.
 */
fun Float.safeToDp(density: Density, min: Dp = 0.dp): Dp =
    with(density) { this@safeToDp.toDp() }.coerceAtLeast(min)