package com.ll.standard.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.global.app.AppConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Ut {
    public static class str {
        public static boolean isBlank(String str) {
            return str == null || str.trim().isEmpty();
        }
    }

    public static class json {
        private static final ObjectMapper om = AppConfig.getObjectMapper();

        @SneakyThrows
        public static String toString(Object obj) {
            return om.writeValueAsString(obj);
        }
    }

    public static class jwt {
        public static String toString(String secret, long expireSeconds, Map<String, Object> body) {
            Date issuedAt = new Date();
            Date expiration = new Date(issuedAt.getTime() + 1000L * expireSeconds);

            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

            String jwt = Jwts.builder()
                    .claims(body)
                    .issuedAt(issuedAt)
                    .expiration(expiration)
                    .signWith(secretKey)
                    .compact();

            return jwt;
        }

        public static boolean isValid(String secret, String jwtStr) {
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

            try {
                Jwts
                        .parser()
                        .verifyWith(secretKey)
                        .build()
                        .parse(jwtStr);
            } catch (Exception e) {
                return false;
            }

            return true;
        }

        public static Map<String, Object> payload(String secret, String jwtStr) {
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

            try {
                return (Map<String, Object>) Jwts
                        .parser()
                        .verifyWith(secretKey)
                        .build()
                        .parse(jwtStr)
                        .getPayload();
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static class file {
        private static final String ORIGIN_FILE_NAME_SEPARATOR = "--originFileName_";

        private static final Map<String, String> MIME_TYPE_MAP = new LinkedHashMap<>() {{
            put("application/json", "json");
            put("text/plain", "txt");
            put("text/html", "html");
            put("text/css", "css");
            put("application/javascript", "js");
            put("image/jpeg", "jpg");
            put("image/png", "png");
            put("image/gif", "gif");
            put("image/webp", "webp");
            put("image/svg+xml", "svg");
            put("application/pdf", "pdf");
            put("application/xml", "xml");
            put("application/zip", "zip");
            put("application/gzip", "gz");
            put("application/x-tar", "tar");
            put("application/x-7z-compressed", "7z");
            put("application/vnd.rar", "rar");
            put("audio/mpeg", "mp3");
            put("audio/wav", "wav");
            put("video/mp4", "mp4");
            put("video/webm", "webm");
            put("video/x-msvideo", "avi");
        }};


        @SneakyThrows
        public static String downloadByHttp(String url, String dirPath) {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            String tempFilePath = dirPath + "/" + UUID.randomUUID() + ".tmp";

            Ut.file.mkdir(dirPath);

            // 실제 파일 다운로드
            HttpResponse<Path> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofFile(Path.of(tempFilePath))
            );

            // 파일 확장자 추출
            String extension = getExtensionFromResponse(response);

            if (extension.equals("tmp")) {
                extension = getExtensionByTika(tempFilePath);
            }

            // 파일명 추출
            String filename = getFilenameFromUrl(url);

            String newFilePath = dirPath + "/" + filename + "." + extension;

            mv(tempFilePath, newFilePath);

            return newFilePath;
        }

        public static String getExtensionByTika(String filePath) {
            String mineType = AppConfig.getTika().detect(filePath);

            return MIME_TYPE_MAP.getOrDefault(mineType, "tmp");
        }

        @SneakyThrows
        public static void mv(String oldFilePath, String newFilePath) {
            mkdir(Paths.get(newFilePath).getParent().toString());

            Files.move(
                    Path.of(oldFilePath),
                    Path.of(newFilePath),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        @SneakyThrows
        private static void mkdir(String dirPath) {
            Path path = Path.of(dirPath);

            if (Files.exists(path)) return;

            Files.createDirectories(path);
        }

        private static String getFilenameFromUrl(String url) {
            try {
                String path = new URI(url).getPath();
                String filename = Path.of(path).getFileName().toString();
                // 확장자 제거
                return filename.contains(".")
                        ? filename.substring(0, filename.lastIndexOf('.'))
                        : filename;
            } catch (URISyntaxException e) {
                // URL에서 파일명을 추출할 수 없는 경우 타임스탬프 사용
                return "download_" + System.currentTimeMillis();
            }
        }

        private static String getExtensionFromResponse(HttpResponse<?> response) {
            return response.headers()
                    .firstValue("Content-Type")
                    .map(contentType -> MIME_TYPE_MAP.getOrDefault(contentType, "tmp"))
                    .orElse("tmp");
        }

        @SneakyThrows
        public static void delete(String filePath) {
            Files.deleteIfExists(Path.of(filePath));
        }

        @SneakyThrows
        public static String toFile(MultipartFile multipartFile, String dirPath) {
            if (multipartFile == null) return "";
            if (multipartFile.isEmpty()) return "";

            String filePath = dirPath + "/" + UUID.randomUUID() + ORIGIN_FILE_NAME_SEPARATOR + multipartFile.getOriginalFilename();

            System.out.println("filePath = " + filePath);

            Ut.file.mkdir(dirPath);
            multipartFile.transferTo(new File(filePath));

            return filePath;
        }

        public static String getOriginFileName(String file) {
            if (file.contains(ORIGIN_FILE_NAME_SEPARATOR)) {
                String[] fileInfos = file.split(ORIGIN_FILE_NAME_SEPARATOR);
                return fileInfos[fileInfos.length - 1];
            }

            return Paths.get(file).getFileName().toString();
        }

        public static String getFileExtTypeCodeFromFileExt(String ext) {
            return switch (ext) {
                case "jpeg", "jpg", "gif", "png" -> "img";
                case "mp4", "avi", "mov" -> "video";
                case "mp3" -> "audio";
                default -> "etc";
            };
        }

        public static String getFileExtType2CodeFromFileExt(String ext) {
            return switch (ext) {
                case "jpeg", "jpg" -> "jpg";
                case "gif", "png", "mp4", "mov", "avi", "mp3" -> ext;
                default -> "etc";
            };
        }

        public static String getExt(String filename) {
            return Optional.ofNullable(filename)
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(filename.lastIndexOf(".") + 1).toLowerCase())
                    .orElse("");
        }

        public static String getDirName(String filePath) {
            return Paths.get(filePath).getParent().getFileName().toString();
        }
    }


    public static class cmd {
        public static void runAsync(String cmd) {
            new Thread(() -> run(cmd)).start();
        }

        public static void run(String cmd) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", cmd);
                Process process = processBuilder.start();
                process.waitFor(1, TimeUnit.MINUTES);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static class date {
        public static String getCurrentDateFormatted(String pattern) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            return simpleDateFormat.format(new Date());
        }
    }
}
