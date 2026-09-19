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

package io.github.astritveliu.showcaseoverlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import io.github.astritveliu.showcaseoverlay.ui.composable.ShowcaseTooltip
import io.github.astritveliu.showcaseoverlay.ui.model.ShowcaseTarget
import io.github.astritveliu.showcaseoverlay.utils.drawCutout
import io.github.astritveliu.showcaseoverlay.utils.drawGlowHalos
import io.github.astritveliu.showcaseoverlay.ui.state.ShowcaseStyle
import io.github.astritveliu.showcaseoverlay.ui.state.ShowcaseState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Cinematic showcase overlay with seamless step-to-step transitions.
 *
 * COORDINATE SPACE
 * [io.github.astritveliu.showcaseoverlay.utils.showcaseTarget] (or its
 * vendored equivalent) records each target's bounds relative to the Compose
 * root, via `boundsInRoot()`. This overlay is very likely NOT positioned at
 * the root's own origin (e.g. it can sit under a `Scaffold`, `Surface`, or
 * `.statusBarsPadding()`), so root-relative target bounds must be translated
 * into coordinates local to this composable's own `Box`/`Canvas` before
 * they're usable for drawing or for tooltip placement math. [originInRoot]
 * captures this composable's own root-relative position for that purpose.
 *
 * INITIAL-FRAME SAFETY
 * [originInRoot] starts out `null` (not `Offset.Zero`) specifically so that
 * nothing below it - none of the `remember { Animatable(...) }` calls
 * executes before the real origin is known. Seeding an `Animatable` with an
 * `Offset.Zero`-relative (i.e. wrong) value on the very first composition
 * would lock in incorrect bounds permanently, since a plain `remember` never
 * re-seeds itself once created. Gating on a nullable origin guarantees every
 * `Animatable` here is created for the first time only once translation is
 * already correct.
 *
 * LOOPING ANIMATIONS
 * Heartbeat, outer pulse and the radar ring run on [rememberInfiniteTransition]
 * and are never cancelled between steps.
 *
 * PADDING / NEGATIVE-VALUE SAFETY
 * Half-widths/heights are coerced to be non-negative before use, so a
 * pathological or future negative `scaleFactor`/`padding` value on
 * [ShowcaseTarget] can't propagate into a negative `Canvas` radius or a
 * negative `Rect` size, either of which would crash. This is a defensive
 * backstop only; well-formed targets never hit the clamp.
 *
 * @param state The [ShowcaseState] driving this overlay.
 * @param modifier Applied to the overlay's root.
 * @param style Visual configuration - accent color, scrim color, card color,
 *   corner radius. Defaults to [ShowcaseStyle]'s built-in defaults.
 */
