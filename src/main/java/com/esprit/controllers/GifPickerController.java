package com.esprit.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class GifPickerController {

    @FXML
    private VBox gifPickerPane;

    @FXML
    private TextField gifSearchField;

    @FXML
    private FlowPane gifFlowPane;

    @FXML
    private ScrollPane gifScrollPane;

    @FXML
    private Button gifCloseButton;

    @FXML
    private ProgressIndicator loadingIndicator;

    private final String GIPHY_API_KEY = com.esprit.config.Secrets.get("GIPHY_API_KEY");
    
    // Cache for storing already loaded GIF data
    private static final Map<String, List<GifItem>> GIF_CACHE = new ConcurrentHashMap<>();
    
    // Constants for category searches
    private static final String CATEGORY_TRENDING = "trending";
    private static final String CATEGORY_EMOJIS = "emojis";
    private static final String CATEGORY_REACTIONS = "reactions";
    private static final String CATEGORY_FUNNY = "funny";
    private static final String CATEGORY_LOVE = "love";
    private static final String CATEGORY_ANIMALS = "animals";
    
    // Cache for storing already loaded GIF images
    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();
    
    // Last search query to avoid redundant API calls
    private String lastSearchQuery = "";
    
    // Flag to track if trending GIFs are already loaded
    private boolean trendingGifsLoaded = false;

    private final ObservableList<GifItem> gifItems = FXCollections.observableArrayList();

    public interface GifSelectListener {
        void onGifSelected(String gifUrl);
    }

    private GifSelectListener listener;

    public void setGifSelectListener(GifSelectListener listener) {
        this.listener = listener;
    }

    @FXML
    private void initialize() {
        gifCloseButton.setOnAction(this::handleClose);

        // Add delay to search to prevent too many API calls while typing
        gifSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                if (!trendingGifsLoaded) {
                    fetchTrendingGifs();
                }
            } else {
                // Avoid redundant API calls for the same search term
                if (!newVal.equals(lastSearchQuery)) {
                    lastSearchQuery = newVal;
                    // Use a delayed search task to prevent excessive API calls while typing
                    if (searchTask != null && searchTask.isRunning()) {
                        searchTask.cancel();
                    }
                    searchTask = createSearchTask(newVal);
                    new Thread(searchTask).start();
                }
            }
        });
    }
    
    /**
     * Handle click on Trending category button
     */
    @FXML
    private void handleTrendingCategory(ActionEvent event) {
        fetchCategoryGifs(CATEGORY_TRENDING);
    }
    
    /**
     * Handle click on Emojis category button
     */
    @FXML
    private void handleEmojisCategory(ActionEvent event) {
        fetchCategoryGifs(CATEGORY_EMOJIS);
    }
    
    /**
     * Handle click on Reactions category button
     */
    @FXML
    private void handleReactionsCategory(ActionEvent event) {
        fetchCategoryGifs(CATEGORY_REACTIONS);
    }
    
    /**
     * Handle click on Funny category button
     */
    @FXML
    private void handleFunnyCategory(ActionEvent event) {
        fetchCategoryGifs(CATEGORY_FUNNY);
    }
    
    /**
     * Handle click on Love category button
     */
    @FXML
    private void handleLoveCategory(ActionEvent event) {
        fetchCategoryGifs(CATEGORY_LOVE);
    }
    
    /**
     * Handle click on Animals category button
     */
    @FXML
    private void handleAnimalsCategory(ActionEvent event) {
        fetchCategoryGifs(CATEGORY_ANIMALS);
    }
    
    /**
     * Fetch GIFs for a specific category
     */
    private void fetchCategoryGifs(String category) {
        // Clear search field when category is selected
        gifSearchField.clear();
        lastSearchQuery = "";
        
        // Check if category GIFs are already in cache
        if (GIF_CACHE.containsKey(category)) {
            populateGifPicker(GIF_CACHE.get(category));
            return;
        }
        
        // Show loading indicator
        loadingIndicator.setVisible(true);
        
        Task<List<GifItem>> task = new Task<>() {
            @Override
            protected List<GifItem> call() {
                String url;
                if (category.equals(CATEGORY_TRENDING)) {
                    url = "https://api.giphy.com/v1/gifs/trending?api_key=" + GIPHY_API_KEY + "&limit=30";
                } else if (category.equals(CATEGORY_EMOJIS)) {
                    // Use stickers endpoint for emojis for better results
                    url = "https://api.giphy.com/v1/stickers/search?api_key=" + GIPHY_API_KEY + "&q=emoji&limit=30";
                } else {
                    url = "https://api.giphy.com/v1/gifs/search?api_key=" + GIPHY_API_KEY + "&q=" + category + "&limit=30";
                }
                return fetchGifsFromGiphy(url);
            }
        };

        task.setOnSucceeded(e -> {
            List<GifItem> gifs = task.getValue();
            // Store in cache
            GIF_CACHE.put(category, gifs);
            populateGifPicker(gifs);
            loadingIndicator.setVisible(false);
            if (category.equals(CATEGORY_TRENDING)) {
                trendingGifsLoaded = true;
            }
        });
        
        task.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            e.getSource().getException().printStackTrace();
        });

        new Thread(task).start();
    }
    
    // Task for delayed search to prevent excessive API calls while typing
    private Task<Void> searchTask;
    
    private Task<Void> createSearchTask(String query) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Add a small delay to wait for user to finish typing
                    Thread.sleep(300);
                    if (!isCancelled()) {
                        Platform.runLater(() -> searchGifs(query));
                    }
                } catch (InterruptedException e) {
                    // Task was cancelled
                }
                return null;
            }
        };
        return task;
    }

    void fetchTrendingGifs() {
        fetchCategoryGifs(CATEGORY_TRENDING);
    }

    void searchGifs(String query) {
        // Check if search results are already in cache
        if (GIF_CACHE.containsKey(query)) {
            populateGifPicker(GIF_CACHE.get(query));
            return;
        }
        
        Task<List<GifItem>> task = new Task<>() {
            @Override
            protected List<GifItem> call() {
                String url = "https://api.giphy.com/v1/gifs/search?api_key=" + GIPHY_API_KEY + "&q=" + query + "&limit=25";
                return fetchGifsFromGiphy(url);
            }
        };

        task.setOnRunning(e -> loadingIndicator.setVisible(true));
        task.setOnSucceeded(e -> {
            List<GifItem> gifs = task.getValue();
            // Store in cache
            GIF_CACHE.put(query, gifs);
            populateGifPicker(gifs);
            loadingIndicator.setVisible(false);
        });
        task.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            e.getSource().getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private List<GifItem> fetchGifsFromGiphy(String urlString) {
        List<GifItem> gifs = new ArrayList<>();
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); // Set timeout to 5 seconds
            conn.setReadTimeout(5000);    // Set read timeout to 5 seconds
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Error fetching GIFs: HTTP response code " + responseCode);
                return gifs;
            }

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            JsonArray data = json.getAsJsonArray("data");

            for (int i = 0; i < data.size(); i++) {
                JsonObject gifObj = data.get(i).getAsJsonObject();
                JsonObject images = gifObj.getAsJsonObject("images");
                
                // Check if this is a sticker/emoji (smaller size is better)
                boolean isSticker = urlString.contains("stickers");
                
                JsonObject preview;
                if (isSticker) {
                    // For stickers/emojis, use fixed_width_small for better display
                    preview = images.getAsJsonObject("fixed_width_small");
                    if (preview == null) {
                        preview = images.getAsJsonObject("fixed_height_small");
                    }
                } else {
                    // For regular GIFs, use preview_gif for better performance
                    preview = images.getAsJsonObject("preview_gif");
                    if (preview == null) {
                        preview = images.getAsJsonObject("fixed_height_small");
                    }
                }
                
                String urlGif = preview.get("url").getAsString();
                gifs.add(new GifItem(urlGif));
            }
        } catch (Exception e) {
            System.err.println("Error fetching GIFs: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Properly close resources
            try {
                if (reader != null) reader.close();
                if (conn != null) conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return gifs;
    }

    private void populateGifPicker(List<GifItem> gifs) {
        Platform.runLater(() -> {
            gifFlowPane.getChildren().clear();
            
            // Create a Task for loading images in background
            Task<Void> loadImagesTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    int count = 0;
                    for (GifItem gif : gifs) {
                        if (isCancelled()) break;
                        
                        final int index = count++;
                        // Check if image is already in cache
                        Image image;
                        if (IMAGE_CACHE.containsKey(gif.getUrl())) {
                            image = IMAGE_CACHE.get(gif.getUrl());
                        } else {
                            // Load image with background priority and caching
                            image = new Image(gif.getUrl(), 120, 120, true, true, true);
                            IMAGE_CACHE.put(gif.getUrl(), image);
                        }
                        
                        // Update UI on JavaFX thread
                        final Image finalImage = image;
                        Platform.runLater(() -> {
                            if (!isCancelled()) {
                                // Create a container for each GIF with a border
                                VBox gifContainer = new VBox();
                                gifContainer.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 5; -fx-background-color: #f8f9fa;");
                                gifContainer.setAlignment(javafx.geometry.Pos.CENTER);
                                gifContainer.setCursor(javafx.scene.Cursor.HAND);
                                
                                // Create the image view
                                ImageView imageView = new ImageView(finalImage);
                                imageView.setFitWidth(120);
                                imageView.setFitHeight(120);
                                imageView.setPreserveRatio(true);
                                imageView.setSmooth(true);
                                imageView.setCache(true);
                                
                                // Add the image to the container
                                gifContainer.getChildren().add(imageView);
                                
                                // Add hover effect
                                gifContainer.setOnMouseEntered(event -> {
                                    gifContainer.setStyle("-fx-border-color: #3498db; -fx-border-radius: 5; -fx-padding: 5; -fx-background-color: #f0f8ff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 1);");
                                });
                                
                                gifContainer.setOnMouseExited(event -> {
                                    gifContainer.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 5; -fx-background-color: #f8f9fa;");
                                });
                                
                                // Add click handler
                                gifContainer.setOnMouseClicked(event -> {
                                    if (listener != null) {
                                        listener.onGifSelected(gif.getUrl());
                                    }
                                    // Add null check for gifPickerPane
                                    if (gifPickerPane != null) {
                                        gifPickerPane.setVisible(false);
                                        gifPickerPane.setManaged(false);
                                    }
                                });
                                
                                // Add to flow pane
                                gifFlowPane.getChildren().add(gifContainer);
                            }
                        });
                        
                        // Small delay to prevent UI freezing
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            if (isCancelled()) break;
                        }
                    }
                    return null;
                }
            };
            
            // Start loading images in background
            new Thread(loadImagesTask).start();
        });
    }

    @FXML
    private void handleClose(ActionEvent event) {
        gifPickerPane.setVisible(false);
        gifPickerPane.setManaged(false);
    }

    public Button getGifCloseButton() {
        return gifCloseButton;
    }

    public TextField getGifSearchField() {
        return gifSearchField;
    }
    
    public FlowPane getGifFlowPane() {
        return gifFlowPane;
    }
    
    public ScrollPane getGifScrollPane() {
        return gifScrollPane;
    }

    public VBox getGifPickerPane() {
        return gifPickerPane;
    }

    private static class GifItem {
        private final String url;

        public GifItem(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }

    public void loadTrendingGifs() {
        if (!trendingGifsLoaded) {
            fetchTrendingGifs();
        } else {
            // If already loaded, just repopulate from cache
            String trendingUrl = "trending";
            if (GIF_CACHE.containsKey(trendingUrl)) {
                populateGifPicker(GIF_CACHE.get(trendingUrl));
            } else {
                fetchTrendingGifs();
            }
        }
    }
    
    // Clear caches when needed (e.g., low memory)
    public void clearCaches() {
        GIF_CACHE.clear();
        IMAGE_CACHE.clear();
        trendingGifsLoaded = false;
    }
}
