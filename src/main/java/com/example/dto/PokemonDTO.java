package com.example.dto;

import com.example.domain.Pokemon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PokemonDTO {
    private Long id;
    private Integer pokedexNumber;
    private String name;
    private Integer hp;
    private Integer attack;
    private Integer defense;
    private Integer speed;
    private List<String> types;

    public static PokemonDTO from(Pokemon pokemon) {
        if (pokemon == null) {
            return null;
        }
        
        PokemonDTO dto = new PokemonDTO();
        dto.setId(pokemon.getId());
        dto.setPokedexNumber(pokemon.getPokedexNumber());
        dto.setName(pokemon.getName());
        dto.setHp(pokemon.getHp());
        dto.setAttack(pokemon.getAttack());
        dto.setDefense(pokemon.getDefense());
        dto.setSpeed(pokemon.getSpeed());
        
        if (pokemon.getTypes() != null) {
            dto.setTypes(pokemon.getTypes().stream()
                    .map(type -> type.getName())
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
}
