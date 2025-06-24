package com.esprit.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageUtils {
    private static final String DEFAULT_IMAGE_PATH = "/images/products/default_renewable.png";
    private static final String DEFAULT_LOGO_PATH = "/images/logos/default_logo.png";
    
    public static ImageView createImageView(String imagePath, int width, int height) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setImage(loadImage(imagePath));
        return imageView;
    }

    public static Image loadImage(String imagePath) {
        try {
            if (imagePath == null || imagePath.isEmpty() || imagePath.startsWith("data:image/svg+xml")) {
                return getDefaultImage();
            }

            // Check if imagePath is the default image filename, return default image directly
            if ("default_renewable.png".equals(imagePath) || DEFAULT_IMAGE_PATH.endsWith(imagePath)) {
                return getDefaultImage();
            }
            
            // Check if it's a logo path
            if (imagePath.startsWith("logos/")) {
                return loadLogoImage(imagePath.substring(6)); // Remove the "logos/" prefix
            }
            
            // Check if it's the default logo
            if ("default_logo.png".equals(imagePath)) {
                return getDefaultLogoImage();
            }

            // Try loading from file system first
            File file = new File("src/main/resources/images/products/" + imagePath);
            if (file.exists()) {
                return new Image(file.toURI().toString());
            }

            // Fallback to resource stream
            String resourcePath = imagePath.startsWith("/") ? imagePath : "/images/products/" + imagePath;
            InputStream stream = ImageUtils.class.getResourceAsStream(resourcePath);
            
            if (stream != null) {
                return new Image(stream);
            }
            return getDefaultImage();
        } catch (Exception e) {
            System.err.println("Error loading image: " + imagePath + " - " + e.getMessage());
            return getDefaultImage();
        }
    }
    
    public static Image loadLogoImage(String logoPath) {
        try {
            if (logoPath == null || logoPath.isEmpty()) {
                return getDefaultLogoImage();
            }

            // Check if logoPath is the default logo filename
            if ("default_logo.png".equals(logoPath)) {
                return getDefaultLogoImage();
            }

            // Try loading from file system first
            File file = new File("src/main/resources/images/logos/" + logoPath);
            if (file.exists()) {
                return new Image(file.toURI().toString());
            }

            // Fallback to resource stream
            String resourcePath = logoPath.startsWith("/") ? logoPath : "/images/logos/" + logoPath;
            InputStream stream = ImageUtils.class.getResourceAsStream(resourcePath);
            
            if (stream != null) {
                return new Image(stream);
            }
            return getDefaultLogoImage();
        } catch (Exception e) {
            System.err.println("Error loading logo image: " + logoPath + " - " + e.getMessage());
            return getDefaultLogoImage();
        }
    }

    
    private static Image getDefaultImage() {
        try {
            InputStream stream = ImageUtils.class.getResourceAsStream(DEFAULT_IMAGE_PATH);
            if (stream != null) {
                return new Image(stream);
            }
            System.err.println("Default image not found at: " + DEFAULT_IMAGE_PATH);
            return createColorPlaceholder();
        } catch (Exception e) {
            System.err.println("Error loading default image: " + e.getMessage());
            return createColorPlaceholder();
        }
    }
    
    private static Image getDefaultLogoImage() {
        try {
            InputStream stream = ImageUtils.class.getResourceAsStream(DEFAULT_LOGO_PATH);
            if (stream != null) {
                return new Image(stream);
            }
            System.err.println("Default logo image not found at: " + DEFAULT_LOGO_PATH);
            return createColorPlaceholder();
        } catch (Exception e) {
            System.err.println("Error loading default logo image: " + e.getMessage());
            return createColorPlaceholder();
        }
    }
    
    private static Image createColorPlaceholder() {
        return new Image(new ByteArrayInputStream(new byte[0]), 150, 150, true, true);
    }

    public static String saveProductImage(File imageFile, String productName) {
        try {
            String resourcesPath = "src/main/resources/images/products/";
            Files.createDirectories(Paths.get(resourcesPath));
            
            String originalName = imageFile.getName();
            String extension = originalName.contains(".") ? 
                originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
            
            String cleanName = productName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = cleanName + "_" + timestamp + extension;
            String destPath = resourcesPath + fileName;
            
            Files.copy(imageFile.toPath(), Paths.get(destPath), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (Exception e) {
            System.err.println("Error saving product image: " + e.getMessage());
            return DEFAULT_IMAGE_PATH;
        }
    }

    public static boolean deleteProductImage(String imagePath) {
        try {
            String resourcesPath = "src/main/resources/images/products/";
            File file = new File(resourcesPath + imagePath);
            if (file.exists() && file.isFile()) {
                return file.delete();
            }
        } catch (Exception e) {
            System.err.println("Error deleting product image: " + e.getMessage());
        }
        return false;
    }
    
    public static String saveCompanyLogo(File logoFile, String companyName) {
        try {
            String resourcesPath = "src/main/resources/images/logos/";
            Files.createDirectories(Paths.get(resourcesPath));
            
            String originalName = logoFile.getName();
            String extension = originalName.contains(".") ? 
                originalName.substring(originalName.lastIndexOf(".")) : ".png";
            
            String cleanName = companyName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = "logo_" + cleanName + "_" + timestamp + extension;
            String destPath = resourcesPath + fileName;
            
            Files.copy(logoFile.toPath(), Paths.get(destPath), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (Exception e) {
            System.err.println("Error saving company logo: " + e.getMessage());
            return "default_logo.png";
        }
    }

    public static boolean deleteCompanyLogo(String logoPath) {
        try {
            if ("default_logo.png".equals(logoPath)) {
                return false; // Don't delete default logo
            }
            
            String resourcesPath = "src/main/resources/images/logos/";
            File file = new File(resourcesPath + logoPath);
            if (file.exists() && file.isFile()) {
                return file.delete();
            }
        } catch (Exception e) {
            System.err.println("Error deleting company logo: " + e.getMessage());
        }
        return false;
    }

}
