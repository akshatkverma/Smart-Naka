package ethos.lifetime.smartnaka

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.textfield.TextInputEditText
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import ethos.lifetime.smartnaka.databinding.FragmentSearchBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_IMAGE_CAPTURE = 1
    private var img : Bitmap? = null
    private var currentTextField : TextInputEditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textField1.setEndIconOnClickListener {
            dispatchTakePictureIntent()
            currentTextField = binding.registrationNumber
        }

        binding.textField2.setEndIconOnClickListener {
            dispatchTakePictureIntent()
            currentTextField = binding.chassisNumber
        }

        binding.textField3.setEndIconOnClickListener {
            dispatchTakePictureIntent()
            currentTextField = binding.engineNumber
        }

        binding.searchButton.setOnClickListener {
            binding.firebaseProgressBar.visibility = View.VISIBLE
            binding.searchButton.visibility = View.GONE

            val regNumber = binding.registrationNumber.text.toString()
            val engineNum = binding.engineNumber.text.toString()
            val chassisNum = binding.chassisNumber.text.toString()

            val dao = VehiclesDao()

            GlobalScope.launch (Dispatchers.IO){
                if(dao.checkVehicle(regNumber, chassisNum, engineNum)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Vehicle is Stolen !!", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Vehicle not found in database.", Toast.LENGTH_SHORT).show()
                    }
                }

                withContext(Dispatchers.Main) {
                    binding.firebaseProgressBar.visibility = View.GONE
                    binding.searchButton.visibility = View.VISIBLE
                }
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.imgViewer.visibility = View.VISIBLE
            binding.imgViewer.setImageBitmap(imageBitmap)
            img = imageBitmap

//            val py = Python.getInstance()
//            val pyObj = py.getModule("script")
//
//            val obj = pyObj.callAttr("main", img)
//
//            Toast.makeText(requireContext(), "The result is : ${obj.toString()}", Toast.LENGTH_SHORT).show()
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(imageBitmap, 0)
            val result = recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    Toast.makeText(requireContext(), visionText.text, Toast.LENGTH_LONG).show()
                    currentTextField?.setText(visionText.text)
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    Toast.makeText(requireContext(), "$e", Toast.LENGTH_SHORT).show()
                }


        }
    }
}