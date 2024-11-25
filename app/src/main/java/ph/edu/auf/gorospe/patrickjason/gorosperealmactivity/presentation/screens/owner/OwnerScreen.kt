package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.realmmodel.OwnerData
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.components.ItemOwner

// OwnerScreen.kt - Keeping functionality, enhancing visuals
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerScreen(
    ownerViewModel: OwnerViewModel = viewModel()
) {
    val owners by ownerViewModel.owners.collectAsState()
    val searchQuery by ownerViewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditOwnerDialog by remember { mutableStateOf(false) }
    var showAddOwnerDialog by remember { mutableStateOf(false) }
    var selectedOwner by remember { mutableStateOf<OwnerData?>(null) }

    LaunchedEffect(Unit) {
        ownerViewModel.showSnackbar.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        modifier = Modifier.background(Color(0xFFFFF3E0)), // Light orange background
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pet Owners",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = Color(0xFF5D4037) // Brown color
                            )
                        )
                        Text(
                            text = "${owners.size} registered owners",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF5D4037).copy(alpha = 0.7f)
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF3E0) // Light orange background
                ),
                actions = {
                    IconButton(
                        onClick = { showAddOwnerDialog = true },
                        modifier = Modifier
                            .padding(8.dp)
                            .shadow(4.dp, CircleShape)
                            .background(Color(0xFF64B5F6), CircleShape) // Light blue from HomeScreen
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Owner",
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
                onValueChange = { ownerViewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                placeholder = { Text("Search owners...", color = Color(0xFF5D4037)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF64B5F6) // Light blue
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White,
                    unfocusedBorderColor = Color(0xFF64B5F6),
                    focusedBorderColor = Color(0xFF64B5F6),
                    focusedTextColor = Color(0xFF5D4037)
                )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                itemsIndexed(
                    items = owners,
                    key = { _, item -> item.id }
                ) { _, owner ->
                    ItemOwner(
                        ownerData = owner,
                        onRemove = { ownerViewModel.deleteOwner(it) },
                        onEdit = {
                            selectedOwner = it
                            showEditOwnerDialog = true
                        }
                    )
                }
            }
        }
    }

    // Keeping all your existing dialogs
    if (showEditOwnerDialog && selectedOwner != null) {
        EditOwnerDialog(
            ownerData = selectedOwner!!,
            pets = emptyList(),
            onDismiss = {
                showEditOwnerDialog = false
                selectedOwner = null
            },
            onEditOwner = { ownerData ->
                ownerViewModel.updateOwner(ownerData)
                showEditOwnerDialog = false
                selectedOwner = null
            }
        )
    }

    if (showAddOwnerDialog) {
        AddOwnerDialog(
            pets = emptyList(),
            onDismiss = { showAddOwnerDialog = false },
            onAddOwner = { ownerData ->
                ownerViewModel.addOwner(ownerData)
                showAddOwnerDialog = false
            }
        )
    }
}