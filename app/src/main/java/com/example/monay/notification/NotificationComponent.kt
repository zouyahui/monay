package com.example.monay.notification

import android.content.Context
import com.example.monay.repository.BillRepository
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * 通知服务的依赖注入组件
 * 用于在非Hilt管理的组件中获取依赖
 */
@Singleton
@Component(modules = [NotificationModule::class])
interface NotificationComponent {
    
    fun inject(listener: MyNotificationListener)
    
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun applicationContext(context: Context): Builder
        
        fun build(): NotificationComponent
    }
    
    companion object {
        fun create(context: Context): NotificationComponent {
            return DaggerNotificationComponent.builder()
                .applicationContext(context)
                .build()
        }
    }
}

/**
 * 通知模块，提供通知相关依赖
 * 注意：这个模块不使用 @InstallIn，因为它是为 NotificationComponent 专门设计的，
 * 而不是为 Hilt 的全局依赖图设计的
 */
@Module
class NotificationModule {
    
    @Provides
    @Singleton
    fun provideTransactionParser(billRepository: BillRepository): TransactionParser {
        return TransactionParser(billRepository)
    }
    
    @Provides
    @Singleton
    fun provideBillRepository(context: Context): BillRepository {
        // 使用 AppDatabase 获取 BillDao 并创建 BillRepository
        val database = com.example.monay.data.AppDatabase.getDatabase(context)
        return BillRepository(database.billDao())
    }
} 