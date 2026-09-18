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

package io.github.astritveliu.showcaseoverlay.demo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import io.github.astritveliu.showcaseoverlay.ui.composable.ShowcaseScaffold
import io.github.astritveliu.showcaseoverlay.ui.state.ShowcaseStyle
import io.github.astritveliu.showcaseoverlay.ui.model.TargetShape
import io.github.astritveliu.showcaseoverlay.ui.state.rememberShowcaseState
import io.github.astritveliu.showcaseoverlay.utils.showcaseTarget


@Composable
fun DemoScreen(modifier: Modifier) {
    val context = LocalContext.current

    val showcase = rememberShowcaseState(
        autoStartAfter = 0,
        onFinished = {
            Toast.makeText(context, "Tour finished", Toast.LENGTH_LONG).show()
        },
    )

    ShowcaseScaffold(
        states = arrayOf(showcase),
        style = ShowcaseStyle(
            accentColor = Color(0xFF6750A4),
            cardColor = MaterialTheme.colorScheme.primaryContainer,
            closeIcon = ImageVector.vectorResource(R.drawable.baseline_close_fullscreen_24)
        ),
    ) {
        Surface(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = "ShowcaseOverlay demo",
                    style = MaterialTheme.typography.headlineSmall,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .showcaseTarget(
                                state = showcase,
                                index = 0,
                                title = "Your profile",
                                description = "Tap here any time to edit your account.",
                                shape = TargetShape.Circle,
                            ),
                    )

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .showcaseTarget(
                                state = showcase,
                                index = 1,
                                title = "Notifications",
                                description = "New activity shows up here first.",
                                shape = TargetShape.Circle,
                            ),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .showcaseTarget(
                            state = showcase,
                            index = 2,
                            title = "Main content",
                            description = "This is where most of your time is spent.",
                            shape = TargetShape.RoundedRect,
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .padding(24.dp),
                ) {
                    Text("Main content card")
                }

                Text(
                    text = "Start tour",
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { showcase.start() }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
