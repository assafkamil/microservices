package com.sample.logprocessor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LogService {
    @SqsListener("wqw-log-queue")
    public void handle(String msg) {

    }

    @Autowired
    private AmazonSQS amazonSQS;

    @Async
    public void processLog() {
        while(true) {
            ReceiveMessageResult messageResult = amazonSQS.receiveMessage("qw-process-logs");
            for (Message msg : messageResult.getMessages()) {
                System.out.println(msg.getBody());
                amazonSQS.deleteMessage("qw-process-logs", msg.getReceiptHandle());
            }
        }
    }
}
