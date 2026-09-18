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

package io.github.astritveliu.showcaseoverlay.ui.composable

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.astritveliu.showcaseoverlay.ui.model.ShowcaseTarget
import io.github.astritveliu.showcaseoverlay.ui.model.TargetShape
import io.github.astritveliu.showcaseoverlay.ui.state.ShowcaseStyle
import io.github.astritveliu.showcaseoverlay.ui.model.HighlightEdges
import io.github.astritveliu.showcaseoverlay.ui.model.TooltipZone
import io.github.astritveliu.showcaseoverlay.ui.model.pickZone
import io.github.astritveliu.showcaseoverlay.utils.highlightCircleRadius
import io.github.astritveliu.showcaseoverlay.utils.highlightRect
import io.github.astritveliu.showcaseoverlay.utils.pulseClick
import io.github.astritveliu.showcaseoverlay.utils.safeToDp


/**
 * Tooltip card.
 *
 * [slideOffset] and [alpha] are driven by [Animatable]s in the parent and
 * applied via `graphicsLayer`, giving the slide-fade entrance/exit.
 *
 * All edge-derived padding values go through [safeToDp] to guarantee they
 * are never negative, preventing [IllegalArgumentException] during transitions.
 */
@Composable
fun BoxScope.ShowcaseTooltip(
    target: ShowcaseTarget,
    currentStep: Int,
    totalSteps: Int,
    slideOffset: Float,
    alpha: Float,
    style: ShowcaseStyle,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDismiss: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWPx = windowInfo.containerSize.width.toFloat()
    val screenHPx = windowInfo.containerSize.height.toFloat()

    if (screenWPx <= 0f || screenHPx <= 0f) return

    BackHandler { if (currentStep > 0) onPrevious() else onDismiss() }

    val maxPulse = 20f
    val edges = when (target.shape) {
        TargetShape.Circle -> {
            val c = Offset(target.bounds.center.x, target.bounds.center.y + target.translationY)
            val r = target.highlightCircleRadius(maxPulse)
            HighlightEdges(c.x - r, c.y - r, c.x + r, c.y + r)
        }

        TargetShape.RoundedRect -> {
            val r = target.highlightRect(maxPulse).translate(0f, target.translationY)
            HighlightEdges(r.left, r.top, r.right, r.bottom)
        }
    }

    val zone = edges.pickZone(screenWPx, screenHPx)
    val gapPx = 20f
    val rimDp = 16.dp

    val tooltipModifier = when (zone) {
        TooltipZone.RIGHT -> Modifier
            .fillMaxHeight()
            .align(Alignment.CenterStart)
            .padding(
                start = (edges.right + gapPx).safeToDp(density),
                end = rimDp,
                top = rimDp,
                bottom = rimDp,
            )

        TooltipZone.LEFT -> Modifier
            .fillMaxHeight()
            .align(Alignment.CenterStart)
            .padding(
                start = rimDp,
                end = (screenWPx - edges.left + gapPx).safeToDp(density),
                top = rimDp,
                bottom = rimDp,
            )

        TooltipZone.BOTTOM -> Modifier
            .fillMaxWidth()
            .align(Alignment.TopStart)
            .padding(
                start = rimDp,
                end = rimDp,
                top = (edges.bottom + gapPx).safeToDp(density),
                bottom = rimDp,
            )

        TooltipZone.TOP -> Modifier
            .fillMaxWidth()
            .align(Alignment.BottomStart)
            .padding(
                start = rimDp,
                end = rimDp,
                top = rimDp,
                bottom = (screenHPx - edges.top + gapPx).safeToDp(density),
            )
    }

    Column(
        modifier = tooltipModifier.graphicsLayer {
            translationY = slideOffset
            this.alpha = alpha
        },
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
    ) {
        StepIndicator(
            cornerRadius = style.cornerRadius,
            accentColor = style.accentColor,
            current = currentStep,
            total = totalSteps,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            style.accentColor,
                            style.accentColor.copy(alpha = 0.5f),
                            Color.Transparent,
                        ),
                    ),
                    RoundedCornerShape(style.cornerRadius),
                )
                .clip(RoundedCornerShape(style.cornerRadius))
                .background(style.cardColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                style.accentColor.copy(alpha = 0.6f),
                                style.accentColor.copy(alpha = 0.4f),
                                style.accentColor.copy(alpha = 0.6f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier.padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 20.dp,
                    bottom = 18.dp,
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = target.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDismiss()
                        },
                        modifier = Modifier.size(28.dp),
                    ) {
                        style.closeIcon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = "Dismiss",
                                tint = style.accentColor,
                                modifier = Modifier.size(14.dp),
                            )
                        } ?: CloseGlyph(
                            tint = style.accentColor,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = target.description,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (currentStep > 0) {
                        Text(
                            modifier = Modifier.pulseClick {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPrevious()
                            },
                            text = "Back",
                            fontSize = 14.sp,
                        )
                    } else {
                        Text(
                            modifier = Modifier.pulseClick {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDismiss()
                            },
                            text = "Skip tour",
                            fontSize = 14.sp,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .pulseClick {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNext()
                            }
                            .clip(RoundedCornerShape(style.cornerRadius))
                            .background(style.accentColor)
                            .padding(horizontal = 24.dp, vertical = 11.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (currentStep == totalSteps - 1) "Got it" else "Next",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.3.sp,
                        )
                    }
                }
            }
        }
    }
}