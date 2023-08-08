package io.nanfeng.genericx.compiler

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import io.nanfeng.genericx.core.*

class GenericSymbolProcessor(val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    companion object {

        val genericX_CLASS_NAME = GenericX::class.qualifiedName!!
    }


    override fun process(resolver: Resolver): List<KSAnnotated> {
        // 获取到当前项目中所有Pick的子类
        val allGenericXClass = resolver.getSymbolsWithAnnotation(genericX_CLASS_NAME)
            .filter { it is KSClassDeclaration }
            .map { it as KSClassDeclaration }
            .toList()
        val genericXClassesWithPick = allGenericXClass.filter {
            it.getAllSuperTypes().any { it.declaration.qualifiedName?.asString() == Pick::class.qualifiedName }
        }
        val genericXClassesWithRequired = allGenericXClass.filter {
            it.getAllSuperTypes().any { it.declaration.qualifiedName?.asString() == Required::class.qualifiedName }
        }
        val genericXClassesWithOmit = allGenericXClass.filter {
            it.getAllSuperTypes().any { it.declaration.qualifiedName?.asString() == Omit::class.qualifiedName }
        }

        val genericXClassesWithPartial = genericXClassesWithPick.filter {
            it.getAllSuperTypes().any { it.declaration.qualifiedName?.asString() == Partial::class.qualifiedName }
        }

        genericXClassesWithPick.map {
            val pickType = it.superTypes.first {
                it.resolve().declaration.qualifiedName!!.asString() == Pick::class.qualifiedName!!
            }

            it.accept(object : KSDefaultVisitor<Any, Any>() {
                override fun defaultHandler(node: KSNode, data: Any): Any {
                    println("defaultHandler: ${node}")
                    return data
                }

            }, Unit)

        }

        return genericXClassesWithPick

    }
}