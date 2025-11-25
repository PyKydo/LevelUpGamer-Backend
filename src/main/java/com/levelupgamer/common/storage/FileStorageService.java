package com.levelupgamer.common.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface FileStorageService {

    String uploadFile(InputStream inputStream, String originalFileName, long contentLength) throws IOException;

    Optional<String> readContentIfManaged(String publicUrl) throws IOException;
}
