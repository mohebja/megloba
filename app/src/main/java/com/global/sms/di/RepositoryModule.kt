package com.global.sms.di

import android.content.Context
import com.global.sms.data.dao.CategoryDao
import com.global.sms.data.dao.ClassificationRuleDao
import com.global.sms.data.dao.SettingsDao
import com.global.sms.data.repository.SettingsRepository
import com.global.sms.core.contact.ContactCacheManager
import com.global.sms.core.contact.ContactRepository
import com.global.sms.core.contact.ContactRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsDao: SettingsDao,
        categoryDao: CategoryDao,
        classificationRuleDao: ClassificationRuleDao
    ): SettingsRepository {
        return SettingsRepository(settingsDao, categoryDao, classificationRuleDao)
    }

    @Provides
    @Singleton
    fun provideContactCacheManager(): ContactCacheManager {
        return ContactCacheManager.getInstance()
    }

    @Provides
    @Singleton
    fun provideContactRepository(
        @ApplicationContext context: Context,
        cacheManager: ContactCacheManager
    ): ContactRepository {
        return ContactRepositoryImpl(context, cacheManager)
    }
}
