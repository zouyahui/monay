package com.example.monay.di

import android.content.Context
import androidx.room.Room
import com.example.monay.data.AppDatabase
import com.example.monay.data.BillDao
import com.example.monay.notification.NotificationServiceManager
import com.example.monay.repository.BillRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase =
        Room.databaseBuilder(appContext, AppDatabase::class.java, "app_database").build()

    @Provides
    @Singleton
    fun provideBillDao(db: AppDatabase): BillDao = db.billDao()

    @Provides
    @Singleton
    fun provideBillRepository(billDao: BillDao): BillRepository = BillRepository(billDao)
    
    @Provides
    @Singleton
    fun provideNotificationServiceManager(@ApplicationContext appContext: Context): NotificationServiceManager =
        NotificationServiceManager(appContext)
}