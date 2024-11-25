package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.screens.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.PetModel
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.toPetData
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.components.ItemPet

// PetScreen.kt - Keeping functionality, enhancing visuals
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetScreen(
    navController: NavHostController,
    petViewModel: PetViewModel = viewModel()
) {
    val pets by petViewModel.pets.collectAsState()
    val searchQuery by petViewModel.searchQuery.collectAsState()
    val petTypes by petViewModel.petTypes.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddPetDialog by remember { mutableStateOf(false) }
    var showEditPetDialog by remember { mutableStateOf(false) }
    var showAdoptPetDialog by remember { mutableStateOf(false) }
    var selectedPet by remember { mutableStateOf<PetModel?>(null) }
    val owners by petViewModel.owners.collectAsState()

    LaunchedEffect(Unit) {
        petViewModel.showSnackbar.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        modifier = Modifier.background(Color(0xFFFFF3E0)), // Light orange background from HomeScreen
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pets",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = Color(0xFF5D4037) // Brown color from HomeScreen
                            )
                        )
                        Text(
                            text = "${pets.size} total pets",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF5D4037).copy(alpha = 0.7f)
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF5D4037) // Brown color matching title
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF3E0) // Light orange background
                ),
                actions = {
                    IconButton(
                        onClick = { showAddPetDialog = true },
                        modifier = Modifier
                            .padding(8.dp)
                            .shadow(4.dp, CircleShape)
                            .background(Color(0xFF81C784), CircleShape) // Light green from HomeScreen
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Pet",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFFF3E0)) // Light orange background
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { petViewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                placeholder = { Text("Search pets...", color = Color(0xFF5D4037)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF81C784) // Light green
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White,
                    unfocusedBorderColor = Color(0xFF81C784),
                    focusedBorderColor = Color(0xFF81C784),
                    focusedTextColor = Color(0xFF5D4037)
                )
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                itemsIndexed(
                    items = pets,
                    key = { _, item -> item.id }
                ) { _, pet ->
                    ItemPet(
                        petModel = pet,
                        onRemove = { model ->
                            petViewModel.deletePet(model.id)
                        },
                        onEdit = { model ->
                            selectedPet = model
                            showEditPetDialog = true
                        },
                        onAdopt = { model ->
                            selectedPet = model
                            showAdoptPetDialog = true
                        }
                    )
                }
            }
        }
    }

    // Keeping all your existing dialogs
    if (showAddPetDialog) {
        AddPetDialog(
            petTypes = petTypes,
            owners = owners,
            onDismiss = { showAddPetDialog = false },
            onAddPet = { newPet ->
                petViewModel.addPet(
                    name = newPet.name,
                    type = newPet.petType,
                    age = newPet.age,
                    hasOwner = newPet.hasOwner,
                    ownerName = newPet.ownerName
                )
                showAddPetDialog = false
            }
        )
    }

    if (showEditPetDialog && selectedPet != null) {
        EditPetDialog(
            petData = selectedPet!!.toPetData(),
            petTypes = petTypes,
            owners = owners,
            onDismiss = {
                showEditPetDialog = false
                selectedPet = null
            },
            onEditPet = { petData ->
                petViewModel.updatePet(petData)
                showEditPetDialog = false
                selectedPet = null
            }
        )
    }

    if (showAdoptPetDialog && selectedPet != null) {
        AdoptPetDialog(
            petData = selectedPet!!.toPetData(),
            owners = owners,
            onDismiss = {
                showAdoptPetDialog = false
                selectedPet = null
            },
            onAdopt = { petId, ownerName ->
                petViewModel.adoptPet(petId, ownerName)
                showAdoptPetDialog = false
                selectedPet = null
            }
        )
    }
}


