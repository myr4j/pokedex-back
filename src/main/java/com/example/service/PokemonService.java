package com.example.service;

import com.example.domain.Pokemon;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class PokemonService {

    @PersistenceContext
    private EntityManager em;

    public Pokemon createPokemon(Integer pokedexNumber, String name, Integer hp, Integer attack, Integer defense, Integer speed) {
        Pokemon pokemon = new Pokemon(pokedexNumber, name);
        pokemon.setHp(hp);
        pokemon.setAttack(attack);
        pokemon.setDefense(defense);
        pokemon.setSpeed(speed);
        em.persist(pokemon);
        return pokemon;
    }

    public Pokemon findPokemonById(Long id) {
        return em.find(Pokemon.class, id);
    }

    public List<Pokemon> findAllPokemons() {
        return em.createQuery("SELECT p FROM Pokemon p ORDER BY p.pokedexNumber", Pokemon.class)
                .getResultList();
    }

    public Pokemon updatePokemon(Long id, Integer pokedexNumber, String name, Integer hp, Integer attack, Integer defense, Integer speed) {
        Pokemon pokemon = findPokemonById(id);
        if (pokemon != null) {
            pokemon.setPokedexNumber(pokedexNumber);
            pokemon.setName(name);
            pokemon.setHp(hp);
            pokemon.setAttack(attack);
            pokemon.setDefense(defense);
            pokemon.setSpeed(speed);
            em.merge(pokemon);
        }
        return pokemon;
    }

    public void deletePokemon(Long id) {
        Pokemon pokemon = findPokemonById(id);
        if (pokemon != null) {
            em.remove(pokemon);
        }
    }
}
