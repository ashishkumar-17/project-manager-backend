package com.projectmanager.service.Impl;

import com.projectmanager.service.R2StorageService;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

@Service
public class R2StorageServiceImpl implements R2StorageService {

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.publicBase}")
    private String publicBase;

    private final S3Client s3Client;

    // Image compression settings
    private static final int MAX_IMAGE_WIDTH = 1920;
    private static final int MAX_IMAGE_HEIGHT = 1080;
    private static final float JPEG_QUALITY = 0.85f;

    // File types that should be compressed with gzip
    private static final Set<String> GZIP_COMPRESSIBLE_TYPES = Set.of(
            "text/plain", "text/html", "text/css", "text/javascript",
            "application/json", "application/xml", "text/xml"
    );

    // Image types that should be compressed
    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    public R2StorageServiceImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String uploadFile(String key, InputStream inputStream, long size, String contentType) {
        try {
            // Determine compression strategy based on content type
            if (shouldCompressImage(contentType)) {
                return uploadCompressedImage(key, inputStream, contentType);
            } else if (shouldGzipCompress(contentType)) {
                return uploadGzipCompressed(key, inputStream, contentType);
            } else {
                return uploadDirect(key, inputStream, size, contentType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to R2: " + e.getMessage(), e);
        }
    }

    private String uploadCompressedImage(String key, InputStream inputStream, String contentType) throws IOException {
        // Read and compress image
        byte[] compressedImageData = compressImage(inputStream, contentType);

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("image/jpeg") // Convert all images to JPEG for consistency
                .contentLength((long) compressedImageData.length)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(compressedImageData));
        return publicBase + "/" + key;
    }

    private String uploadGzipCompressed(String key, InputStream inputStream, String contentType) throws IOException {
        // Compress with gzip
        byte[] compressedData = compressWithGzip(inputStream);

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentEncoding("gzip")
                .contentLength((long) compressedData.length)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(compressedData));
        return publicBase + "/" + key;
    }

    private String uploadDirect(String key, InputStream inputStream, long size, String contentType) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength(size)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, size));
        return publicBase + "/" + key;
    }

    private byte[] compressImage(InputStream inputStream, String contentType) throws IOException {
        BufferedImage originalImage = null;

        try {
            // First attempt: Standard ImageIO reading
            originalImage = ImageIO.read(inputStream);
        } catch (Exception e) {
            // If standard reading fails, try alternative approaches
            try {
                // Reset stream if possible
                if (inputStream.markSupported()) {
                    inputStream.reset();
                } else {
                    throw new IOException("Cannot reset stream for alternative reading");
                }

                // Try reading with different decoders
                originalImage = readImageWithFallback(inputStream);
            } catch (Exception fallbackException) {
                throw new IOException("Unable to read image data. Original error: " + e.getMessage() +
                        ", Fallback error: " + fallbackException.getMessage());
            }
        }

        if (originalImage == null) {
            throw new IOException("Unable to read image data - image is null");
        }

        // Convert to standard RGB format to avoid colorspace issues
        BufferedImage rgbImage = convertToRGB(originalImage);

        // Resize image if it's too large
        BufferedImage resizedImage = resizeImageIfNeeded(rgbImage);

        // Convert to JPEG with compression
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Use simpler ImageIO.write for better compatibility
        if (!ImageIO.write(resizedImage, "jpg", baos)) {
            throw new IOException("Failed to write JPEG image");
        }

        return baos.toByteArray();
    }

    private BufferedImage readImageWithFallback(InputStream inputStream) throws IOException {
        // Try with explicit format readers
        String[] formats = {"jpg", "jpeg", "png", "gif", "webp"};

        for (String format : formats) {
            try {
                Iterator<javax.imageio.ImageReader> readers = ImageIO.getImageReadersByFormatName(format);
                if (readers.hasNext()) {
                    javax.imageio.ImageReader reader = readers.next();
                    try (javax.imageio.stream.ImageInputStream iis = ImageIO.createImageInputStream(inputStream)) {
                        reader.setInput(iis);
                        BufferedImage image = reader.read(0);
                        if (image != null) {
                            return image;
                        }
                    } finally {
                        reader.dispose();
                    }
                }
            } catch (Exception e) {
                // Continue to next format
                continue;
            }
        }

        throw new IOException("All image reading attempts failed");
    }

    private BufferedImage convertToRGB(BufferedImage originalImage) {
        // If already RGB, return as-is
        if (originalImage.getType() == BufferedImage.TYPE_INT_RGB) {
            return originalImage;
        }

        // Convert to RGB to avoid colorspace issues
        BufferedImage rgbImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = rgbImage.createGraphics();
        g2d.setColor(Color.WHITE); // Set white background for transparent images
        g2d.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();

        return rgbImage;
    }

    private BufferedImage resizeImageIfNeeded(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Check if resize is needed
        if (originalWidth <= MAX_IMAGE_WIDTH && originalHeight <= MAX_IMAGE_HEIGHT) {
            return originalImage;
        }

        // Calculate new dimensions maintaining aspect ratio
        double aspectRatio = (double) originalWidth / originalHeight;
        int newWidth, newHeight;

        if (aspectRatio > 1) { // Landscape
            newWidth = Math.min(originalWidth, MAX_IMAGE_WIDTH);
            newHeight = (int) (newWidth / aspectRatio);
        } else { // Portrait or square
            newHeight = Math.min(originalHeight, MAX_IMAGE_HEIGHT);
            newWidth = (int) (newHeight * aspectRatio);
        }

        // Create resized image
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();

        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resizedImage;
    }

    private byte[] compressWithGzip(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
            inputStream.transferTo(gzos);
        }

        return baos.toByteArray();
    }

    private boolean shouldCompressImage(String contentType) {
        return contentType != null && IMAGE_TYPES.contains(contentType.toLowerCase());
    }

    private boolean shouldGzipCompress(String contentType) {
        return contentType != null && GZIP_COMPRESSIBLE_TYPES.contains(contentType.toLowerCase());
    }

    // Enhanced method with compression options
    public String uploadFileWithOptions(String key, InputStream inputStream, long size,
                                        String contentType, boolean forceCompression) {
        try {
            if (forceCompression || shouldCompressImage(contentType)) {
                return uploadCompressedImage(key, inputStream, contentType);
            } else if (forceCompression || shouldGzipCompress(contentType)) {
                return uploadGzipCompressed(key, inputStream, contentType);
            } else {
                return uploadDirect(key, inputStream, size, contentType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to R2: " + e.getMessage(), e);
        }
    }

    // Method to get compression stats
    public CompressionStats getCompressionStats(InputStream inputStream, String contentType) throws IOException {
        // Reset stream if possible
        if (inputStream.markSupported()) {
            inputStream.mark(Integer.MAX_VALUE);
        }

        byte[] originalData = inputStream.readAllBytes();
        long originalSize = originalData.length;

        // Reset stream if marked
        if (inputStream.markSupported()) {
            inputStream.reset();
        }

        byte[] compressedData;
        if (shouldCompressImage(contentType)) {
            compressedData = compressImage(new ByteArrayInputStream(originalData), contentType);
        } else if (shouldGzipCompress(contentType)) {
            compressedData = compressWithGzip(new ByteArrayInputStream(originalData));
        } else {
            return new CompressionStats(originalSize, originalSize, 0.0);
        }

        long compressedSize = compressedData.length;
        double compressionRatio = (double) (originalSize - compressedSize) / originalSize * 100;

        return new CompressionStats(originalSize, compressedSize, compressionRatio);
    }

    @Override
    public byte[] downloadFile(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
        return objectBytes.asByteArray();
    }

    @Override
    public void deleteFile(String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    // Helper class for compression statistics
    public static class CompressionStats {
        private final long originalSize;
        private final long compressedSize;
        private final double compressionRatio;

        public CompressionStats(long originalSize, long compressedSize, double compressionRatio) {
            this.originalSize = originalSize;
            this.compressedSize = compressedSize;
            this.compressionRatio = compressionRatio;
        }

        public long getOriginalSize() { return originalSize; }
        public long getCompressedSize() { return compressedSize; }
        public double getCompressionRatio() { return compressionRatio; }
        public long getSavedBytes() { return originalSize - compressedSize; }
    }
}