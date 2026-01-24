package com.example.messaging;

import com.example.dto.TrainerMessage;
import jakarta.ejb.Stateless;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class TrainerMessageProducer {
    
    private static final Logger logger = Logger.getLogger(TrainerMessageProducer.class.getName());
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "trainers";
    
    public void sendTrainerCreatedMessage(TrainerMessage message) {
        Connection connection = null;
        Session session = null;
        
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
            connection = connectionFactory.createConnection();
            connection.start();
            
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(QUEUE_NAME);
            MessageProducer producer = session.createProducer(queue);
            
            ObjectMessage objectMessage = session.createObjectMessage(message);
            producer.send(objectMessage);
            
            // message jms envoye avec succes
            logger.info(String.format(
                "jms message sent - trainer created: %s (id: %d, email: %s)",
                message.getTrainerName(),
                message.getTrainerId(),
                message.getTrainerEmail()
            ));
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "unable to send jms message for trainer: " + message.getTrainerName(), e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException e) {
                // erreur lors de la fermeture des ressources jms
                logger.log(Level.SEVERE, "error closing jms resources", e);
            }
        }
    }
}
