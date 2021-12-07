package moe.nea89.website

sealed class KFile {
    /**
     * Only be empty for the root fs
     * */
    var parent: Directory? = null
        private set

    val name: List<String>
        get() =
            parent?.let { it.name + it.files.filter { it.value == this }.keys.first() } ?: emptyList()

    fun linkTo(parent: Directory) {
        if (this.parent == null)
            this.parent = parent
    }

    val fileType: String
        get() = when (this) {
            is Directory -> "directory"
            is Download -> "download"
            is Image -> "image"
            is Text -> "text file"
        }

    data class Text(val text: String) : KFile()
    data class Image(val url: String) : KFile()
    data class Download(val url: String) : KFile()
    data class Directory(val files: Map<String, KFile>) : KFile()
}

data class KFileSystem(val root: KFile.Directory) {
    init {
        if (!verifyHierarchy(root)) {
            throw RuntimeException("File system had missing links. Use linkTo with the primary parent directory")
        }
    }

    private fun verifyHierarchy(el: KFile.Directory): Boolean =
        el.files.values.all {
            it.parent == el && (it !is KFile.Directory || verifyHierarchy(it))
        }


    /**
     * Uses normalized paths
     * */
    fun resolve(parts: List<String>): KFile? =
        parts.fold<String, KFile?>(root) { current, part ->
            if (part == "." || part == "")
                current
            else if (part == "..")
                current?.parent
            else if (current is KFile.Directory) {
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

    fun cd(path: String): FSError? {
        val file = resolve(path) ?: return FSError.ENOENT
        return when (file) {
            !is KFile.Directory -> FSError.EISNOTDIR
            else -> {
                currentDir = file.name
                null
            }
        }
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

    fun build() = KFile.Directory(files).also { dir ->
        files.values.forEach { file -> file.linkTo(dir) }
    }
}
