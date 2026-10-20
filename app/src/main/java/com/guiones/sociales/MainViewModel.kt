package com.guiones.sociales

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class MainViewModel(private val app: Application) : AndroidViewModel(app) {
    private val store = Store(app)

    var scripts by mutableStateOf(store.loadScripts()); private set
    var categories by mutableStateOf(store.loadCategories()); private set
    var pinnedId by mutableStateOf(store.pinnedId); private set
    val pinnedIndex: Int get() = store.pinnedIndex

    fun upsert(s: Script) {
        val old = scripts.firstOrNull { it.id == s.id }
        if (old?.imagePath != null && old.imagePath != s.imagePath) Store.deleteImage(old.imagePath)
        scripts = if (old == null) listOf(s) + scripts else scripts.map { if (it.id == s.id) s else it }
        store.saveScripts(scripts)
        if (pinnedId == s.id) refreshSurfaces()
    }

    fun delete(id: Long) {
        scripts.firstOrNull { it.id == id }?.let { Store.deleteImage(it.imagePath) }
        scripts = scripts.filter { it.id != id }
        store.saveScripts(scripts)
        if (pinnedId == id) unpin()
    }

    fun toggleFavorite(id: Long) {
        scripts = scripts.map { if (it.id == id) it.copy(favorite = !it.favorite) else it }
        store.saveScripts(scripts)
    }

    fun addCategory(name: String) {
        val n = name.trim()
        if (n.isEmpty() || categories.any { it.equals(n, true) }) return
        categories = categories + n
        store.saveCategories(categories)
    }

    fun deleteCategory(name: String) {
        categories = categories - name
        store.saveCategories(categories)
        scripts = scripts.map { if (it.category == name) it.copy(category = "") else it }
        store.saveScripts(scripts)
    }

    fun saveImage(uri: Uri): String? = store.saveImage(uri)

    fun pin(id: Long, index: Int) {
        store.pinnedId = id; store.pinnedIndex = index; pinnedId = id
        refreshSurfaces()
    }

    fun unpin() {
        store.pinnedId = -1L; pinnedId = -1L
        Notifier.cancel(app); ScriptWidget.updateAll(app)
    }

    fun setPinnedIndex(i: Int) {
        store.pinnedIndex = i
        refreshSurfaces()
    }

    /** El usuario pudo quitar el guion desde la notificación o el widget. */
    fun refreshPinned() { pinnedId = store.pinnedId }

    private fun refreshSurfaces() {
        Notifier.show(app); ScriptWidget.updateAll(app)
    }
}
