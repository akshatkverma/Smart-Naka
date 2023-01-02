package ethos.lifetime.smartnaka

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        application.setTheme(R.style.Theme_SmartNaka)
        setTheme(R.style.Theme_SmartNaka)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}