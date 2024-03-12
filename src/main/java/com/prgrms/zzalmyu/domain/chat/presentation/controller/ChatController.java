package com.prgrms.zzalmyu.domain.chat.presentation.controller;

import com.prgrms.zzalmyu.domain.chat.application.ChatService;
import com.prgrms.zzalmyu.domain.chat.presentation.dto.req.ChatHelloRequest;
import com.prgrms.zzalmyu.domain.chat.presentation.dto.req.ChatPhotoRequest;
import com.prgrms.zzalmyu.domain.chat.presentation.dto.res.ChatHelloResponse;
import com.prgrms.zzalmyu.domain.chat.presentation.dto.res.ChatImageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessageSendingOperations simpMessageSendingOperations;
    private final ChatService chatService;

    @MessageMapping("/hello")
    public void greeting(ChatHelloRequest request) {
        String nickname = chatService.generateNickname();
        chatService.saveNickname(request.getEmail(), nickname);
        simpMessageSendingOperations.convertAndSend("/sub/" + request.getChannelId(), ChatHelloResponse.of(request.getEmail(), nickname));
    }

    @MessageMapping("/image")
    public void sendPhoto(ChatPhotoRequest request) {
        String nickname = chatService.getNickname(request.getEmail());
        simpMessageSendingOperations.convertAndSend("/sub/" + request.getChannelId(), ChatImageResponse.of(request.getEmail(), request.getImage(), nickname));
    }
}
