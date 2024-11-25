package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.RealmHelper
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.domain.navigation.AppNavigation
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.ui.theme.RealmActivityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RealmHelper.initializeRealm()
        enableEdgeToEdge()
        setContent {
            RealmActivityTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RealmActivityTheme {
        Greeting("Android")
    }
}