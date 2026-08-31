package dev.caferati.awesomebutton.demo

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp

internal enum class DemoIconAsset(
    @get:DrawableRes val drawableRes: Int,
) {
    Paintbrush(R.drawable.ic_fa_paintbrush),
    Gauge(R.drawable.ic_fa_gauge),
    ShareNodes(R.drawable.ic_fa_share_nodes),
    SizeChanges(R.drawable.ic_fa_up_right_and_down_left_from_center),
    RightLeft(R.drawable.ic_fa_right_left),
    ForwardStep(R.drawable.ic_fa_forward_step),
    Bars(R.drawable.ic_fa_bars),
    TableCellsLarge(R.drawable.ic_fa_table_cells_large),
    TrashCan(R.drawable.ic_fa_trash_can),
    SquarePlus(R.drawable.ic_fa_square_plus),
    UserPlus(R.drawable.ic_fa_user_plus),
    LocationArrow(R.drawable.ic_fa_location_arrow),
    Facebook(R.drawable.ic_fa_facebook_f),
    X(R.drawable.ic_fa_x_twitter),
    Messenger(R.drawable.ic_fa_facebook_messenger),
    Instagram(R.drawable.ic_fa_instagram),
    Whatsapp(R.drawable.ic_fa_whatsapp),
    Youtube(R.drawable.ic_fa_youtube),
    Linkedin(R.drawable.ic_fa_linkedin_in),
    Pinterest(R.drawable.ic_fa_pinterest_p),
}

@Composable
internal fun DemoIcon(
    asset: DemoIconAsset,
    tint: Color,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(asset.drawableRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(tint),
        )
    }
}
