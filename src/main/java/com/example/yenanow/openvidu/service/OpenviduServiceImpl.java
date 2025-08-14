package com.example.yenanow.openvidu.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.common.util.UuidUtil;
import com.example.yenanow.gallery.entity.Relay;
import com.example.yenanow.gallery.entity.RelayCut;
import com.example.yenanow.gallery.repository.RelayCutRepository;
import com.example.yenanow.gallery.repository.RelayRepository;
import com.example.yenanow.openvidu.dto.request.CodeRequest;
import com.example.yenanow.openvidu.dto.request.TokenRelayRequest;
import com.example.yenanow.openvidu.dto.request.TokenRequest;
import com.example.yenanow.openvidu.dto.response.CodeResponse;
import com.example.yenanow.openvidu.dto.response.TokenResponse;
import com.example.yenanow.s3.service.S3Service;
import com.example.yenanow.s3.util.S3KeyFactory;
import com.example.yenanow.users.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import io.livekit.server.WebhookReceiver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import livekit.LivekitWebhook.WebhookEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;

@Service
@RequiredArgsConstructor
public class OpenviduServiceImpl implements OpenviduService {

    @Value("${livekit.api.key}")
    private String LIVEKIT_API_KEY;

    @Value("${livekit.api.secret}")
    private String LIVEKIT_API_SECRET;

    private final UserRepository userRepository;
    private final RelayRepository relayRepository;
    private final RelayCutRepository relayCutRepository;
    private final StringRedisTemplate redisTemplate;
    private final S3Service s3Service;
    private final S3KeyFactory s3KeyFactory;
    private final ObjectMapper objectMapper;

