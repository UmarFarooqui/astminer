package cli

import astminer.common.getNormalizedToken
import astminer.common.model.*
import astminer.common.preOrder
import astminer.common.setNormalizedToken
import astminer.common.splitToSubtokens
import astminer.parse.antlr.java.JavaMethodSplitter
import astminer.parse.antlr.java.JavaParser
import astminer.parse.antlr.python.PythonMethodSplitter
import astminer.parse.antlr.python.PythonParser
import astminer.parse.cpp.FuzzyCppParser
import astminer.parse.cpp.FuzzyMethodSplitter
import astminer.paths.Code2VecPathStorage
import astminer.paths.PathMiner
import astminer.paths.PathRetrievalSettings
import astminer.paths.toPathContext
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import java.io.File

class Code2VecExtractor : CliktCommand() {

    val methodContent: String by option(
        "--methodContent",
        help = "Content of the method for which the path contexts will be generated"
    ).required()

    val outputDirName: String by option(
        "--output",
        help = "Path to directory where the output will be stored"
    ).required()

    val maxPathContexts: Int by option(
        "--maxContexts",
        help = "Number of path contexts to keep from each method."
    ).int().default(500)

    val maxTokens: Long by option(
        "--maxTokens",
        help = "Keep only contexts with maxTokens most popular tokens."
    ).long().default(Long.MAX_VALUE)

    val maxPaths: Long by option(
        "--maxPaths",
        help = "Keep only contexts with maxTokens most popular paths."
    ).long().default(Long.MAX_VALUE)

    private fun <T : Node> extractFromMethods(
        root: T,
        methodSplitter: TreeMethodSplitter<T>,
        miner: PathMiner,
        storage: Code2VecPathStorage
    ) {
        val methods = methodSplitter.splitIntoMethods(root)

        methods.forEach { methodInfo ->
            val methodNameNode = methodInfo.method.nameNode ?: return@forEach
            val methodRoot = methodInfo.method.root
            val label = splitToSubtokens(methodNameNode.getToken()).joinToString("|")
            methodRoot.preOrder().forEach { it.setNormalizedToken() }
            methodNameNode.setNormalizedToken("METHOD_NAME")

            // Retrieve paths from every node individually
            val paths = miner.retrievePaths(methodRoot).take(maxPathContexts)
            storage.store(LabeledPathContexts(label, paths.map {
                toPathContext(it) { node ->
                    node.getNormalizedToken()
                }
            }))
        }
    }

    private fun extract() {
        val maxPathHeight = 5    // Maximum height of path for code2vec
        val maxPathWidth = 5    // Maximum width of path. Note, that here width is the difference between token indices in contrast to the original code2vec.
        val miner = PathMiner(PathRetrievalSettings(maxPathHeight, maxPathWidth))
        val storage = Code2VecPathStorage(outputDirName)

        val parser = PythonParser()
        val input = methodContent
        val input_stream = input.byteInputStream(Charsets.UTF_8)
        val root = parser.parse(input_stream)
        if (root != null)
        {
            val non_nullable_root = root
            extractFromMethods(non_nullable_root, PythonMethodSplitter(), miner, storage)
        }

        // Save stored data on disk
        // TODO: implement batches for path context extraction
        storage.save(maxPaths, maxTokens)
    }

    override fun run() {
        extract()
    }
}