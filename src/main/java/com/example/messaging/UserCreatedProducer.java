package com.example.messaging;

import com.example.domain.User;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.jms.*;

@Stateless
public class UserCreatedProducer {

    @Resource(lookup = "jms/__defaultConnectionFactory")
    private ConnectionFactory factory;

    @Resource(lookup = "jms/UserCreatedQueue")
    private Queue queue;

    public void sendUserCreatedEvent(User user) {
        try (JMSContext ctx = factory.createContext()) {
            String payload = "UserCreated:" + user.getId();
            ctx.createProducer().send(queue, payload);
            System.out.println("📤 Sent JMS message: " + payload);
        }
    }
}
