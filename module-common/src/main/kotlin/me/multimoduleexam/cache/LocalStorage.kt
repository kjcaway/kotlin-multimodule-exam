package me.multimoduleexam.cache

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LocalStorage<K, V>(
    // 만료 시간을 초 단위로 설정 가능 (기본값: 5분)
    private val expirationTimeSeconds: Long = 300
) {

    private val storage = ConcurrentHashMap<K, StorageItem<V>>()
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    init {
        // 매 1분마다 만료된 아이템 정리
        scheduler.scheduleAtFixedRate(this::cleanExpiredItems, 1, 1, TimeUnit.MINUTES)
    }

    fun put(key: K, value: V) {
        storage[key] = StorageItem(value, Instant.now().plusSeconds(expirationTimeSeconds))
    }

    fun get(key: K): V? {
        val item = storage[key] ?: return null
        return if (item.isExpired()) {
            storage.remove(key)
            null
        } else {
            item.value
        }
    }

    fun remove(key: K) {
        storage.remove(key)
    }

    fun clear() {
        storage.clear()
    }

    private fun cleanExpiredItems() {
        storage.keys.forEach { key ->
            storage[key]?.let {
                if (it.isExpired()) {
                    storage.remove(key)
                }
            }
        }
    }

    private data class StorageItem<V>(
        val value: V,
        val expiresAt: Instant
    ) {
        fun isExpired() = Instant.now().isAfter(expiresAt)
    }

    fun shutdown() {
        scheduler.shutdown()
    }
}