package io.nanfeng.genericx.compiler

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

@AutoService(SymbolProcessorProvider::class)
class GenericSymbolProcessorProvider: SymbolProcessorProvider{
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = GenericSymbolProcessor(environment)
}