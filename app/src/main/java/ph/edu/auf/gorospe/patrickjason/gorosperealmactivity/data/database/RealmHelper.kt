package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.migration.AutomaticSchemaMigration
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.OwnerModel
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.PetModel

object RealmHelper {
    private lateinit var realmInstance: Realm

    fun initializeRealm() {
        val config = RealmConfiguration.Builder(schema = setOf(PetModel::class, OwnerModel::class))
            .name("petrealm.realm")
            .schemaVersion(2) // Use the property `schemaVersion` instead of a method
//            .initialData {
//                copyToRealm(PetModel().apply { name = "Browny"; age = 5; petType = "Dog" })
//                copyToRealm(OwnerModel().apply {
//                    name = "Angelo"; pets.addAll(
//                    listOf(
//                        PetModel().apply { name = "Choco"; age = 5; petType = "Dog" }
//                    )
//                )
//                })
//            }
            .build()

        realmInstance = Realm.open(config)
    }

    fun getRealmInstance(): Realm {
        if (!RealmHelper::realmInstance.isInitialized) {
            throw IllegalStateException("Realm is not initialized. Call initializeRealm() first")
        }
        return realmInstance
    }

    fun closeRealm() {
        if (RealmHelper::realmInstance.isInitialized && !realmInstance.isClosed()) {
            realmInstance.close()
        }
    }
}
