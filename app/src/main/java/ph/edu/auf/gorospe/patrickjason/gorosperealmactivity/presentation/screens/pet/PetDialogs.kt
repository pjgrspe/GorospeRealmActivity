package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.screens.pet

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.OwnerModel
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.PetData
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.PetModel
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.PetTypeWithIcon
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.components.CustomDialog
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.components.CustomDropdownMenuField
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.components.CustomTextField
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.components.PetTypeDropdown
import java.util.UUID

// Add Pet Dialog
@Composable
fun AddPetDialog(
    petTypes: List<PetTypeWithIcon>,
    owners: List<OwnerModel>,
    onDismiss: () -> Unit,
    onAddPet: (PetModel) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(petTypes.firstOrNull() ?: PetTypeWithIcon("Other", "🐾")) }
    var age by remember { mutableStateOf("") }
    var hasOwner by remember { mutableStateOf(false) }
    var selectedOwner by remember { mutableStateOf<OwnerModel?>(null) }
    var isError by remember { mutableStateOf(false) }

    CustomDialog(
        title = "Add New Pet",
        onConfirm = {
            if (name.isNotBlank() && age.isNotBlank()) {
                val parsedAge = age.toIntOrNull()
                if (parsedAge != null && parsedAge >= 0) {
                    val newPet = PetModel().apply {
                        id = UUID.randomUUID().toString()
                        this.name = name.trim()
                        petType = selectedType.type
                        this.age = parsedAge
                        hasOwner = hasOwner
                        ownerName = selectedOwner?.name
                    }
                    onAddPet(newPet)
                    onDismiss()
                    isError = false
                } else {
                    isError = true
                }
            } else {
                isError = true
            }
        },
        onDismiss = onDismiss,
        enableConfirm = !isError,
        content = {
            CustomTextField(
                value = name,
                placeholder = "Name",
                onValueChange = { name = it; isError = false },
                label = "Pet Name"
            )
            Spacer(modifier = Modifier.height(8.dp))
            PetTypeDropdown(
                petTypes = petTypes,
                selectedType = selectedType,
                onTypeSelect = { selectedType = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
            CustomTextField(
                value = age,
                placeholder = "Age",
                onValueChange = {
                    if (it.isEmpty() || it.toIntOrNull() != null) {
                        age = it
                        isError = false
                    }
                },
                label = "Age"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = hasOwner,
                    onCheckedChange = {
                        hasOwner = it
                        if (!it) selectedOwner = null
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = androidx.compose.ui.graphics.Color(0xFF64B5F6),
                        uncheckedColor = androidx.compose.ui.graphics.Color(0xFF64B5F6),
                        checkmarkColor = androidx.compose.ui.graphics.Color.White
                    )
                )
                Text("Has Owner")
            }
            if (hasOwner) {
                Spacer(modifier = Modifier.height(8.dp))
                CustomDropdownMenuField(
                    label = "Select Owner",
                    placeholder = "Select an owner",
                    selectedItem = selectedOwner,
                    items = owners,
                    onItemSelect = { selectedOwner = it },
                    itemText = { it.name }
                )
            }
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
fun EditPetDialog(
    petData: PetData,  // Now receives PetData instead of PetModel
    petTypes: List<PetTypeWithIcon>,
    owners: List<OwnerModel>,
    onDismiss: () -> Unit,
    onEditPet: (PetData) -> Unit
) {
    var name by remember { mutableStateOf(petData.name) }
    var selectedType by remember {
        mutableStateOf(petTypes.find { it.type == petData.petType } ?: PetTypeWithIcon("Other", "❔"))
    }
    var age by remember { mutableStateOf(petData.age.toString()) }
    var hasOwner by remember { mutableStateOf(petData.hasOwner) }
    var selectedOwner by remember { mutableStateOf(owners.firstOrNull { it.name == petData.ownerName }) }
    var isError by remember { mutableStateOf(false) }

    CustomDialog(
        title = "Edit Pet",
        onConfirm = {
            if (name.isNotBlank() && age.isNotBlank()) {
                val parsedAge = age.toIntOrNull()
                if (name.isNotBlank() && age.isNotBlank()) {
                    val parsedAge = age.toIntOrNull()
                    if (parsedAge != null && parsedAge >= 0) {
                        val updatedPet = PetData(  // Create new PetData
                            id = petData.id,
                            name = name.trim(),
                            petType = selectedType.type,
                            age = parsedAge,
                            hasOwner = hasOwner,
                            ownerName = selectedOwner?.name
                        )
                        onEditPet(updatedPet)
                        onDismiss()
                        isError = false
                    } else {
                        isError = true
                    }
                } else {
                    isError = true
                }
            } else {
                isError = true
            }
        },
        onDismiss = onDismiss,
        enableConfirm = !isError,
        content = {
            CustomTextField(
                value = name,
                placeholder = "Name",
                onValueChange = { name = it; isError = false },
                label = "Pet Name"
            )
            Spacer(modifier = Modifier.height(8.dp))
            PetTypeDropdown(
                petTypes = petTypes,
                selectedType = selectedType,
                onTypeSelect = { selectedType = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
            CustomTextField(
                value = age,
                placeholder = "Age",
                onValueChange = {
                    if (it.isEmpty() || it.toIntOrNull() != null) {
                        age = it
                        isError = false
                    }
                },
                label = "Age"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = hasOwner,
                    onCheckedChange = {
                        hasOwner = it
                        if (!it) selectedOwner = null
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = androidx.compose.ui.graphics.Color(0xFF64B5F6),
                        uncheckedColor = androidx.compose.ui.graphics.Color(0xFF64B5F6),
                        checkmarkColor = androidx.compose.ui.graphics.Color.White
                    )
                )
                Text("Has Owner")
            }
            if (hasOwner) {
                Spacer(modifier = Modifier.height(8.dp))
                CustomDropdownMenuField(
                    label = "Select Owner",
                    placeholder = "Select an owner",
                    selectedItem = selectedOwner,
                    items = owners,
                    onItemSelect = { selectedOwner = it },
                    itemText = { it.name }
                )
            }
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
fun AdoptPetDialog(
    petData: PetData,
    owners: List<OwnerModel>,
    onDismiss: () -> Unit,
    onAdopt: (String, String) -> Unit
) {
    var selectedOwner by remember { mutableStateOf<OwnerModel?>(null) }
    var isError by remember { mutableStateOf(false) }

    // Filter out the current owner from the available owners list
    val availableOwners = remember(owners, petData.ownerName) {
        owners.filter { it.name != petData.ownerName }
    }

    CustomDialog(
        title = "Adopt Pet",
        onConfirm = {
            if (selectedOwner != null) {
                onAdopt(petData.id, selectedOwner?.name ?: "")
                onDismiss()
                isError = false
            } else {
                isError = true
            }
        },
        onDismiss = onDismiss,
        enableConfirm = !isError,
        content = {
            if (availableOwners.isEmpty()) {
                Text(
                    "No other owners available for adoption.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text("Select an owner for ${petData.name}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                CustomDropdownMenuField(
                    label = "Select Owner",
                    placeholder = "Select an owner",
                    selectedItem = selectedOwner,
                    items = availableOwners,
                    onItemSelect = { selectedOwner = it },
                    itemText = { it.name }
                )
                if (isError) {
                    Text(
                        "Please select an owner before confirming.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}