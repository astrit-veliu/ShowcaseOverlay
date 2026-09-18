package io.github.astritveliu.showcaseoverlay.ui.state

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Visual configuration for [io.github.astritveliu.showcaseoverlay.ShowcaseOverlay] / [io.github.astritveliu.showcaseoverlay.ui.composable.ShowcaseScaffold].
 *
 * This is a plain, host-app-independent value object rather than a
 * `CompositionLocal` - pass it explicitly wherever you build an overlay, or
 * define your own app-wide default (e.g. `val AppShowcaseStyle =
 * ShowcaseStyle(accentColor = MyTheme.colors.primary)`) and reuse it.
 *
 * @param accentColor Color used for the glow ring, progress dots and CTA button.
 * @param scrimColor Background color drawn behind the cutout.
 * @param cardColor Background color of the tooltip card.
 * @param cornerRadius Corner radius used for the tooltip card and the
 *   rounded-rect cutout shape.
 */
@Immutable
data class ShowcaseStyle(
    val accentColor: Color = Color(0xFFE53935),
    val scrimColor: Color = Color(0xE5080008),
    val cardColor: Color = Color(0xFFFFFFFF),
    val cornerRadius: Dp = 16.dp,
    val closeIcon: ImageVector? = null
)