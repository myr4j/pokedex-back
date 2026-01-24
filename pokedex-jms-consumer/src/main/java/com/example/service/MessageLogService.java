package com.example.service;

import com.example.dto.CaptureMessage;
import com.example.dto.TrainerMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// service pour stocker les messages jms en memoire (captures et trainers)
public class MessageLogService {
    
    private static final MessageLogService INSTANCE = new MessageLogService();
    private static final int MAX_MESSAGES = 100;
    
    // liste des messages de capture
    private final List<CaptureMessage> captureMessages;
    // liste des messages de creation de trainer
    private final List<TrainerMessage> trainerMessages;
    
    private MessageLogService() {
        this.captureMessages = Collections.synchronizedList(new ArrayList<>());
        this.trainerMessages = Collections.synchronizedList(new ArrayList<>());
    }
    
    public static MessageLogService getInstance() {
        return INSTANCE;
    }
    
    // ajoute un message de capture, supprime le plus ancien si depasse la limite
    public void addCaptureMessage(CaptureMessage message) {
        if (message == null) return;
        synchronized (captureMessages) {
            captureMessages.add(message);
            if (captureMessages.size() > MAX_MESSAGES) {
                captureMessages.remove(0);
            }
        }
    }
    
    // ajoute un message de trainer, supprime le plus ancien si depasse la limite
    public void addTrainerMessage(TrainerMessage message) {
        if (message == null) return;
        synchronized (trainerMessages) {
            trainerMessages.add(message);
            if (trainerMessages.size() > MAX_MESSAGES) {
                trainerMessages.remove(0);
            }
        }
    }
    
    public List<CaptureMessage> getCaptureMessages(int limit) {
        synchronized (captureMessages) {
            int size = captureMessages.size();
            int fromIndex = limit > 0 && limit < size ? size - limit : 0;
            List<CaptureMessage> result = new ArrayList<>(captureMessages.subList(fromIndex, size));
            Collections.reverse(result);
            return result;
        }
    }
    
    public List<TrainerMessage> getTrainerMessages(int limit) {
        synchronized (trainerMessages) {
            int size = trainerMessages.size();
            int fromIndex = limit > 0 && limit < size ? size - limit : 0;
            List<TrainerMessage> result = new ArrayList<>(trainerMessages.subList(fromIndex, size));
            Collections.reverse(result);
            return result;
        }
    }
    
    public List<CaptureMessage> getAllCaptureMessages() {
        return getCaptureMessages(0);
    }
    
    public List<TrainerMessage> getAllTrainerMessages() {
        return getTrainerMessages(0);
    }
    
    public long getTotalCaptureCount() {
        return captureMessages.size();
    }
    
    public long getTotalTrainerCount() {
        return trainerMessages.size();
    }
    
    public long getTotalCount() {
        return captureMessages.size() + trainerMessages.size();
    }
}
