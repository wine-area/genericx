package io.nanfeng.genericx.compiler

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.constructors


class BuildIrClass {
    /**
     * Build ir class
     * 如果当前类不是Pick<T>的子类，跳过
     * 如果当前类是Pick<T>的子类，那么
     * 改写当前类的ir,使其成为一个DataClass：
     * 1. 去掉当前类的父类 即Pick<T>
     * 2. 获取调用父类时传入的类属性引用，如果存在，则新的IrClass只包含对应的属性
     * 3. 如果不存在，则新的IrClass包含所有属性
     * 4. 继承原来属性上的注解，如果属性引用存在新的注解，添加新的注解
     */
    fun build(irClass: IrClass) {
        // 1. 去掉当前类的父类 即Pick<T>
        irClass.superTypes = emptyList()
        // 2. 获取调用父类时传入的类属性引用，如果存在，则新的IrClass只包含对应的属性
        irClass.constructors.first()
        // 3. 如果不存在，则新的IrClass包含所有属性
        // 4. 继承原来属性上的注解，如果属性引用存在新的注解，添加新的注解

    }
}