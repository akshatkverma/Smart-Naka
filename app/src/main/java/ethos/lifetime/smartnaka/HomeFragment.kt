package ethos.lifetime.smartnaka

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import ethos.lifetime.smartnaka.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_IMAGE_CAPTURE = 1
    private var img : Bitmap? = null

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
//            Toast.makeText(requireContext(), "Button Clicked", Toast.LENGTH_SHORT).show()
            dispatchTakePictureIntent()
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
                    Log.w("Firebase", "Vehicle NOT Found !!!!")

            }
        }

        if(!Python.isStarted()) {
            Python.start(AndroidPlatform(requireContext()))
        }

    }


    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Error Occured : $e", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.imgViewer.setImageBitmap(imageBitmap)
            img = imageBitmap
            val py = Python.getInstance()
            val pyObj = py.getModule("script")

            val obj = pyObj.callAttr("main", img)

            Toast.makeText(requireContext(), "The result is : ${obj.toString()}", Toast.LENGTH_SHORT).show()
        }
    }

}