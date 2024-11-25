package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.PetModel
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.getDefaultPetTypes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemPet(
    petModel: PetModel,
    onRemove: (PetModel) -> Unit,
    onEdit: (PetModel) -> Unit,
    onAdopt: (PetModel) -> Unit
) {
    val currentItem by rememberUpdatedState(petModel)
    var dismissDirection by remember { mutableStateOf<SwipeToDismissBoxValue?>(null) }

    // Get emoji from PetTypes
    val petEmoji = getDefaultPetTypes()
        .find { it.type == petModel.petType }
        ?.emoji ?: "🐾"

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onRemove(currentItem)
                    dismissDirection = dismissValue // Update direction
                    false // Don't dismiss yet, wait for onRemove result
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onEdit(currentItem)
                    false
                }
                else -> false
            }
        },
        positionalThreshold = { it * 0.25f }
    )

    // Animate offset based on dismiss state and direction
    val offset = animateDpAsState(
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> if (dismissDirection == SwipeToDismissBoxValue.StartToEnd) (-100).dp else 0.dp
            SwipeToDismissBoxValue.EndToStart -> if (dismissDirection == SwipeToDismissBoxValue.EndToStart) 100.dp else 0.dp
            else -> 0.dp
        }
    )

    LaunchedEffect(key1 = dismissState.currentValue) {
        // Reset dismiss direction when dismissed or reset
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissDirection = null
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFFE57373)
                SwipeToDismissBoxValue.EndToStart -> Color(0xFF4CAF50)
                else -> Color.Transparent
            }
            val icon = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Filled.Delete
                SwipeToDismissBoxValue.EndToStart -> Icons.Filled.Edit
                else -> null
            }
            val alignment = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            Box(
                modifier = Modifier
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
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .offset(x = offset.value),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = petEmoji,
                                style = TextStyle(fontSize = 24.sp)
                            )
                            Column {
                                Text(
                                    text = petModel.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color(0xFF5D4037),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${petModel.age} years old",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF757575),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = petModel.petType,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF5D4037).copy(alpha = 0.7f)
                                    )
                                )
                                petModel.ownerName?.let {
                                    Text(
                                        text = "Owner: $it",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF64B5F6)
                                        )
                                    )
                                }
                            }
                        }
                        FilledIconButton(
                            onClick = { onAdopt(petModel) },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF64B5F6),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Adopt Pet"
                            )
                        }
                    }
                }
            }
        }
    )
}


