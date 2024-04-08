package edu.utap.finalproject

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import edu.utap.finalproject.databinding.ContentMainBinding
import edu.utap.finalproject.ui.theme.FinalProjectTheme
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ContentMainBinding
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ContentMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            viewModel.cars.collect{
                Log.d(javaClass.simpleName, "Main activity cars: $it")
            }
            viewModel.cars.onCompletion {
                Log.e(javaClass.simpleName, "Error in viewModel: $it")
            }
            Log.d(javaClass.simpleName, "Collection launched")
        }
        Log.d(javaClass.simpleName, "Start finished")
    }
}