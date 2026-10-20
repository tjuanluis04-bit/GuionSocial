package com.guiones.sociales

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Recibe los botones Anterior / Siguiente / Quitar de la notificación y del widget. */
class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val store = Store(context)
        val script = store.loadScripts().firstOrNull { it.id == store.pinnedId }
        if (script == null) {
            Notifier.cancel(context); ScriptWidget.updateAll(context); return
        }
        val last = Phrases.split(script.text).lastIndex
        when (intent.action) {
            Notifier.ACTION_NEXT -> store.pinnedIndex = (store.pinnedIndex + 1).coerceAtMost(last)
            Notifier.ACTION_PREV -> store.pinnedIndex = (store.pinnedIndex - 1).coerceAtLeast(0)
            Notifier.ACTION_DISMISS -> {
                store.pinnedId = -1L
                Notifier.cancel(context); ScriptWidget.updateAll(context); return
            }
        }
        Notifier.show(context)
        ScriptWidget.updateAll(context)
    }
}
