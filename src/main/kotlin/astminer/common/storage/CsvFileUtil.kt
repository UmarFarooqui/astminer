package astminer.common.storage

import astminer.common.model.OrientedNodeType
import java.io.File

fun <T> dumpIdStorageToCsv(storage: RankedIncrementalIdStorage<T>,
                           typeHeader: String,
                           csvSerializer: (T) -> String,
                           file: File,
                           limit: Long = Long.MAX_VALUE) {
    val lines = mutableListOf("id,$typeHeader")

    storage.idPerItem.forEach {
        val id = it.value
        val item = it.key
        if (storage.getKeyRank(item) <= limit) {
            lines.add("$id,${csvSerializer.invoke(item)}")
        }
    }

    writeLinesToFile(lines, file)
}

fun <T> getIdStorageCsv(storage: RankedIncrementalIdStorage<T>,
                           typeHeader: String,
                           csvSerializer: (T) -> String,
                           limit: Long = Long.MAX_VALUE): MutableList<String> {
    val lines = mutableListOf("id,$typeHeader")

    storage.idPerItem.forEach {
        val id = it.value
        val item = it.key
        if (storage.getKeyRank(item) <= limit) {
            lines.add("$id,${csvSerializer.invoke(item)}")
        }
    }

    return lines
}

fun writeLinesToFile(lines: Collection<String>, file: File) {
    file.printWriter().use { out ->
        lines.forEach { out.println(it) }
    }
}

val tokenToCsvString: (String) -> String = { token -> token }

val nodeTypeToCsvString: (String) -> String = { nodeType -> nodeType }

val orientedNodeToCsvString: (OrientedNodeType) -> String = { nt -> "${nt.typeLabel} ${nt.direction}" }

val pathToCsvString: (List<Long>) -> String = { path -> path.joinToString(separator = " ") }
