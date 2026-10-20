package com.guiones.sociales

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

data class Script(
    val id: Long,
    val title: String,
    val text: String,
    val category: String,
    val imagePath: String? = null,
    val favorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

object Phrases {
    private val sentenceEnd = Regex("(?<=[.!?…])\\s+")

    /** Una frase por línea; si el texto es un solo bloque, se separa por oraciones. */
    fun split(text: String): List<String> {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val parts = if (lines.size > 1) lines
        else text.trim().split(sentenceEnd).map { it.trim() }.filter { it.isNotEmpty() }
        return parts.ifEmpty { listOf(text.trim()) }
    }
}

class Store(context: Context) {
    private val ctx = context.applicationContext
    private val prefs = ctx.getSharedPreferences("guiones", Context.MODE_PRIVATE)

    fun loadScripts(): List<Script> {
        if (!prefs.contains("scripts")) {
            val seed = seedScripts()
            saveScripts(seed)
            return seed
        }
        val arr = JSONArray(prefs.getString("scripts", "[]"))
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Script(
                id = o.getLong("id"),
                title = o.getString("title"),
                text = o.getString("text"),
                category = o.optString("category"),
                imagePath = o.optString("image").ifEmpty { null },
                favorite = o.optBoolean("fav"),
                createdAt = o.optLong("created")
            )
        }
    }

    fun saveScripts(list: List<Script>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id); put("title", it.title); put("text", it.text)
                put("category", it.category); put("image", it.imagePath ?: "")
                put("fav", it.favorite); put("created", it.createdAt)
            })
        }
        prefs.edit().putString("scripts", arr.toString()).apply()
    }

    fun loadCategories(): List<String> {
        val raw = prefs.getString("categories", null) ?: return DEFAULT_CATEGORIES
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { arr.getString(it) }
    }

    fun saveCategories(list: List<String>) {
        prefs.edit().putString("categories", JSONArray(list).toString()).apply()
    }

    var pinnedId: Long
        get() = prefs.getLong("pinned_id", -1L)
        set(v) = prefs.edit().putLong("pinned_id", v).apply()

    var pinnedIndex: Int
        get() = prefs.getInt("pinned_index", 0)
        set(v) = prefs.edit().putInt("pinned_index", v).apply()

    /** Copia la imagen elegida al almacenamiento privado de la app (reducida y con la rotación corregida). */
    fun saveImage(uri: Uri): String? {
        return try {
            val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            var sample = 1
            while (bounds.outWidth / sample > 2048 || bounds.outHeight / sample > 2048) sample *= 2
            val bmp = BitmapFactory.decodeByteArray(
                bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample }
            ) ?: return null
            val orientation = ctx.contentResolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
            } ?: 1
            val degrees = when (orientation) { 6 -> 90f; 3 -> 180f; 8 -> 270f; else -> 0f }
            val finalBmp = if (degrees != 0f)
                Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, Matrix().apply { postRotate(degrees) }, true)
            else bmp
            val dir = File(ctx.filesDir, "images").apply { mkdirs() }
            val file = File(dir, "img_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { finalBmp.compress(Bitmap.CompressFormat.JPEG, 88, it) }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        val DEFAULT_CATEGORIES = listOf(
            "Presentaciones", "Tiendas y servicios", "Trabajo y estudio", "Amistades", "Citas", "Llamadas"
        )

        fun decode(path: String, max: Int): Bitmap? {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            var sample = 1
            while (bounds.outWidth / sample > max * 2 || bounds.outHeight / sample > max * 2) sample *= 2
            return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
        }

        fun deleteImage(path: String?) {
            if (path != null) runCatching { File(path).delete() }
        }

        private fun seedScripts(): List<Script> {
            val t = System.currentTimeMillis()
            return listOf(
                Script(
                    t + 1, "Presentarme", """
                    Hola, mucho gusto. Me llamo [tu nombre].
                    Trabajo en [tu trabajo] y me encanta [algo que disfrutas].
                    ¿Y tú, a qué te dedicas?
                """.trimIndent(), "Presentaciones", favorite = true
                ),
                Script(
                    t + 2, "Pedir algo en una tienda", """
                    Buenas tardes, disculpa.
                    Estoy buscando [producto]. ¿Me podrías ayudar?
                    ¿Cuánto cuesta?
                    Perfecto, me lo llevo. Muchas gracias.
                """.trimIndent(), "Tiendas y servicios"
                ),
                Script(
                    t + 3, "Salir de una conversación con calma", """
                    Fue un gusto hablar contigo.
                    Voy a saludar a unas personas, pero nos vemos pronto.
                    ¡Que estés muy bien!
                """.trimIndent(), "Amistades"
                ),
                Script(
                    t + 4, "Pedir ayuda en el trabajo", """
                    ¿Tienes un minuto? Quisiera preguntarte algo.
                    Estoy trabajando en [tarea] y me quedé atascado en [parte].
                    ¿Me puedes orientar? Te lo agradezco mucho.
                """.trimIndent(), "Trabajo y estudio"
                )
            )
        }
    }
}
