package io.nanfeng.genericx.compiler

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.incremental.util.ExceptionLocation
import org.jetbrains.kotlin.incremental.util.reportException
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.js.utils.typeArguments
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.declarations.addField
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrPropertyReference
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSetFieldImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.constructedClass
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction
import org.jetbrains.kotlin.utils.addToStdlib.cast

internal class PickTypeVisitor(
    private val logger: MessageCollector,
) : IrElementVisitorVoid {

    lateinit var irFactory: IrFactory
    val PICK_QUALIFIED_NAME = "io.nanfeng.genericx.core.Pick"
    override fun visitModuleFragment(declaration: IrModuleFragment) {
        irFactory = declaration.irBuiltins.irFactory
        declaration.files.forEach { file ->
            file.accept(this, null)
        }
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitFile(declaration: IrFile) {
        val filteredClasses = declaration
            .declarations
            .filterIsInstance<IrClass>()
        logger.report(CompilerMessageSeverity.ERROR,
            "before ir ${filteredClasses.map { it.dumpKotlinLike() }}"
        )
        val irClassesWithPick = filteredClasses
            .filter {
                it.superTypes
                    .firstOrNull()
                    ?.classOrNull
                    ?.descriptor
                    ?.fqNameSafe?.asString() == PICK_QUALIFIED_NAME
            }
        // filter
        irClassesWithPick
            .mapNotNull {
                it.constructors.singleOrNull() ?: run {
                    logger.reportException(
                        RuntimeException("Pick class must have only one constructor"),
                        ExceptionLocation.INCREMENTAL_COMPILATION
                    )
                    return@run null
                }
            }
            .forEach {
                it.accept(this, null)
            }
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitConstructor(declaration: IrConstructor) {
        declaration.constructedClass.superTypes = emptyList()
        val delegatingConstructorCall =
            declaration.body?.statements?.filterIsInstance<IrDelegatingConstructorCall>()
                ?.firstOrNull() ?: return

        val actualType = delegatingConstructorCall.typeArguments.single() ?: return
        val pickProperties: List<IrProperty> =
            getPropertiesFromDelegatingConstructorCall(delegatingConstructorCall, actualType)
                ?: return

        val setFieldsStatements = mutableSetOf<IrStatement>()

        pickProperties.forEach { irProperty ->
            val createdField = addFieldToConstructedClass(declaration, irProperty)
            createdField.annotations += irProperty.backingField?.annotations ?: irProperty.annotations
            if (actualType.classOrNull?.owner?.isData == true) {
                val valueParameter = declaration.addValueParameter {
                    name = irProperty.name
                    type = irProperty.getter?.returnType ?: irProperty.backingField?.type!!
                    isAssignable = irProperty.isVar
                }
                val thisReceiver = declaration.constructedClass.thisReceiver?.let {
                    IrGetFieldImpl(
                        startOffset = declaration.startOffset,
                        endOffset = declaration.endOffset,
                        type = createdField.type,
                        symbol = createdField.symbol,
                    )
                }
                IrSetFieldImpl(
                    symbol = createdField.symbol,
                    startOffset = declaration.startOffset,
                    endOffset = declaration.endOffset,
                    type = irProperty.getter?.returnType ?: irProperty.backingField?.type!!,

                    ).also {
                    it.receiver = thisReceiver
                    it.value = IrGetValueImpl(
                        startOffset = declaration.startOffset,
                        endOffset = declaration.endOffset,
                        type = valueParameter.type,
                        symbol = valueParameter.symbol,
                    )
                    setFieldsStatements.add(it)
                }
            }
            logger.report(CompilerMessageSeverity.INFO, "add value parameter for  ${irProperty.name}")
        }
        // todo
        val statements = declaration.body!!.statements

        statements.first()
//        declaration.body = irFactory.createBlockBody(
//            startOffset = declaration.startOffset,
//            endOffset = declaration.endOffset,
//            statements =
//        )

        logger.report(
            CompilerMessageSeverity.ERROR,
            "irClassAfter ${declaration.constructedClass.dumpKotlinLike()} is transformed"
        )


    }

    @OptIn(UnsafeCastFunction::class)
    private fun getPropertiesFromDelegatingConstructorCall(
        delegatingConstructorCall: IrDelegatingConstructorCall,
        actualType: IrType
    ) = (delegatingConstructorCall
        .valueArguments
        .filterIsInstance<IrVararg>().firstOrNull()
        ?.elements
        ?.map {
            it.cast<IrPropertyReference>().symbol.owner
        }
        ?: actualType.classOrNull?.owner?.declarations?.filterIsInstance<IrProperty>())

    private fun addFieldToConstructedClass(
        declaration: IrConstructor,
        irProperty: IrProperty
    ) = declaration.constructedClass.addField {
        name = irProperty.name
        type = irProperty.getter?.returnType ?: irProperty.backingField?.type!!
        isFinal = !irProperty.isVar
        isStatic = irProperty.backingField?.isStatic ?: false
        metadata = irProperty.metadata
        visibility = irProperty.backingField?.visibility ?: irProperty.visibility
        origin = irProperty.origin
    }
}