package ch.heigvd.lab5

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.lab5.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import java.io.FileOutputStream
import java.io.IOError
import java.io.IOException
import java.net.URL

class ImageGalleryAdapter(var coroutineScope: CoroutineScope, var cacheDir: File) :
    RecyclerView.Adapter<ImageGalleryAdapter.ViewHolder>() {

    private fun get(key: String) : Bitmap? {
        val file = File(cacheDir, key)
        if (file.exists()) {
            return BitmapFactory.decodeFile(file.absolutePath)
        }
        return null
    }

    private fun set(key: String, image: Bitmap) {
        Log.d("ImageGalleryAdapter", "set($key, $image)")
        val file = File(cacheDir, key)
        val outputStream = FileOutputStream(file)
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
    }

    private suspend fun downloadImage(url: URL): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val bytes = url.readBytes();
            yield();
            bytes
        } catch (e: IOException) {
            Log.w("ImageGalleryAdapter", "Error while downloading image $e, $url")
            yield()
            null
        }
    }

    private suspend fun decodeByteArray(bytes: ByteArray?): Bitmap? =
        withContext(Dispatchers.Default) {
            try {
                 val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes?.size ?: 0)
                yield()
                bitmap;
            } catch (e: IOError) {
                Log.w("ImageGalleryAdapter", "Error while decoding image", e)
                yield();
                null
            }
        }

    private suspend fun displayImage(view: ImageView, bitmap: Bitmap?) = withContext(Dispatchers.Main) {
        if (bitmap != null) {
            view.setImageBitmap(bitmap)
            yield()
        } else {
            // set round progress bar
            view.setImageResource(android.R.color.transparent);
            yield()
        }
    }

    inner class ViewHolder(val view: android.view.View) : RecyclerView.ViewHolder(view) {
        private val imageView = view.findViewById<ImageView>(R.id.image_view);
        private var image: Bitmap? = null
        private var job: Job? = null

        fun onBindViewHolder(position: Int) {
            val progessBar = view.findViewById<ProgressBar>(R.id.progressBar)

            job?.cancel();
            val url = URL("https://daa.iict.ch/images/$position.jpg")
            job = coroutineScope.launch {
                image = get(url.hashCode().toString())
                if (image != null) Log.d("ImageGalleryAdapter", "[CACHE HIT] image: $image")
                if (image == null) {
                    progessBar.visibility = android.view.View.VISIBLE;
                    imageView.visibility = android.view.View.INVISIBLE;
                    val bytes = downloadImage(url)
                    image = decodeByteArray(bytes)
                    set(url.hashCode().toString(), image!!)
                    Log.d("ImageGalleryAdapter", "[CACHE MISS] image: $image")
                }
                imageView.visibility = android.view.View.VISIBLE;
                progessBar.visibility = android.view.View.GONE;
                displayImage(imageView, image)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindViewHolder(position)
    }

    override fun getItemCount(): Int {
        return 1000;
    }
}