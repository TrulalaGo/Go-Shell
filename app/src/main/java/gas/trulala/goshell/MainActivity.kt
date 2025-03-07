
package gas.trulala.goshell

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import android.view.View
import android.view.LayoutInflater
import android.content.Intent
import java.io.*
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile

class MainActivity : AppCompatActivity() {
    private var copiedFile: File? = null
    private var isCutOperation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val terminalBtn = findViewById<ImageView>(R.id.terminal)
        val liner = findViewById<LinearLayout>(R.id.liner)
        terminalBtn.setOnClickListener {
            liner.visibility = if (liner.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        val hasil = findViewById<TextView>(R.id.hasil)
        val perintahInput = findViewById<EditText>(R.id.perintah)
        val mulaiBtn = findViewById<TextView>(R.id.mulai)
        val clearBtn = findViewById<Button>(R.id.clear)

        mulaiBtn.setOnClickListener {
            val perintah = perintahInput.text.toString()
            try {
                val process = Runtime.getRuntime().exec(arrayOf("/system/bin/sh", "-c", perintah))

                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                val output = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
                while (errorReader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }

                hasil.append(output.toString())
            } catch (e: IOException) {
                hasil.append("Error: ${e.message}\n")
            }
        }

        clearBtn.setOnClickListener {
            hasil.text = ""
        }

        val grid = findViewById<GridLayout>(R.id.grid)
        val tekanLama = findViewById<LinearLayout>(R.id.tekanLama)
        val salin = findViewById<ImageView>(R.id.salin)
        val potong = findViewById<ImageView>(R.id.potong)
        val hapus = findViewById<ImageView>(R.id.hapus)
        val tempel = findViewById<ImageView>(R.id.tempel)
        val setNama = findViewById<ImageView>(R.id.rename)
        val folderBaru = findViewById<ImageView>(R.id.folderBaru)
        val fileBaru = findViewById<ImageView>(R.id.fileBaru)
        val memoFolder = findViewById<ImageView>(R.id.memoFolder)
        val memoFile = findViewById<ImageView>(R.id.memoFile)

        val memoDir = filesDir
        refreshGrid(grid, memoDir)

        folderBaru.setOnClickListener {
            showCreateDialog("Folder") { name ->
                val newFolder = File(memoDir, name)
                if (!newFolder.exists()) {
                    if (newFolder.mkdirs()) {
                        Toast.makeText(this, "Folder berhasil dibuat", Toast.LENGTH_SHORT).show()
                        refreshGrid(grid, memoDir)
                    } else {
                        Toast.makeText(this, "Gagal membuat folder", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fileBaru.setOnClickListener {
            showCreateDialog("File") { name ->
                val newFile = File(memoDir, name)
                if (!newFile.exists()) {
                    if (newFile.createNewFile()) {
                        Toast.makeText(this, "File berhasil dibuat", Toast.LENGTH_SHORT).show()
                        refreshGrid(grid, memoDir)
                    } else {
                        Toast.makeText(this, "Gagal membuat file", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        memoFolder.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            startActivityForResult(intent, 100)
        }

        memoFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            startActivityForResult(intent, 101)
        }
    }

    private fun refreshGrid(grid: GridLayout, directory: File) {
        grid.removeAllViews()
        directory.listFiles()?.forEach { file ->
            val item = LayoutInflater.from(this).inflate(R.layout.item_vertical, grid, false)
            val gambar = item.findViewById<ImageView>(R.id.gambar)
            val nama = item.findViewById<TextView>(R.id.nama)

            nama.text = file.name
            gambar.setImageResource(if (file.isDirectory) R.drawable.folder else R.drawable.file)

            grid.addView(item)

            val kembali = findViewById<ImageView>(R.id.kembali)
            val rootDir = filesDir 
            var currentDir = rootDir

            item.setOnClickListener {
             kembali.visibility = View.VISIBLE
            if (file.isDirectory) {
            currentDir = file
            refreshGrid(grid, file)
            } else {
            openFile(file)
        }
    }

        kembali.setOnClickListener {
            if (currentDir.parentFile != null && currentDir != rootDir) {
        currentDir = currentDir.parentFile!!
        refreshGrid(grid, currentDir)
        } 

    if (currentDir == rootDir) {
        kembali.visibility = View.GONE
    }
}

            item.setOnLongClickListener {
                showLongPressMenu(file, grid)
                true
            }
        }
    }

    private fun showLongPressMenu(file: File, grid: GridLayout) {
        val tekanLama = findViewById<LinearLayout>(R.id.tekanLama)
        val salin = findViewById<ImageView>(R.id.salin)
        val potong = findViewById<ImageView>(R.id.potong)
        val hapus = findViewById<ImageView>(R.id.hapus)
        val tempel = findViewById<ImageView>(R.id.tempel)
        val setNama = findViewById<ImageView>(R.id.rename)

        tekanLama.visibility = View.VISIBLE

        salin.setOnClickListener {
            copiedFile = file
            isCutOperation = false
            tekanLama.visibility = View.GONE
        }

        potong.setOnClickListener {
            copiedFile = file
            isCutOperation = true
            tekanLama.visibility = View.GONE
        }

        setNama.setOnClickListener {
            showRenameDialog(file, grid)
            tekanLama.visibility = View.GONE
        }

        tempel.setOnClickListener {
            if (copiedFile != null) {
            val destination = File(file.parentFile, copiedFile!!.name)
            if (isCutOperation) {
            if (copiedFile!!.renameTo(destination)) {
                Toast.makeText(this, "File/Folder dipindahkan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal memindahkan File/Folder", Toast.LENGTH_SHORT).show()
            }
            } else {
            copiedFile!!.copyTo(destination)
            }
            refreshGrid(grid, file.parentFile)
            tekanLama.visibility = View.GONE
        }
    }

        hapus.setOnClickListener {
            file.delete()
            refreshGrid(grid, file.parentFile)
            tekanLama.visibility = View.GONE
        }
    }

    private fun showRenameDialog(file: File, grid: GridLayout) {
        val dialog = LayoutInflater.from(this).inflate(R.layout.item_ketik, null)
        val ketik = dialog.findViewById<EditText>(R.id.ketik)
        val batal = dialog.findViewById<Button>(R.id.batal)
        val mulai = dialog.findViewById<Button>(R.id.mulai)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialog)
            .create()

        mulai.setOnClickListener {
            val newName = ketik.text.toString()
            val newFile = File(file.parentFile, newName)
            if (file.renameTo(newFile)) {
                Toast.makeText(this, "Nama berhasil diubah", Toast.LENGTH_SHORT).show()
                refreshGrid(grid, file.parentFile)
            } else {
                Toast.makeText(this, "Gagal mengubah nama", Toast.LENGTH_SHORT).show()
            }
            alertDialog.dismiss()
        }

        batal.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun openFile(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.fromFile(file), getMimeType(file))
        startActivity(intent)
    }

    private fun getMimeType(file: File): String {
        return when (file.extension) {
            "txt" -> "text/plain"
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            else -> "*/*"
        }
    }

    private fun showCreateDialog(type: String, onCreate: (String) -> Unit) {
        val dialog = LayoutInflater.from(this).inflate(R.layout.item_ketik, null)
        val ketik = dialog.findViewById<EditText>(R.id.ketik)
        val batal = dialog.findViewById<Button>(R.id.batal)
        val mulai = dialog.findViewById<Button>(R.id.mulai)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialog)
            .create()

        mulai.setOnClickListener {
            val name = ketik.text.toString()
            if (name.isNotEmpty()) {
                onCreate(name)
            }
            alertDialog.dismiss()
        }

        batal.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode == RESULT_OK && data != null) {
        val uri = data.data ?: return
        val currentDir = File(filesDir, "home") 

        if (requestCode == 100) { 
            val pickedDir = DocumentFile.fromTreeUri(this, uri)
            if (pickedDir != null) {
                copyDocumentTree(pickedDir, currentDir)
            }
        } else if (requestCode == 101) { 
            val pickedFile = DocumentFile.fromSingleUri(this, uri)
            if (pickedFile != null) {
                copyDocumentFile(pickedFile, currentDir)
            }
        }
        refreshGrid(findViewById<GridLayout>(R.id.grid), currentDir)
    }
}

private fun copyDocumentTree(sourceDir: DocumentFile, destinationDir: File) {
    sourceDir.listFiles().forEach { file ->
        if (file.isDirectory) {
            val newDestinationDir = File(destinationDir, file.name!!)
            newDestinationDir.mkdirs()
            copyDocumentTree(file, newDestinationDir)
        } else {
            copyDocumentFile(file, destinationDir)
        }
    }
}

    private fun copyDocumentFile(sourceFile: DocumentFile, destinationDir: File) {
        try {
            val inputStream = contentResolver.openInputStream(sourceFile.uri)
            val outputFile = File(destinationDir, sourceFile.name!!)

            inputStream?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(this, "File ${sourceFile.name} disalin", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal menyalin ${sourceFile.name}", Toast.LENGTH_SHORT).show()
        }
    }
}
