package com.example.lab_week_8.worker

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class FirstWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    // Fungsi ini mengeksekusi proses yang telah ditentukan berdasarkan input
    // dan mengembalikan output setelah selesai
    override fun doWork(): Result {
        // Mendapatkan input parameter
        val id = inputData.getString(INPUT_DATA_ID)

        // Tidurkan proses selama 3 detik untuk simulasi proses berat
        Thread.sleep(3000L)

        // Membangun output berdasarkan hasil proses
        val outputData = Data.Builder()
            .putString(OUTPUT_DATA_ID, id)
            .build()

        // Mengembalikan output sukses
        return Result.success(outputData)
    }

    companion object {
        const val INPUT_DATA_ID = "inId"
        const val OUTPUT_DATA_ID = "outId"
    }
}