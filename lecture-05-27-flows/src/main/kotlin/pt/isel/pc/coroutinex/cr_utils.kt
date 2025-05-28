package pt.isel.pc.coroutinex

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

fun getJobState(job: Job) : String {
    return "isActive=${job.isActive}, isCancelled=${job.isCancelled}, isCompleted=${job.isCompleted}"
}

fun getJobTypeFomContext(context: CoroutineContext) : String {
    val job = context[Job]
    return if (job != null) "${job::class.simpleName}"; else "None"
}

suspend fun getCurrentJobType() : String {
    val job = coroutineContext[Job]
    return if (job != null) "${job::class.simpleName}"; else "None"
}

fun showContext(context: CoroutineContext) {
    val name = context[CoroutineName]?.name?:context[MyCoroutineName]?:"None"
    val builder = StringBuilder("Name: $name")
    builder.append(", Job: ${context[Job]}")
    val interceptor = context[ContinuationInterceptor]
    val interceptorName = if (interceptor != null) interceptor::class.simpleName ; else "None"
    builder.append(", Interceptor: $interceptorName")
    println(builder.toString())
}

suspend fun showContext() {
    showContext(coroutineContext)
}

// context element example
class MyCoroutineName(val name: String) : CoroutineContext.Element {
    companion object Key: CoroutineContext.Key<MyCoroutineName>
    override val key: CoroutineContext.Key<*>
        get() = MyCoroutineName
    
    override fun toString(): String = name
}

