package com.guiones.sociales

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews

class ScriptWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { manager.updateAppWidget(it, build(context)) }
    }

    companion object {
        fun build(ctx: Context): RemoteViews {
            val rv = RemoteViews(ctx.packageName, R.layout.widget_script)
            val store = Store(ctx)
            val script = store.loadScripts().firstOrNull { it.id == store.pinnedId }
            if (script == null) {
                rv.setTextViewText(R.id.w_title, "Guiones sociales")
                rv.setTextViewText(R.id.w_counter, "")
                rv.setTextViewText(R.id.w_text, "Abre la app y fija un guion para tenerlo siempre a la mano.")
            } else {
                val phrases = Phrases.split(script.text)
                val idx = store.pinnedIndex.coerceIn(0, phrases.lastIndex)
                rv.setTextViewText(R.id.w_title, script.title)
                rv.setTextViewText(R.id.w_counter, "${idx + 1}/${phrases.size}")
                rv.setTextViewText(R.id.w_text, phrases[idx])
            }
            rv.setOnClickPendingIntent(R.id.w_prev, Notifier.actionIntent(ctx, Notifier.ACTION_PREV))
            rv.setOnClickPendingIntent(R.id.w_next, Notifier.actionIntent(ctx, Notifier.ACTION_NEXT))
            rv.setOnClickPendingIntent(R.id.w_root, Notifier.openAppIntent(ctx))
            return rv
        }

        fun updateAll(ctx: Context) {
            val mgr = AppWidgetManager.getInstance(ctx)
            val ids = mgr.getAppWidgetIds(ComponentName(ctx, ScriptWidget::class.java))
            ids.forEach { mgr.updateAppWidget(it, build(ctx)) }
        }
    }
}
