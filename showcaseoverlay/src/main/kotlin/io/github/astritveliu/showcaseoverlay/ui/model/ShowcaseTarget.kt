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

import androidx.compose.ui.geometry.Rect

/**
 * Represents a single registered showcase target.
 *
 * @param index Step order (0-based). Must be unique per [io.github.astritveliu.showcaseoverlay.ui.state.ShowcaseState].
 * @param bounds Screen-space bounding box updated on every layout pass.
 * @param title Headline shown in the tooltip card.
 * @param description Body text shown below the title.
 * @param shape Cutout shape: [TargetShape.Circle] or [TargetShape.RoundedRect].
 * @param padding Extra space in pixels added around the scaled target bounds.
 * @param scaleFactor Multiplier applied to the target size before drawing the
 * cutout and rings. `1f` = natural size, `0.5f` = half, `2f` = double. Useful
 * when the tap target is larger/smaller than the visual element you want to
 * highlight.
 * @param translationY Extra vertical offset (in px) applied to the bounds,
 * for targets that are animated/translated independently of layout.
 */
data class ShowcaseTarget(
  val index: Int,
  val bounds: Rect,
  val title: String,
  val description: String,
  val shape: TargetShape = TargetShape.Circle,
  val padding: Float = 24f,
  val scaleFactor: Float = 1f,
  val translationY: Float = 0f,
)

/** The cutout shape drawn around a [ShowcaseTarget]. */
enum class TargetShape { Circle, RoundedRect }
