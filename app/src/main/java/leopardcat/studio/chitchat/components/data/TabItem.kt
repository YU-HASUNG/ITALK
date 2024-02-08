package leopardcat.studio.chitchat.components.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.ui.graphics.vector.ImageVector

data class TabItem (
    val title: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
)

val tabItems = listOf(
    TabItem(
        title = "게시물",
        unselectedIcon = Icons.Outlined.PhotoCamera,
        selectedIcon = Icons.Filled.PhotoCamera
    ),
    TabItem(
        title = "HOT 게시물",
        unselectedIcon = Icons.Outlined.LocalFireDepartment,
        selectedIcon = Icons.Filled.LocalFireDepartment
    )
)