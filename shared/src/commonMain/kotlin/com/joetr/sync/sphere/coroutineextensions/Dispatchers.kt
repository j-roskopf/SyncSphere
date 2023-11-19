package com.joetr.sync.sphere.coroutineextensions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.qualifier.named
import org.koin.dsl.module

val DefaultDispatcher = named("DefaultDispatcher")
val IoDispatcher = named("IoDispatcher")
val MainDispatcher = named("MainDispatcher")
val MainImmediateDispatcher = named("MainImmediateDispatcher")

val dispatcherModule = module {
    single(qualifier = DefaultDispatcher) { Dispatchers.Default }
    single(qualifier = IoDispatcher) { Dispatchers.IO }
    single(qualifier = MainDispatcher) { Dispatchers.Main }
    single(qualifier = MainImmediateDispatcher) { Dispatchers.Main.immediate }
}
