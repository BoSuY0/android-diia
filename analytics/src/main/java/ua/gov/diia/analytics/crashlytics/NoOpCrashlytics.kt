package ua.gov.diia.analytics.crashlytics

import ua.gov.diia.core.util.delegation.WithCrashlytics
import javax.inject.Inject

internal class NoOpCrashlytics @Inject constructor() : WithCrashlytics {
    override fun sendNonFatalError(e: Throwable) = Unit
    override fun sendMarkedErr(msg: String) = Unit
}
