package edu.utap.finalproject

import android.content.Context
import android.os.Bundle
import android.text.Layout.Directions
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import edu.utap.finalproject.databinding.ContentMainBinding
import edu.utap.finalproject.ui.theme.FinalProjectTheme
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ContentMainBinding
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ContentMainBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbar)
        setContentView(binding.root)
        auth = Firebase.auth
        lifecycleScope.launch {
            viewModel.cars.collect {
                Log.d(javaClass.simpleName, "Main activity cars: $it")
            }
        }
        lifecycleScope.launch {
            viewModel.cars.onCompletion {
                Log.e(javaClass.simpleName, "Error in viewModel: $it")
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.showToolbar.collect {
                    Log.d(javaClass.simpleName, "Setting toolbar to $it")
                    if (it) {
                        binding.toolbar.visibility = View.VISIBLE
                    } else {
                        binding.toolbar.visibility = View.GONE
                    }
                }
            }
        }
        Log.d(javaClass.simpleName, "Collection launched")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController: NavController = findNavController(R.id.mainActivityFragment)
        return when(item.itemId) {
            R.id.action_sign_out -> {
                auth.signOut()
                navController.navigate(MainActivityFragmentDirections.actionToSignIn())
                viewModel.setShowToolbar(false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
    }
}