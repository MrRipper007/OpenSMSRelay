package io.github.opensmsrelay.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// EmailForwarder and SmsForwarder are provided automatically via @Inject constructor + @Singleton.
// No explicit @Provides needed.
@Module
@InstallIn(SingletonComponent::class)
object ForwardingModule
