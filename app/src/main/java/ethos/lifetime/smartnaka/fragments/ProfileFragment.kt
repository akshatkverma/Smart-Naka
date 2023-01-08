package ethos.lifetime.smartnaka.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import ethos.lifetime.smartnaka.R
import ethos.lifetime.smartnaka.activities.SignInActivity
import ethos.lifetime.smartnaka.dao.VehiclesDao
import ethos.lifetime.smartnaka.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profileSectionSV.visibility = View.GONE

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser !!

        val dao = VehiclesDao()

        dao.getUser(currentUser.uid) { user ->
            binding.profileProgressBar.visibility = View.GONE
            binding.profileSectionSV.visibility = View.VISIBLE
            binding.nameTV.text = "Name : ${user.name}"
            binding.emailTV.text = "E-mail : ${user.email}"

            Glide.with(requireContext()).load(user.photoUrl).into(binding.ivProfilePicture)
        }

        binding.signOutButton.setOnClickListener {
            mAuth.signOut()
            googleSignInClient.signOut().addOnCompleteListener {
                val signInActivityIntent = Intent(requireContext(), SignInActivity::class.java)
                startActivity(signInActivityIntent)
                requireActivity().finish()
            }
        }
    }

}