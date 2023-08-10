package io.nanfeng.genericx.compiler

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.incremental.util.ExceptionLocation
import org.jetbrains.kotlin.incremental.util.reportException
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.js.utils.typeArguments
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.declarations.addField
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrPropertyReference
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.constructedClass
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction
import org.jetbrains.kotlin.utils.addToStdlib.cast

internal class PickTypeVisitor(
    private val logger: MessageCollector,
) : IrElementVisitorVoid {


    val PICK_QUALIFIED_NAME = "io.nanfeng.genericx.core.Pick"
    override fun visitModuleFragment(declaration: IrModuleFragment) {
        declaration.files.forEach { file ->
            file.accept(this, null)
        }
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitFile(declaration: IrFile) {
        val irClassesWithPick = declaration
            .declarations
            .filterIsInstance<IrClass>()
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

    @OptIn(UnsafeCastFunction::class)
    override fun visitConstructor(declaration: IrConstructor) {
        val delegatingConstructorCall =
            declaration.body?.statements?.filterIsInstance<org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall>()
                ?.firstOrNull() ?: return

        val actualType = delegatingConstructorCall.typeArguments.single() ?: return


        val pickProperties: List<IrProperty> = delegatingConstructorCall
            .valueArguments
            .filterIsInstance<IrVararg>().firstOrNull()
            ?.elements
            ?.map {
                it.cast<IrPropertyReference>().symbol.owner
            }
            ?: actualType.classOrNull?.owner?.declarations?.filterIsInstance<IrProperty>()
            ?: return
        pickProperties.forEach {
            declaration.constructedClass.addField(
                it.name,it.getter!!.returnType,it.visibility
            )

        }


    }
}