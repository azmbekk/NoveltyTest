package com.novelty.test

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.protobuf.ByteString
import kz.novelty.grpc.image.ImageScalerGrpc
import kz.novelty.grpc.image.ScaleRequest


/**
 * Класс совершающий RPC запросы для масштабироваия изобращения
 * @param stub является заглушкой сервиса ImageScaler
 *
 */
class Scaler(private val stub: ImageScalerGrpc.ImageScalerBlockingStub) {

    /**
     * Масштабирует изображение используя GRPC
     * Функция создает асинхронный запрос и поэтому не должна выполнятся в MainThread во избежания ANR
     *
     * @param scale величина масштабирования изображения
     * @param fileName имя файла изображения
     * @param data массив байтов
     *
     * @return В случае успеха возвращает Bitmap. В противном случае возвращает null
     */
    fun scaleImage(scale: String, fileName: String?, data: ByteString?): Bitmap? {
        return try {
            //Формируем GRPC запрос
            val request = ScaleRequest.newBuilder()
                .addScales(scale)
                .setData(data)
                .setName(fileName)
                .build()

            //Запрос GRPC
            val response = stub.scale(request)
            //Ищем в ответе сообщение 'done' это говорит о том что запрос
            //успешно выполнен. В противном случае возвращаем null
            if (response?.message != "done") {
                return null
            }

            //Извлекаем данные из ответа. Ответ представляет собой вложенный массив.
            //По нулевому индексу мы получаем массив данных на первое изображение
            val bytes = response.getImages(0)?.data?.toByteArray()

            //Возвращаем готовый Bitmap для отображения
            BitmapFactory.decodeByteArray(bytes, 0, bytes?.size!!)

        } catch (e: Exception) {
            e.printStackTrace()
            //При исключении возвращается null
            //TODO Сделать адекватную обработку исключений
            null
        }
    }
    /**
     * Масштабирует изображение используя GRPC.
     * Функция создает асинхронный запрос и поэтому не должна выполнятся в MainThread во избежания ANR
     *
     * @param scale величина масштабирования изображения
     * @param fileName имя файла изображения
     * @param data массив байтов
     *
     * @return В случае успеха возвращает String который содержит ссылку на масштабированное изобраение. В противном случае возвращает null
     */
    fun getUrl(scale: String, fileName: String?, data: ByteString?): String? {

        //Формируем GRPC запрос
        val request = ScaleRequest.newBuilder()
            .addScales(scale)
            .setName(fileName)
            .setData(data)
            .build()


        return try {

            //Запрос GRPC
            val response = stub.scaleUrl(request)
            //Ищем в ответе сообщение 'done' это говорит о том что запрос
            //успешно выполнен. В противном случае возвращаем null
            if (response?.message != "done") {
                return null
            }
            //Возвращаем URL масштабированного изображения
            response.getUrls(0)?.url

        } catch (e: Exception) {
            e.printStackTrace()

            //При исключении возвращается null
            //TODO Сделать адекватную обработку исключений
            null
        }


    }


}