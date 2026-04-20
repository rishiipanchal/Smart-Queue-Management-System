package com.rishi.taskqueue.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisQueueService {

    private final StringRedisTemplate redisTemplate;
    private static final String QUEUE_NAME = "task_queue";

    public RedisQueueService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void pushTask(String taskJson) {
        redisTemplate.opsForList().rightPush(QUEUE_NAME, taskJson);
        System.out.println("Pushed to queue. Size: " + getQueueSize());
    }

    public String popTask() {
        String val = redisTemplate.opsForList().leftPop(QUEUE_NAME);
        System.out.println("Popped from queue. Size: " + getQueueSize());
        return val;
    }
    
    public Long getQueueSize() {
        return redisTemplate.opsForList().size(QUEUE_NAME);
    }
}