package com.paybridge.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.paybridge.data.local.dao.ClaimDao
import com.paybridge.data.local.dao.IncomingNotificationDao
import com.paybridge.data.local.dao.ListenerHeartbeatDao
import com.paybridge.data.local.dao.UnparsedNotificationDao
import com.paybridge.data.local.entity.ClaimEntity
import com.paybridge.data.local.entity.IncomingNotificationEntity
import com.paybridge.data.local.entity.ListenerHeartbeatEntity
import com.paybridge.data.local.entity.UnparsedNotificationEntity

/**
 * This database holds a real log of the shopkeeper's payment activity. It is sensitive even
 * though it never leaves the device — see AndroidManifest's allowBackup="false", which keeps it
 * out of Android's automatic cloud backup.
 */
@Database(
    entities = [
        ClaimEntity::class,
        IncomingNotificationEntity::class,
        UnparsedNotificationEntity::class,
        ListenerHeartbeatEntity::class,
    ],
    version = 1,
    // No migrations exist yet at v1 — exported schema history would be unused ceremony until
    // a version 2 actually needs one.
    exportSchema = false,
)
abstract class PayBridgeDatabase : RoomDatabase() {
    abstract fun claimDao(): ClaimDao
    abstract fun incomingNotificationDao(): IncomingNotificationDao
    abstract fun unparsedNotificationDao(): UnparsedNotificationDao
    abstract fun listenerHeartbeatDao(): ListenerHeartbeatDao

    companion object {
        @Volatile private var instance: PayBridgeDatabase? = null

        fun getInstance(context: Context): PayBridgeDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PayBridgeDatabase::class.java,
                    "paybridge.db",
                ).build().also { instance = it }
            }
    }
}
