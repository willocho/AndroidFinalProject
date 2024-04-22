package edu.utap.finalproject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import edu.utap.finalproject.databinding.FragmentSignInBinding

class SignInFragment : Fragment(){

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController

    private val viewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        navController = findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater)
        binding.signInButton.setOnClickListener{
            val username = binding.usernameET.text.toString()
            val password = binding.passwordET.text.toString()
            auth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        val toMainActivity = SignInFragmentDirections.actionToMainActivity()
                        navController.navigate(toMainActivity)
                        viewModel.setShowToolbar(true)
                        Log.d(javaClass.simpleName, "Signed in with user ${auth.currentUser!!.email}")
                    }
                    else{
                        Log.d(javaClass.simpleName, "Failed to sign in: $it")
                    }
                }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null) {
            Log.d(javaClass.simpleName, "User ${currentUser.email} is already signed in")
            viewModel.setShowToolbar(true)
            val toMainActivity = SignInFragmentDirections.actionToMainActivity()
            navController.navigate(toMainActivity)
        }
    }

}