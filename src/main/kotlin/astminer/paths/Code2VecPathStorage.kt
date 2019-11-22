package astminer.paths

import astminer.common.storage.*
import java.io.File

class Code2VecPathStorage(outputFolderPath: String) : CountingPathStorage<String>(outputFolderPath) {

    override fun dumpPathContexts(file: File, tokensLimit: Long, pathsLimit: Long) {
        val lines = mutableListOf<String>()
        labeledPathContextIdsList.forEach { labeledPathContextIds ->
            val pathContextIdsString = labeledPathContextIds.pathContexts.filter {
                tokensMap.getIdRank(it.startTokenId) <= tokensLimit &&
                        tokensMap.getIdRank(it.endTokenId) <= tokensLimit &&
                        pathsMap.getIdRank(it.pathId) <= pathsLimit
            }.joinToString(separator = " ") { pathContextId ->
                "${pathContextId.startTokenId},${pathContextId.pathId},${pathContextId.endTokenId}"
            }
            lines.add("${labeledPathContextIds.label} $pathContextIdsString")
        }

        writeLinesToFile(lines, file)
    }

    override fun getPathContexts(file: File, tokensLimit: Long, pathsLimit: Long): MutableList<String> {
        val lines = mutableListOf<String>()
        labeledPathContextIdsList.forEach { labeledPathContextIds ->
            val pathContextIdsString = labeledPathContextIds.pathContexts.filter {
                tokensMap.getIdRank(it.startTokenId) <= tokensLimit &&
                        tokensMap.getIdRank(it.endTokenId) <= tokensLimit &&
                        pathsMap.getIdRank(it.pathId) <= pathsLimit
            }.joinToString(separator = " ") { pathContextId ->
                "${pathContextId.startTokenId},${pathContextId.pathId},${pathContextId.endTokenId}"
            }
            lines.add("${labeledPathContextIds.label} $pathContextIdsString")
        }

        return lines
    }

}
