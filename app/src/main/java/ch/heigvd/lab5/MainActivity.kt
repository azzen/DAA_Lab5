package ch.heigvd.lab5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ch.heigvd.lab5.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = binding.imageGalleryRecyclerView
        val adapter = ImageGalleryAdapter(lifecycleScope, cacheDir)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        val cacheWorkManager = WorkManager.getInstance(applicationContext)
        val periodicWorkRequest = PeriodicWorkRequestBuilder<CacheWorkManager>(15, TimeUnit.MINUTES)
            .build();
        cacheWorkManager.enqueueUniquePeriodicWork("clear_cache", ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_action_clear_cache -> {
                val oneTimeRequest = OneTimeWorkRequestBuilder<CacheWorkManager>().build()
                WorkManager.getInstance(applicationContext).enqueueUniqueWork("clear_cache_single", ExistingWorkPolicy.KEEP, oneTimeRequest)
                binding.imageGalleryRecyclerView.adapter?.notifyDataSetChanged()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}