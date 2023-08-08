package io.nanfeng.genericx.compiler

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.nanfeng.genericx.core.GenericX
import io.nanfeng.genericx.core.Pick
import net.bytebuddy.ByteBuddy
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

class GenericSymbolProcessorTest {
    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `test pick`() {
        val kotlin = SourceFile.kotlin(
            "TestPick.kt", """


        """.trimIndent()
        )

        val compilation = KotlinCompilation().apply {
            sources = listOf(kotlin)
            symbolProcessorProviders = listOf(GenericSymbolProcessorProvider())
            inheritClassPath = true
            messageOutputStream = System.out
        }
        compilation.compile()
    }

    data class TestPick(
        val name: String,
        val age: Int,
        val address: String
    )

    @GenericX
    class TestPick1 : Pick<TestPick>(TestPick::name, TestPick::age)

    @Test

    fun `test required`() {
        ByteBuddy()
            .subclass(Pick::class.java)
            .make()
            .load(Pick::class.java.classLoader)

    }
}