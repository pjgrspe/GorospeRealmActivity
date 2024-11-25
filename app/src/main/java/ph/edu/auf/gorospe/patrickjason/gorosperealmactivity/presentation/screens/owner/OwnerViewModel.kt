package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.screens.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.RealmHelper
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.OwnerData
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.OwnerModel
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.PetModel
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.toOwnerData

class OwnerViewModel : ViewModel() {
    // StateFlow for managing the list of owners in the UI
    private val _owners = MutableStateFlow<List<OwnerData>>(emptyList())
    val owners: StateFlow<List<OwnerData>> = _owners

    // StateFlow for handling search functionality
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // SharedFlow for displaying snackbar messages to the user
    private val _showSnackbar = MutableSharedFlow<String>()
    val showSnackbar: SharedFlow<String> = _showSnackbar

    init {
        loadOwners()
        observeSearchQuery()
    }

    // Observes search query changes with debounce to prevent rapid database queries
    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .collect { query ->
                    if (query.isEmpty()) loadOwners() else searchOwners(query)
                }
        }
    }

    // Updates the current search query value
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Loads all owners from the database
    private fun loadOwners() {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            val results = realm.query<OwnerModel>().find()
            _owners.value = results.map { it.toOwnerData() }
        }
    }

    // Performs case-insensitive search on owner names
    private fun searchOwners(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            val results = realm.query<OwnerModel>("name CONTAINS[c] $0", query)
                .find()
            _owners.value = results.map { it.toOwnerData() }
        }
    }

    // Updates existing owner information and manages associated pet relationships
    fun updateOwner(ownerData: OwnerData) {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()

            try {
                // Check for duplicate owner names before updating
                val existingOwner = realm.query<OwnerModel>("name CONTAINS[c] $0 AND id != $1",
                    ownerData.name, ownerData.id)
                    .first()
                    .find()

                if (existingOwner != null) {
                    _showSnackbar.emit("An owner with this name already exists")
                    return@launch
                }

                realm.write {
                    // Find and update the owner's information
                    val owner = query<OwnerModel>("id == $0", ownerData.id).first().find()

                    owner?.let { currentOwner ->
                        val oldName = currentOwner.name
                        currentOwner.name = ownerData.name

                        // Update owner name reference in associated pets
                        currentOwner.pets.forEach { pet ->
                            findLatest(pet)?.apply {
                                if (this.ownerName == oldName) {
                                    this.ownerName = ownerData.name
                                }
                            }
                        }

                        // Update owner name in pets that might reference this owner but aren't in the pets list
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

    // Adds a new owner to the database and associates selected pets
    fun addOwner(ownerData: OwnerData) {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()

            try {
                realm.write {
                    // Create new owner with provided details
                    val newOwner = OwnerModel().apply {
                        id = ownerData.id
                        name = ownerData.name
                    }

                    // Associate selected pets with the new owner
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

    // Deletes an owner and updates pet relationships accordingly
    fun deleteOwner(ownerData: OwnerData) {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            try {
                realm.write {
                    // Find owner and remove all pet associations before deletion
                    val owner = query<OwnerModel>("id == $0", ownerData.id).first().find()
                    owner?.let { ownerModel ->
                        // Remove owner references from all associated pets
                        ownerModel.pets.forEach { pet ->
                            findLatest(pet)?.apply {
                                hasOwner = false
                                ownerName = null
                            }
                        }
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