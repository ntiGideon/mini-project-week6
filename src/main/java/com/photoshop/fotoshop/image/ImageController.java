package com.photoshop.fotoshop.image;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Controller
@RequestMapping("/")
public class ImageController {
    private final ImageService imageService;
    private final ImageRepository imageRepository;

    public ImageController(ImageService imageService, ImageRepository imageRepository) {
        this.imageService = imageService;
        this.imageRepository=imageRepository;
    }

    @GetMapping("/upload")
    public String showUploadPage(Model model) {
        model.addAttribute("title", "Upload Image");
        return "upload";
    }

    @PostMapping("/api/images")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "featured") boolean featured
            ) {

        try {
            ImageDto imageDto = new ImageDto(name, description,featured, file);
            Long imageId = imageService.uploadImage(imageDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", imageId);
            response.put("message", "Image uploaded successfully");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    public String home(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {

        Page<Image> imagePage = imageService.getAllImages(page, size);

        List<Image> featuredImages = imageRepository.findTop3ByFeaturedTrueOrderByCreatedAtDesc();

        // Calculate pagination values
        int currentPage = imagePage.getNumber();
        int totalPages = imagePage.getTotalPages();
        long totalItems = imagePage.getTotalElements();
        long firstItem = (long) currentPage * size + 1;
        long lastItem = firstItem + size - 1;
        if (lastItem > totalItems) lastItem = totalItems;

        // Generate page numbers for display
        List<Integer> pageNumbers = IntStream.rangeClosed(0, totalPages - 1)
                .boxed()
                .collect(Collectors.toList());

        model.addAttribute("title", "Photo Gallery");
        model.addAttribute("images", imagePage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("firstItem", firstItem);
        model.addAttribute("lastItem", lastItem);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("hasPrev", currentPage > 0);
        model.addAttribute("hasNext", currentPage < totalPages - 1);
        model.addAttribute("prevPage", currentPage - 1);
        model.addAttribute("nextPage", currentPage + 1);
        model.addAttribute("featuredImages", featuredImages);

        return "index";
    }

    @GetMapping("/images/{id}")
    public String imageDetails(@PathVariable Long id, Model model) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found"));


        List<Image> relatedImages = imageRepository.findTop4ByIdNotOrderByCreatedAtDesc(id);

        model.addAttribute("title", image.getName() + " | Photo Details");
        model.addAttribute("image", image);
        model.addAttribute("hasRelated", !relatedImages.isEmpty());
        model.addAttribute("relatedImages", relatedImages);

        return "details";
    }


    @DeleteMapping("/images/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable Long id) {
        try {
            Image image = imageRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Image not found"));

            String key = imageService.extractKeyFromUrl(image.getS3Url());

            imageService.deleteFile(key);

            imageRepository.deleteById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Image deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }



    @GetMapping("/images/{id}/download")
    public ResponseEntity<byte[]> downloadImage(@PathVariable Long id) throws IOException {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        // Extract key from URL (you already have this method)
        String key = imageService.extractKeyFromUrl(image.getS3Url());

        byte[] imageBytes = imageService.downloadImage(key);

        // Determine content type
        String contentType = determineContentType(image.getS3Url());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + getDownloadFilename(image) + "\"")
                .body(imageBytes);
    }

    private String determineContentType(String s3Url) {

        String extension = s3Url.substring(s3Url.lastIndexOf('.') + 1).toLowerCase();

        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            default:
                return "application/octet-stream";
        }
    }

    private String getDownloadFilename(Image image) {
        String originalFilename = image.getS3Url().substring(image.getS3Url().lastIndexOf('/') + 1);
        String safeName = image.getName().replaceAll("[^a-zA-Z0-9.-]", "_");
        return safeName + "_" + originalFilename;
    }

}
