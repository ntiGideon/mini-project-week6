package com.photoshop.fotoshop.image;

import com.photoshop.fotoshop.s3.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Service
@Transactional
public class ImageService {
    private final ImageRepository imageRepository;
    private final S3Service s3Service;

    @Autowired
    public ImageService(ImageRepository imageRepository, S3Service s3Service) {
        this.imageRepository = imageRepository;
        this.s3Service = s3Service;
    }

    public Long uploadImage(ImageDto imageDto) throws IOException {
        try {
            if (imageDto.file().getSize() > 5_242_880) {
                throw new IOException("File size exceeds 5MB limit");
            }

            String contentType = imageDto.file().getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IOException("Invalid file type. Only images are allowed");
            }

            String s3Url = s3Service.uploadFile(imageDto.file());

            Image image = new Image();
            image.setName(imageDto.name());
            image.setDescription(imageDto.description());
            image.setS3Url(s3Url);
            image.setFeatured(image.getFeatured());

            Image savedImage = imageRepository.save(image);
            return savedImage.getId();
        } catch (Exception e) {
            throw new IOException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    public Page<Image> getAllImages(int page, int size){
        return imageRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    public void deleteFile(String key) {
        s3Service.deleteFile(key);
    }

    public byte[] downloadImage(String key) throws IOException {
        return s3Service.downloadFile(key);
    }

    public String extractKeyFromUrl(String s3Url) {

        try {
            URI uri = new URI(s3Url);
            String path = uri.getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid S3 URL format", e);
        }
    }

}
