package com.back.domain.image.test;

import com.back.domain.blog.blogFile.service.BlogMediaService;
import com.back.global.ut.ImageUrlToMultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/images/temp")
@RequiredArgsConstructor
public class TempImageController {
    private final BlogMediaService blogMediaService;
    private final ImageUrlToMultipartFile imageUrlToMultipartFile;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestPart("apiImageUrl") String apiImageUrl, // List<>
            @RequestParam Long userId,
            @RequestParam Long blogId
    ) {
//        if (apiImageUrl != null && !apiImageUrl.isBlank() && (file == null || file.isEmpty()) {}
//        MultipartFile file = imageUrlToMultipartFile.convert(apiImageUrl);
//        System.out.println("😀 파일 변환 후 업로드 시작!!");
//        blogMediaService.uploadBlogMedia(userId, blogId, file, apiImageUrl, ImageType.THUMBNAIL, "");
        return ResponseEntity.ok(null);
    }
}
