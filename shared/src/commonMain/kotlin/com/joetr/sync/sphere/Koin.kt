package com.joetr.sync.sphere

import com.joetr.sync.sphere.constants.Dictionary
import com.joetr.sync.sphere.constants.DictionaryImpl
import com.joetr.sync.sphere.coroutineextensions.IoDispatcher
import com.joetr.sync.sphere.coroutineextensions.dispatcherModule
import com.joetr.sync.sphere.crash.CrashReporting
import com.joetr.sync.sphere.crash.CrashReportingImpl
import com.joetr.sync.sphere.data.RoomConstants
import com.joetr.sync.sphere.data.RoomRepository
import com.joetr.sync.sphere.data.RoomRepositoryImpl
import com.joetr.sync.sphere.ui.icon.IconSelectionModel
import com.joetr.sync.sphere.ui.new.NewRoomScreenModel
import com.joetr.sync.sphere.ui.pre.PreScreenModel
import com.joetr.sync.sphere.ui.previous.PreviousScreenModel
import com.joetr.sync.sphere.ui.results.ResultsScreenModel
import com.joetr.sync.sphere.ui.time.TimeSelectionScreenModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule = module {
    factory { IconSelectionModel(get()) }
    factory { PreScreenModel(get(IoDispatcher), get()) }
    factory { ResultsScreenModel(get(), get(), get(IoDispatcher)) }
    factory { NewRoomScreenModel(get(), get(IoDispatcher)) }
    factory { PreviousScreenModel(get(IoDispatcher), get(), get()) }
    factory { TimeSelectionScreenModel(get(), get(IoDispatcher)) }
    single<RoomRepository> { RoomRepositoryImpl(get(), get(), get(), get()) }
    single<Dictionary> { DictionaryImpl }
    single<CrashReporting> { CrashReportingImpl() }
    single { RoomConstants(get()) }
    single { SyncSphereRoomDatabase(get()) }
}

@Suppress("SpreadOperator")
fun initKoin(
    block: KoinApplication.() -> Unit = {
        // no-op by default
    },
    modules: List<Module>,
) = startKoin {
    this.block()
    modules(appModule, dispatcherModule, *modules.toTypedArray())
}
