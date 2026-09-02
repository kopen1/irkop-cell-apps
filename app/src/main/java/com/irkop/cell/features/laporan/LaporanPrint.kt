package com.irkop.cell.features.laporan

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView

fun printLaporan(context: Context, title: String, bodyHtml: String) {
    val webView = WebView(context)
    webView.settings.javaScriptEnabled = false
    webView.loadDataWithBaseURL(null, "<html><head><meta name='viewport' content='width=device-width'/><style>body{font-family:sans-serif;padding:24px}table{width:100%;border-collapse:collapse}td,th{border:1px solid #999;padding:6px;text-align:left}</style></head><body><h1>${escapeHtml(title)}</h1>$bodyHtml</body></html>", "text/html", "UTF-8", null)
    webView.webViewClient = object : android.webkit.WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            manager.print(title, webView.createPrintDocumentAdapter(title), PrintAttributes.Builder().build())
        }
    }
}

private fun escapeHtml(value: String): String =
    value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
