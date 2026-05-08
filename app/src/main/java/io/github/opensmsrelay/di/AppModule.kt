package io.github.opensmsrelay.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.opensmsrelay.core.security.SecureStorage
import javax.inject.Named
import javax.inject.Singleton

private val Context.emailDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "email_settings")

private val Context.smsDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "sms_settings")

private val Context.appDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "app_settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("email")
    fun provideEmailDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.emailDataStore

    @Provides
    @Singleton
    @Named("sms")
    fun provideSmsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.smsDataStore

    @Provides
    @Singleton
    @Named("app")
    fun provideAppDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.appDataStore

    @Provides
    @Singleton
    fun provideSecureStorage(@ApplicationContext context: Context): SecureStorage =
        SecureStorage(context)
}
