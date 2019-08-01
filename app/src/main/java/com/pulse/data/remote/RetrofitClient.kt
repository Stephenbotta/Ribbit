package com.pulse.data.remote

import com.google.gson.*
import com.pulse.BuildConfig
import com.pulse.data.local.UserManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL_CLIENT = "http://52.35.234.66:8001/"
    //    private const val BASE_URL_DEV = "http://52.35.234.66:8000/"
    private const val BASE_URL_LOCAL = "http://192.168.102.33:8000/"
    private const val BASE_URL_DEV = "http://35.161.59.237:8000/"
    private const val BASE_URL = BASE_URL_DEV

    private val LOGGING_INTERCEPTOR by lazy {
        HttpLoggingInterceptor().setLevel(if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.NONE
        } else {
            HttpLoggingInterceptor.Level.NONE
        })
    }

    private val OK_HTTP_CLIENT by lazy {
        OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(LOGGING_INTERCEPTOR)
                .addInterceptor(AuthorizationInterceptor())
                .build()
    }

    private val RETROFIT by lazy {
        Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(GSON))
                .client(OK_HTTP_CLIENT)
                .build()
    }

    val GSON: Gson by lazy {
        GsonBuilder()
                .registerTypeAdapter(ZonedDateTime::class.java, DateDeserializer())
                .create()
    }

    val conversifyApi: ConversifyApi by lazy { RETROFIT.create(ConversifyApi::class.java) }

    /**
     * Parse all received date to zoned date time in local time zone
     *
     * All the date received from the api will be in UTC milliseconds
     * */
    private class DateDeserializer : JsonDeserializer<ZonedDateTime?> {
        override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): ZonedDateTime? {
            return try {
                Instant.ofEpochMilli(json.asLong)
                        .atZone(ZoneId.systemDefault())
            } catch (exception: Exception) {
                Timber.w(exception)
                null
            }
        }
    }

    private class AuthorizationInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            val authorization = UserManager.getAuthorization()

            return if (authorization != null) {
                Timber.i("Adding authorization in header")
                val authRequest = originalRequest.newBuilder()
                        .header("authorization", authorization)
                        .build()
                chain.proceed(authRequest)
            } else {
                Timber.i("User authorization does not exist")
                chain.proceed(originalRequest)
            }
        }
    }
}