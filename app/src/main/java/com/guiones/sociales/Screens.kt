@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.guiones.sociales

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface Route {
    data object Home : Route
    data class Editor(val id: Long?) : Route
    data class Reader(val id: Long) : Route
}

@Composable
fun App(vm: MainViewModel) {
    var route by remember { mutableStateOf<Route>(Route.Home) }
    BackHandler(enabled = route !is Route.Home) {
        val r = route
        route = if (r is Route.Editor && r.id != null) Route.Reader(r.id) else Route.Home
    }
    Crossfade(targetState = route, animationSpec = tween(220), label = "route") { r ->
        when (r) {
            Route.Home -> HomeScreen(
                vm, onOpen = { route = Route.Reader(it) }, onNew = { route = Route.Editor(null) }
            )
            is Route.Editor -> EditorScreen(
                vm, r.id,
                onClose = { route = if (r.id != null) Route.Reader(r.id) else Route.Home },
                onSaved = { route = Route.Reader(it) },
                onDeleted = { route = Route.Home }
            )
            is Route.Reader -> ReaderScreen(
                vm, r.id, onBack = { route = Route.Home }, onEdit = { route = Route.Editor(r.id) }
            )
        }
    }
}

/* ───────────────────────── Piezas comunes ───────────────────────── */

@Composable
fun LocalImage(path: String?, modifier: Modifier, maxSize: Int = 600) {
    val bmp by produceState<ImageBitmap?>(null, path) {
        value = if (path == null) null
        else withContext(Dispatchers.IO) { Store.decode(path, maxSize)?.asImageBitmap() }
    }
    bmp?.let { Image(bitmap = it, contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop) }
}

@Composable
fun NameDialog(title: String, label: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var value by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value, onValueChange = { value = it }, label = { Text(label) },
                singleLine = true, shape = RoundedCornerShape(14.dp)
            )
        },
        confirmButton = {
            TextButton(enabled = value.isNotBlank(), onClick = { onConfirm(value.trim()) }) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun CategoriesDialog(vm: MainViewModel, onDismiss: () -> Unit) {
    var adding by remember { mutableStateOf(false) }
    if (adding) {
        NameDialog("Nueva categoría", "Nombre", { adding = false }) { vm.addCategory(it); adding = false }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Categorías") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                vm.categories.forEach { c ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(c, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { vm.deleteCategory(c) }) {
                            Icon(Icons.Default.Delete, "Eliminar $c", tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
                TextButton(onClick = { adding = true }) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Nueva categoría")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Listo") } }
    )
}

/* ───────────────────────── Inicio ───────────────────────── */

@Composable
fun HomeScreen(vm: MainViewModel, onOpen: (Long) -> Unit, onNew: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var filterRaw by rememberSaveable { mutableStateOf("Todos") }
    var showCats by remember { mutableStateOf(false) }
    val filter = if (filterRaw == "Todos" || filterRaw == "Favoritos" || filterRaw in vm.categories) filterRaw else "Todos"
    val shown = vm.scripts.filter { s ->
        (filter == "Todos" || (filter == "Favoritos" && s.favorite) || s.category == filter) &&
            (query.isBlank() || s.title.contains(query, true) || s.text.contains(query, true))
    }
    val pinned = vm.scripts.firstOrNull { it.id == vm.pinnedId }

    if (showCats) CategoriesDialog(vm) { showCats = false }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNew,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nuevo guion", fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { pad ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 20.dp, end = 20.dp,
                top = pad.calculateTopPadding() + 12.dp, bottom = pad.calculateBottomPadding() + 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.monkey_avatar), null,
                        Modifier.size(56.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Guiones sociales", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Tu voz, ya ensayada.", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text("Buscar un guion") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, "Borrar búsqueda") }
                    },
                    shape = RoundedCornerShape(28.dp)
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Todos", "Favoritos") + vm.categories) { c ->
                        FilterChip(selected = filter == c, onClick = { filterRaw = c }, label = { Text(c) })
                    }
                    item {
                        AssistChip(
                            onClick = { showCats = true }, label = { Text("Editar") },
                            leadingIcon = { Icon(Icons.Default.Settings, null, Modifier.size(18.dp)) }
                        )
                    }
                }
            }
            if (pinned != null) {
                item {
                    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Row(Modifier.padding(start = 14.dp, top = 4.dp, bottom = 4.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "En tu pantalla de bloqueo: ${pinned.title}", Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 2, overflow = TextOverflow.Ellipsis
                            )
                            TextButton(onClick = { vm.unpin() }) { Text("Quitar") }
                        }
                    }
                }
            }
            if (shown.isEmpty()) {
                item { EmptyState(hasScripts = vm.scripts.isNotEmpty()) }
            } else {
                items(shown, key = { it.id }) { s -> ScriptCard(s, onClick = { onOpen(s.id) }, onFav = { vm.toggleFavorite(s.id) }) }
            }
        }
    }
}

