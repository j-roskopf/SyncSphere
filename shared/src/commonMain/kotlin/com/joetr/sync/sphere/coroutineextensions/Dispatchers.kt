package com.joetr.sync.sphere.coroutineextensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.qualifier.named
import org.koin.dsl.module

val DefaultDispatcher = named("DefaultDispatcher")
val IoDispatcher = named("IoDispatcher")
val MainDispatcher = named("MainDispatcher")
val MainImmediateDispatcher = named("MainImmediateDispatcher")

val dispatcherModule = module {
    single<CoroutineDispatcher>(qualifier = DefaultDispatcher) { Dispatchers.Default }
    single<CoroutineDispatcher>(qualifier = IoDispatcher) { Dispatchers.IO }
    single<CoroutineDispatcher>(qualifier = MainDispatcher) { Dispatchers.Main }
    single<CoroutineDispatcher>(qualifier = MainImmediateDispatcher) { Dispatchers.Main.immediate }
}