    @Override
    public CodeResponse createCode(String userUuid, CodeRequest codeRequest) {
        UuidUtil.validateUuid(userUuid);

        String nickname = userRepository.findNicknameById(userUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        Random random = new Random();
        String roomCode;

        while (true) {
            roomCode = String.format("%06d", random.nextInt(999999));
            String key = "room:" + roomCode;

            if (!redisTemplate.hasKey(key)) {
                HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();

                Map<String, String> roomDataMap = new HashMap<>();
                roomDataMap.put("background_url",
                    s3KeyFactory.extractKeyFromUrl(codeRequest.getBackgroundUrl()));
                roomDataMap.put("take_count", String.valueOf(codeRequest.getTakeCount()));
                roomDataMap.put("cut_count", String.valueOf(codeRequest.getCutCount()));
                roomDataMap.put("time_limit", String.valueOf(codeRequest.getTimeLimit()));
                roomDataMap.put("cuts", "[]");

                hashOps.putAll(key, roomDataMap);
                break;
            }
        }

        AccessToken token = new AccessToken(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
        token.setName(nickname);
        token.setIdentity(userUuid);
        token.addGrants(new RoomJoin(true), new RoomName(roomCode));

        return new CodeResponse(roomCode, token.toJwt());
    }

    @Override
    public TokenResponse createToken(String userUuid, TokenRequest tokenRequest) {
        UuidUtil.validateUuid(userUuid);

        String nickname = userRepository.findNicknameById(userUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        String roomCode = tokenRequest.getRoomCode();
        String key = "room:" + roomCode;
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();

        Map<String, Object> roomData = hashOps.entries(key);
        if (roomData.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_CODE);
        }

        AccessToken token = new AccessToken(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
        token.setName(nickname);
        token.setIdentity(userUuid);
        token.addGrants(new RoomJoin(true), new RoomName(roomCode));

        String backgroundUrl = s3Service.getFileUrl((String) roomData.get("background_url"));
        Integer takeCount = Integer.parseInt(roomData.get("take_count").toString());
        Integer cutCount = Integer.parseInt(roomData.get("cut_count").toString());
        Integer timeLimit = Integer.parseInt(roomData.get("time_limit").toString());
        String cutsJson = (String) roomData.get("cuts");
        List<String> cutKeys = parseCutsJson(cutsJson);
        List<String> cutUrls = new ArrayList<>();
        for (String cutUrl : cutKeys) {
            cutUrls.add(s3Service.getFileUrl(cutUrl));
        }

        return new TokenResponse(token.toJwt(), backgroundUrl, takeCount, cutCount, timeLimit,
            cutUrls);
    }

    @Override
    public void reciveWebhook(String authHeader, String body) {
        WebhookReceiver webhookReceiver = new WebhookReceiver(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
        try {
            WebhookEvent event = webhookReceiver.receive(body, authHeader);
            // log.info("LiveKit Webhook Received: {}", event.getEvent());

            if ("room_finished".equals(event.getEvent())) {
                String roomCode = event.getRoom().getName();
                String key = "room:" + roomCode;
                HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
                Map<String, Object> roomData = hashOps.entries(key);

                if (!roomData.isEmpty()) {
                    String backgroundUrl = (String) roomData.get("background_url");
                    String cutsJson = (String) roomData.get("cuts");
                    List<String> cutUrls = parseCutsJson(cutsJson);

                    s3Service.deleteObject(backgroundUrl);
                    for (String cutUrl : cutUrls) {
                        s3Service.deleteObject(cutUrl);
                    }

                    redisTemplate.delete(key);
                }
            }
        } catch (Exception e) {
            // log.error("Error validating webhook event: {}", e.getMessage());
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public TokenResponse createRelayToken(String userUuid, TokenRelayRequest tokenRelayRequest) {
        String relayUuid = tokenRelayRequest.getRelayUuid();

        if (!relayRepository.existsByRelayUuidAndUserUserUuid(relayUuid, userUuid)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        String nickname = userRepository.findNicknameById(userUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        Relay relay = relayRepository.findById(relayUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        List<RelayCut> allCuts = relayCutRepository.findByRelayRelayUuid(relayUuid);

        String relayKey = "relay:" + relayUuid;
        String roomCode = redisTemplate.opsForValue().get(relayKey);

        if (roomCode == null) {
            Random random = new Random();
            while (true) {
                String newRoomCode = String.format("%06d", random.nextInt(999999));
                String roomKey = "room:" + newRoomCode;

                if (!redisTemplate.hasKey(roomKey)) {
                    roomCode = newRoomCode;
                    try {
                        List<String> takenCutKeys = allCuts.stream()
                            .filter(RelayCut::isTaken)
                            .map(RelayCut::getCutUrl)
                            .toList();

                        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
                        Map<String, String> roomDataMap = new HashMap<>();

                        roomDataMap.put("background_url", relay.getBackgroundUrl());
                        roomDataMap.put("take_count", String.valueOf(relay.getTakeCount()));
                        roomDataMap.put("cut_count", String.valueOf(relay.getCutCount()));
                        roomDataMap.put("time_limit", String.valueOf(relay.getTimeLimit()));
                        roomDataMap.put("cuts", objectMapper.writeValueAsString(takenCutKeys));

                        hashOps.putAll(roomKey, roomDataMap);

                        redisTemplate.opsForValue().set(relayKey, roomCode);
                        break;
                    } catch (JsonProcessingException e) {
                        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
                    }
                }
            }
        }

        AccessToken token = new AccessToken(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
        token.setName(nickname);
        token.setIdentity(userUuid);
        token.addGrants(new RoomJoin(true), new RoomName(roomCode));

        String roomKey = "room:" + roomCode;
        Map<Object, Object> roomData = redisTemplate.opsForHash().entries(roomKey);

        if (roomData.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_CODE);
        }

        String backgroundUrl = s3Service.getFileUrl((String) roomData.get("background_url"));
        Integer takeCount = Integer.parseInt(roomData.get("take_count").toString());
        Integer cutCount = Integer.parseInt(roomData.get("cut_count").toString());
        Integer timeLimit = Integer.parseInt(roomData.get("time_limit").toString());
        String cutsJson = (String) roomData.get("cuts");
        List<String> cutKeys = parseCutsJson(cutsJson);
        List<String> cutUrls = new ArrayList<>();
        for (String cutKey : cutKeys) {
            cutUrls.add(s3Service.getFileUrl(cutKey));
        }

        return new TokenResponse(token.toJwt(), backgroundUrl, takeCount, cutCount, timeLimit,
            cutUrls);
    }

    private List<String> parseCutsJson(String cutsJson) {
        List<String> cutUrls = new ArrayList<>();
        if (cutsJson != null && !cutsJson.isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                cutUrls = objectMapper.readValue(cutsJson, new TypeReference<List<String>>() {
                });
            } catch (JsonProcessingException e) {
                // log.error("JSON 파싱 오류 발생: {}", e.getMessage());
            }
        }
        return cutUrls;
    }

    @Override
    public void addCutKeyToRoom(String roomCode, String cutKey) {
        if (roomCode == null || roomCode.isBlank()) {
            throw new BusinessException(ErrorCode.MISSING_ROOM_CODE);
        }
        if (cutKey == null || cutKey.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        final String roomKey = "room:" + roomCode;
        final int maxRetries = 5;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            List<Object> tx = redisTemplate.execute(new SessionCallback<List<Object>>() {
                @Override
                @SuppressWarnings("unchecked")
                public List<Object> execute(RedisOperations operations) throws DataAccessException {
                    operations.watch(roomKey);

                    @SuppressWarnings("unchecked")
                    HashOperations<String, String, Object> hashOps =
                        (HashOperations<String, String, Object>) operations.opsForHash();
                    Map<String, Object> roomData = hashOps.entries(roomKey);
                    if (roomData == null || roomData.isEmpty()) {
                        operations.unwatch();
                        // 방 키 자체가 없을 때: 의미상 ROOM NOT FOUND가 맞음
                        throw new BusinessException(ErrorCode.NOT_FOUND_ROOM);
                    }

                    String cutsJson = (String) roomData.get("cuts");
                    List<String> cutKeys = parseCutsJson(cutsJson);

                    // 이미 존재하면 아무 작업 없이 종료
                    if (cutKeys.contains(cutKey)) {
                        operations.unwatch();
                        return Collections.emptyList();
                    }

                    Integer takeCount = tryParseInt(roomData.get("take_count"));
                    if (takeCount != null && cutKeys.size() >= takeCount) {
                        operations.unwatch();
                        throw new BusinessException(ErrorCode.CUT_TAKE_COUNT_EXCEEDED);
                    }

                    cutKeys.add(cutKey);

                    final String updatedJson;
                    try {
                        updatedJson = objectMapper.writeValueAsString(cutKeys);
                    } catch (Exception e) {
                        operations.unwatch();
                        throw new BusinessException(ErrorCode.INVALID_CUTS_JSON);
                    }

                    operations.multi();
                    hashOps.put(roomKey, "cuts", updatedJson);
                    return operations.exec(); // null 이면 충돌(동시 수정) → 재시도
                }
            });

            if (tx != null) {
                // 성공 또는 중복으로 인한 no-op 성공
                return;
            }
            // 충돌 → 루프 돌며 재시도
        }

        throw new BusinessException(ErrorCode.REDIS_TX_RETRY_EXCEEDED);
    }

    private Integer tryParseInt(Object v) {
        if (v == null) return null;
        try { return Integer.parseInt(v.toString()); }
        catch (NumberFormatException ignore) { return null; }
    }
}
