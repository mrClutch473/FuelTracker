package com.example.fueltracker_android.data

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.CopyOnWriteArrayList

/**
 * In-memory CookieJar — хранит ft_session cookie между запросами в рамках
 * одного запуска приложения. Разделяется единственным экземпляром OkHttpClient.
 */
class SessionCookieJar : CookieJar {

    private val store = CopyOnWriteArrayList<Cookie>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        // Заменяем cookies с тем же именем, добавляем новые
        store.removeAll { saved -> cookies.any { it.name == saved.name } }
        store.addAll(cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> = store.toList()

    /** Вызывается при выходе из аккаунта — очищает сессию. */
    fun clear() = store.clear()

    val isEmpty: Boolean get() = store.isEmpty()
}
