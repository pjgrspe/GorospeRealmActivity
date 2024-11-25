package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import androidx.navigation.NavController
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.domain.navigation.AppNavRoutes

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF3E0)) // Light orange background
    ) {
        Text(
            text = "🐾 Pet Realm Sampler 🐾",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 1.2.em,
                color = Color(0xFF5D4037) // Brown color
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate(AppNavRoutes.PetList.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)), // Light green
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "🐕 View Pet List",
                style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.White)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate(AppNavRoutes.OwnerList.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)), // Light blue
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "👤 View Owner List",
                style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.White)
            )
        }
    }
}
