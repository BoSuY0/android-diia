package ua.gov.diia.opensource.data.contracts.storage

import java.util.UUID
import ua.gov.diia.diia_storage.DiiaStorage
import ua.gov.diia.diia_storage.model.PreferenceKey
import ua.gov.diia.diia_storage.store.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides стабільний client_id для контрактів.
 * Зберігається у сховищі, щоб не змінювався між запусками.
 */
@Singleton
class ClientIdStorage @Inject constructor(
    private val diiaStorage: DiiaStorage
) {

    fun get(): String {
        val current = diiaStorage.getString(CLIENT_ID_KEY, "")
        if (current.isNotBlank()) return current
        val generated = UUID.randomUUID().toString()
        diiaStorage.set(CLIENT_ID_KEY, generated)
        return generated
    }

    private object ClientIdKey : PreferenceKey(
        "contracts_client_id",
        Preferences.Scopes.USER_SCOPE,
        String::class.java
    )

    private val CLIENT_ID_KEY: PreferenceKey = ClientIdKey
}
