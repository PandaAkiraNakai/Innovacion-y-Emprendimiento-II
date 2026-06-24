package cl.vigia.app.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/* Genera el informe en la caché y abre la hoja de "compartir" para que el
   vecino lo guarde o lo envíe. No requiere permisos. */
fun shareReport(context: Context, filename: String, content: String, mime: String) {
    val dir = File(context.cacheDir, "informes").apply { mkdirs() }
    val file = File(dir, filename)
    file.writeText(content)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, filename)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir informe"))
}
