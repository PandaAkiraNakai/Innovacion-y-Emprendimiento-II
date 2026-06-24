package cl.vigia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cl.vigia.app.ui.VigiaApp
import cl.vigia.app.ui.theme.VigiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VigiaTheme {
                VigiaApp()
            }
        }
    }
}
