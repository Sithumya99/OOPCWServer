package com.sithumya20220865.OOPCW;

import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class MessageQueueService {

    private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>(100);

    public boolean enqueue(Message message) {
        return queue.offer(message);
    }

    public Message dequeue() throws InterruptedException {
        return queue.take();
    }
}
