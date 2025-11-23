package ua.gov.diia.analytics.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ua.gov.diia.analytics.DiiaAnalytics
import ua.gov.diia.analytics.DiiaAnalyticsImpl
import ua.gov.diia.analytics.crashlytics.NoOpCrashlytics
import ua.gov.diia.analytics.crashlytics.WithCrashlyticsImpl
import ua.gov.diia.core.util.delegation.WithCrashlytics
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideDiiaAnalytics(
        @ApplicationContext context: Context
    ): DiiaAnalytics = DiiaAnalyticsImpl(context)

    @Provides
    @Singleton
    fun provideCrashlytics(
        @ApplicationContext context: Context
    ): WithCrashlytics {
        val resources = context.resources
        fun String.fromResources(): String {
            val id = resources.getIdentifier(this, "string", context.packageName)
            return if (id != 0) resources.getString(id) else ""
        }
        val appId = "google_app_id".fromResources()
        val apiKey = "google_api_key".fromResources()
        val isPlaceholder = appId.contains("placeholder", ignoreCase = true) ||
            apiKey.equals("DUMMY_KEY", ignoreCase = true) ||
            appId.isBlank() || apiKey.isBlank()
        return if (isPlaceholder) NoOpCrashlytics() else WithCrashlyticsImpl()
    }
}
