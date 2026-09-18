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

package io.github.astritveliu.showcaseoverlay.ui.model

enum class TooltipZone { RIGHT, LEFT, BOTTOM, TOP }

data class HighlightEdges(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)

/**
 * 4-quadrant free-space algorithm — picks the zone with most available space.
 * Works correctly in portrait, landscape, and multi-window.
 */
fun HighlightEdges.pickZone(screenW: Float, screenH: Float) =
    when (maxOf(screenW - right, left, screenH - bottom, top)) {
        screenW - right -> TooltipZone.RIGHT
        left -> TooltipZone.LEFT
        screenH - bottom -> TooltipZone.BOTTOM
        else -> TooltipZone.TOP
    }