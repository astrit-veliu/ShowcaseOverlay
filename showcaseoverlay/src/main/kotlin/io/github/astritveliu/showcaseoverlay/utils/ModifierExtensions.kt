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

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.unit.toSize
import io.github.astritveliu.showcaseoverlay.ui.model.ShowcaseTarget
import io.github.astritveliu.showcaseoverlay.ui.model.TargetShape
import io.github.astritveliu.showcaseoverlay.ui.state.ShowcaseState

private enum class ButtonState { Idle, Pressed }


/**
 * Registers this composable as a step in [state]'s showcase sequence.
 *
 * Performance notes
 * -----------------
 * - Uses [positionOnScreen] (not `positionInRoot`) so GPU-layer transforms
 *   applied by `graphicsLayer` are correctly reflected in stored bounds.
 * - Zero-size bounds are rejected immediately — guards against the first-frame
 *   callback inside `AnimatedVisibility` before the element is measured.
 * - Off-screen positions (e.g. `HorizontalPager` swipe noise) are filtered
 *   inside [io.github.astritveliu.showcaseoverlay.ui.state.ShowcaseState.registerTarget], keeping this modifier allocation-free.
 * - No Compose state is read here, so this modifier never triggers recomposition.
 *
 * @param state Shared [io.github.astritveliu.showcaseoverlay.ui.state.ShowcaseState] for this screen.
 * @param index Zero-based step order. Must be unique per state instance.
 * @param title Bold headline shown in the tooltip.
 * @param description Body text shown below the title.
 * @param shape [TargetShape.Circle] or [TargetShape.RoundedRect].
 * @param padding Extra space in pixels around the (scaled) target bounds.
 * @param scaleFactor Size multiplier for the cutout. `1f` = natural size,
 *   `0.5f` = half size, `2f` = double. Handy when the composable is larger
 *   than the visual you want to spotlight.
 * @param translationY Extra vertical offset (in px), e.g. for targets that
 *   are already animated/translated independently of layout.
 */
fun Modifier.showcaseTarget(
  state: ShowcaseState,
  index: Int,
  title: String,
  description: String,
  shape: TargetShape = TargetShape.Circle,
  padding: Float = 24f,
  scaleFactor: Float = 1f,
  translationY: Float = 0f,
): Modifier = this.onGloballyPositioned { layoutCoordinates ->
  val size = layoutCoordinates.size.toSize()
  if (size.width == 0f || size.height == 0f) return@onGloballyPositioned

  val screenWidth = layoutCoordinates.findRootCoordinates().size.width.toFloat()
  val rootBounds = layoutCoordinates.boundsInRoot()
  val windowOffset = layoutCoordinates.findRootCoordinates().positionOnScreen()

  val screenBounds = Rect(
    left = rootBounds.left + windowOffset.x,
    top = rootBounds.top + windowOffset.y,
    right = rootBounds.right + windowOffset.x,
    bottom = rootBounds.bottom + windowOffset.y,
  )

  val cx = screenBounds.center.x
  val cy = screenBounds.center.y
  val hw = (screenBounds.width / 2f) * scaleFactor
  val hh = (screenBounds.height / 2f) * scaleFactor
  val scaledBounds = Rect(cx - hw, cy - hh, cx + hw, cy + hh)

  state.registerTarget(
    target = ShowcaseTarget(
      index = index,
      bounds = scaledBounds,
      title = title,
      description = description,
      shape = shape,
      padding = padding,
      scaleFactor = 1f,
      translationY = translationY,
    ),
    screenWidth = screenWidth,
  )
}

/**
 * Scales this composable down slightly on press and back up on release,
 * invoking [onClick] on a completed tap. Used internally for the tooltip's
 * Back / Skip / Next controls; kept dependency-free and self-contained so
 * the library needs no design-system input from the host app.
 */
internal fun Modifier.pulseClick(
  enabled: Boolean = true,
  interactionSource: MutableInteractionSource? = null,
  pressedScaleFactor: Float = 0.92f,
  onClick: (() -> Unit)? = null,
): Modifier = composed {
  var state by remember { mutableStateOf(ButtonState.Idle) }
  val scale by animateFloatAsState(
    targetValue = if (state == ButtonState.Pressed) pressedScaleFactor else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow,
    ),
    label = "bounce",
  )

  graphicsLayer {
    scaleX = scale
    scaleY = scale
    clip = true
  }
    .pointerInput(enabled, onClick) {
      if (!enabled) return@pointerInput
      detectTapGestures(
        onPress = { offset ->
          val press = PressInteraction.Press(offset)
          state = ButtonState.Pressed
          interactionSource?.tryEmit(press)

          val released = tryAwaitRelease()

          state = ButtonState.Idle
          if (released) {
            interactionSource?.tryEmit(PressInteraction.Release(press))
          } else {
            interactionSource?.tryEmit(PressInteraction.Cancel(press))
          }
        },
        onTap = { onClick?.invoke() },
      )
    }
}
