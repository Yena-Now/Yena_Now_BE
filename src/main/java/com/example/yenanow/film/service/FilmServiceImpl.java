package com.example.yenanow.film.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.film.dto.request.MergeRequest;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {

    private final FrameRepository frameRepository;
    private final StickerRepository stickerRepository;
    private final BackgroundRepository backgroundRepository;

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

    @Override
    public MergeResponse createMergedOutput(MergeRequest request) {
        String frameUuid = request.getFrameUuid();
        Frame frame = frameRepository.findById(frameUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        int frameCut = frame.getFrameCut();
        int frameType = frame.getFrameType();

        return null;
    }

    // 프레임 크기 구하는 메서드
    public Map<Integer, Integer> getFrameSize(int frameCut, int frameType) {
        return null;
    }

    // 프레임에 merge할 좌표 구하는 메서드
    public List<Map<Integer, Integer>> getCoordinate(int frameCut, int frameType) {
        List<Map<Integer, Integer>> coordinates = new ArrayList<>();

        switch (frameCut) {
            case 1 -> {
                Map<Integer, Integer> pos1 = new HashMap<>();
                pos1.put(40, 40);
                coordinates.add(pos1);
            }
            case 2 -> {
                switch (frameType) {
                    case 1 -> { // 1 * 2, 세로
                        Map<Integer, Integer> pos1 = new HashMap<>();
                        pos1.put(40, 40);
                        coordinates.add(pos1);

                        Map<Integer, Integer> pos2 = new HashMap<>();
                        pos2.put(40, 560);
                        coordinates.add(pos2);
                    }
                    case 2 -> { // 2 * 1, 가로
                        Map<Integer, Integer> pos1 = new HashMap<>();
                        pos1.put(40, 40);
                        coordinates.add(pos1);

                        Map<Integer, Integer> pos2 = new HashMap<>();
                        pos2.put(720, 40);
                        coordinates.add(pos2);
                    }
                    default -> throw new BusinessException(ErrorCode.NOT_FOUND);
                }
            }
            case 4 -> {
                switch (frameType) {
                    case 1 -> { // 1 * 4, 세로
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
                    }
                    case 2 -> { // 4 * 1, 가로
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
                    }
                    case 3 -> { // 2 * 2
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
                    }
                    default -> throw new BusinessException(ErrorCode.NOT_FOUND);
                }
            }
            case 6 -> {
                switch (frameType) {
                    case 1 -> { // 2 * 3, 세로
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
                    }
                    case 2 -> { // 3 * 2, 가로
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
                    }
                    default -> throw new BusinessException(ErrorCode.NOT_FOUND);
                }
            }
            default -> throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        return coordinates;
    }
}
