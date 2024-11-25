package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel

import androidx.room.PrimaryKey
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import java.util.UUID

class OwnerModel : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var name: String = ""
    var pets: RealmList<PetModel> = realmListOf()
}

data class OwnerData(
    val id: String,
    val name: String,
    val pets: List<PetModel> = emptyList()
)

fun OwnerModel.toOwnerData(): OwnerData {
    return OwnerData(
        id = this.id,
        name = this.name,
        pets = this.pets.toList()
    )
}