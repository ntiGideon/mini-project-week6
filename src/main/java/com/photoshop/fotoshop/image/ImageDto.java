package com.photoshop.fotoshop.image;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record ImageDto(
        @NotNull
        String name,
        @NotNull
        String description,
        boolean featured,
        @Size(min = 1)
        @NotNull
        MultipartFile file
) {

}
