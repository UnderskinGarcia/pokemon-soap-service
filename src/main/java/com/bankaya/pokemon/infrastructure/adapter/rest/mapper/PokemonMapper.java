package com.bankaya.pokemon.infrastructure.adapter.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.bankaya.pokemon.domain.model.Pokemon;
import com.bankaya.pokemon.infrastructure.adapter.rest.dto.PokemonApiResponse;

/**
 * MapStruct Mapper for Pokemon
 * Converts between PokeAPI DTOs and Domain Models
 */
@Mapper
public interface PokemonMapper {

    PokemonMapper INSTANCE = Mappers.getMapper(PokemonMapper.class);

    @Mapping(target = "abilities", source = "abilities")
    @Mapping(target = "heldItems", source = "heldItems")
    Pokemon toDomain(PokemonApiResponse response);

    @Mapping(target = "name", source = "ability.name")
    @Mapping(target = "url", source = "ability.url")
    @Mapping(target = "isHidden", source = "isHidden")
    @Mapping(target = "slot", source = "slot")
    Pokemon.Ability toAbility(PokemonApiResponse.AbilitySlot abilitySlot);

    @Mapping(target = "name", source = "item.name")
    @Mapping(target = "url", source = "item.url")
    Pokemon.HeldItem toHeldItem(PokemonApiResponse.HeldItemSlot heldItemSlot);
}
