package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.screens.pet
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.RealmHelper
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.OwnerModel
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.PetData
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.PetModel
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.PetTypeWithIcon
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.getDefaultPetTypes
class PetViewModel : ViewModel() {
    private val _petTypes = MutableStateFlow(getDefaultPetTypes())
    val petTypes: StateFlow<List<PetTypeWithIcon>> = _petTypes.asStateFlow()
    private val _pets = MutableStateFlow<List<PetModel>>(emptyList())
    val pets: StateFlow<List<PetModel>> = _pets.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _showSnackbar = MutableSharedFlow<String>()
    val showSnackbar: SharedFlow<String> = _showSnackbar
    private val _owners = MutableStateFlow<List<OwnerModel>>(emptyList())
    val owners: StateFlow<List<OwnerModel>> = _owners.asStateFlow()
    init {
        loadPets()
        observeSearchQuery()
        loadOwners()
    }
    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .collect { query ->
                    if (query.isEmpty()) {
                        loadPets()
                    } else {
                        searchPets(query)
                    }
                }
        }
    }
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    private fun loadPets() {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            val results = realm.query<PetModel>().find()
            _pets.value = results
        }
    }
    private fun loadOwners() {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            val results = realm.query<OwnerModel>().find()
            _owners.value = results
        }
    }
    private fun searchPets(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            val results = realm.query<PetModel>("name CONTAINS[c] $0 OR petType CONTAINS[c] $0", query)
                .find()
            _pets.value = results
        }
    }
    fun addPet(newPet: PetData) {
        viewModelScope.launch {
            val realm = RealmHelper.getRealmInstance()
            realm.write {
                // Create a new pet instance
                val pet = PetModel().apply {
                    this.name = newPet.name
                    this.petType = newPet.petType
                    this.age = newPet.age
                    this.hasOwner = newPet.hasOwner  // Now uses the passed hasOwner parameter
                    this.ownerName = newPet.ownerName
                }
                val createdPet = copyToRealm(pet)
                Log.d("PetAdd", "Created Pet: ${createdPet.name}, ID: ${createdPet.id}")
                Log.d("PetAdd", "hasOwner value: ${createdPet.hasOwner}")
                Log.d("PetAdd", "ownerName value: ${createdPet.ownerName}")

                // Only add to owner if hasOwner is true and ownerName is not null
                if (createdPet.hasOwner && !createdPet.ownerName.isNullOrEmpty()) {
                    val existingOwner = query<OwnerModel>("name == $0", createdPet.ownerName).first().find()
                    existingOwner?.let { owner ->
                        val latestOwner = findLatest(owner)
                        latestOwner?.pets?.add(createdPet)
                        Log.d("PetAdd", "Owner ${latestOwner?.name} Pet List: ${latestOwner?.pets?.map { it.name }}")
                        Log.d("PetAdd", "Total Pets for Owner: ${latestOwner?.pets?.size}")
                    }
                }
            }
            loadPets() // Refresh pets list
            viewModelScope.launch {
                _showSnackbar.emit("Pet added successfully")
            }
        }
    }
    fun updatePet(updatedPet: PetData) {
        viewModelScope.launch {
            val realm = RealmHelper.getRealmInstance()
            try {
                realm.write {
                    // Find and update the pet within the same write transaction
                    val existingPet = this.query<PetModel>("id == $0", updatedPet.id).first().find()
                        ?: throw Exception("Pet not found")
                    // If the pet has an owner and the owner name is changing, we need to handle the owner relationships
                    if (existingPet.hasOwner && existingPet.ownerName != updatedPet.ownerName) {
                        // Remove pet from old owner if exists
                        val oldOwner = this.query<OwnerModel>("name == $0", existingPet.ownerName).first().find()
                        oldOwner?.pets?.removeAll { it.id == existingPet.id }
                        // Add pet to new owner if specified
                        if (updatedPet.hasOwner && !updatedPet.ownerName.isNullOrEmpty()) {
                            val newOwner = this.query<OwnerModel>("name == $0", updatedPet.ownerName).first().find()
                            newOwner?.pets?.add(existingPet)
                        }
                    }
                    // Update pet details
                    existingPet.apply {
                        name = updatedPet.name
                        petType = updatedPet.petType
                        age = updatedPet.age
                        hasOwner = updatedPet.hasOwner
                        ownerName = updatedPet.ownerName
                    }
                }
                // Refresh the lists after the transaction is complete
                loadPets()
                loadOwners()
                _showSnackbar.emit("Pet updated successfully")
            } catch (e: Exception) {
                _showSnackbar.emit("Error updating pet: ${e.message}")
            }
        }
    }
    fun deletePet(petId: String) {
        viewModelScope.launch {
            val realm = RealmHelper.getRealmInstance()
            realm.write {
                val pet = this.query<PetModel>("id == $0", petId).first().find()
                if (pet == null) {
                    Log.e("PetDelete", "Pet with ID $petId not found.")
                    viewModelScope.launch { _showSnackbar.emit("Pet not found.") }
                    return@write
                }
                // Check if the pet has an owner
                if (pet.hasOwner) {
                    Log.e("PetDelete", "Cannot delete pet with ID $petId because it has an owner.")
                    viewModelScope.launch {
                        _showSnackbar.emit("Cannot delete this pet because it has an owner. Please remove owner association first.")
                    }
                    return@write
                }
                // If we get here, the pet has no owner and can be safely deleted
                delete(pet)
                Log.d("PetDelete", "Pet deleted successfully.")
                viewModelScope.launch { _showSnackbar.emit("Pet deleted successfully.") }
            }
            loadPets()
        }
    }
    fun adoptPet(petId: String, ownerName: String) {
        viewModelScope.launch {
            val realm = RealmHelper.getRealmInstance()
            try {
                realm.write {
                    // Find the owner by name
                    val owner = this.query<OwnerModel>("name == $0", ownerName).first().find()
                        ?: throw Exception("Owner not found")
                    // Find the pet by its ID
                    val pet = this.query<PetModel>("id == $0", petId).first().find()
                        ?: throw Exception("Pet not found")
                    // Ensure pet is not null before calling findLatest
                    val latestPet = findLatest(pet)
                        ?: throw Exception("Pet not found")
                    // Check if pet is already adopted
                    if (latestPet.hasOwner) {
                        throw Exception("This pet is already adopted by ${latestPet.ownerName}")
                    }
                    // Check if owner already has this pet
                    val isDuplicate = owner.pets.any { it.id == petId }
                    if (isDuplicate) {
                        throw Exception("You have already adopted this pet")
                    }
                    // Update pet's owner status
                    latestPet.hasOwner = true
                    latestPet.ownerName = ownerName
                    // Add pet to owner's list of pets
                    owner.pets.add(latestPet)
                }
                // Refresh pets and owners lists
                loadPets()
                loadOwners()
                _showSnackbar.emit("Pet adopted successfully")
            } catch (e: Exception) {
                _showSnackbar.emit("Pet already has an owner, edit pet instead to transfer ownership")
            }
        }
    }
}