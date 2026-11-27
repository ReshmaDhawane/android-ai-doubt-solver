package com.reshma.aidoubtsolver.di

import android.content.Context
import androidx.room.Room
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.reshma.aidoubtsolver.BuildConfig
import com.reshma.aidoubtsolver.data.local.AppDatabase
import com.reshma.aidoubtsolver.data.local.MessageDao
import com.reshma.aidoubtsolver.data.remote.AiApi
import com.reshma.aidoubtsolver.data.remote.AiDataSource
import com.reshma.aidoubtsolver.data.remote.MockAiDataSource
import com.reshma.aidoubtsolver.data.remote.RemoteAiDataSource
import com.reshma.aidoubtsolver.data.repository.ChatRepositoryImpl
import com.reshma.aidoubtsolver.domain.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun database(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "doubts.db").build()

    @Provides
    fun messageDao(db: AppDatabase): MessageDao = db.messageDao()

    @Provides @Singleton
    fun aiDataSource(): AiDataSource {
        if (BuildConfig.AI_API_KEY.isBlank()) return MockAiDataSource()

        val client = OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer ${BuildConfig.AI_API_KEY}")
                        .build(),
                )
            }
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }
            .build()

        val json = Json { ignoreUnknownKeys = true }
        val api = Retrofit.Builder()
            .baseUrl(BuildConfig.AI_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AiApi::class.java)

        return RemoteAiDataSource(api, BuildConfig.AI_MODEL)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun chatRepository(impl: ChatRepositoryImpl): ChatRepository
}
