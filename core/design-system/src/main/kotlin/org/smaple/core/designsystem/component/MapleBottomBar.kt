package org.smaple.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.smaple.core.designsystem.theme.MapleAmber
import org.smaple.core.designsystem.theme.Ink
import org.smaple.core.designsystem.theme.Line
import org.smaple.core.designsystem.theme.MapleCaption
import org.smaple.core.designsystem.theme.MapleDimensions
import org.smaple.core.designsystem.theme.MapleSpacing
import org.smaple.core.designsystem.theme.MapleTheme
import org.smaple.core.designsystem.theme.Mist2
import org.smaple.core.designsystem.theme.Moss
import org.smaple.core.designsystem.theme.PillShape
import org.smaple.core.designsystem.theme.Pine
import org.smaple.core.designsystem.theme.White

data class MapleBottomBarItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int? = null
)

/**
 * Standard S-MAPLE Bottom Bar (Section 3.3, 5).
 * - 96 dp tall, White surface, 1 dp top border.
 * - At most three navigation destinations.
 * - Active item: Mist 2 pill background behind icon, bold Pine label.
 * - Inactive item: plain icon, Moss label.
 * - Badge: small amber circle with Ink number.
 */
@Composable
fun MapleBottomBar(
    items: List<MapleBottomBarItem>,
    selectedItemId: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    require(items.size in 1..3) { "Bottom bar supports at most 3 navigation items" }

    Surface(
        color = White,
        modifier = modifier
            .fillMaxWidth()
            .height(MapleDimensions.bottomBarHeight)
    ) {
        Column {
            Divider(color = Line, thickness = MapleDimensions.borderWidth)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = item.id == selectedItemId
                    MapleBottomBarTab(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onItemSelected(item.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MapleBottomBarTab(
    item: MapleBottomBarItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(PillShape)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(vertical = MapleSpacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(if (isSelected) Mist2 else White)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = if (isSelected) Pine else Moss,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Count badge
            if (item.badgeCount != null && item.badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(MapleAmber),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.badgeCount.toString(),
                        color = Ink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = item.label,
            style = MapleCaption,
            color = if (isSelected) Pine else Moss,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MapleBottomBarPreview() {
    MapleTheme {
        MapleBottomBar(
            items = listOf(
                MapleBottomBarItem("home", "Home", Icons.Default.Home),
                MapleBottomBarItem("notes", "My notes", Icons.Default.Description, badgeCount = 3),
                MapleBottomBarItem("me", "Me", Icons.Default.Person)
            ),
            selectedItemId = "home",
            onItemSelected = {}
        )
    }
}