@Composable
fun ShowcaseOverlay(
    state: ShowcaseState,
    modifier: Modifier = Modifier,
    style: ShowcaseStyle = ShowcaseStyle(),
) {
    var originInRoot by remember { mutableStateOf<Offset?>(null) }

    fun ShowcaseTarget.toOverlayLocal(origin: Offset): ShowcaseTarget {
        val b = bounds.translate(-origin.x, -origin.y)
        val safeRight = maxOf(b.left, b.right)
        val safeBottom = maxOf(b.top, b.bottom)
        return copy(bounds = Rect(b.left, b.top, safeRight, safeBottom))
    }

    AnimatedVisibility(
        visible = state.isVisible,
        enter = fadeIn(tween(400, easing = EaseOutCubic)),
        exit = fadeOut(tween(300)),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { originInRoot = it.boundsInRoot().topLeft }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {},
                ),
        ) {
            val origin = originInRoot ?: return@Box
            val target = state.currentTarget?.toOverlayLocal(origin) ?: return@Box

            val cornerRadiusPx by remember(style.cornerRadius) {
                mutableFloatStateOf(style.cornerRadius.value * 2)
            }

            val infiniteTransition = rememberInfiniteTransition(label = "showcase-loop")

            val outerPulse by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 14f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "outer-pulse",
            )

            val heartbeat by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1800
                        0.0f at 0 using EaseInOutSine
                        1.0f at 200 using EaseInOutSine
                        0.4f at 400 using EaseInOutSine
                        0.8f at 600 using EaseInOutSine
                        0.0f at 900 using EaseInOutSine
                        0.0f at 1800
                    },
                    repeatMode = RepeatMode.Restart,
                ),
                label = "heartbeat",
            )

            val cutoutProgress = remember { Animatable(0f) }
            val scrimProgress = remember { Animatable(0f) }

            val cutoutCenterX = remember { Animatable(target.bounds.center.x) }
            val cutoutCenterY = remember { Animatable(target.bounds.center.y) }
            val cutoutHalfW = remember { Animatable(maxOf(0f, target.bounds.width / 2f)) }
            val cutoutHalfH = remember { Animatable(maxOf(0f, target.bounds.height / 2f)) }

            val tooltipOffset = remember { Animatable(60f) }
            val tooltipAlpha = remember { Animatable(0f) }
            val radarProgress = remember { Animatable(0f) }

            val travelSpec = spring<Float>(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            )

            LaunchedEffect(state.currentIndex) {
                val newTarget = state.currentTarget?.toOverlayLocal(origin) ?: return@LaunchedEffect

                if (state.currentIndex == 0 && cutoutProgress.value == 0f) {
                    launch { scrimProgress.animateTo(1f, tween(300, easing = FastOutSlowInEasing)) }
                    launch { cutoutProgress.animateTo(1f, tween(420, easing = EaseOutCubic)) }

                    launch {
                        delay(200.milliseconds)
                        launch { tooltipAlpha.animateTo(1f, tween(220)) }
                        tooltipOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                    }
                    return@LaunchedEffect
                }

                launch {
                    launch { tooltipAlpha.animateTo(0f, tween(120)) }
                    tooltipOffset.animateTo(-30f, tween(120, easing = FastOutSlowInEasing))
                }

                launch {
                    launch { cutoutCenterX.animateTo(newTarget.bounds.center.x, travelSpec) }
                    launch {
                        cutoutHalfW.animateTo(maxOf(0f, newTarget.bounds.width / 2f), travelSpec)
                    }
                    launch {
                        cutoutHalfH.animateTo(maxOf(0f, newTarget.bounds.height / 2f), travelSpec)
                    }
                    cutoutCenterY.animateTo(newTarget.bounds.center.y, travelSpec)
                }

                launch {
                    delay(280.milliseconds)

                    tooltipOffset.snapTo(60f)
                    tooltipAlpha.snapTo(0f)

                    launch { tooltipAlpha.animateTo(1f, tween(220)) }

                    tooltipOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                }
            }

            LaunchedEffect(state.isVisible) {
                if (!state.isVisible) return@LaunchedEffect

                while (true) {
                    radarProgress.snapTo(0f)
                    radarProgress.animateTo(1f, tween(1400, easing = EaseOutCubic))

                    delay(600.milliseconds)
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
            ) {
                val animCenter = Offset(cutoutCenterX.value, cutoutCenterY.value)

                drawRect(
                    color = style.scrimColor.copy(
                        alpha = style.scrimColor.alpha * scrimProgress.value.coerceIn(0f, 1f)
                    )
                )

                drawCutout(
                    target = target,
                    animCenter = animCenter,
                    animHalfW = maxOf(0f, cutoutHalfW.value),
                    animHalfH = maxOf(0f, cutoutHalfH.value),
                    pulse = outerPulse,
                    cornerRadius = maxOf(0f, cornerRadiusPx),
                    progress = cutoutProgress.value.coerceIn(0f, 1f),
                )
                drawGlowHalos(
                    accentColor = style.accentColor,
                    target = target,
                    animCenter = animCenter,
                    animHalfW = maxOf(0f, cutoutHalfW.value),
                    animHalfH = maxOf(0f, cutoutHalfH.value),
                    pulse = outerPulse,
                    intensity = heartbeat.coerceIn(0f, 1f),
                    cornerRadius = maxOf(0f, cornerRadiusPx),
                )
            }

            ShowcaseTooltip(
                target = target,
                currentStep = state.currentIndex,
                totalSteps = state.totalTargets,
                slideOffset = tooltipOffset.value,
                alpha = tooltipAlpha.value.coerceIn(0f, 1f),
                style = style,
                onNext = state::next,
                onPrevious = state::previous,
                onDismiss = state::dismiss,
            )
        }
    }
}