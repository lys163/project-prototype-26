package com.picturebook.book.dto;

import com.picturebook.book.entity.BookCharacter;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CharacterResponse {

    private String name;
    private String description;

    public static CharacterResponse from(BookCharacter character) {
        return CharacterResponse.builder()
                .name(character.getName())
                .description(character.getDescription())
                .build();
    }
}
