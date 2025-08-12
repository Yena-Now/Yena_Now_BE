package com.example.yenanow.film.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.film.dto.request.MergeRequest;
import com.example.yenanow.film.dto.request.MergeRequestItem;
import com.example.yenanow.film.dto.response.BackgroundListResponse;
import com.example.yenanow.film.dto.response.FrameListResponseItem;
import com.example.yenanow.film.dto.response.MergeResponse;
import com.example.yenanow.film.dto.response.StickerListResponse;
import com.example.yenanow.film.dto.response.StickerListResponseItem;
import com.example.yenanow.film.entity.Background;
import com.example.yenanow.film.entity.Frame;
import com.example.yenanow.film.entity.Sticker;
import com.example.yenanow.film.repository.BackgroundRepository;
import com.example.yenanow.film.repository.FrameRepository;
import com.example.yenanow.film.repository.StickerRepository;
import com.example.yenanow.s3.service.S3Service;
import com.example.yenanow.s3.util.S3KeyFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
    private final S3Service s3Service;
    private final S3Client s3Client;
    private final S3KeyFactory s3KeyFactory;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Override
    public void createBackground(String s3Key) {
        require(s3Key, "s3Key");
        Background background = new Background();
        background.setBackgroundUrl(s3Key);
        backgroundRepository.save(background);
    }

    @Override
    public List<FrameListResponseItem> getFrames(int frameCut) {
        List<FrameListResponseItem> frames = frameRepository.findByFrameCut(frameCut);

        return frames.stream()
            .map(frame -> {
                String objectUrl = s3Service.getFileUrl(frame.getFrameUrl());

                return FrameListResponseItem.builder()
                    .frameUuid(frame.getFrameUuid())
                    .frameName(frame.getFrameName())
                    .frameUrl(objectUrl)
                    .frameCut(frame.getFrameCut())
                    .frameType(frame.getFrameType())
                    .build();
            })
            .toList();
    }

    @Override
    public StickerListResponse getStickers(Pageable pageable) {
        Page<Sticker> stickerPage = stickerRepository.findAll(pageable);

        List<StickerListResponseItem> stickerListResponseItems = stickerPage.getContent().stream()
            .map(sticker -> StickerListResponseItem.builder()
                .stickerUuid(sticker.getStickerUuid())
                .stickerName(sticker.getStickerName())
                .stickerUrl(s3Service.getFileUrl(sticker.getStickerUrl())) // Key → URL 변환
                .build())
            .toList();

        return StickerListResponse.builder()
            .totalPages(stickerPage.getTotalPages())
            .stickers(stickerListResponseItems)
            .build();
    }

    @Override
    public List<BackgroundListResponse> getBackgrounds() {
        return backgroundRepository.findAll().stream()
            .map(background -> BackgroundListResponse.builder()
                .backgroundUuid(background.getBackgroundUuid())
                .backgroundUrl(s3Service.getFileUrl(background.getBackgroundUrl())) // Key → URL 변환
                .build())
            .toList();
    }

    @Async("taskExecutor")
    @Override
    public CompletableFuture<MergeResponse> createMergedOutput(MergeRequest request,
        String userUuid) {
        String frameUuid = request.getFrameUuid();
        String roomCode = request.getRoomCode();
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
            String framePath = downloadFromS3(bucketName, frameKey, "frame", tempDirPath);

            List<String> cutPaths = new ArrayList<>();
            for (int i = 0; i < contentUrls.size(); i++) {
                String httpUrlString = contentUrls.get(i).getContentUrl();
                String cutKey = s3KeyFactory.extractKeyFromUrl(httpUrlString);
                cutPaths.add(downloadFromS3(bucketName, cutKey, "cut" + i, tempDirPath));
            }

            boolean containsVideo = cutPaths.stream()
                .anyMatch(path -> path.endsWith(".mp4") || path.endsWith(".mov") || path.endsWith(
                    ".webm"));
            String ext = containsVideo ? "mp4" : "jpg";
            String outputFileName = "output." + ext;
            String outputPath = tempDirPath + File.separator + outputFileName;

            ProcessBuilder ffmpegBuilder = buildFfmpegCommand(framePath, cutPaths, coordinates,
                outputPath, containsVideo);
            ffmpegBuilder.redirectErrorStream(true);
            Process process = ffmpegBuilder.start();

            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) { // 버퍼를 비워줘야 함
                    log.debug("FFMPEG LOG ({}): {}", uniqueId, line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.error("FFmpeg process exited with error code: {}", exitCode);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            String objectKey = s3KeyFactory.createKey("ncut", outputFileName, userUuid, null,
                roomCode);
            String contentType = containsVideo ? "video/mp4" : "image/jpeg";
            String resultS3Url = uploadToS3(outputPath, objectKey, contentType);

            return CompletableFuture.completedFuture(new MergeResponse(resultS3Url));

        } catch (Exception e) {
            log.error("N컷 합성 중 오류가 발생했습니다", e);
            return CompletableFuture.failedFuture(e); // 실패 시 실패한 Future 반환
        } finally {
            // 항상 임시 폴더 삭제
            deleteDirectory(tempDir);
        }
    }

    // 프레임 컷 수, 프레임 타입으로 각 컷 배치할 좌표 구하는 메서드
    public List<Map<Integer, Integer>> getCoordinate(int frameCut, int frameType) {
        return switch (frameCut) {
            case 1 -> List.of(Map.of(40, 40));
            case 2 -> switch (frameType) {
                case 1 -> List.of(Map.of(40, 40), Map.of(40, 560)); // 1x2 세로
                case 2 -> List.of(Map.of(40, 40), Map.of(720, 40)); // 2x1 가로
                default -> throw new BusinessException(ErrorCode.NOT_FOUND);
            };
            case 4 -> switch (frameType) {
                case 1 -> List.of( // 1x4 세로
                    Map.of(40, 40), Map.of(40, 560),
                    Map.of(40, 1080), Map.of(40, 1600)
                );
                case 2 -> List.of( // 4x1 가로
                    Map.of(40, 40), Map.of(720, 40),
                    Map.of(1400, 40), Map.of(2080, 40)
                );
                case 3 -> List.of( // 2x2
                    Map.of(40, 40), Map.of(720, 40),
                    Map.of(40, 560), Map.of(720, 560)
                );
                default -> throw new BusinessException(ErrorCode.NOT_FOUND);
            };
            case 6 -> switch (frameType) {
                case 1 -> List.of( // 2x3 세로
                    Map.of(40, 40), Map.of(720, 40),
                    Map.of(40, 560), Map.of(720, 560),
                    Map.of(40, 1080), Map.of(720, 1080)
                );
                case 2 -> List.of( // 3x2 가로
                    Map.of(40, 40), Map.of(720, 40), Map.of(1400, 40),
                    Map.of(40, 560), Map.of(720, 560), Map.of(1400, 560)
                );
                default -> throw new BusinessException(ErrorCode.NOT_FOUND);
            };
            default -> throw new BusinessException(ErrorCode.NOT_FOUND);
        };
    }

    // FFmpeg 명령어 반환 메서드
    private ProcessBuilder buildFfmpegCommand(
        String framePath,
        List<String> cutPaths,
        List<Map<Integer, Integer>> positions,
        String outputPath,
        boolean containsVideo) {

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

        String lastOutput = "[0:v]"; // 시작은 프레임(배경 역할)
        for (int i = 0; i < cutPaths.size(); i++) {
            String currentOverlayInput = "[v" + i + "]";
            String nextOutput = (i == cutPaths.size() - 1) ? "[cuts_on_frame]" : "[bg" + i + "]";
            filter.append(lastOutput).append(currentOverlayInput)
                .append("overlay=").append(getX(positions.get(i))).append(":")
                .append(getY(positions.get(i)))
                .append(nextOutput).append(";");
            lastOutput = nextOutput;
        }

        // 프레임을 한 번 더 오버레이
        filter.append("[cuts_on_frame][0:v]overlay=0:0[out]");

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


    private String downloadFromS3(String bucket, String key, String fileName, String tempDirPath) {
        String ext = "";
        int lastDot = key.lastIndexOf('.');
        if (lastDot >= 0) {
            ext = key.substring(lastDot);
        }

        File file = new File(tempDirPath, fileName + ext);
        s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build(),
            ResponseTransformer.toFile(file));
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

    private void require(String v, String name) {
        if (v == null || v.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }
}