package ethos.lifetime.smartnaka

import android.os.Bundle
import android.provider.Settings.Global
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import ethos.lifetime.smartnaka.databinding.FragmentHomeBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textField1.setEndIconOnClickListener {
            Toast.makeText(requireContext(), "Button Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.searchButton.setOnClickListener {
            val regNumber = binding.registrationNumber.text.toString()
            val engineNum = binding.engineNumber.text.toString()
            val chassisNum = binding.chassisNumber.text.toString()

            val dao = VehiclesDao()

            GlobalScope.launch (Dispatchers.IO){
                if(dao.checkVehicle(regNumber, chassisNum, engineNum))
                    Log.w("Firebase", "Vehicle Found")
                else
                    Log.w("Firebase", "Vehicle NOT Found !!!!!!!!!!!!!!!!!")

            }
        }
    }

}