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
import com.example.yenanow.s3.util.S3KeyFactory;
import java.io.BufferedReader;
import java.io.File;
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
    private final S3KeyFactory s3KeyFactory;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

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

    @Async("taskExecutor")
    @Override
    public CompletableFuture<MergeResponse> createMergedOutput(MergeRequest request,
        String userUuid) {
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

            String objectKey = s3KeyFactory.createKey("ncut", outputFileName, userUuid, null);
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

    // FFmpeg 명령어 반환 메서드
    private ProcessBuilder buildFfmpegCommand(
        String framePath,
        List<String> cutPaths,
        List<Map<Integer, Integer>> positions,
        String outputPath,
        boolean containsVideo) {

        List<String> command = new ArrayList<>();
        command.add("ffmpeg");

        // FFmpeg는 입력 순서에 따라 [0:v], [1:v], [2:v], .. 와 같이 스트림을 식별
        command.add("-i");
        command.add(framePath);
        for (String cut : cutPaths) {
            command.add("-i");
            command.add(cut);
        }

        // 각 컷([1:v], [2:v], ...)을 640x480로 크기 조절 후 임시 이름 부여(v0, v1, ...)
        StringBuilder filter = new StringBuilder();
        for (int i = 0; i < cutPaths.size(); i++) {
            filter.append("[").append(i + 1).append(":v]scale=640:480[v").append(i).append("];");
        }

        // 프레임([0:v]) 위에 컷(v0)을 겹치고 그 위에 다시 다음 컷(v1)을 겹치는 과정을 반복
        String lastOutput = "[0:v]";
        for (int i = 0; i < cutPaths.size(); i++) {
            String currentOverlayInput = "[v" + i + "]";
            // 최종 결과 스트림의 이름을 [out]으로 지정
            String nextOutput = (i == cutPaths.size() - 1) ? "[out]" : "[bg" + i + "]";
            filter.append(lastOutput).append(currentOverlayInput)
                .append("overlay=").append(getX(positions.get(i))).append(":")
                .append(getY(positions.get(i)))
                .append(nextOutput).append(";");
            lastOutput = nextOutput;
        }

        // 불필요한 세미콜론 제거
        if (filter.length() > 0) {
            filter.setLength(filter.length() - 1);
        }

        // 생성된 필터 그래프를 FFmpeg 명령어에 추가
        command.add("-filter_complex");
        command.add(filter.toString());

        // 최종 출력물로 사용할 스트림을 지정 (여기선 [out])
        command.add("-map");
        command.add("[out]");

        // 비디오 포함 여부에 따라 인코딩 옵션을 다르게 설정
        if (containsVideo) {
            command.add("-c:v");
            command.add("libx264");
            command.add("-pix_fmt");
            command.add("yuv420p");
        } else {
            command.add("-frames:v");
            command.add("1");
        }

        // 동일 output 파일 있으면 그냥 덮어쓰도록 -y옵션
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
}