package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.domain.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.screens.home.HomeScreen
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.screens.owner.OwnerScreen
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.screens.pet.PetScreen

@Composable
fun AppNavigation(navController: NavHostController){
    NavHost(navController, startDestination = AppNavRoutes.Home.route){
        composable(AppNavRoutes.Home.route){ HomeScreen(navController)}
        composable(AppNavRoutes.PetList.route){ PetScreen(navController)}
        composable(AppNavRoutes.OwnerList.route){ OwnerScreen(navController)}
    }
}