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
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.*

class PetViewModel : ViewModel() {
    // StateFlows for UI state management
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

    // Observes search query changes with debounce to prevent rapid database queries
    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQuery
                .debounce(300) // Prevent rapid firing of search queries
                .collect { query ->
                    if (query.isEmpty()) loadPets() else searchPets(query)
                }
        }
    }

    // Updates the current search query value
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Performs case-insensitive search on pet name and type
    private fun searchPets(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            val results = realm.query<PetModel>("name CONTAINS[c] $0 OR petType CONTAINS[c] $0", query)
                .find()
            _pets.value = results
        }
    }

    // Loads all pets from the database
    private fun loadPets() {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            _pets.value = realm.query<PetModel>().find()
        }
    }

    // Loads all owners from the database
    private fun loadOwners() {
        viewModelScope.launch(Dispatchers.IO) {
            val realm = RealmHelper.getRealmInstance()
            _owners.value = realm.query<OwnerModel>().find()
        }
    }

    // Adds a new pet to the database and associates it with an owner if specified
    fun addPet(name: String, type: String, age: Int, hasOwner: Boolean, ownerName: String?) {
        viewModelScope.launch {
            val realm = RealmHelper.getRealmInstance()

            realm.write {
                // Create new pet instance with provided details
                val pet = PetModel().apply {
                    this.name = name
                    this.petType = type
                    this.age = age
                    this.hasOwner = true
                    this.ownerName = ownerName
                }

                val createdPet = copyToRealm(pet)

                // Log pet creation details
                Log.d("PetAdd", "Created Pet: ${createdPet.name}, ID: ${createdPet.id}")
                Log.d("PetAdd", "hasOwner: $hasOwner, ownerName: $ownerName")

                // Associate pet with owner if one is specified
                query<OwnerModel>("name == $0", ownerName).first().find()?.let { owner ->
                    findLatest(owner)?.pets?.add(createdPet)
                    Log.d("PetAdd", "Owner ${owner.name} Pet List: ${owner.pets.map { it.name }}")
                }
            }

            loadPets()
            _showSnackbar.emit("Pet added successfully")
        }
    }

    // Updates existing pet information and manages owner relationships
    fun updatePet(updatedPet: PetData) {
        viewModelScope.launch {
            val realm = RealmHelper.getRealmInstance()
            try {
                realm.write {
                    // Find existing pet or throw exception if not found
                    val existingPet = this.query<PetModel>("id == $0", updatedPet.id).first().find()
                        ?: throw Exception("Pet not found")

                    // Handle owner relationship changes if necessary
                    if (existingPet.hasOwner && existingPet.ownerName != updatedPet.ownerName) {
                        // Remove pet from current owner's list
                        this.query<OwnerModel>("name == $0", existingPet.ownerName).first().find()?.let { oldOwner ->
                            oldOwner.pets.removeAll { it.id == existingPet.id }
                        }

                        // Add pet to new owner's list if specified
                        if (updatedPet.hasOwner && !updatedPet.ownerName.isNullOrEmpty()) {
                            this.query<OwnerModel>("name == $0", updatedPet.ownerName).first().find()?.let { newOwner ->
                                newOwner.pets.add(existingPet)
                            }
                        }
                    }

                    // Update pet's information
                    existingPet.apply {
                        name = updatedPet.name
                        petType = updatedPet.petType
                        age = updatedPet.age
                        hasOwner = updatedPet.hasOwner
                        ownerName = updatedPet.ownerName
                    }
                }

                loadPets()
                loadOwners()
                _showSnackbar.emit("Pet updated successfully")
            } catch (e: Exception) {
                _showSnackbar.emit("Error updating pet: ${e.message}")
            }
        }
    }

    // Deletes a pet if it has no owner
    fun deletePet(petId: String) {
        viewModelScope.launch {
            val realm = RealmHelper.getRealmInstance()
            realm.write {
                val pet = this.query<PetModel>("id == $0", petId).first().find()

                when {
                    pet == null -> {
                        // Log and notify if pet not found
                        Log.e("PetDelete", "Pet with ID $petId not found.")
                        viewModelScope.launch { _showSnackbar.emit("Pet not found.") }                    }
                    pet.hasOwner -> {
                        // Prevent deletion of owned pets
                        Log.e("PetDelete", "Cannot delete pet with ID $petId - has owner.")
                        viewModelScope.launch {
                            _showSnackbar.emit("Cannot delete this pet because it has an owner. Please remove owner association first.")
                        }
                    }
                    else -> {
                        // Delete pet if conditions are met
                        delete(pet)
                        Log.d("PetDelete", "Pet deleted successfully.")
                        viewModelScope.launch { _showSnackbar.emit("Pet deleted successfully.") }                    }
                }
            }

            loadPets()
        }
    }

    // Handles pet adoption process by linking pet with owner
    fun adoptPet(petId: String, ownerName: String) {
        viewModelScope.launch {
            val realm = RealmHelper.getRealmInstance()
            try {
                realm.write {
                    // Find owner and pet, throw exception if either not found
                    val owner = this.query<OwnerModel>("name == $0", ownerName).first().find()
                        ?: throw Exception("Owner not found")

                    val pet = this.query<PetModel>("id == $0", petId).first().find()?.let { findLatest(it) }
                        ?: throw Exception("Pet not found")

                    // Check adoption conditions and process if valid
                    when {
                        pet.hasOwner -> throw Exception("Pet already adopted by ${pet.ownerName}")
                        owner.pets.any { it.id == petId } -> throw Exception("You already adopted this pet")
                        else -> {
                            pet.hasOwner = true
                            pet.ownerName = ownerName
                            owner.pets.add(pet)
                        }
                    }
                }

                loadPets()
                loadOwners()
                _showSnackbar.emit("Pet adopted successfully")
            } catch (e: Exception) {
                _showSnackbar.emit(e.message ?: "Error during adoption")
            }
        }
    }
}