package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

// Remove the Icons imports since we won't need them anymore
data class PetTypeWithIcon(
    val type: String,
    val emoji: String  // Changed from ImageVector to String
)

fun getDefaultPetTypes(): List<PetTypeWithIcon> = listOf(
    PetTypeWithIcon("Dog", "🐕"),
    PetTypeWithIcon("Cat", "🐈"),
    PetTypeWithIcon("Bird", "🦜"),
    PetTypeWithIcon("Fish", "🐠"),
    PetTypeWithIcon("Hamster", "🐹"),
    PetTypeWithIcon("Rabbit", "🐰"),
    PetTypeWithIcon("Other", "🐾")
)