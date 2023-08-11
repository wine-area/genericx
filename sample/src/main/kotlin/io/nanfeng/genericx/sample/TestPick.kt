package io.nanfeng.genericx.sample

import io.nanfeng.genericx.core.GenericX
import io.nanfeng.genericx.core.Pick

data class TestPick(
    @GenericX
    val name: String,
    val age: Int,
    val address: String
)

class TestPick1 : Pick<TestPick>(TestPick::name, TestPick::age){

}

fun helloWorld() = Unit
fun helloWorld2(arg: Any) = arg
fun helloWorld3(arg: Int, arg2: Float) = arg2

fun main() {
    val testPick1 = TestPick1::class.java.constructors.firstOrNull()?.newInstance("saas",2)
    println(testPick1)
}