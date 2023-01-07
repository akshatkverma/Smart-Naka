package ethos.lifetime.smartnaka.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import ethos.lifetime.smartnaka.R
import ethos.lifetime.smartnaka.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_IMAGE_CAPTURE = 1
    private var img: Bitmap? = null
    private var currentTextField: TextInputEditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setToolBar()

        val searchFragment = SearchFragment()
        val profileFragment = ProfileFragment()

        setCurrentFragment(searchFragment)

        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.search -> setCurrentFragment(searchFragment)
                R.id.profile -> setCurrentFragment(profileFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) =
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.flFragment, fragment)
            commit()
        }

    private fun setToolBar() {
        val toolbar = binding.toolbarHomeFragment
        toolbar.toolbarTitle.text = "Smart Naka"
        toolbar.toolbarSubtitle.visibility = View.GONE
        toolbar.backButton.visibility = View.INVISIBLE
        toolbar.deleteButton.visibility = View.INVISIBLE
        toolbar.openBrowser.visibility = View.INVISIBLE
        toolbar.addButton.visibility = View.INVISIBLE
    }
}