package com.example.recipefinder.controller;

import com.example.recipefinder.service.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "*") 
public class RecipeController {

    private final RecipeService recipeService;

    
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/search")
    public ResponseEntity<String> search(@RequestParam String query) {
        try {
            String data = recipeService.searchRecipes(query);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getDetails(@PathVariable Long id) {
        try {
            String data = recipeService.getRecipeDetails(id);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }



@GetMapping
public ResponseEntity<String> getAll(@RequestParam(defaultValue = "10") int number) {
    try {
        String filteredData = recipeService.getAllRecipes(number);
        return ResponseEntity.ok(filteredData);
    } catch (Exception e) {
        return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
    }
}


}