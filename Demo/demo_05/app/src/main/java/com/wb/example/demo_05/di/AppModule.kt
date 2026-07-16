package com.wb.example.demo_05.di

import com.wb.example.demo_05.data.local.AppDatabase
import com.wb.example.demo_05.data.local.CachedUserDao
import com.wb.example.demo_05.data.remote.GitHubApi
import com.wb.example.demo_05.data.remote.RetrofitClient
import com.wb.example.demo_05.data.repository.UserRepository
import com.wb.example.demo_05.data.repository.UserRepositoryImpl
import com.wb.example.demo_05.ui.search.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<GitHubApi> { RetrofitClient.api }
    single<AppDatabase> { AppDatabase.build(get()) }
    single<CachedUserDao> { get<AppDatabase>().cachedUserDao() }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    viewModel { SearchViewModel(get()) }
}
