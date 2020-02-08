package com.novelty.test

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.google.protobuf.ByteString
import io.grpc.okhttp.OkHttpChannelBuilder
import kotlinx.android.synthetic.main.activity_image.*
import kz.novelty.grpc.image.ImageScalerGrpc
import kotlin.concurrent.thread


class ImageActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = ImageActivity::class.java.canonicalName

    private lateinit var scaler: Scaler
    private var scaleValue: String = "0.5"
    private var fileName: String? = null
    private var imageBytes: ByteString? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        //Извлекаем адрес и порт
        val host = intent.getStringExtra("host")
        val port = intent.getIntExtra("port", 80)

        //Создаем канал передачи данных
        val channel = OkHttpChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build()

        tv_seek.text = "Scale: $scaleValue"


        val stub = ImageScalerGrpc.newBlockingStub(channel)
        scaler = Scaler(stub)


        //Listeners for handle the UI
        bt_select_image.setOnClickListener(this)
        bt_get_url.setOnClickListener(this)
        bt_scale.setOnClickListener(this)


        //Seekbar для наглядного масштабирования
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //Инициализируем переменную scaleImage которая будет использоваться для дальнейшего масштабирования
                //исходных изображений
                //Величина масштабирования от 0.1 .. 1.0
                var temp = progress.toFloat() / 10
                if (temp < 0.1)
                    temp = 0.1f

                scaleValue = temp.toString()

                tv_seek.text = "Scale: $scaleValue"
                Log.i(TAG, "Scale: $scaleValue")
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Проверка возвращаемых данных
        if (requestCode == 1 && data != null) {
            val uri = data.data!!
            //Выводим изображение для визуализации
            imageView.setImageURI(uri)

            //Инициализируем переменные
            fileName = uri.getFileName()
            imageBytes = uri.toByteString()
        }
    }

    override fun onClick(v: View?) {
        when (v) {

            bt_select_image -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Выберите изображение"), 1)
            }

            bt_scale -> {
                thread {
                    val bitmap = scaler.scaleImage(scaleValue, fileName, imageBytes)
                    runOnUiThread {
                        if (bitmap != null)
                            imageView.setImageBitmap(bitmap)
                        else
                            tv_url.text = "Не удалось конвертировать приложение"
                    }
                }
            }

            bt_get_url -> {
                thread {
                    val url = scaler.getUrl(scaleValue, fileName, imageBytes)

                    runOnUiThread {
                        tv_url.text =
                            if (!url.isNullOrEmpty()) url else "Не удалось получить ссылку"
                    }
                }
            }
        }
    }

    private fun Uri.toByteString(): ByteString {
        return ByteString.readFrom(contentResolver.openInputStream(this))
    }

    private fun Uri.getFileName(): String? {
        return contentResolver.query(
            this,
            arrayOf(MediaStore.Images.ImageColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.let {
            it.moveToFirst()
            it.getString(it.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME))
        }
    }
}


