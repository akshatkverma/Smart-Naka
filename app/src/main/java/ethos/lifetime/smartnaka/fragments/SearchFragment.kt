package ethos.lifetime.smartnaka.fragments

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.textfield.TextInputEditText
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import ethos.lifetime.smartnaka.dao.VehiclesDao
import ethos.lifetime.smartnaka.databinding.FragmentSearchBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_IMAGE_CAPTURE = 1
    private var img: Bitmap? = null
    private var currentTextField: TextInputEditText? = null
    private var imageFileLocation = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.layoutSearchFile.textField1.setEndIconOnClickListener {
            dispatchTakePictureIntent()
            currentTextField = binding.layoutSearchFile.registrationNumber
        }

        binding.layoutSearchFile.textField2.setEndIconOnClickListener {
            dispatchTakePictureIntent()
            currentTextField = binding.layoutSearchFile.chassisNumber
        }

        binding.layoutSearchFile.textField3.setEndIconOnClickListener {
            dispatchTakePictureIntent()
            currentTextField = binding.layoutSearchFile.engineNumber
        }

        binding.layoutSearchFile.searchButton.setOnClickListener {



            val regNumber = binding.layoutSearchFile.registrationNumber.text.toString()
            val engineNum = binding.layoutSearchFile.engineNumber.text.toString()
            val chassisNum = binding.layoutSearchFile.chassisNumber.text.toString()

            if(isRegistrationNumber(regNumber)||isEngineNumber(engineNum)||isChasisNumber(chassisNum)){
                binding.layoutSearchFile.textField2.error=null
                binding.layoutSearchFile.textField3.error=null
                binding.layoutSearchFile.textField1.error=null
                binding.layoutSearchFile.firebaseProgressBar.visibility = View.VISIBLE
                binding.layoutSearchFile.searchButton.visibility = View.GONE
                getdao(regNumber, chassisNum, engineNum)

            }
            else{
                binding.layoutSearchFile.textField2.error="Invalid input for Chassis Number"
                binding.layoutSearchFile.textField3.error="Invalid input for Engine Number"
                binding.layoutSearchFile.textField1.error="Invalid input for Registration Number"
                //return@setOnClickListener
            }




        }

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(requireContext()))
        }

    }
    private fun isEngineNumber(value: String):Boolean{
        return (value.length==6)&&(value.all{char->char.isDigit()})
    }
    private fun isChasisNumber(value: String):Boolean{
        return (value.length==17)
    }
    private fun getdao(regNumber: String, chassisNum:String, engineNum:String){
        val dao = VehiclesDao()

        GlobalScope.launch(Dispatchers.IO) {
            if (dao.checkVehicle(regNumber, chassisNum, engineNum)) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Vehicle is Stolen !!", Toast.LENGTH_SHORT).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Vehicle not found in database.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            withContext(Dispatchers.Main) {
                binding.layoutSearchFile.firebaseProgressBar.visibility = View.GONE
                binding.layoutSearchFile.searchButton.visibility = View.VISIBLE
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        var photoFile: File? = null
        try {
            photoFile = createImageFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        takePictureIntent.putExtra(
            MediaStore.EXTRA_OUTPUT,
            FileProvider.getUriForFile(
                requireContext(), context?.applicationContext?.packageName
                        + ".provider", createImageFile()
            )
        )

        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Error Occured : $e", Toast.LENGTH_LONG).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            var imageBitmap = BitmapFactory.decodeFile(imageFileLocation)
            imageBitmap = rotateImage(imageBitmap)
//            img = imageBitmap

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

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "IMG_$timeStamp"
        val storageDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        val image = File.createTempFile(imageFileName, ".jpg", storageDirectory)
        imageFileLocation = image.absolutePath
        return image
    }

    private fun rotateImage(bitmap: Bitmap): Bitmap {
        var exifInterface: ExifInterface? = null
        try {
            exifInterface = ExifInterface(imageFileLocation)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val orientation =
            exifInterface?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun isRegistrationNumber(text: String): Boolean {
        val temp = mutableListOf<Char>()
        for (char in text) {
            if (char != '-' && char != ' ') temp.add(char)
        }
        val size = temp.size
        if (size in 9..11) {
            if (!temp[0].isDigit() && !temp[1].isDigit() && temp[2].isDigit() && temp[3].isDigit() && !temp[4].isDigit() && temp[size - 1].isDigit() && temp[size - 2].isDigit() && temp[size - 3].isDigit() && temp[size - 4].isDigit()) return true
        }
        return false
    }
}