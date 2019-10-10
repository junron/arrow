package arrow.meta.plugin.testing

import arrow.meta.plugin.testing.Check.GeneratedClasses
import arrow.meta.plugin.testing.Check.GeneratedSourceCode
import org.junit.Test

class CompilationTest {

  @Test
  fun `metadebug consideration works as expected`() {
    assertThis(CompilationData(
      sourceFilename = "Example.kt",
      sourceCode = contentFromResource(javaClass, "Example.kt.source"),
      checks = listOf(
        GeneratedSourceCode(code = contentFromResource(javaClass, "Example.kt.meta")),
        GeneratedClasses(filenamesWithoutExt = listOf("ExampleKt", "ForId2", "Id2", "ForId2\$Companion"))
      ),
      compilationStatus = CompilationStatus.OK
    ))
  }

  @Test
  fun `compilation errors are detected`() {
    assertThis(CompilationData(
      sourceFilename = "Example.kt",
      sourceCode = "classs Error",
      checks = emptyList(),
      compilationStatus = CompilationStatus.COMPILATION_ERROR
    ))
  }
}
