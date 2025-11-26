package com.levelupgamer.common.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface FileStorageService {

    default String uploadFile(InputStream inputStream, String originalFileName, long contentLength) throws IOException {
        return uploadFile(inputStream, originalFileName, contentLength, null, null);
    }

    default String uploadFile(InputStream inputStream, String originalFileName, long contentLength, String folder)
            throws IOException {
        return uploadFile(inputStream, originalFileName, contentLength, folder, null);
    }

    String uploadFile(InputStream inputStream, String originalFileName, long contentLength, String folder,
            String contentType) throws IOException;

    Optional<String> readContentIfManaged(String publicUrl) throws IOException;
}
