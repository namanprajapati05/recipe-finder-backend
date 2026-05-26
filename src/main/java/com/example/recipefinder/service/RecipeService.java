package com.example.recipefinder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class RecipeService {

    @Value("${spoonacular.api.key}")
    private String apiKey;

    @Value("${spoonacular.api.base-url}")
    private String baseUrl;

    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
 

    public String searchRecipes(String query) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/recipes/complexSearch")
                .queryParam("query", query)
                .queryParam("apiKey", apiKey)
                .queryParam("number", 10)
                .toUriString();

        try {
            //  JSON data
            String rawJson = restTemplate.getForObject(url, String.class);
            
            JsonNode rootNode = objectMapper.readTree(rawJson);
            JsonNode resultsNode = rootNode.path("results");

            ArrayNode filteredRecipes = objectMapper.createArrayNode();

            if (resultsNode.isArray()) {
                for (JsonNode recipe : resultsNode) {
                    ObjectNode cleanRecipe = objectMapper.createObjectNode();
                    cleanRecipe.put("id", recipe.path("id").asLong());
                    cleanRecipe.put("title", recipe.path("title").asText());
                    cleanRecipe.put("image", recipe.path("image").asText());
                    filteredRecipes.add(cleanRecipe);
                }
            }
            return objectMapper.writeValueAsString(filteredRecipes);

        } catch (Exception e) {
            return "{\"error\": \"Search failed: " + e.getMessage() + "\"}";
        }
    }

    // 2. Get AllRandom Recipes
    public String getAllRecipes(int number) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/recipes/random")
                .queryParam("apiKey", apiKey)
                .queryParam("number", number)
                .toUriString();

        try {
            // RestTemplate 
            String rawJson = restTemplate.getForObject(url, String.class);

            JsonNode rootNode = objectMapper.readTree(rawJson);
            JsonNode recipesNode = rootNode.path("recipes");

            ArrayNode filteredRecipes = objectMapper.createArrayNode();

            if (recipesNode.isArray()) {
                for (JsonNode recipe : recipesNode) {
                    ObjectNode cleanRecipe = objectMapper.createObjectNode();
                    
                    cleanRecipe.put("id", recipe.path("id").asLong());
                    cleanRecipe.put("title", recipe.path("title").asText());
                    cleanRecipe.put("image", recipe.path("image").asText());
                    cleanRecipe.put("recipeLink", recipe.path("sourceUrl").asText());
                    
                    // Summary se HTML tags clear karne ke liye
                    String rawSummary = recipe.path("summary").asText("");
                    String cleanSummary = rawSummary.replaceAll("<[^>]*>", "");
                    cleanRecipe.put("description", cleanSummary);

                    filteredRecipes.add(cleanRecipe);
                }
            }
            return objectMapper.writeValueAsString(filteredRecipes);

        } catch (Exception e) {
            return "{\"error\": \"JSON Parsing failed: " + e.getMessage() + "\"}";
        }
    }

    // 3. Get Detaile Recepe info
    public String getRecipeDetails(Long id) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/recipes/{id}/information")
                .queryParam("apiKey", apiKey)
                .buildAndExpand(id)
                .toUriString();

        try {
            String rawJson = restTemplate.getForObject(url, String.class);
            
            JsonNode root = objectMapper.readTree(rawJson);
            ObjectNode detailRecipe = objectMapper.createObjectNode();

            // Basic Details
            detailRecipe.put("id", root.path("id").asLong());
            detailRecipe.put("title", root.path("title").asText());
            detailRecipe.put("image", root.path("image").asText());
            detailRecipe.put("readyInMinutes", root.path("readyInMinutes").asInt());
            detailRecipe.put("servings", root.path("servings").asInt());
            
            // Instructions clean ki
            String rawInstructions = root.path("instructions").asText("");
            String cleanInstructions = rawInstructions.replaceAll("<[^>]*>", ""); 
            detailRecipe.put("instructions", cleanInstructions);

            // Ingredients filter kiye
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
            return "{\"error\": \"Failed to get details: " + e.getMessage() + "\"}";
        }
    }
}