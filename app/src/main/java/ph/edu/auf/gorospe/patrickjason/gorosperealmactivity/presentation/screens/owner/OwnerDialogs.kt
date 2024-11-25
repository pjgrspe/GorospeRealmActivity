package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.OwnerData
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.PetModel
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.components.CustomDialog
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.components.CustomTextField
import java.util.UUID

@Composable
fun AddOwnerDialog(
    pets: List<PetModel>,
    onDismiss: () -> Unit,
    onAddOwner: (OwnerData) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedPets by remember { mutableStateOf(listOf<PetModel>()) }
    var isError by remember { mutableStateOf(false) }

    CustomDialog(
        title = "Add New Owner",
        onConfirm = {
            if (name.isNotBlank()) {
                val newOwner = OwnerData(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    pets = selectedPets
                )
                onAddOwner(newOwner)
                onDismiss()
                isError = false
            } else {
                isError = true
            }
        },
        onDismiss = onDismiss,
        enableConfirm = !isError,
        content = {
            CustomTextField(
                value = name,
                onValueChange = {
                    name = it
                    isError = false
                },
                label = "Owner Name",
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text
                )
            )

            if (isError) {
                Text(
                    "Please fill all required fields correctly",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Composable
fun EditOwnerDialog(
    ownerData: OwnerData,
    pets: List<PetModel>,
    onDismiss: () -> Unit,
    onEditOwner: (OwnerData) -> Unit
) {
    var name by remember { mutableStateOf(ownerData.name) }
    var isError by remember { mutableStateOf(false) }

    CustomDialog(
        title = "Edit Owner",
        onConfirm = {
            if (name.isNotBlank()) {
                val editedOwner = ownerData.copy(
                    name = name.trim()
                )
                onEditOwner(editedOwner)
                onDismiss()
            } else {
                isError = true
            }
        },
        onDismiss = onDismiss,
        enableConfirm = !isError,
        content = {
            CustomTextField(
                value = name,
                onValueChange = {
                    name = it
                    isError = it.isBlank()
                },
                label = "Owner Name",
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text
                )
            )

            if (isError) {
                Text(
                    "Please fill all required fields correctly",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}