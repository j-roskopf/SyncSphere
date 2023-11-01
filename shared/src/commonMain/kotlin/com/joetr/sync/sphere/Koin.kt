package com.joetr.sync.sphere

import com.joetr.sync.sphere.constants.Dictionary
import com.joetr.sync.sphere.constants.DictionaryImpl
import com.joetr.sync.sphere.coroutineextensions.IoDispatcher
import com.joetr.sync.sphere.coroutineextensions.dispatcherModule
import com.joetr.sync.sphere.data.RoomRepository
import com.joetr.sync.sphere.data.RoomRepositoryImpl
import com.joetr.sync.sphere.ui.new.NewRoomScreenModel
import com.joetr.sync.sphere.ui.pre.PreScreenModel
import com.joetr.sync.sphere.ui.results.ResultsScreenModel
import com.joetr.sync.sphere.ui.time.TimeSelectionScreenModel
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single<Dictionary> { DictionaryImpl }
    single<RoomRepository> { RoomRepositoryImpl(get()) }
    factory { PreScreenModel(get(IoDispatcher), get()) }
    factory { ResultsScreenModel(get(IoDispatcher), get()) }
    factory { NewRoomScreenModel(get(IoDispatcher), get()) }
    factory { TimeSelectionScreenModel(get(IoDispatcher), get()) }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(appModule, dispatcherModule)
}

fun initKoin() = initKoin {}
