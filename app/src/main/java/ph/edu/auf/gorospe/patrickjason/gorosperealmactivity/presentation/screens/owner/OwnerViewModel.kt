package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.screens.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.RealmHelper
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.OwnerData
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.OwnerModel
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.PetModel
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.toOwnerData

class OwnerViewModel : ViewModel() {
    private val _owners = MutableStateFlow<List<OwnerData>>(emptyList())
    val owners: StateFlow<List<OwnerData>> = _owners

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showSnackbar = MutableSharedFlow<String>()
    val showSnackbar: SharedFlow<String> = _showSnackbar

    init {
        loadOwners()
        observeSearchQuery()
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .collect { query ->
                    if (query.isEmpty()) {
                        loadOwners()
                    } else {
                        searchOwners(query)
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun loadOwners() {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            val results = realm.query<OwnerModel>().find()
            _owners.value = results.map { it.toOwnerData() }
        }
    }

    private fun searchOwners(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            val results = realm.query<OwnerModel>("name CONTAINS[c] $0", query)
                .find()
            _owners.value = results.map { it.toOwnerData() }
        }
    }

    fun updateOwner(ownerData: OwnerData) {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()

            try {
                // Check if another owner already has this name
                val existingOwner = realm.query<OwnerModel>("name CONTAINS[c] $0 AND id != $1",
                    ownerData.name, ownerData.id)
                    .first()
                    .find()

                if (existingOwner != null) {
                    _showSnackbar.emit("An owner with this name already exists")
                    return@launch
                }

                realm.write {
                    // Find the owner to update
                    val owner = query<OwnerModel>("id == $0", ownerData.id).first().find()

                    owner?.let { currentOwner ->
                        // Store the old name before updating
                        val oldName = currentOwner.name

                        // Update owner's name
                        currentOwner.name = ownerData.name

                        // Update ownerName in all associated pets
                        currentOwner.pets.forEach { pet ->
                            findLatest(pet)?.apply {
                                if (this.ownerName == oldName) {
                                    this.ownerName = ownerData.name
                                }
                            }
                        }

                        // Also update any pets that might reference this owner but aren't in the pets list
                        query<PetModel>("ownerName == $0", oldName).find().forEach { pet ->
                            findLatest(pet)?.apply {
                                this.ownerName = ownerData.name
                            }
                        }
                    }
                }
                loadOwners()
                _showSnackbar.emit("Owner updated successfully")
            } catch (e: Exception) {
                _showSnackbar.emit("Error updating owner: ${e.message}")
            }
        }
    }

    fun addOwner(ownerData: OwnerData) {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()

            try {
                realm.write {
                    val newOwner = OwnerModel().apply {
                        id = ownerData.id
                        name = ownerData.name
                    }

                    // Associate selected pets with the owner
                    ownerData.pets.forEach { pet ->
                        val realmPet = query<PetModel>("id == $0", pet.id).first().find()
                        realmPet?.let {
                            it.hasOwner = true
                            it.ownerName = ownerData.name
                            newOwner.pets.add(it)
                        }
                    }

                    copyToRealm(newOwner)
                }

                loadOwners()
                _showSnackbar.emit("Owner added successfully")
            } catch (e: Exception) {
                _showSnackbar.emit("Error adding owner: ${e.message}")
            }
        }
    }

    fun deleteOwner(ownerData: OwnerData) {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            try {
                realm.write {
                    // Find all pets associated with this owner and update them
                    val owner = query<OwnerModel>("id == $0", ownerData.id).first().find()
                    owner?.let { ownerModel ->
                        // Update all pets to remove owner association
                        ownerModel.pets.forEach { pet ->
                            findLatest(pet)?.apply {
                                hasOwner = false
                                ownerName = null
                            }
                        }
                        // Delete the owner
                        delete(ownerModel)
                    }
                }

                loadOwners()
                _showSnackbar.emit("Owner deleted successfully")
            } catch (e: Exception) {
                _showSnackbar.emit("Error deleting owner: ${e.message}")
            }
        }
    }
}