@Composable
private fun EmptyState(hasScripts: Boolean) {
    Column(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painterResource(R.drawable.monkey_scene), null,
            Modifier.size(200.dp).clip(RoundedCornerShape(28.dp)), contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (hasScripts) "Ningún guion coincide" else "Aún no tienes guiones",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (hasScripts) "Prueba con otra búsqueda o categoría."
            else "Toca «Nuevo guion» y escribe lo que quieres decir. Así nunca más te quedarás en blanco.",
            style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun ScriptCard(s: Script, onClick: () -> Unit, onFav: () -> Unit) {
    Card(
        onClick = onClick, shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (s.imagePath != null) {
                LocalImage(s.imagePath, Modifier.size(76.dp).clip(RoundedCornerShape(16.dp)), 240)
                Spacer(Modifier.width(14.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(s.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (s.category.isNotBlank()) {
                    Text(s.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    s.text.replace("\n", "  "), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onFav) {
                Icon(
                    if (s.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    if (s.favorite) "Quitar de favoritos" else "Marcar como favorito",
                    tint = if (s.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/* ───────────────────────── Editor ───────────────────────── */

@Composable
fun EditorScreen(vm: MainViewModel, id: Long?, onClose: () -> Unit, onSaved: (Long) -> Unit, onDeleted: () -> Unit) {
    val existing = remember(id) { vm.scripts.firstOrNull { it.id == id } }
    var title by rememberSaveable { mutableStateOf(existing?.title ?: "") }
    var text by rememberSaveable { mutableStateOf(existing?.text ?: "") }
    var category by rememberSaveable { mutableStateOf(existing?.category ?: "") }
    var image by rememberSaveable { mutableStateOf(existing?.imagePath) }
    var newCategory by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            val saved = withContext(Dispatchers.IO) { vm.saveImage(uri) }
            if (saved != null) image = saved
        }
    }

    if (newCategory) {
        NameDialog("Nueva categoría", "Nombre", { newCategory = false }) {
            vm.addCategory(it); category = it; newCategory = false
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("¿Eliminar este guion?") },
            text = { Text("Se borrará «${existing?.title}» y su imagen. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { vm.delete(existing!!.id); onDeleted() }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Nuevo guion" else "Editar guion", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Cerrar") } },
                actions = {
                    if (existing != null) {
                        IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, "Eliminar guion") }
                    }
                    Button(
                        enabled = title.isNotBlank() && text.isNotBlank(),
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = {
                            val sid = existing?.id ?: System.currentTimeMillis()
                            vm.upsert(
                                Script(sid, title.trim(), text.trim(), category, image, existing?.favorite ?: false, existing?.createdAt ?: sid)
                            )
                            onSaved(sid)
                        }
                    ) { Text("Guardar") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).consumeWindowInsets(pad).imePadding().fillMaxSize()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center
            ) {
                if (image != null) {
                    LocalImage(image, Modifier.fillMaxSize(), 900)
                    IconButton(
                        onClick = { image = null },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(36.dp)
                            .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                    ) { Icon(Icons.Default.Close, "Quitar imagen", tint = Color.White, modifier = Modifier.size(20.dp)) }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddCircle, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(6.dp))
                        Text("Añadir imagen (opcional)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Algo que te recuerde la situación", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            OutlinedTextField(
                value = title, onValueChange = { title = it }, label = { Text("Título") },
                placeholder = { Text("Ej. Presentarme en una reunión") },
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Categoría", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    vm.categories.forEach { c ->
                        FilterChip(selected = category == c, onClick = { category = if (category == c) "" else c }, label = { Text(c) })
                    }
                    AssistChip(
                        onClick = { newCategory = true }, label = { Text("Nueva") },
                        leadingIcon = { Icon(Icons.Default.Add, null, Modifier.size(18.dp)) }
                    )
                }
            }

            OutlinedTextField(
                value = text, onValueChange = { text = it }, label = { Text("Tu guion") },
                minLines = 8, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                supportingText = { Text("Escribe una frase por línea: en el modo ensayo, el widget y la notificación las verás una a una.") }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ───────────────────────── Lectura / ensayo ───────────────────────── */

@Composable
fun ReaderScreen(vm: MainViewModel, id: Long, onBack: () -> Unit, onEdit: () -> Unit) {
    val script = vm.scripts.firstOrNull { it.id == id }
    if (script == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val context = LocalContext.current
    val phrases = remember(script.text) { Phrases.split(script.text) }
    var index by rememberSaveable { mutableIntStateOf(if (vm.pinnedId == id) vm.pinnedIndex else 0) }
    var fontSize by rememberSaveable { mutableIntStateOf(28) }
    var full by rememberSaveable { mutableStateOf(false) }
    val safe = index.coerceIn(0, phrases.lastIndex)
    val isPinned = vm.pinnedId == id

    LaunchedEffect(safe) { if (vm.pinnedId == id) vm.setPinnedIndex(safe) }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.pin(id, safe)
        else Toast.makeText(context, "Activa las notificaciones para ver el guion en la pantalla de bloqueo", Toast.LENGTH_LONG).show()
    }
    fun togglePin() {
        when {
            isPinned -> vm.unpin()
            Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ->
                permission.launch(Manifest.permission.POST_NOTIFICATIONS)
            else -> vm.pin(id, safe)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(script.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver") } },
                actions = {
                    IconButton(onClick = { vm.toggleFavorite(id) }) {
                        Icon(
                            if (script.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favorito",
                            tint = if (script.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Create, "Editar") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (script.imagePath != null) {
                LocalImage(script.imagePath, Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(20.dp)), 900)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !full, onClick = { full = false }, label = { Text("Frase a frase") })
                FilterChip(selected = full, onClick = { full = true }, label = { Text("Texto completo") })
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { fontSize = (fontSize - 2).coerceAtLeast(16) }) { Text("A−", fontSize = 16.sp) }
                TextButton(onClick = { fontSize = (fontSize + 2).coerceAtMost(44) }) { Text("A+", fontSize = 20.sp) }
            }

            Card(
                Modifier.weight(1f).fillMaxWidth(), shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                if (full) {
                    Text(
                        script.text, Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                        fontSize = fontSize.sp, lineHeight = (fontSize * 1.4f).sp, fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    AnimatedContent(
                        targetState = safe, label = "frase",
                        transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(140)) }
                    ) { i ->
                        BoxWithConstraints(Modifier.fillMaxSize()) {
                            val h = maxHeight
                            Column(
                                Modifier.fillMaxWidth().heightIn(min = h).verticalScroll(rememberScrollState()).padding(24.dp),
                                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    phrases[i.coerceIn(0, phrases.lastIndex)], textAlign = TextAlign.Center,
                                    fontSize = fontSize.sp, lineHeight = (fontSize * 1.4f).sp, fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            if (!full) {
                LinearProgressIndicator(
                    progress = { (safe + 1f) / phrases.size }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.outlineVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { index = (safe - 1).coerceAtLeast(0) }, enabled = safe > 0,
                        modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(18.dp)
                    ) { Icon(Icons.Default.ArrowBack, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Anterior") }
                    Button(
                        onClick = { index = if (safe < phrases.lastIndex) safe + 1 else 0 },
                        modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(18.dp)
                    ) {
                        if (safe < phrases.lastIndex) {
                            Text("Siguiente"); Spacer(Modifier.width(8.dp)); Icon(Icons.Default.ArrowForward, null, Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.Refresh, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Repetir")
                        }
                    }
                }
            }

            FilledTonalButton(
                onClick = { togglePin() }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp)
            ) {
                Icon(if (isPinned) Icons.Default.Check else Icons.Default.Lock, null, Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(if (isPinned) "Fijado en la pantalla de bloqueo · Quitar" else "Fijar en la pantalla de bloqueo")
            }
            Text(
                "También puedes añadir el widget «Guiones sociales» desde tu pantalla de inicio.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
        }
    }
}
