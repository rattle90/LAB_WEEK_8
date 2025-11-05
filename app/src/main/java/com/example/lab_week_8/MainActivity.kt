package com.example.lab_week_8

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.lab_week_8.worker.FirstWorker
import com.example.lab_week_8.worker.SecondWorker
import com.example.lab_week_8.worker.ThirdWorker

class MainActivity : AppCompatActivity() {
    private val workManager by lazy { WorkManager.getInstance(this) }
    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        startChainProcess()
    }

    private fun startChainProcess() {
        val id = "001"
        val firstRequest = OneTimeWorkRequest.Builder(FirstWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData("inId", id))
            .build()
        val secondRequest = OneTimeWorkRequest.Builder(SecondWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData("inId", id))
            .build()

        // 1. Jalankan FirstWorker -> SecondWorker
        workManager.beginWith(firstRequest).then(secondRequest).enqueue()

        workManager.getWorkInfoByIdLiveData(firstRequest.id).observe(this) { info ->
            if (info.state.isFinished) showResult("First process is done")
        }

        // 2. Setelah SecondWorker selesai, jalankan NotificationService
        workManager.getWorkInfoByIdLiveData(secondRequest.id).observe(this) { info ->
            if (info.state.isFinished) {
                showResult("Second process is done")
                launchNotificationService()
            }
        }

        // 3. Setelah NotificationService selesai, jalankan ThirdWorker
        NotificationService.trackingCompletion.observe(this) {
            if (it == "001") {
                showResult("Service 001 Done. Starting Third Worker...")
                startThirdWorker()
            }
        }

        // 5. (Observer untuk SecondNotificationService - opsional untuk ditampilkan)
        SecondNotificationService.trackingCompletion.observe(this) {
            if (it == "002") showResult("All assignments done!")
        }
    }

    private fun startThirdWorker() {
        val thirdRequest = OneTimeWorkRequest.Builder(ThirdWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData("inId", "002"))
            .build()

        workManager.enqueue(thirdRequest)

        // 4. Setelah ThirdWorker selesai, jalankan SecondNotificationService
        workManager.getWorkInfoByIdLiveData(thirdRequest.id).observe(this) { info ->
            if (info.state.isFinished) {
                showResult("Third process is done")
                launchSecondNotificationService()
            }
        }
    }

    private fun launchNotificationService() {
        val intent = Intent(this, NotificationService::class.java).apply {
            putExtra(NotificationService.EXTRA_ID, "001")
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun launchSecondNotificationService() {
        val intent = Intent(this, SecondNotificationService::class.java).apply {
            putExtra(SecondNotificationService.EXTRA_ID, "002")
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun getIdInputData(key: String, value: String) =
        Data.Builder().putString(key, value).build()

    private fun showResult(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}