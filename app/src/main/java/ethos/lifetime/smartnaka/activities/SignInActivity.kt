package ethos.lifetime.smartnaka.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import ethos.lifetime.smartnaka.dao.VehiclesDao
import ethos.lifetime.smartnaka.databinding.ActivitySignInBinding
import ethos.lifetime.smartnaka.models.User

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var auth: FirebaseAuth

    private companion object {
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        emailPasswordSignIn()
        forgotPassword()
        googleSignIn()
        switchToSignUpPage()
        switchToSignInPage()
        emailPasswordSignUp()

    }

    override fun onStart() {
        super.onStart()
        checkUser()
    }

    private fun emailPasswordSignIn() {
        auth = Firebase.auth

        binding.layoutLoginFile.loginButton.setOnClickListener {
            val email = binding.layoutLoginFile.loginEmail.text.toString().trim()
            val password = binding.layoutLoginFile.loginPassword.text.toString()

            var fieldEmpty = false

            if (email == "") {
                fieldEmpty = true
                binding.layoutLoginFile.textInputLoginEmail.error = "Email field can't be empty."
            } else {
                binding.layoutLoginFile.textInputLoginEmail.error = null
            }

            if (password == "") {
                fieldEmpty = true
                binding.layoutLoginFile.textInputLoginPassword.error = "Password field can't be empty."
            } else {
                binding.layoutLoginFile.textInputLoginPassword.error = null
            }

            if (fieldEmpty)
                return@setOnClickListener

            binding.layoutLogin.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        checkUser()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                        checkUser()
                    }
                }
        }

    }

    private fun forgotPassword() {
        binding.layoutLoginFile.forgotPasswordTV.setOnClickListener {
            val email = binding.layoutLoginFile.loginEmail.text.toString()

            binding.layoutLoginFile.textInputLoginPassword.error = null

            if (email == "") {
                binding.layoutLoginFile.textInputLoginEmail.error = "Email field can't be empty."
                return@setOnClickListener
            } else {
                binding.layoutLoginFile.textInputLoginEmail.error = null
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Email sent.")
                        Toast.makeText(this, "Password reset link has been sent to email.",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Please check your email address.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun googleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.layoutLoginFile.googleSignInImage.setOnClickListener {
            Log.d(TAG, "Begin SignIn")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }

    private fun switchToSignUpPage() {
        binding.layoutLoginFile.signUpTV.setOnClickListener {
            binding.layoutLoginFile.layoutLoginFileRootElement.visibility = View.GONE
            binding.layoutRegisterFile.layoutRegisterFileRootElement. visibility = View.VISIBLE
        }
    }

    private fun switchToSignInPage() {
        binding.layoutRegisterFile.signInTV.setOnClickListener {
            binding.layoutLoginFile.layoutLoginFileRootElement.visibility = View.VISIBLE
            binding.layoutRegisterFile.layoutRegisterFileRootElement. visibility = View.GONE
        }
    }

    private fun emailPasswordSignUp() {
        auth = Firebase.auth

        binding.layoutRegisterFile.buttonRegister.setOnClickListener {
            val name = binding.layoutRegisterFile.registerName.text.toString()
            val email = binding.layoutRegisterFile.registerEmail.text.toString().trim()
            val password = binding.layoutRegisterFile.registerPassword.text.toString()

            var fieldEmpty = false

            if (name == "") {
                fieldEmpty = true
                binding.layoutRegisterFile.textInputRegisterName.error = "Name field can't be empty."
            } else {
                binding.layoutRegisterFile.textInputRegisterName.error = null
            }

            if (email == "") {
                fieldEmpty = true
                binding.layoutRegisterFile.textInputRegisterEmail.error = "Email field can't be empty."
            } else {
                binding.layoutRegisterFile.textInputRegisterEmail.error = null
            }

            if (password == "") {
                fieldEmpty = true
                binding.layoutRegisterFile.textInputRegisterPassword.error = "Password field can't be empty."
            } else {
                binding.layoutRegisterFile.textInputRegisterPassword.error = null
            }

            if (fieldEmpty)
                return@setOnClickListener

            binding.layoutLogin.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val dao = VehiclesDao()
                        val firebaseUser = firebaseAuth.currentUser !!

                        dao.addUser(User(firebaseUser.uid, name, email, ""))

                        checkUser()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                        checkUser()
                    }
                }
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
            finish()
        } else {
            binding.layoutLogin.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
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
                Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin")
//        Toast.makeText(this, "starting in...", Toast.LENGTH_LONG).show()
        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)

        binding.layoutLogin.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

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
//                    Toast.makeText(this, "logged in...", Toast.LENGTH_LONG).show()
                    val dao = VehiclesDao()
                    dao.addUser(User(firebaseUser.uid, firebaseUser.displayName.toString(), firebaseUser.email.toString(), firebaseUser.photoUrl.toString()))
                } else {
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing user")
                    Toast.makeText(this, "logged in...", Toast.LENGTH_LONG).show()
                }
                checkUser()
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Login Failed")
                Toast.makeText(this, "Login Failed...", Toast.LENGTH_LONG).show()
            }
    }
}