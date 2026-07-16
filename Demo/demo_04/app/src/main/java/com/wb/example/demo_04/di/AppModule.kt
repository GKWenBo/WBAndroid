package com.wb.example.demo_04.di

import com.wb.example.demo_04.data.local.AppDatabase
import com.wb.example.demo_04.data.local.CharacterDao
import com.wb.example.demo_04.data.remote.RickAndMortyApi
import com.wb.example.demo_04.data.remote.RetrofitClient
import com.wb.example.demo_04.data.repository.CharacterRepository
import com.wb.example.demo_04.data.repository.CharacterRepositoryImpl
import com.wb.example.demo_04.ui.characters.CharactersViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Koin 模块 = 依赖图，对应 Swift 的 工厂/单例容器（如 Swinject / 手动 DI）。
// single{} 进程内单例；factory{} 每次新建；viewModel{} 绑定到生命周期的 ViewModel。
val appModule = module {
    // 网络层单例
    single<RickAndMortyApi> { RetrofitClient.api }

    // 数据库（single 保证唯一，对应 iOS 的数据库单例）
    single<AppDatabase> { AppDatabase.build(get()) }   // get() = androidContext 提供的 Context
    single<CharacterDao> { get<AppDatabase>().characterDao() }

    // Repository（依赖上面两个 single）
    single<CharacterRepository> { CharacterRepositoryImpl(get(), get()) }

    // ViewModel（Koin 自动按生命周期创建与复用）
    viewModel { CharactersViewModel(get()) }
}
