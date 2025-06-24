package com.esprit.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Utility class for handling image paths consistently across the application
 */
public class ImagePathUtil {
    
    /**
     * Get the absolute path to the blog images directory
     * @return The absolute path to the blog images directory
     */
    public static String getBlogImagesPath() {
        return System.getProperty("user.dir") + 
               File.separator + "src" + 
               File.separator + "main" + 
               File.separator + "resources" + 
               File.separator + "images" + 
               File.separator + "blogs";
    }
    
    /**
     * Get a File object for a blog image by its filename
     * @param filename The filename of the blog image
     * @return A File object pointing to the blog image
     */
    public static File getBlogImageFile(String filename) {
        return new File(getBlogImagesPath() + File.separator + filename);
    }
    
    /**
     * Get the URI string for a blog image by its filename
     * @param filename The filename of the blog image
     * @return The URI string for the blog image
     */
    public static String getBlogImageUri(String filename) {
        File imageFile = getBlogImageFile(filename);
        if (imageFile.exists()) {
            return imageFile.toURI().toString();
        }
        return null;
    }
    
    /**
     * Ensure the blog images directory exists
     * @return true if the directory exists or was created, false otherwise
     */
    public static boolean ensureBlogImagesDirectoryExists() {
        File directory = new File(getBlogImagesPath());
        if (!directory.exists()) {
            return directory.mkdirs();
        }
        return true;
    }
    
    /**
     * List all files in the blog images directory
     * @return An array of filenames in the blog images directory
     */
    public static String[] listBlogImages() {
        File directory = new File(getBlogImagesPath());
        if (directory.exists() && directory.isDirectory()) {
            return directory.list();
        }
        return new String[0];
    }
    
    /**
     * Validate and convert an image to a format that JavaFX can display
     * @param sourceFile The source image file
     * @return The converted image file (or the original if no conversion was needed)
     * @throws IOException If the image cannot be validated or converted
     */
    public static File validateAndConvertImage(File sourceFile) throws IOException {
        if (sourceFile == null || !sourceFile.exists()) {
            throw new IOException("Source image file does not exist");
        }
        
        System.out.println("Attempting to validate and convert image: " + sourceFile.getAbsolutePath());
        
        // Try multiple approaches to read the image
        BufferedImage sourceImage = null;
        Exception lastException = null;
        
        // First try: Standard ImageIO.read
        try {
            sourceImage = ImageIO.read(sourceFile);
        } catch (Exception e) {
            lastException = e;
            System.err.println("Standard ImageIO.read failed: " + e.getMessage());
        }
        
        // Second try: Try with FileInputStream if first attempt failed
        if (sourceImage == null) {
            try (FileInputStream fis = new FileInputStream(sourceFile)) {
                sourceImage = ImageIO.read(fis);
            } catch (Exception e) {
                lastException = e;
                System.err.println("FileInputStream approach failed: " + e.getMessage());
            }
        }
        
        // Third try: If it's a JPEG, try with the specialized JPEGImageReader
        if (sourceImage == null && sourceFile.getName().toLowerCase().endsWith(".jpeg") 
                || sourceFile.getName().toLowerCase().endsWith(".jpg")) {
            try {
                Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("jpeg");
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    try (FileImageInputStream input = new FileImageInputStream(sourceFile)) {
                        reader.setInput(input);
                        sourceImage = reader.read(0);
                    } finally {
                        reader.dispose();
                    }
                }
            } catch (Exception e) {
                lastException = e;
                System.err.println("JPEG specialized reader failed: " + e.getMessage());
            }
        }
        
        // If all attempts failed, throw an exception with detailed information
        if (sourceImage == null) {
            String errorMsg = "Failed to read image data from file: " + sourceFile.getAbsolutePath();
            if (lastException != null) {
                errorMsg += " - Last error: " + lastException.getMessage();
            }
            throw new IOException(errorMsg);
        }
        
        // Create a new RGB image with transparency support
        BufferedImage convertedImage = new BufferedImage(
            sourceImage.getWidth(),
            sourceImage.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        
        // Draw the original image onto the new one
        Graphics2D g2d = convertedImage.createGraphics();
        try {
            g2d.drawImage(sourceImage, 0, 0, null);
        } finally {
            g2d.dispose(); // Properly dispose of graphics context
        }
        
        // Create a temporary file with .png extension
        String baseName = sourceFile.getName();
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = baseName.substring(0, dotIndex);
        }
        
        File convertedFile = new File(sourceFile.getParent(), baseName + ".png");
        
        // Write the image as PNG (which JavaFX definitely supports)
        boolean success = ImageIO.write(convertedImage, "png", convertedFile);
        if (!success) {
            throw new IOException("No appropriate image writer found for PNG format");
        }
        
        System.out.println("Successfully converted image to PNG format: " + convertedFile.getAbsolutePath());
        return convertedFile;
    }
    
    /**
     * Copy an image file to the blog images directory with proper format conversion if needed
     * @param sourceFile The source image file
     * @return The filename of the copied image in the blog images directory
     * @throws IOException If the image cannot be copied or converted
     */
    public static String copyImageToBlogDirectory(File sourceFile) throws IOException {
        if (sourceFile == null || !sourceFile.exists()) {
            throw new IOException("Source image file does not exist");
        }
        
        // Ensure the blog images directory exists
        if (!ensureBlogImagesDirectoryExists()) {
            throw new IOException("Failed to create blog images directory");
        }
        
        // Validate and convert the image if needed
        File validImageFile = validateAndConvertImage(sourceFile);
        
        // Create a unique filename
        String baseName = validImageFile.getName();
        String destFileName = System.currentTimeMillis() + "_" + baseName;
        File destFile = new File(getBlogImagesPath(), destFileName);
        
        // Copy the file
        System.out.println("Copying image from: " + validImageFile.getAbsolutePath());
        System.out.println("Copying image to: " + destFile.getAbsolutePath());
        
        Files.copy(validImageFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        // Verify the file was copied successfully
        if (!destFile.exists() || destFile.length() == 0) {
            throw new IOException("File copy failed or resulted in empty file");
        }
        
        System.out.println("Image copied successfully. Size: " + destFile.length() + " bytes");
        return destFileName;
    }
}
