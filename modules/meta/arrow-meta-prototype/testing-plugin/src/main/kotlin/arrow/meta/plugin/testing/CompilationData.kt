package arrow.meta.plugin.testing

const val META_PREFIX = "//meta"

enum class CompilationStatus {
  OK,
  INTERNAL_ERROR,
  COMPILATION_ERROR,
  SCRIPT_EXECUTION_ERROR

}

data class CompilationData(
  val sourceFilename: String,
  val sourceCode: String,
  val compilationStatus: CompilationStatus,
  val checks: List<Check>
)

sealed class Check {

  data class GeneratedSourceCode(
    val code: String
  ): Check()

  data class GeneratedClasses(
    val filenamesWithoutExt: List<String>
  ): Check()

  data class Call(
    val simpleClassName: String,
    val methodName: String,
    val output: Result
  ): Check()

}

data class Field(
  val name: String,
  val simpleClassName: String,
  val value: Any
)

data class Result(
  val simpleClassName: String,
  val fields: List<Field>
)
