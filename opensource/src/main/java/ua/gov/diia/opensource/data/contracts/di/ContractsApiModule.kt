package ua.gov.diia.opensource.data.contracts.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ua.gov.diia.core.di.data_source.http.UnauthorizedClient
import ua.gov.diia.opensource.BuildConfig
import ua.gov.diia.opensource.data.contracts.api.ContractsApi
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class ContractsApiClient

@Module
@InstallIn(SingletonComponent::class)
object ContractsApiModule {

    @Provides
    @Singleton
    @ContractsApiClient
    fun provideContractsRetrofit(
        moshi: Moshi,
        @UnauthorizedClient okHttpClient: OkHttpClient
    ): Retrofit {
        val rawBaseUrl = if (BuildConfig.CONTRACTS_BASE_URL.isNotBlank()) {
            BuildConfig.CONTRACTS_BASE_URL
        } else {
            BuildConfig.SERVER_URL
        }
        val baseUrl = if (rawBaseUrl.endsWith("/")) rawBaseUrl else "$rawBaseUrl/"
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideContractsApi(
        @ContractsApiClient retrofit: Retrofit
    ): ContractsApi = retrofit.create(ContractsApi::class.java)
}
