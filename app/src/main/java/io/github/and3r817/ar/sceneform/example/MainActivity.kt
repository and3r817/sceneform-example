package io.github.and3r817.ar.sceneform.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.ar.sceneform.ux.ArFragment


class MainActivity : AppCompatActivity() {

    private var arFragment: ArFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment
    }
}
