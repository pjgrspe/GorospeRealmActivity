package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.PetTypeWithIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CustomDropdownMenuField(
    label: String,
    selectedItem: T?,
    items: List<T>,
    placeholder: String = "",
    onItemSelect: (T) -> Unit,
    itemText: @Composable (T) -> String,
    itemEmoji: (@Composable (T) -> String?)? = null,  // Changed from ImageVector to String
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedItemText = selectedItem?.let { itemText(it) } ?: ""
    var textFieldWidth by remember { mutableStateOf(0) }

    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF5D4037),
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Box {
            OutlinedTextField(
                value = selectedItemText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowDropDown,
                            contentDescription = "Expand Dropdown",
                            tint = Color.Black
                        )
                    }
                },
                placeholder = {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray
                        )
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White,
                    unfocusedBorderColor = Color(0xFF5D4037),
                    focusedBorderColor = Color(0xFF5D4037),
                    cursorColor = Color(0xFF81C784),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.DarkGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        textFieldWidth = coordinates.size.width
                    }
                    .shadow(2.dp, RoundedCornerShape(16.dp))
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(with(LocalDensity.current) { textFieldWidth.toDp() })
                    .background(Color.White)
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                itemEmoji?.invoke(item)?.let { emoji ->
                                    Text(
                                        text = emoji,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                Text(
                                    text = itemText(item),
                                    color = Color(0xFF5D4037)
                                )
                            }
                        },
                        onClick = {
                            onItemSelect(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PetTypeDropdown(
    petTypes: List<PetTypeWithIcon>,
    selectedType: PetTypeWithIcon?,
    onTypeSelect: (PetTypeWithIcon) -> Unit
) {
    CustomDropdownMenuField(
        label = "Pet Type",
        selectedItem = selectedType,
        items = petTypes,
        onItemSelect = onTypeSelect,
        placeholder = "Choose one...",
        itemText = { it.type },
        itemEmoji = { it.emoji }  // Changed from icon to emoji
    )
}