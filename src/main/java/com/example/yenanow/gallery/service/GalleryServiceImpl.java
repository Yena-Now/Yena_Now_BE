package com.example.yenanow.gallery.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.common.util.UuidUtil;
import com.example.yenanow.film.entity.Frame;
import com.example.yenanow.film.repository.FrameRepository;
import com.example.yenanow.gallery.dto.request.CreateNcutRelayRequest;
import com.example.yenanow.gallery.dto.request.CreateNcutRequest;
import com.example.yenanow.gallery.dto.request.CreateRelayNcutRequest;
import com.example.yenanow.gallery.dto.request.UpdateNcutContentRequest;
import com.example.yenanow.gallery.dto.request.UpdateNcutVisibilityRequest;
import com.example.yenanow.gallery.dto.request.UpdateRelayRequest;
import com.example.yenanow.gallery.dto.response.MyGalleryResponse;
import com.example.yenanow.gallery.dto.response.NcutDetailResponse;
import com.example.yenanow.gallery.dto.response.NcutLikeResponse;
import com.example.yenanow.gallery.dto.response.NcutLikesResponse;
import com.example.yenanow.gallery.dto.response.NcutLikesResponseItem;
import com.example.yenanow.gallery.dto.response.NcutRelayListResponse;
import com.example.yenanow.gallery.dto.response.RelayListItem;
import com.example.yenanow.gallery.dto.response.UpdateNcutContentResponse;
import com.example.yenanow.gallery.dto.response.UpdateNcutVisibilityResponse;
import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.entity.NcutLike;
import com.example.yenanow.gallery.entity.Relay;
import com.example.yenanow.gallery.entity.RelayCut;
import com.example.yenanow.gallery.entity.RelayParticipant;
import com.example.yenanow.gallery.entity.Visibility;
import com.example.yenanow.gallery.repository.NcutLikeRepository;
import com.example.yenanow.gallery.repository.NcutRepository;
import com.example.yenanow.gallery.repository.RelayRepository;
import com.example.yenanow.s3.service.S3Service;
import com.example.yenanow.s3.util.S3KeyFactory;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.FollowRepository;
import com.example.yenanow.users.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GalleryServiceImpl implements GalleryService {

    private final NcutRepository ncutRepository;
    private final NcutLikeRepository ncutLikeRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FrameRepository frameRepository;
    private final RelayRepository relayRepository;
    private final StringRedisTemplate redisTemplate;
    private final NcutCountSyncService ncutCountSyncService;
    private final S3KeyFactory s3KeyFactory;
    private final S3Service s3Service;

    @Override
    public MyGalleryResponse getMyGallery(String userUuid, int pageNum, int display) {
        validateUserUuid(userUuid);

        Pageable pageable = PageRequest.of(pageNum, display);
        Page<Ncut> ncutPage = ncutRepository.findByUserUserUuid(userUuid, pageable);

        // 데이터가 없으면 빈 응답 반환
        if (ncutPage.isEmpty()) {
            return MyGalleryResponse.builder()
                .totalPages(0)
                .ncuts(List.of())
                .build();
        }
        return MyGalleryResponse.fromEntity(ncutPage);
    }

    @Override
    public MyGalleryResponse getOtherGallery(String userUuid, int pageNum, int display) {
        validateUserUuid(userUuid);

        Pageable pageable = PageRequest.of(pageNum, display);
        Page<Ncut> ncutPage = ncutRepository.findByUserUserUuidAndVisibility(
            userUuid, Visibility.PUBLIC, pageable
        );

        if (ncutPage.isEmpty()) {
            return MyGalleryResponse.builder()
                .totalPages(0)
                .ncuts(List.of())
                .build();
        }
        return MyGalleryResponse.fromEntity(ncutPage);
    }

    @Override
    public MyGalleryResponse getPublicGallery(int pageNum, int display) {
        Pageable pageable = PageRequest.of(pageNum, display);
        Page<Ncut> ncutPage = ncutRepository.findPublicGalleryWithUser(pageable);

        if (ncutPage.isEmpty()) {
            return MyGalleryResponse.builder()
                .totalPages(0)
                .ncuts(List.of())
                .build();
        }
        return MyGalleryResponse.fromEntityWithUser(ncutPage);
    }

    @Override
    public MyGalleryResponse getFollowingsGallery(String userUuid, int pageNum, int display) {
        validateUserUuid(userUuid);

        List<String> followingUuids = followRepository.findFollowingUuids(userUuid);
        if (followingUuids.isEmpty()) {
            // 팔로잉한 유저가 아예 없는 경우도 정상 처리 (빈 결과 반환)
            return MyGalleryResponse.builder()
                .totalPages(0)
                .ncuts(List.of())
                .build();
        }

        List<Visibility> allowedVisibilities = List.of(Visibility.PUBLIC, Visibility.FOLLOW);
        Pageable pageable = PageRequest.of(pageNum, display);
        Page<Ncut> ncutPage = ncutRepository.findFollowingsGalleryWithUser(
            followingUuids, allowedVisibilities, pageable
        );

        if (ncutPage.isEmpty()) {
            return MyGalleryResponse.builder()
                .totalPages(0)
                .ncuts(List.of())
                .build();
        }
        return MyGalleryResponse.fromEntityWithUser(ncutPage);
    }

    @Override
    public NcutDetailResponse getNcut(String userUuid, String ncutUuid) {
        NcutDetailResponse ncutDetailResponse = ncutRepository.findNcutById(ncutUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NCUT));

        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        String key = "ncut:" + ncutUuid;
        Map<String, Object> ncutData = hashOps.entries(key);
        Object likeValue = ncutData.get("like_count");
        Object commentValue = ncutData.get("comment_count");
        Integer likeCount = (likeValue != null) ? Integer.parseInt(likeValue.toString()) : 0;
        Integer commentCount =
            (commentValue != null) ? Integer.parseInt(commentValue.toString()) : 0;
        Boolean isMine = ncutDetailResponse.getUserUuid().equals(userUuid);

        return NcutDetailResponse.builder()
            .ncutUuid(ncutDetailResponse.getNcutUuid())
            .ncutUrl(ncutDetailResponse.getNcutUrl())
            .userUuid(ncutDetailResponse.getUserUuid())
            .nickname(ncutDetailResponse.getNickname())
            .profileUrl(ncutDetailResponse.getProfileUrl())
            .content(ncutDetailResponse.getContent())
            .createdAt(ncutDetailResponse.getCreatedAt())
            .isRelay(ncutDetailResponse.getIsRelay())
            .visibility(ncutDetailResponse.getVisibility())
            .likeCount(likeCount)
            .commentCount(commentCount)
            .isMine(isMine)
            .build();
    }

    @Override
    @Transactional
    public void deleteNcut(String userUuid, String ncutUuid) {
        Ncut ncut = ncutRepository.findById(ncutUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NCUT));
        if (!ncut.getUser().getUserUuid().equals(userUuid)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        ncutRepository.delete(ncut);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                String userKey = "user:" + userUuid;
                Integer totalCut = UuidUtil.incrementCounter(redisTemplate, userKey, "total_cut",
                    -1).intValue();
                ncutCountSyncService.syncTotalCutToDB(userUuid, totalCut);

                String ncutKey = "ncut:" + ncutUuid;
                redisTemplate.delete(ncutKey);
            }
        });
    }

    @Override
    @Transactional
    public UpdateNcutContentResponse updateNcutContent(String userUuid, String ncutUuid,
        UpdateNcutContentRequest updateNcutContentRequest) {
        Ncut ncut = ncutRepository.findById(ncutUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NCUT));
        if (!ncut.getUser().getUserUuid().equals(userUuid)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        ncut.setContent(updateNcutContentRequest.getContent());

        return UpdateNcutContentResponse.builder()
            .ncutUuid(ncut.getNcutUuid())
            .content(ncut.getContent())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Override
    @Transactional
    public UpdateNcutVisibilityResponse updateNcutVisibility(String userUuid, String ncutUuid,
        UpdateNcutVisibilityRequest updateNcutVisibilityRequest) {
        Ncut ncut = ncutRepository.findById(ncutUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NCUT));
        if (!ncut.getUser().getUserUuid().equals(userUuid)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        ncut.setVisibility(updateNcutVisibilityRequest.getVisibility());

        return UpdateNcutVisibilityResponse.builder()
            .ncutUuid(ncut.getNcutUuid())
            .visibility(ncut.getVisibility())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Override
    public NcutLikesResponse getNcutLikes(String userUuid, String ncutUuid, int pageNum,
        int display) {
        boolean isLiked = ncutLikeRepository.existsByNcutNcutUuidAndUserUserUuid(ncutUuid,
            userUuid);
        Pageable pageable = PageRequest.of(pageNum, display);
        Page<NcutLikesResponseItem> ncutLikesResponseItem = ncutLikeRepository.findNcutLikeByNcutUuid(
            ncutUuid, pageable).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        return NcutLikesResponse.builder()
            .isLiked(isLiked)
            .likeCount(Long.valueOf(ncutLikesResponseItem.getTotalElements()).intValue())
            .likes(ncutLikesResponseItem.getContent())
            .build();
    }

    @Override
    @Transactional
    public NcutLikeResponse createNcutLike(String userUuid, String ncutUuid) {
        if (!ncutRepository.existsById(ncutUuid)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_NCUT);
        }

        if (ncutLikeRepository.existsByNcutNcutUuidAndUserUserUuid(ncutUuid, userUuid)) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS);
        }

        Ncut ncutProxy = ncutRepository.getReferenceById(ncutUuid);
        User userProxy = userRepository.getReferenceById(userUuid);
        NcutLike ncutLike = NcutLike.builder()
            .ncut(ncutProxy)
            .user(userProxy)
            .build();
        ncutLikeRepository.save(ncutLike);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                String key = "ncut:" + ncutUuid;
                Integer likeCount = UuidUtil.incrementCounter(redisTemplate, key, "like_count", 1)
                    .intValue();
                ncutCountSyncService.syncLikeCountToDB(ncutUuid, likeCount);
            }
        });

        long currentLikeCount = ncutLikeRepository.countByNcutNcutUuid(ncutUuid);

        return NcutLikeResponse.builder()
            .isLiked(true)
            .likeCount((int) currentLikeCount)
            .build();
    }

    @Override
    @Transactional
    public NcutLikeResponse deleteNcutLike(String userUuid, String ncutUuid) {
        NcutLike ncutLike = ncutLikeRepository.findByNcutNcutUuidAndUserUserUuid(ncutUuid, userUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        ncutLikeRepository.delete(ncutLike);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                String key = "ncut:" + ncutUuid;
                Integer likeCount = UuidUtil.incrementCounter(redisTemplate, key, "like_count", -1)
                    .intValue();
                ncutCountSyncService.syncLikeCountToDB(ncutUuid, likeCount);
            }
        });

        long currentLikeCount = ncutLikeRepository.countByNcutNcutUuid(ncutUuid);

        return NcutLikeResponse.builder()
            .isLiked(false)
            .likeCount((int) currentLikeCount)
            .build();
    }

    @Override
    @Transactional
    public NcutDetailResponse createNcut(String userUuid, CreateNcutRequest createNcutRequest) {
        Ncut createdNcut = createAndSaveNcut(userUuid, createNcutRequest);
        updateUserNcutCount(userUuid, createdNcut.getNcutUuid());

        return buildNcutDetailResponse(createdNcut);
    }

    @Override
    @Transactional
    public void createNcutRelay(String userUuid, CreateNcutRelayRequest createNcutRelayRequest) {
        User creator = userRepository.getReferenceById(userUuid);
        Frame frame = frameRepository.getReferenceById(createNcutRelayRequest.getFrameUuid());

        Relay relay = Relay.builder()
            .timeLimit(createNcutRelayRequest.getTimeLimit())
            .takeCount(createNcutRelayRequest.getTakeCount())
            .cutCount(createNcutRelayRequest.getCutCount())
            .backgroundUrl(
                s3KeyFactory.extractKeyFromUrl(createNcutRelayRequest.getBackgroundUrl()))
            .expiredAt(LocalDateTime.now().plusDays(7))
            .user(creator)
            .frame(frame)
            .build();

        RelayParticipant creatorParticipant = RelayParticipant.builder()
            .user(creator)
            .build();
        relay.addParticipant(creatorParticipant);

        createNcutRelayRequest.getParticipants().forEach(participantItem -> {
            User participantUser = userRepository.getReferenceById(participantItem.getUserUuid());
            RelayParticipant participant = RelayParticipant.builder()
                .user(participantUser)
                .build();
            relay.addParticipant(participant);
        });

        createNcutRelayRequest.getCuts().forEach(cutItem -> {
            String cutKey = null;
            String originalUrl = cutItem.getCutUrl();

            if (originalUrl != null && !originalUrl.isBlank()) {
                cutKey = s3KeyFactory.extractKeyFromUrl(originalUrl);
            }

            RelayCut cut = RelayCut.builder()
                .cutUrl(cutKey)
                .cutIndex(Integer.parseInt(cutItem.getCutIndex()))
                .isTaken(cutItem.getIsTaken())
                .build();

            relay.addCut(cut);
        });

        relayRepository.save(relay);
    }

    @Override
    public NcutRelayListResponse getRelayList(String userUuid, int pageNum, int display) {
        Pageable pageable = PageRequest.of(pageNum, display);
        Page<Relay> relayPage = relayRepository.findByUserUuid(userUuid,
            pageable);

        List<RelayListItem> relayListItems = relayPage.getContent().stream()
            .map(relay -> RelayListItem.fromEntity(relay, s3Service))
            .toList();

        return NcutRelayListResponse.builder()
            .totalPages(relayPage.getTotalPages())
            .relay(relayListItems)
            .build();
    }

    @Override
    @Transactional
    public NcutDetailResponse createRelayNcut(String userUuid,
        CreateRelayNcutRequest createRelayNcutRequest) {
        Relay relay = relayRepository.findById(createRelayNcutRequest.getRelayUuid())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        CreateNcutRequest createNcutRequest = new CreateNcutRequest(
            createRelayNcutRequest.getNcutUrl(),
            createRelayNcutRequest.getThumbnailUrl(),
            createRelayNcutRequest.getContent(),
            createRelayNcutRequest.getVisibility(),
            true
        );

        Ncut mainCreatedNcut = createAndSaveNcut(userUuid, createNcutRequest);
        updateUserNcutCount(userUuid, mainCreatedNcut.getNcutUuid());

        List<RelayParticipant> allParticipants = relay.getParticipants();

        for (RelayParticipant participant : allParticipants) {
            User participantUser = participant.getUser();

            if (!participantUser.getUserUuid().equals(userUuid)) {

                CreateNcutRequest placeholderNcutRequest = new CreateNcutRequest(
                    createRelayNcutRequest.getNcutUrl(),
                    createRelayNcutRequest.getThumbnailUrl(),
                    null,
                    Visibility.PRIVATE,
                    true
                );

                Ncut createdNcut = createAndSaveNcut(participantUser.getUserUuid(),
                    placeholderNcutRequest);
                updateUserNcutCount(participantUser.getUserUuid(), createdNcut.getNcutUuid());
            }
        }

        relayRepository.delete(relay);

        return buildNcutDetailResponse(mainCreatedNcut);
    }

    @Override
    @Transactional
    public void updateRelay(String userUuid, UpdateRelayRequest updateRelayRequest) {

    }

    private Ncut createAndSaveNcut(String userUuid, CreateNcutRequest createNcutRequest) {
        User userProxy = userRepository.getReferenceById(userUuid);

        Ncut ncut = Ncut.builder()
            .ncutUrl(s3KeyFactory.extractKeyFromUrl(createNcutRequest.getNcutUrl()))
            .thumbnailUrl(s3KeyFactory.extractKeyFromUrl(createNcutRequest.getThumbnailUrl()))
            .content(createNcutRequest.getContent())
            .visibility(createNcutRequest.getVisibility())
            .isRelay(createNcutRequest.getIsRelay())
            .user(userProxy)
            .likeCount(0)
            .commentCount(0)
            .build();

        Ncut createdNcut = ncutRepository.save(ncut);

        return createdNcut;
    }

    private void updateUserNcutCount(String userUuid, String ncutUuid) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                String userKey = "user:" + userUuid;
                Integer totalCut = UuidUtil.incrementCounter(redisTemplate, userKey, "total_cut", 1)
                    .intValue();
                ncutCountSyncService.syncTotalCutToDB(userUuid, totalCut);

                String ncutKey = "ncut:" + ncutUuid;
                HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
                Map<String, String> ncutDataMap = new HashMap<>();
                ncutDataMap.put("like_count", "0");
                ncutDataMap.put("comment_count", "0");
                hashOps.putAll(ncutKey, ncutDataMap);
            }
        });
    }

    private NcutDetailResponse buildNcutDetailResponse(Ncut ncut) {
        User user = ncut.getUser();

        return NcutDetailResponse.builder()
            .ncutUuid(ncut.getNcutUuid())
            .ncutUrl(s3Service.getFileUrl(ncut.getNcutUrl()))
            .userUuid(user.getUserUuid())
            .nickname(user.getNickname())
            .profileUrl(s3Service.getFileUrl(user.getProfileUrl()))
            .content(ncut.getContent())
            .createdAt(ncut.getCreatedAt())
            .isRelay(ncut.isRelay())
            .visibility(ncut.getVisibility())
            .likeCount(ncut.getLikeCount())
            .commentCount(ncut.getCommentCount())
            .isMine(true)
            .build();
    }

    private void validateUserUuid(String userUuid) {
        if (userUuid == null || userUuid.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }
}
