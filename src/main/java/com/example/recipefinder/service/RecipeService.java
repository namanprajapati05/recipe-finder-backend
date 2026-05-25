package com.example.recipefinder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class RecipeService {

    @Value("${spoonacular.api.key}")
    private String apiKey;

    @Value("${spoonacular.api.base-url}")
    private String baseUrl;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON parsing ke liye

    // Purane search aur details wale methods waise hi rahenge...




    public String searchRecipes(String query) {

    String url = UriComponentsBuilder.fromUriString(baseUrl)
            .path("/recipes/complexSearch")
            .queryParam("query", query)
            .queryParam("apiKey", apiKey)
            .toUriString();

    return restClient.get()
            .uri(url)
            .retrieve()
            .body(String.class);
}


    // 3. Get All/Random Recipes (Filtered for Frontend)
    public String getAllRecipes(int number) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/recipes/random")
                .queryParam("apiKey", apiKey)
                .queryParam("number", number)
                .toUriString();

        // 1. Raw Response fetch kiya Spoonacular se
        String rawJson = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        try {
            // 2. Raw JSON ko Tree structure me convert kiya
            JsonNode rootNode = objectMapper.readTree(rawJson);
            JsonNode recipesNode = rootNode.path("recipes");

            // 3. Frontend ke liye ek naya clean Array banaya
            ArrayNode filteredRecipes = objectMapper.createArrayNode();

            if (recipesNode.isArray()) {
                for (JsonNode recipe : recipesNode) {
                    ObjectNode cleanRecipe = objectMapper.createObjectNode();
                    
                    // Sirf wahi fields nikali jo aapko chahiye
                    cleanRecipe.put("title", recipe.path("title").asText());
                    cleanRecipe.put("image", recipe.path("image").asText());
                    cleanRecipe.put("recipeLink", recipe.path("sourceUrl").asText());
                    cleanRecipe.put("description", recipe.path("summary").asText()); // summary me description hota he

                    filteredRecipes.add(cleanRecipe);
                }
            }

            // 4. Filtered JSON string return kar di
            return objectMapper.writeValueAsString(filteredRecipes);

        } catch (Exception e) {
            return "{\"error\": \"JSON Parsing failed: " + e.getMessage() + "\"}";
        }
    }





    // 2. Get Detailed Recipe Information by ID (Filtered for Frontend)
public String getRecipeDetails(Long id) {
    String url = UriComponentsBuilder.fromUriString(baseUrl)
            .path("/recipes/{id}/information")
            .queryParam("apiKey", apiKey)
            .buildAndExpand(id)
            .toUriString();

    // Spoonacular se raw data fetched kiya
    String rawJson = restClient.get()
            .uri(url)
            .retrieve()
            .body(String.class);

    try {
        JsonNode root = objectMapper.readTree(rawJson);
        ObjectNode detailRecipe = objectMapper.createObjectNode();

        // Basic Info
        detailRecipe.put("id", root.path("id").asLong());
        detailRecipe.put("title", root.path("title").asText());
        detailRecipe.put("image", root.path("image").asText());
        detailRecipe.put("readyInMinutes", root.path("readyInMinutes").asInt());
        detailRecipe.put("servings", root.path("servings").asInt());
        
        // Detailed Instructions (HTML tags clean karke)
        String rawInstructions = root.path("instructions").asText("");
        String cleanInstructions = rawInstructions.replaceAll("<[^>]*>", ""); 
        detailRecipe.put("instructions", cleanInstructions);

        // Ingredients Array nikalna aur filter karna
        ArrayNode ingredientsArray = objectMapper.createArrayNode();
        JsonNode extendedIngredients = root.path("extendedIngredients");
        
        if (extendedIngredients.isArray()) {
            for (JsonNode ing : extendedIngredients) {
                ObjectNode cleanIng = objectMapper.createObjectNode();
                cleanIng.put("name", ing.path("name").asText());
                cleanIng.put("amount", ing.path("amount").asDouble());
                cleanIng.put("unit", ing.path("unit").asText());
                ingredientsArray.add(cleanIng);
            }
        }
        detailRecipe.set("ingredients", ingredientsArray);

        return objectMapper.writeValueAsString(detailRecipe);

    } catch (Exception e) {
        return "{\"error\": \"JSON Parsing failed: \"}";
    }
}


}