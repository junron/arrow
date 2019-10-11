package arrow.meta.plugin.testing

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.assertj.core.api.Assertions.assertThat
import java.io.File
import java.nio.file.Paths
import io.github.classgraph.ClassGraph
import java.net.URLClassLoader

fun assertThis(compilationData: CompilationData): Unit {

  val sourceFilename = compilationData.sourceFilename

  val compilationResult = KotlinCompilation().apply {
    sources = listOf(SourceFile.kotlin(sourceFilename, compilationData.sourceCode))
    //
    // TODO: waiting for the arrow-annotations release which contains higherkind annotation
    //    classpaths = listOf(classpathOf("arrow-annotations:x.x.x"))
    //
    classpaths = listOf(classpathOf("arrow-annotations:rr-meta-prototype-integration-SNAPSHOT"))
    pluginClasspaths = listOf(classpathOf("compiler-plugin"))
  }.compile()

  compilationData.checks.forEach {
    when (it) {
      is Check.GeneratedSourceCode -> {
        val actualGeneratedFileContent = getGeneratedFileContentFrom(compilationResult.outputDirectory, sourceFilename)
        val actualGeneratedFileContentWithoutCommands = removeCommandsFrom(actualGeneratedFileContent)
        val generatedFileContentWithoutCommands = removeCommandsFrom(it.code)

        assertThat(actualGeneratedFileContentWithoutCommands).isEqualToIgnoringNewLines(generatedFileContentWithoutCommands)
      }
      is Check.GeneratedClasses -> assertGeneratedClasses(it, compilationResult)
      is Check.Call -> assertCall(it, compilationResult)
    }
  }
  assertThat(exitStatusFrom(compilationResult.exitCode)).isEqualTo(compilationData.compilationStatus)
}

fun contentFromResource(fromClass: Class<Any>, resourceName: String): String =
  fromClass.getResource(resourceName).readText()

private fun assertGeneratedClasses(generatedClasses: Check.GeneratedClasses, compilationResult: KotlinCompilation.Result) {
  val actualGeneratedClasses = classFilenamesFrom(compilationResult.generatedFiles)
  val expectedGeneratedClasses = createClassFilenamesFrom(generatedClasses.filenamesWithoutExt)

  assertThat(actualGeneratedClasses).containsExactlyInAnyOrder(*expectedGeneratedClasses.toTypedArray())
}

private fun assertCall(call: Check.Call, compilationResult: KotlinCompilation.Result) {
  val classLoader = URLClassLoader(arrayOf(compilationResult.outputDirectory.toURI().toURL()))
  val resultForTest = classLoader.loadClass(call.simpleClassName).getMethod(call.methodName).invoke(null)

  assertThat(resultForTest::class.simpleName).isEqualTo(call.output.simpleClassName)

  val expectedField = call.output.field
  val actualField = resultForTest.javaClass.getField(expectedField.name).get(resultForTest)

  assertThat(actualField).isEqualTo(expectedField.value)
  assertThat(actualField::class).isEqualTo(expectedField.value::class)
}

private fun getGeneratedFileContentFrom(outputDirectory: File, sourceFilename: String): String =
  Paths.get(outputDirectory.parent, "sources", "$sourceFilename.meta").toFile().readText()

private fun createClassFilenamesFrom(filenamesWithoutExt: List<String>): List<String> =
  filenamesWithoutExt.map { "$it.class" }

private fun classFilenamesFrom(generatedFiles: Collection<File>): List<String> =
  generatedFiles.map { it.name }.filter { it.endsWith(".class") }

private fun classpathOf(dependency: String): File {
  val regex = Regex(".*${dependency.replace(':', '-')}.*")
  return ClassGraph().classpathFiles.first { classpath -> classpath.name.matches(regex) }
}

private fun removeCommandsFrom(actualGeneratedFileContent: String): String =
  actualGeneratedFileContent.lines().filter { !it.startsWith(META_PREFIX) }.joinToString()

private fun exitStatusFrom(exitCode: KotlinCompilation.ExitCode): CompilationStatus =
  when (exitCode) {
    KotlinCompilation.ExitCode.OK -> CompilationStatus.OK
    KotlinCompilation.ExitCode.INTERNAL_ERROR -> CompilationStatus.INTERNAL_ERROR
    KotlinCompilation.ExitCode.COMPILATION_ERROR -> CompilationStatus.COMPILATION_ERROR
    KotlinCompilation.ExitCode.SCRIPT_EXECUTION_ERROR -> CompilationStatus.SCRIPT_EXECUTION_ERROR
  }
