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

import android.graphics.BlurMaskFilter
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativePaint
import androidx.compose.ui.graphics.toArgb
import io.github.astritveliu.showcaseoverlay.ui.model.ShowcaseTarget
import io.github.astritveliu.showcaseoverlay.ui.model.TargetShape

// Geometry helpers
private fun animatedRect(
    animCenter: Offset,
    animHalfW: Float,
    animHalfH: Float,
    extra: Float = 0f,
): Rect = Rect(
    left = animCenter.x - animHalfW - extra,
    top = animCenter.y - animHalfH - extra,
    right = animCenter.x + animHalfW + extra,
    bottom = animCenter.y + animHalfH + extra,
)

private fun animatedRadius(
    target: ShowcaseTarget,
    animHalfW: Float,
    animHalfH: Float,
    extra: Float = 0f,
): Float = maxOf(animHalfW, animHalfH) * target.scaleFactor + target.padding + extra

// Canvas draw functions

fun DrawScope.drawCutout(
    target: ShowcaseTarget,
    animCenter: Offset,
    animHalfW: Float,
    animHalfH: Float,
    pulse: Float,
    cornerRadius: Float,
    progress: Float = 1f,
) {
    when (target.shape) {
        TargetShape.Circle -> drawCircle(
            color = Color.Transparent,
            radius = animatedRadius(target, animHalfW, animHalfH, pulse) * progress,
            center = animCenter,
            blendMode = BlendMode.Clear,
        )

        TargetShape.RoundedRect -> {
            val r = animatedRect(animCenter, animHalfW * progress, animHalfH * progress, pulse)
            drawRoundRect(
                color = Color.Transparent,
                topLeft = r.topLeft,
                size = r.size,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                blendMode = BlendMode.Clear,
            )
        }
    }
}

fun DrawScope.drawGlowHalos(
    accentColor: Color,
    target: ShowcaseTarget,
    animCenter: Offset,
    animHalfW: Float,
    animHalfH: Float,
    pulse: Float,
    intensity: Float,
    cornerRadius: Float,
) {
    data class Halo(val blur: Float, val stroke: Float, val alphaScale: Float)
    drawIntoCanvas { canvas ->
        listOf(
            Halo(28f, 8f, 0.35f),
            Halo(12f, 3.5f, 0.7f),
        ).forEach { halo ->
            val nativePaint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = halo.stroke
                color = accentColor.copy(alpha = intensity * halo.alphaScale).toArgb()
                maskFilter = BlurMaskFilter(halo.blur, BlurMaskFilter.Blur.NORMAL)
            }
            val p = Paint().also { it.nativePaint.set(nativePaint) }
            when (target.shape) {
                TargetShape.Circle -> canvas.drawCircle(
                    center = animCenter,
                    radius = animatedRadius(target, animHalfW, animHalfH, pulse) + halo.stroke,
                    paint = p,
                )

                TargetShape.RoundedRect -> {
                    val r = animatedRect(animCenter, animHalfW, animHalfH, pulse + halo.stroke)
                    canvas.drawRoundRect(
                        r.left,
                        r.top,
                        r.right,
                        r.bottom,
                        cornerRadius,
                        cornerRadius,
                        p,
                    )
                }
            }
        }
    }
}