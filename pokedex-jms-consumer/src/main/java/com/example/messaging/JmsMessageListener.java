package com.example.messaging;

import com.example.dto.CaptureMessage;
import com.example.dto.TrainerMessage;
import com.example.service.MessageLogService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import javax.jms.*;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


@Singleton
@Startup
public class JmsMessageListener {
    
    private static final Logger logger = Logger.getLogger(JmsMessageListener.class.getName());
    
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String CAPTURES_QUEUE = "captures";
    private static final String TRAINERS_QUEUE = "trainers";
    
    private Connection connection;
    private Session session;
    private MessageConsumer capturesConsumer;
    private MessageConsumer trainersConsumer;
    
    @PostConstruct
    public void init() {
        logger.info("Initializing jms message listener...");
        connect();
    }
    
    private void connect() {
        try {
            ConnectionFactory connectionFactory = new org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory(BROKER_URL);

            connection = connectionFactory.createConnection();
            connection.start();
            
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
   
            // queue pour les captures de pokemon
            Queue capturesQueue = session.createQueue(CAPTURES_QUEUE);
            capturesConsumer = session.createConsumer(capturesQueue);
            capturesConsumer.setMessageListener(message -> {
                try {
                    handleCaptureMessage(message);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "error processing capture message", e);
                }
            });
            
            // queue pour les trainers qui s'inscrivent
            Queue trainersQueue = session.createQueue(TRAINERS_QUEUE);
            trainersConsumer = session.createConsumer(trainersQueue);
            trainersConsumer.setMessageListener(message -> {
                try {
                    handleTrainerMessage(message);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "error processing trainer message", e);
                }
            });
            
            logger.info("Jms message listener initialized successfully. waiting for messages on queues: " + CAPTURES_QUEUE + " and " + TRAINERS_QUEUE);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to connect to artemis (" + BROKER_URL + "). application will continue without jms. Error: " + e.getMessage());
            logger.log(Level.SEVERE, "Jms connection error details", e);
        }
    }

    private void handleCaptureMessage(Message message) {
        try {
            if (message instanceof ObjectMessage) {
                ObjectMessage objectMessage = (ObjectMessage) message;
                Object obj = objectMessage.getObject();
                
                if (obj instanceof CaptureMessage) {
                    CaptureMessage captureMessage = (CaptureMessage) obj;
                    
                    // log le message de capture recu
                    logger.info(String.format(
                        "capture message received - trainer: %s (id: %d) caught %s (id: %d) on %s",
                        captureMessage.getTrainerName(),
                        captureMessage.getTrainerId(),
                        captureMessage.getPokemonName(),
                        captureMessage.getPokemonId(),
                        captureMessage.getCaptureDate()
                    ));
                    
                    MessageLogService.getInstance().addCaptureMessage(captureMessage);
                    
                } else {
                    logger.warning("capture message received but unexpected type: " + obj.getClass().getName());
                }
            } else if (message instanceof BytesMessage) {
                BytesMessage bytesMessage = (BytesMessage) message;
                byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                bytesMessage.readBytes(bytes);
                
                try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                    Object obj = ois.readObject();
                    if (obj instanceof CaptureMessage) {
                        CaptureMessage captureMessage = (CaptureMessage) obj;
                        
                        logger.info(String.format(
                            "Capture message received (bytes) - trainer: %s (id: %d) caught %s (id: %d)",
                            captureMessage.getTrainerName(),
                            captureMessage.getTrainerId(),
                            captureMessage.getPokemonName(),
                            captureMessage.getPokemonId()
                        ));
                        
                        MessageLogService.getInstance().addCaptureMessage(captureMessage);
                    }
                }
            } else {
                logger.warning("Unsupported message type: " + message.getClass().getName());
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing jms capture message", e);
        }
    }
    
    private void handleTrainerMessage(Message message) {
        try {
            if (message instanceof ObjectMessage) {
                ObjectMessage objectMessage = (ObjectMessage) message;
                Object obj = objectMessage.getObject();
                
                if (obj instanceof TrainerMessage) {
                    TrainerMessage trainerMessage = (TrainerMessage) obj;
                    
                    // log le message de creation de trainer
                    logger.info(String.format(
                        "Trainer created - %s (id: %d, email: %s) on %s",
                        trainerMessage.getTrainerName(),
                        trainerMessage.getTrainerId(),
                        trainerMessage.getTrainerEmail(),
                        trainerMessage.getRegistrationDate()
                    ));
                    
                    MessageLogService.getInstance().addTrainerMessage(trainerMessage);
                    
                } else {
                    logger.warning("Trainer message received but unexpected type: " + obj.getClass().getName());
                }
            } else if (message instanceof BytesMessage) {
                BytesMessage bytesMessage = (BytesMessage) message;
                byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                bytesMessage.readBytes(bytes);
                
                try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                    Object obj = ois.readObject();
                    if (obj instanceof TrainerMessage) {
                        TrainerMessage trainerMessage = (TrainerMessage) obj;
                        
                        logger.info(String.format(
                            "Trainer created (bytes) - %s (id: %d, email: %s)",
                            trainerMessage.getTrainerName(),
                            trainerMessage.getTrainerId(),
                            trainerMessage.getTrainerEmail()
                        ));
                        
                        MessageLogService.getInstance().addTrainerMessage(trainerMessage);
                    }
                }
            } else {
                logger.warning("Unsupported message type: " + message.getClass().getName());
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing jms trainer message", e);
        }
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            logger.info("Cleaning up jms message listener...");
            
            if (capturesConsumer != null) {
                capturesConsumer.close();
            }
            if (trainersConsumer != null) {
                trainersConsumer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
            
            logger.info("Jms message listener cleaned up successfully");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error cleaning up jms message listener", e);
        }
    }
}
