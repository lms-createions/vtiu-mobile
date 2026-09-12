package com.example.vtiu.server.redis

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object RedisQueue {
    private const val VIDEO_PROCESSING_QUEUE = "vtiu_video_tasks"

    fun pushTask(taskType: String, metadata: Map<String, String>) {
        val task = mapOf("type" to taskType) + metadata
        val taskJson = Json.encodeToString(task)
        
        RedisFactory.getJedis().use { 
            it.lpush(VIDEO_PROCESSING_QUEUE, taskJson)
            println("Redis: Task $taskType pushed to queue.")
        }
    }

    // In a real scenario, you'd have a separate worker process 
    // or a long-running coroutine that pops tasks and processes them.
}
