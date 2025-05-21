package com.nelumbo.zoo_api.dto.errors;

public record ResponseMessages() {
    public static final String NO_SPECIES = "No hay especies registradas";
    public static final String NO_ANIMALS = "No hay animales registrados";
    public static final String NO_ANIMALS_DATE = "No hay animales registrados en la fecha especificada";
    public static final String NO_ANIMALS_ZONE = "No hay animales registrados en ninguna zona";
    public static final String NO_Reply_COMMENTS = "No hay respuestas a comentarios para analizar";
    public static final String NO_ANIMALS_SPECIES = "No hay animales registrados para ninguna especie";
    public static final String NO_RESULTS_SEARCH = "No se encontraron resultados para la búsqueda: ";
    public static final String NO_COMMENTS = "No hay comentarios";
    public static final String NO_COMMENTS_FOR_ANIMAL = "No hay comentarios para este animal";
}