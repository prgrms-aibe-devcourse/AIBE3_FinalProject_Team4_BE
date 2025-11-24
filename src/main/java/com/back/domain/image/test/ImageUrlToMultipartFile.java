package com.back.domain.image.test;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

@Component
public class ImageUrlToMultipartFile {

    public MultipartFile convert(String imageUrl) {
        URI uri = URI.create(imageUrl);
        URL url;
        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try (InputStream is = url.openStream()) {
            byte[] bytes = is.readAllBytes();

            String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
            if (contentType == null) contentType = "image/jpeg";

            String ext = contentType.split("/")[1];
            String fileName = "api-image-" + UUID.randomUUID() + "." + ext;

            return new MockMultipartFile(
                    "images",
                    fileName,
                    contentType,
                    bytes
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
