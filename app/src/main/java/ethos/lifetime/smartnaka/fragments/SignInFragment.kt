package ethos.lifetime.smartnaka.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ethos.lifetime.smartnaka.R
import ethos.lifetime.smartnaka.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var auth: FirebaseAuth

    private companion object {
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        checkUser()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailPasswordSignIn()
        googleSignIn()

    }

    private fun emailPasswordSignIn() {
        auth = Firebase.auth

        binding.layoutLoginFile.forgotPasswordTV.setOnClickListener {

        }

        binding.layoutLoginFile.signUpTV.setOnClickListener {
            binding.layoutLoginFile.layoutLoginFileRootElement.visibility = View.GONE
            binding.layoutRegisterFile.layoutRegisterFileRootElement. visibility = View.VISIBLE
        }

        binding.layoutRegisterFile.signInTV.setOnClickListener {
            binding.layoutLoginFile.layoutLoginFileRootElement.visibility = View.VISIBLE
            binding.layoutRegisterFile.layoutRegisterFileRootElement. visibility = View.GONE
        }

        binding.layoutLoginFile.loginButton.setOnClickListener {
            val email = binding.layoutLoginFile.email.text.toString().trim()
            val password = binding.layoutLoginFile.password.text.toString()

            var fieldEmpty = false

            if (email == "") {
                fieldEmpty = true
                binding.layoutLoginFile.email.error = "Email field can't be empty"
            } else {
                binding.layoutLoginFile.email.error = null
            }

            if (password == "") {
                fieldEmpty = true
                binding.layoutLoginFile.password.error = "Password field can't be empty"
            } else {
                binding.layoutLoginFile.password.error = null
            }

            if (fieldEmpty)
                return@setOnClickListener

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        checkUser()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(context, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                        checkUser()
                    }
                }
        }
    }

    private fun googleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.layoutLoginFile.googleSignInImage.setOnClickListener {
            Log.d(TAG, "Begin SignIn")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }


    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToHomeFragment())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "signIn intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            } catch (e: Exception) {
                Log.d(TAG, "onActivityResult:${e.message}")
                Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin")
        Toast.makeText(requireContext(), "starting in...", Toast.LENGTH_LONG).show()
        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                Log.d(TAG, "firebaseAuthWithGoogleAccount: LoggedIn")

                val firebaseUser = firebaseAuth.currentUser

                val uid = firebaseAuth.uid
                val email = firebaseUser!!.email

                Log.d(TAG, "firebaseAuthWithGoogleAccount: uid: ${uid}")
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Email: ${email}")
                if (authResult.additionalUserInfo!!.isNewUser) {
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Account created")
                    Toast.makeText(requireContext(), "logged in...", Toast.LENGTH_LONG).show()
                } else {
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing user")
                    Toast.makeText(requireContext(), "logged in...", Toast.LENGTH_LONG).show()
                }
                checkUser()
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Login Failed")
                Toast.makeText(requireContext(), "Login Failed...", Toast.LENGTH_LONG).show()
            }
    }

}