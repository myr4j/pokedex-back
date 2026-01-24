package com.example.service;

import com.example.dto.AggregatedCaptureStats;
import com.example.dto.CaptureMessage;

import java.util.*;
import java.util.stream.Collectors;

//  regroupe plusieurs messages de capture en statistiques agregees
public class CaptureAggregator {
    
    private static final CaptureAggregator INSTANCE = new CaptureAggregator();
    
    private CaptureAggregator() {}
    
    public static CaptureAggregator getInstance() {
        return INSTANCE;
    }
    
    // agrege les captures par trainer : regroupe tous les messages de capture d'un trainer
    // et calcule le nombre total de captures et le nombre par pokemon
    public AggregatedCaptureStats aggregateByTrainer(Long trainerId, List<CaptureMessage> captures) {
        if (captures == null || captures.isEmpty()) {
            return null;
        }
        
        // filtrer les captures du trainer specifie
        List<CaptureMessage> trainerCaptures = captures.stream()
                .filter(c -> c.getTrainerId().equals(trainerId))
                .collect(Collectors.toList());
        
        if (trainerCaptures.isEmpty()) {
            return null;
        }
        
        // recuperer le nom du trainer depuis le premier message
        String trainerName = trainerCaptures.get(0).getTrainerName();
        
        // compter les captures par pokemon
        Map<Long, Map<String, Integer>> pokemonCounts = new HashMap<>();
        for (CaptureMessage capture : trainerCaptures) {
            Long pokemonId = capture.getPokemonId();
            String pokemonName = capture.getPokemonName();
            
            pokemonCounts.computeIfAbsent(pokemonId, k -> new HashMap<>())
                    .put(pokemonName, pokemonCounts.get(pokemonId).getOrDefault(pokemonName, 0) + 1);
        }
        
        // convertir en liste de PokemonCaptureCount
        List<AggregatedCaptureStats.PokemonCaptureCount> counts = pokemonCounts.entrySet().stream()
                .map(entry -> {
                    Long pokemonId = entry.getKey();
                    Map<String, Integer> nameCount = entry.getValue();
                    // prendre le premier nom (tous les noms devraient etre identiques pour le meme pokemon)
                    String pokemonName = nameCount.keySet().iterator().next();
                    Integer count = nameCount.values().stream().mapToInt(Integer::intValue).sum();
                    
                    return new AggregatedCaptureStats.PokemonCaptureCount(pokemonId, pokemonName, count);
                })
                .sorted((a, b) -> b.getCount().compareTo(a.getCount())) // trier par nombre de captures decroissant
                .collect(Collectors.toList());
        
        AggregatedCaptureStats stats = new AggregatedCaptureStats();
        stats.setTrainerId(trainerId);
        stats.setTrainerName(trainerName);
        stats.setTotalCaptures(trainerCaptures.size());
        stats.setPokemonCounts(counts);
        
        return stats;
    }
    
    // agrege les captures de tous les trainers : regroupe tous les messages et cree des stats par trainer
    public List<AggregatedCaptureStats> aggregateAllTrainers(List<CaptureMessage> captures) {
        if (captures == null || captures.isEmpty()) {
            return Collections.emptyList();
        }
        
        // grouper les captures par trainer id
        Map<Long, List<CaptureMessage>> capturesByTrainer = captures.stream()
                .collect(Collectors.groupingBy(CaptureMessage::getTrainerId));
        
        // agreger pour chaque trainer
        return capturesByTrainer.entrySet().stream()
                .map(entry -> aggregateByTrainer(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getTotalCaptures().compareTo(a.getTotalCaptures())) // trier par nombre total decroissant
                .collect(Collectors.toList());
    }
}
