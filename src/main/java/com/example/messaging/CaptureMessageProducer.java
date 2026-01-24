package com.example.messaging;

import com.example.dto.CaptureMessage;
import jakarta.ejb.Stateless;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class CaptureMessageProducer {
    
    private static final Logger logger = Logger.getLogger(CaptureMessageProducer.class.getName());
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "captures";
    
    public void sendCaptureMessage(CaptureMessage message) {
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
                "jms message sent - capture: trainer %s (id: %d) caught %s (id: %d)",
                message.getTrainerName(),
                message.getTrainerId(),
                message.getPokemonName(),
                message.getPokemonId()
            ));
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "unable to send jms message for capture: " + message.getTrainerName() + " -> " + message.getPokemonName(), e);
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
