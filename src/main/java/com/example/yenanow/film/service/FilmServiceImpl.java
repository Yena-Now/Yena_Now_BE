package com.example.yenanow.film.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.film.dto.request.MergeRequest;
import com.example.yenanow.film.dto.request.MergeRequestItem;
import com.example.yenanow.film.dto.response.BackgroundListResponse;
import com.example.yenanow.film.dto.response.FrameListResponse;
import com.example.yenanow.film.dto.response.MergeResponse;
import com.example.yenanow.film.dto.response.StickerListResponse;
import com.example.yenanow.film.dto.response.StickerListResponseItem;
import com.example.yenanow.film.entity.Frame;
import com.example.yenanow.film.entity.Sticker;
import com.example.yenanow.film.repository.BackgroundRepository;
import com.example.yenanow.film.repository.FrameRepository;
import com.example.yenanow.film.repository.StickerRepository;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {

    private final FrameRepository frameRepository;
    private final StickerRepository stickerRepository;
    private final BackgroundRepository backgroundRepository;
    private final S3Client s3Client;

    @Value("${AWS_S3_BUCKET}")
    private String bucketName;

    // ... getFrames, getStickers, getBackgrounds, createMergedOutputAsync 메서드는 동일 ...
    @Override
    public List<FrameListResponse> getFrames(int frameCut) {
        return frameRepository.findByFrameCut(frameCut).stream()
            .map(FrameListResponse::fromEntity)
            .toList();
    }

    @Override
    public StickerListResponse getStickers(Pageable pageable) {
        Page<Sticker> page = stickerRepository.findAll(pageable);
        return StickerListResponse.builder()
            .totalPages(page.getTotalPages())
            .stickers(page.getContent().stream()
                .map(StickerListResponseItem::fromEntity)
                .toList())
            .build();
    }

    @Override
    public List<BackgroundListResponse> getBackgrounds() {
        return backgroundRepository.findAll().stream()
            .map(BackgroundListResponse::fromEntity)
            .toList();
    }

    @Async
    @Override
    public CompletableFuture<MergeResponse> createMergedOutputAsync(MergeRequest request) {
        MergeResponse response = createMergedOutput(request);
        return CompletableFuture.completedFuture(response);
    }

    @Override
    public MergeResponse createMergedOutput(MergeRequest request) {
        String frameUuid = request.getFrameUuid();
        List<MergeRequestItem> contentUrls = request.getContentUrls();

        contentUrls.sort(java.util.Comparator.comparingInt(MergeRequestItem::getOrder));

        Frame frame = frameRepository.findById(frameUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        List<Map<Integer, Integer>> coordinates = getCoordinate(frame.getFrameCut(),
            frame.getFrameType());

        String uniqueId = UUID.randomUUID().toString();
        // System.getProperty("java.io.tmpdir")는 OS가 제공하는 임시 디렉토리 경로를 반환
        String tempDirPath =
            System.getProperty("java.io.tmpdir") + File.separator + "yenanow-" + uniqueId;
        File tempDir = new File(tempDirPath);
        tempDir.mkdirs();

        try {
            String frameKey = frame.getFrameUrl();
            String frameS3Uri = "s3://" + bucketName + "/" + frameKey;
            String framePath = downloadFromS3(frameS3Uri, "frame", tempDirPath);

            List<String> cutPaths = new ArrayList<>();
            for (int i = 0; i < contentUrls.size(); i++) {
                String httpUrlString = contentUrls.get(i).getContentUrl();
                java.net.URL url = new java.net.URL(httpUrlString);
                String host = url.getHost();
                String parsedBucket = host.substring(0, host.indexOf(".s3."));
                String parsedKey = url.getPath().substring(1);
                String cutS3Uri = "s3://" + parsedBucket + "/" + parsedKey;
                cutPaths.add(downloadFromS3(cutS3Uri, "cut" + i, tempDirPath));
            }

            boolean containsVideo = cutPaths.stream()
                .anyMatch(path -> path.endsWith(".mp4") || path.endsWith(".mov") || path.endsWith(
                    ".webm"));
            String ext = containsVideo ? "mp4" : "jpg";
            String outputPath = tempDirPath + File.separator + "output." + ext;

            ProcessBuilder ffmpegBuilder = buildFfmpegCommand(framePath, cutPaths, coordinates,
                outputPath, containsVideo);
            ffmpegBuilder.redirectErrorStream(true);
            Process process = ffmpegBuilder.start();

            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("FFMPEG LOG ({}): {}", uniqueId, line); // 버퍼를 비워줘야 함
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("FFmpeg process exited with error code: " + exitCode);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            String objectKey = "merged/output-" + UUID.randomUUID() + "." + ext;
            String contentType = containsVideo ? "video/mp4" : "image/jpeg";
            String resultS3Url = uploadToS3(outputPath, objectKey, contentType);

            return new MergeResponse(resultS3Url);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } finally {
            // 작업 성공/실패 여부와 관계없이 항상 임시 폴더 삭제
            deleteDirectory(tempDir);
        }
    }

    public List<Map<Integer, Integer>> getCoordinate(int frameCut, int frameType) {
        List<Map<Integer, Integer>> coordinates = new ArrayList<>();

        switch (frameCut) {
            case 1: {
                Map<Integer, Integer> pos1 = new HashMap<>();
                pos1.put(40, 40);
                coordinates.add(pos1);
                break;
            }
            case 2: {
                switch (frameType) {
                    case 1: { // 1 * 2, 세로
                        Map<Integer, Integer> pos1 = new HashMap<>();
                        pos1.put(40, 40);
                        coordinates.add(pos1);

                        Map<Integer, Integer> pos2 = new HashMap<>();
                        pos2.put(40, 560);
                        coordinates.add(pos2);
                        break;
                    }
                    case 2: { // 2 * 1, 가로
                        Map<Integer, Integer> pos1 = new HashMap<>();
                        pos1.put(40, 40);
                        coordinates.add(pos1);

                        Map<Integer, Integer> pos2 = new HashMap<>();
                        pos2.put(720, 40);
                        coordinates.add(pos2);
                        break;
                    }
                    default:
                        throw new BusinessException(ErrorCode.NOT_FOUND);
                }
                break;
            }
            case 4: {
                switch (frameType) {
                    case 1: { // 1 * 4, 세로
                        Map<Integer, Integer> pos1 = new HashMap<>();
                        pos1.put(40, 40);
                        coordinates.add(pos1);

                        Map<Integer, Integer> pos2 = new HashMap<>();
                        pos2.put(40, 560);
                        coordinates.add(pos2);

                        Map<Integer, Integer> pos3 = new HashMap<>();
                        pos3.put(40, 1080);
                        coordinates.add(pos3);

                        Map<Integer, Integer> pos4 = new HashMap<>();
                        pos4.put(40, 1600);
                        coordinates.add(pos4);
                        break;
                    }
                    case 2: { // 4 * 1, 가로
                        Map<Integer, Integer> pos1 = new HashMap<>();
                        pos1.put(40, 40);
                        coordinates.add(pos1);

                        Map<Integer, Integer> pos2 = new HashMap<>();
                        pos2.put(720, 40);
                        coordinates.add(pos2);

                        Map<Integer, Integer> pos3 = new HashMap<>();
                        pos3.put(1400, 40);
                        coordinates.add(pos3);

                        Map<Integer, Integer> pos4 = new HashMap<>();
                        pos4.put(2080, 40);
                        coordinates.add(pos4);
                        break;
                    }
                    case 3: { // 2 * 2
                        Map<Integer, Integer> pos1 = new HashMap<>();
                        pos1.put(40, 40);
                        coordinates.add(pos1);

                        Map<Integer, Integer> pos2 = new HashMap<>();
                        pos2.put(720, 40);
                        coordinates.add(pos2);

                        Map<Integer, Integer> pos3 = new HashMap<>();
                        pos3.put(40, 560);
                        coordinates.add(pos3);

                        Map<Integer, Integer> pos4 = new HashMap<>();
                        pos4.put(720, 560);
                        coordinates.add(pos4);
                        break;
                    }
                    default:
                        throw new BusinessException(ErrorCode.NOT_FOUND);
                }
                break;
            }
            case 6: {
                switch (frameType) {
                    case 1: { // 2 * 3, 세로
                        Map<Integer, Integer> pos1 = new HashMap<>();
                        pos1.put(40, 40);
                        coordinates.add(pos1);

                        Map<Integer, Integer> pos2 = new HashMap<>();
                        pos2.put(720, 40);
                        coordinates.add(pos2);

                        Map<Integer, Integer> pos3 = new HashMap<>();
                        pos3.put(40, 560);
                        coordinates.add(pos3);

                        Map<Integer, Integer> pos4 = new HashMap<>();
                        pos4.put(720, 560);
                        coordinates.add(pos4);

                        Map<Integer, Integer> pos5 = new HashMap<>();
                        pos5.put(40, 1080);
                        coordinates.add(pos5);

                        Map<Integer, Integer> pos6 = new HashMap<>();
                        pos6.put(720, 1080);
                        coordinates.add(pos6);
                        break;
                    }
                    case 2: { // 3 * 2, 가로
                        Map<Integer, Integer> pos1 = new HashMap<>();
                        pos1.put(40, 40);
                        coordinates.add(pos1);

                        Map<Integer, Integer> pos2 = new HashMap<>();
                        pos2.put(720, 40);
                        coordinates.add(pos2);

                        Map<Integer, Integer> pos3 = new HashMap<>();
                        pos3.put(1400, 40);
                        coordinates.add(pos3);

                        Map<Integer, Integer> pos4 = new HashMap<>();
                        pos4.put(40, 560);
                        coordinates.add(pos4);

                        Map<Integer, Integer> pos5 = new HashMap<>();
                        pos5.put(720, 560);
                        coordinates.add(pos5);

                        Map<Integer, Integer> pos6 = new HashMap<>();
                        pos6.put(1400, 560);
                        coordinates.add(pos6);
                        break;
                    }
                    default:
                        throw new BusinessException(ErrorCode.NOT_FOUND);
                }
                break;
            }
            default:
                throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        return coordinates;
    }

    private ProcessBuilder buildFfmpegCommand(String framePath, List<String> cutPaths,
        List<Map<Integer, Integer>> positions, String outputPath, boolean containsVideo) {

        List<String> command = new ArrayList<>();
        command.add("ffmpeg");

        command.add("-i");
        command.add(framePath);
        for (String cut : cutPaths) {
            command.add("-i");
            command.add(cut);
        }

        StringBuilder filter = new StringBuilder();
        for (int i = 0; i < cutPaths.size(); i++) {
            filter.append("[").append(i + 1).append(":v]scale=640:480[v").append(i).append("];");
        }
        String lastOutput = "[0:v]";
        for (int i = 0; i < cutPaths.size(); i++) {
            String currentOverlayInput = "[v" + i + "]";
            String nextOutput = (i == cutPaths.size() - 1) ? "[out]" : "[bg" + i + "]";
            filter.append(lastOutput).append(currentOverlayInput)
                .append("overlay=").append(getX(positions.get(i))).append(":")
                .append(getY(positions.get(i)))
                .append(nextOutput).append(";");
            lastOutput = nextOutput;
        }
        if (filter.length() > 0) {
            filter.setLength(filter.length() - 1);
        }

        command.add("-filter_complex");
        command.add(filter.toString());

        command.add("-map");
        command.add("[out]");

        if (containsVideo) {
            command.add("-c:v");
            command.add("libx264");
            command.add("-pix_fmt");
            command.add("yuv420p");
        } else {
            command.add("-frames:v");
            command.add("1");
        }

        command.add("-y");
        command.add(outputPath);

        return new ProcessBuilder(command);
    }

    private int getX(Map<Integer, Integer> pos) {
        return pos.entrySet().iterator().next().getKey();
    }

    private int getY(Map<Integer, Integer> pos) {
        return pos.entrySet().iterator().next().getValue();
    }


    private String downloadFromS3(String s3Url, String fileName, String tempDirPath) {
        String bucket = extractBucket(s3Url);
        String key = extractKey(s3Url);
        String ext = extractExt(s3Url);

        File file = new File(tempDirPath, fileName + ext); // 생성자 인자 활용
        file.getParentFile().mkdirs();

        var responseBytes = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build(),
            ResponseTransformer.toBytes());

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(responseBytes.asByteArray());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }

        return file.getAbsolutePath();
    }

    private String uploadToS3(String localPath, String s3Key, String contentType) {
        File file = new File(localPath);
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(contentType)
                .build(),
            RequestBody.fromFile(file));

        String region = s3Client.serviceClientConfiguration().region().id();
        String objectUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region,
            s3Key);

        return objectUrl;
    }

    private String extractBucket(String s3Url) {
        if (!s3Url.startsWith("s3://")) {
            throw new IllegalArgumentException("Invalid S3 URL format");
        }
        String withoutPrefix = s3Url.substring(5);
        return withoutPrefix.substring(0, withoutPrefix.indexOf('/'));
    }

    private String extractKey(String s3Url) {
        if (!s3Url.startsWith("s3://")) {
            throw new IllegalArgumentException("Invalid S3 URL format");
        }
        String withoutPrefix = s3Url.substring(5);
        return withoutPrefix.substring(withoutPrefix.indexOf('/') + 1);
    }

    private String extractExt(String s3Url) {
        if (!s3Url.startsWith("s3://")) {
            throw new IllegalArgumentException("Invalid S3 URL format");
        }

        String[] parts = s3Url.split("/");
        String fileName = parts[parts.length - 1];

        int lastDot = fileName.lastIndexOf(".");
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            throw new IllegalArgumentException("File has no extension: " + fileName);
        }

        return fileName.substring(lastDot);
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}