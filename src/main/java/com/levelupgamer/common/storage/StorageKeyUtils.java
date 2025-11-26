package com.levelupgamer.common.storage;

import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.util.StringUtils;

final class StorageKeyUtils {

    private static final String DEFAULT_FILE_NAME = "asset";
    private static final int SHORT_ID_LENGTH = 6;

    private StorageKeyUtils() {
    }

    static String sanitizeFolder(String folder, String defaultFolder) {
        String candidate = StringUtils.hasText(folder) ? folder : defaultFolder;
        String normalizedDefault = normalizeSegment(defaultFolder);
        List<String> segments = new ArrayList<>();
        for (String rawSegment : candidate.split("[/\\\\]")) {
            String sanitized = normalizeSegment(rawSegment);
            if (StringUtils.hasText(sanitized)) {
                segments.add(sanitized);
            }
        }
        if (segments.isEmpty() && StringUtils.hasText(normalizedDefault)) {
            segments.add(normalizedDefault);
        }
        if (segments.isEmpty()) {
            segments.add(DEFAULT_FILE_NAME);
        }
        return String.join("/", segments);
    }

    static String sanitizeFileName(String originalFileName) {
        String safeName = StringUtils.hasText(originalFileName) ? originalFileName : DEFAULT_FILE_NAME;
        String fileName = Paths.get(safeName).getFileName().toString();
        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replace(' ', '-').toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9\\.\\-_]", "-");
        normalized = normalized.replaceAll("-{2,}", "-");
        normalized = trimDashes(normalized);
        if (!StringUtils.hasText(normalized) || normalized.startsWith(".")) {
            normalized = DEFAULT_FILE_NAME;
        }
        return normalized;
    }

    static String shortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, SHORT_ID_LENGTH);
    }

    private static String normalizeSegment(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replace(' ', '-').toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9\\-_]", "-");
        normalized = normalized.replaceAll("-{2,}", "-");
        return trimDashes(normalized);
    }

    private static String trimDashes(String value) {
        String result = value;
        while (result.startsWith("-")) {
            result = result.substring(1);
        }
        while (result.endsWith("-")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
