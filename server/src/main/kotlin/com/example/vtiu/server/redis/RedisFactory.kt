package com.example.vtiu.server.redis

import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.net.URI

object RedisFactory {
    private var jedisPool: JedisPool? = null

    fun init() {
        println("--- REDIS INITIALIZATION START ---")
        
        val redisUrl = System.getenv("REDIS_URL") ?: System.getenv("REDIS_PUBLIC_URL")
        val redisHost = System.getenv("REDISHOST") ?: "localhost"
        val redisPort = System.getenv("REDISPORT")?.toIntOrNull() ?: 6379
        val redisPassword = System.getenv("REDISPASSWORD")

        val poolConfig = JedisPoolConfig().apply {
            maxTotal = 10
            maxIdle = 5
            minIdle = 1
            testOnBorrow = true
        }

        try {
            jedisPool = if (!redisUrl.isNullOrBlank()) {
                println("Redis: Using REDIS_URL")
                JedisPool(poolConfig, URI(redisUrl))
            } else {
                println("Redis: Connecting to $redisHost:$redisPort")
                JedisPool(poolConfig, redisHost, redisPort, 2000, redisPassword)
            }
            
            // Test Connection
            getJedis().use { 
                it.ping()
                println("Redis: Connection test (PING) successful.")
            }
            println("--- REDIS INITIALIZATION COMPLETE ---")
        } catch (e: Exception) {
            println("Redis: FATAL - Connection failed: ${e.message}")
            e.printStackTrace()
        }
    }

    fun getJedis() = jedisPool?.resource ?: throw IllegalStateException("Redis Pool not initialized")

    fun close() {
        jedisPool?.close()
    }
}
