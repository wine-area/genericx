package io.nanfeng.genericx.compiler

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

class GenericSymbolProcessorTest {

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `test pick`() {
        val kotlin = SourceFile.kotlin(
            "TestPick.kt",
            """
import io.nanfeng.genericx.core.GenericX
import io.nanfeng.genericx.core.Pick
import org.jetbrains.annotations.NotNull
    data class TestPick(
        @GenericX
        val name: String,
        val age: Int,
        val address: String
    )

    class TestPick1 : Pick<TestPick>(TestPick::name, TestPick::age)
    fun helloWorld() = Unit
    fun helloWorld2(arg: Any) = arg
    fun helloWorld3(arg: Int, arg2: Float) = arg2
   
  fun main() {
 println(TestPick1::class)
  }
        """.trimIndent()
        )

        val compilation = KotlinCompilation().apply {
            sources = listOf(kotlin)
            compilerPluginRegistrars = listOf(GenericXCompilerPluginRegistrar())
            commandLineProcessors = listOf(FPCommandLineProcessor())
            this.pluginOptions = listOf(
                PluginOption(
                    pluginId = "io.nanfeng.genericx",
                    optionName = "tag",
                    optionValue = "GenericX"
                )
            )

            // 执行文件中的main函数

            inheritClassPath = true
        }
        compilation.compile()



    }


}