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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Segmented progress bar with "X / N" counter.
 * Active → accent gradient. Done → muted accent. Future → near-transparent.
 */
@Composable
fun StepIndicator(
    cornerRadius: Dp,
    accentColor: Color,
    current: Int,
    total: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${current + 1} / $total",
            color = accentColor.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.5.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(accentColor.copy(alpha = 0.3f))
                .border(1.dp, accentColor.copy(alpha = 0.15f), RoundedCornerShape(cornerRadius))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        )

        repeat(total) { i ->
            StepDot(
                isSelected = i == current,
                isPast = i < current,
                accentColor = accentColor,
            )
        }
    }
}

@Composable
private fun StepDot(isSelected: Boolean, isPast: Boolean, accentColor: Color) {
    val width by animateDpAsState(
        targetValue = if (isSelected) 20.dp else 6.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "step_width",
    )

    val color by animateColorAsState(
        targetValue = when {
            isSelected -> accentColor
            isPast -> accentColor.copy(alpha = 0.5f)
            else -> accentColor.copy(alpha = 0.3f)
        },
        label = "step_color",
    )

    Box(
        modifier = Modifier
            .width(width)
            .height(6.dp)
            .clip(dotShape)
            .background(color),
    )
}

private val dotShape = RoundedCornerShape(50)