package com.example.yenanow.openvidu.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.common.util.UuidUtil;
import com.example.yenanow.openvidu.dto.request.CodeRequest;
import com.example.yenanow.openvidu.dto.request.TokenRequest;
import com.example.yenanow.openvidu.dto.response.CodeResponse;
import com.example.yenanow.openvidu.dto.response.TokenResponse;
import com.example.yenanow.users.repository.UserQueryRepository;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import io.livekit.server.WebhookReceiver;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import livekit.LivekitWebhook.WebhookEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpoenviduServiceImpl implements OpenviduService {

    @Value("${livekit.api.key}")
    private String LIVEKIT_API_KEY;

    @Value("${livekit.api.secret}")
    private String LIVEKIT_API_SECRET;

    private final UserQueryRepository userQueryRepository;
    private final StringRedisTemplate redisTemplate;

    @Override
    public CodeResponse createCode(String userUuid, CodeRequest codeRequest) {
        UuidUtil.validateUuid(userUuid);
        
        String nickname = userQueryRepository.findNicknameById(userUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        Random random = new Random();
        String roomCode;

        while (true) {
            roomCode = String.format("%06d", random.nextInt(999999));
            String key = "room:" + roomCode;

            if (!redisTemplate.hasKey(key)) {
                HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();

                Map<String, String> roomDataMap = new HashMap<>();
                roomDataMap.put("background_url", codeRequest.getBackgroundUrl());
                roomDataMap.put("take_cnt", String.valueOf(codeRequest.getTakeCnt()));
                roomDataMap.put("cut_cnt", String.valueOf(codeRequest.getCutCnt()));
                roomDataMap.put("time_limit", String.valueOf(codeRequest.getTimeLimit()));

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

        String nickname = userQueryRepository.findNicknameById(userUuid)
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

        String backgroundUrl = (String) roomData.get("background_url");
        Integer takeCnt = Integer.parseInt(roomData.get("take_cnt").toString());
        Integer cutCnt = Integer.parseInt(roomData.get("cut_cnt").toString());
        Integer timeLimit = Integer.parseInt(roomData.get("time_limit").toString());

        return new TokenResponse(token.toJwt(), backgroundUrl, takeCnt, cutCnt, timeLimit);
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
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            // log.error("Error validating webhook event: {}", e.getMessage());
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }
}
