package arrow.meta.plugins.comprehensions

import arrow.meta.plugin.testing.Check.Call
import arrow.meta.plugin.testing.Check.GeneratedClasses
import arrow.meta.plugin.testing.CompilationData
import arrow.meta.plugin.testing.CompilationStatus
import arrow.meta.plugin.testing.Field
import arrow.meta.plugin.testing.Result
import arrow.meta.plugin.testing.assertThis
import org.junit.Test

class ComprehensionsTest {

  companion object {
    const val IO_CLASS_4_TESTS = """
      import kotlin.reflect.KProperty
  
      //metadebug
  
      class IO<A>(val value: A) {
  
        operator fun getValue(value: Any?, property: KProperty<*>): A = TODO()
  
        fun <B> flatMap(f: (A) -> IO<B>): IO<B> =
          f(value)
  
        companion object {
          fun <A> fx(f: IO.Companion.() -> A): IO<A> = TODO()
          fun <A> just(a: A): IO<A> = IO(a)
        }
      }
      """
  }

  @Test
  fun `simple_case`() {
    assertThis(CompilationData(
      sourceFilename = "Example.kt",
      sourceCode = """
          $IO_CLASS_4_TESTS
          
          fun test(): IO<Int> =
            IO.fx {
              val a: Int by IO(1)
              val b: Int by IO(2)
              a + b
            }
          """,
      checks = listOf(
        GeneratedClasses(filenamesWithoutExt = listOf(
          "ExampleKt", "IO", "IO\$Companion", "ExampleKt\$\$test\$lambda-1\$lambda-0\$1", "\$test\$lambda-1\$0")),
        Call(
          simpleClassName = "ExampleKt",
          methodName = "test",
          output = Result(
            simpleClassName = "IO",
            field = Field(name = "value", value = 3)
          ))
      ),
      compilationStatus = CompilationStatus.OK
    ))
  }

  @Test
  fun `simple_case_with_type_inference`() {
    assertThis(CompilationData(
      sourceFilename = "Example.kt",
      sourceCode = """
          $IO_CLASS_4_TESTS
          
          fun test(): IO<Int> =
            IO.fx {
              val a by IO(1)
              val b by IO(2)
              a + b
            }
          """,
      checks = listOf(
        GeneratedClasses(filenamesWithoutExt = listOf(
          "ExampleKt", "IO", "IO\$Companion", "ExampleKt\$\$test\$lambda-1\$lambda-0\$1", "\$test\$lambda-1\$0")),
        Call(
          simpleClassName = "ExampleKt",
          methodName = "test",
          output = Result(
            simpleClassName = "IO",
            field = Field(name = "value", value = 3)
          ))
      ),
      compilationStatus = CompilationStatus.OK
    ))
  }

  @Test
  fun `nested_case_with_type_inference`() {
    assertThis(CompilationData(
      sourceFilename = "Example.kt",
      sourceCode = """
          $IO_CLASS_4_TESTS
          
          fun test(): IO<Int> =
            IO.fx {
              val a by IO.fx {
                val a by IO(1)
                val b by IO(2)
                a + b
              }
              val b by IO.fx {
                val a by IO(3)
                val b by IO(4)
                a + b
              }
              a + b
            }
          """,
      checks = listOf(
        GeneratedClasses(filenamesWithoutExt = listOf(
          "ExampleKt", "IO", "IO\$Companion", "ExampleKt\$\$test\$lambda-1\$lambda-0\$2",
          "ExampleKt\$\$test\$lambda-5\$lambda-3\$4", "ExampleKt\$\$test\$lambda-5\$lambda-3\$lambda-2\$3",
          "ExampleKt\$\$test\$lambda-5\$lambda-4\$5", "\$test\$lambda-1\$0", "\$test\$lambda-5\$1")),
        Call(
          simpleClassName = "ExampleKt",
          methodName = "test",
          output = Result(
            simpleClassName = "IO",
            field = Field(name = "value", value = 10)
          ))
      ),
      compilationStatus = CompilationStatus.OK
    ))
  }

  @Test
  fun `mixed_properties_and_expressions`() {
    assertThis(CompilationData(
      sourceFilename = "Example.kt",
      sourceCode = """
          $IO_CLASS_4_TESTS
          
          fun test(): IO<Int> = 
            IO.fx {
              val a by IO(1)
              val t = a + 1
              val b by IO(2)
              val y = a + b
              val f by IO(3)
              val n = a + 1
              val g by IO(4)
              y + f + g + t + n
            }
        """,
      checks = listOf(
        GeneratedClasses(filenamesWithoutExt = listOf(
          "ExampleKt", "IO", "IO\$Companion", "ExampleKt\$\$test\$lambda-3\$lambda-2\$3",
          "ExampleKt\$\$test\$lambda-3\$lambda-2\$lambda-1\$2",
          "ExampleKt\$\$test\$lambda-3\$lambda-2\$lambda-1\$lambda-0\$1", "\$test\$lambda-3\$0"
        )),
        Call(
          simpleClassName = "ExampleKt",
          methodName = "test",
          output = Result(
            simpleClassName = "IO",
            field = Field(name = "value", value = 14)
          ))
      ),
      compilationStatus = CompilationStatus.OK
    ))
  }
}
