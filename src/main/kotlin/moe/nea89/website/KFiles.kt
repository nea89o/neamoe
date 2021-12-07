package moe.nea89.website

sealed interface KFile {
    val fileType: String
        get() = when (this) {
            is KFile.Directory -> "directory"
            is KFile.Download -> "download"
            is KFile.Image -> "image"
            is KFile.Text -> "text file"
        }

    data class Text(val text: String) : KFile
    data class Image(val url: String) : KFile
    data class Download(val url: String) : KFile
    data class Directory(val files: Map<String, KFile>) : KFile
}

data class KFileSystem(val root: KFile.Directory) {
    /**
     * Uses normalized paths
     * */
    fun resolve(parts: List<String>): KFile? =
        parts.fold<String, KFile?>(root) { current, part ->
            if (current is KFile.Directory) {
                current.files[part]
            } else
                null
        }
}


enum class FSError {
    ENOENT, EISNOTDIR
}

class FileAccessor(val fileSystem: KFileSystem, var implicitPushD: Boolean = false) { // TODO implicit pushd support
    val dirStack = mutableListOf<List<String>>()
    var currentDir = listOf<String>()

    private fun directoryUp(): FSError? {
        if (currentDir.isEmpty()) return FSError.ENOENT
        currentDir = currentDir.dropLast(1)
        return null
    }

    private fun tryEnterDirectory(path: String): FSError? {
        val cwd = fileSystem.resolve(currentDir)
            ?: throw RuntimeException("Current working directory $currentDir does not exist in filesystem")
        if (cwd !is KFile.Directory)
            throw RuntimeException("Current working directory $currentDir is not a directory in filesystem")
        val file = cwd.files[path] ?: return FSError.ENOENT
        if (file !is KFile.Directory) return FSError.EISNOTDIR
        currentDir = currentDir + path
        return null
    }

    fun cdSingle(path: String): FSError? {
        if ('/' in path) throw RuntimeException("Cannot single cd with path: $path")
        return when (path) {
            "." -> null
            ".." -> directoryUp()
            "" -> null
            else -> tryEnterDirectory(path)
        }
    }

    fun cd(path: String): FSError? {
        val parts = path.split("/").filter { it.isNotEmpty() }
        val rollbackPath = currentDir
        if (path.startsWith("/")) {
            currentDir = emptyList()
        }
        parts.forEach {
            val error = cdSingle(it)
            if (error != null) {
                currentDir = rollbackPath
                return error
            }
        }
        return null
    }

    fun resolve(path: String): KFile? {
        val parts = path.split("/").filter { it.isNotEmpty() && it != "." }
        return if (path.startsWith("/")) {
            fileSystem.resolve(parts)
        } else {
            fileSystem.resolve(currentDir + parts)
        }
    }

    fun pushD() {
        dirStack.add(currentDir)
    }

    fun useD(block: () -> Unit) {
        val d = currentDir
        try {
            block()
        } finally {
            currentDir = d
        }
    }

    fun popD(): Boolean {
        currentDir = dirStack.removeLastOrNull() ?: return false
        return true
    }
}

@DslMarker
annotation class KFileDsl

fun fileSystem(block: FileSystemBuilder.() -> Unit): KFileSystem =
    KFileSystem(FileSystemBuilder().also(block).build())


@KFileDsl
class FileSystemBuilder {
    private val files = mutableMapOf<String, KFile>()

    fun addNode(name: String, file: KFile): FileSystemBuilder {
        val parts = name.split("/", limit = 2)
        if (parts.size != 1) {
            return addNode(parts[0], FileSystemBuilder().addNode(parts[1], file).build())
        }
        if (files.containsKey(name)) {
            throw RuntimeException("Tried to double set file: $name")
        }
        files[name] = file
        return this
    }

    infix fun String.text(rawText: String) {
        addNode(this, KFile.Text(rawText))
    }

    infix fun String.image(dataUrl: String) {
        addNode(this, KFile.Image(dataUrl))
    }

    infix fun String.download(url: String) {
        addNode(this, KFile.Download(url))
    }

    operator fun String.invoke(block: FileSystemBuilder.() -> Unit) {
        addNode(this, FileSystemBuilder().also(block).build())
    }

    fun build() = KFile.Directory(files)
}
