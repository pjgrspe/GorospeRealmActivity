package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel

import androidx.room.Ignore
import androidx.room.PrimaryKey
import io.realm.kotlin.types.RealmObject
import java.util.UUID

class PetModel : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var name: String = ""
    var petType: String = ""
    var age: Int = 0
    var hasOwner: Boolean = false
    var ownerName: String? = null
}

// New data class to hold pet data
data class PetData(
    val id: String,
    val name: String,
    val petType: String,
    val age: Int,
    val hasOwner: Boolean,
    val ownerName: String?
)

// Extension function to convert PetModel to PetData
fun PetModel.toPetData() = PetData(
    id = this.id,
    name = this.name,
    petType = this.petType,
    age = this.age,
    hasOwner = this.hasOwner,
    ownerName = this.ownerName
)