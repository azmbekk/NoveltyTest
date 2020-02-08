package com.novelty.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bt_connect.setOnClickListener {
            val host = et_address.text.toString()
            val port = et_port.text.toString()

            //чекпоинты для вводимых данных
            if (host.isNullOrEmpty()) {
                Snackbar.make(it, "IP адрес не может быть пустым!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (port.isNullOrEmpty()) {
                Snackbar.make(it, "PORT должен быть больше нуля!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startImageActivity(host, port.toInt())
        }
    }

    //Стартуем активити для работы с изображениями
    private fun startImageActivity(host: String, port: Int) {

        //в Intent передаем адрес и порт
        val intent = Intent(this, ImageActivity::class.java).apply {
            putExtra("host", host)
            putExtra("port", port)
        }

        Log.i(TAG, "start ImageActivity")
        applicationContext.startActivity(intent)
    }
}
