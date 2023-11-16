package com.joetr.sync.sphere.coroutineextensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

val DefaultDispatcher = named("DefaultDispatcher")
val IoDispatcher = named("IoDispatcher")
val MainDispatcher = named("MainDispatcher")
val MainImmediateDispatcher = named("MainImmediateDispatcher")

val dispatcherModule = module {
    single<CoroutineDispatcher>(qualifier = DefaultDispatcher) { Dispatchers.Unconfined }
    single<CoroutineDispatcher>(qualifier = IoDispatcher) { Dispatchers.Unconfined }
    single<CoroutineDispatcher>(qualifier = MainDispatcher) { Dispatchers.Unconfined }
    single<CoroutineDispatcher>(qualifier = MainImmediateDispatcher) { Dispatchers.Unconfined }
}
