package com.joetr.sync.sphere

import com.joetr.sync.sphere.constants.Dictionary
import com.joetr.sync.sphere.constants.DictionaryImpl
import com.joetr.sync.sphere.coroutineextensions.IoDispatcher
import com.joetr.sync.sphere.coroutineextensions.dispatcherModule
import com.joetr.sync.sphere.data.CrashReporting
import com.joetr.sync.sphere.data.CrashReportingImpl
import com.joetr.sync.sphere.data.RoomRepository
import com.joetr.sync.sphere.data.RoomRepositoryImpl
import com.joetr.sync.sphere.ui.new.NewRoomScreenModel
import com.joetr.sync.sphere.ui.pre.PreScreenModel
import com.joetr.sync.sphere.ui.results.ResultsScreenModel
import com.joetr.sync.sphere.ui.time.TimeSelectionScreenModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModule = module {
    factory { PreScreenModel(get(IoDispatcher), get()) }
    factory { ResultsScreenModel(get(), get()) }
    factory { NewRoomScreenModel(get()) }
    factory { TimeSelectionScreenModel(get()) }
    single<RoomRepository> { RoomRepositoryImpl(get(), get()) }
    single<Dictionary> { DictionaryImpl }
    single<CrashReporting> { CrashReportingImpl() }
}

fun initKoin() = startKoin {
    modules(appModule, dispatcherModule)
}
