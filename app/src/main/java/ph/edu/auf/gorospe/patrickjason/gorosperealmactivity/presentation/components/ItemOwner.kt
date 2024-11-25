package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.OwnerData
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.OwnerModel

// ItemOwner.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemOwner(
    ownerData: OwnerData,
    onRemove: (OwnerData) -> Unit,
    onEdit: (OwnerData) -> Unit
) {
    val currentItem by rememberUpdatedState(ownerData)

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onRemove(currentItem)
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onEdit(currentItem)
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
        positionalThreshold = { it * .25f }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFFE57373)
                SwipeToDismissBoxValue.EndToStart -> Color(0xFF4CAF50)
                SwipeToDismissBoxValue.Settled -> Color.Transparent
            }
            val alignment = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.Settled -> Alignment.Center
            }
            val icon = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Filled.Delete
                SwipeToDismissBoxValue.EndToStart -> Icons.Filled.Edit
                SwipeToDismissBoxValue.Settled -> null
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFFFFF)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "👤 ${ownerData.name}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF5D4037),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Owns ${ownerData.pets.size} pets",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    if (ownerData.pets.isNotEmpty()) {
                        Text(
                            text = "Pets: ${ownerData.pets.joinToString(", ") { it.name }}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF5D4037).copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }
        }
    )
}



