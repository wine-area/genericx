package io.nanfeng.genericx.core

import kotlin.reflect.KProperty1

abstract class Pick<T>(vararg properties: KProperty1<T, Any?>) {
}

abstract class Required<T>(vararg properties: KProperty1<T, Any?>) {
}


abstract class Omit<T>(vararg properties: KProperty1<T, Any?>) {
}

abstract class Partial<T>(vararg properties: KProperty1<T, Any?>) {
}


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class GenericX