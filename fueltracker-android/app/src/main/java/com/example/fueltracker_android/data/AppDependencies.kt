package com.example.fueltracker_android.data

/**
 * Единственная точка доступа к зависимостям data-слоя.
 *
 * Все фрагменты используют AppDependencies.repository вместо того,
 * чтобы создавать новые экземпляры ApiService/FuelRepository самостоятельно.
 * Это гарантирует, что у всех запросов один и тот же OkHttpClient →
 * один SessionCookieJar → сессионный cookie ft_session живёт на протяжении
 * всей сессии приложения.
 */
object AppDependencies {
    val cookieJar: SessionCookieJar = SessionCookieJar()

    val apiService: ApiService by lazy {
        ApiService.create(cookieJar)
    }

    val repository: FuelRepository by lazy {
        FuelRepository(apiService)
    }
}
