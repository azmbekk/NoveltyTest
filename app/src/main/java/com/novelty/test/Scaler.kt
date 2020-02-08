package com.novelty.test

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.protobuf.ByteString
import kz.novelty.grpc.image.ImageScalerGrpc
import kz.novelty.grpc.image.ScaleRequest

class Scaler(private val stub: ImageScalerGrpc.ImageScalerBlockingStub) {

     fun scaleImage(scale:String, fileName:String?, data:ByteString?): Bitmap? {
        return try {
            val request = ScaleRequest.newBuilder()
                .addScales(scale)
                .setData(data)
                .setName(fileName)
                .build()


            val response = stub.scale(request)
            if (response?.message != "done") {
                return null
            }

            val bytes = response.getImages(0)?.data?.toByteArray()
            BitmapFactory.decodeByteArray(bytes, 0, bytes?.size!!)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

     fun getUrl(scale: String, fileName: String?, data: ByteString?): String? {

        val request = ScaleRequest.newBuilder()
            .addScales(scale)
            .setName(fileName)
            .setData(data)
            .build()


        return try {
            val response = stub.scaleUrl(request)
            if (response?.message != "done") {
                return null
            }
            response.getUrls(0)?.url

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }


    }



}