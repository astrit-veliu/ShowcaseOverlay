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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.astritveliu.showcaseoverlay.ShowcaseOverlay
import io.github.astritveliu.showcaseoverlay.ui.state.ShowcaseStyle
import io.github.astritveliu.showcaseoverlay.ui.state.ShowcaseState

/**
 * Wrapper that places [content] behind one [io.github.astritveliu.showcaseoverlay.ShowcaseOverlay] per [states] entry.
 *
 * Each overlay is a last child of the [Box], so it always renders on top of
 * all screen content, including custom backdrops and pagers.
 *
 * @param modifier Applied to the root [Box].
 * @param states The [ShowcaseState]s driving each overlay. Pass more than one
 *   when a single screen has independent, sequential tours (e.g. a tour that
 *   only starts after another one finishes).
 * @param style Visual style shared by every overlay. See [ShowcaseStyle].
 * @param content Your screen content — backdrop, scaffold, pager, etc.
 */
@Composable
fun ShowcaseScaffold(
    modifier: Modifier = Modifier,
    vararg states: ShowcaseState,
    style: ShowcaseStyle = ShowcaseStyle(),
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        content()
        states.forEach { state ->
            ShowcaseOverlay(state = state, style = style)
        }
    }
}
