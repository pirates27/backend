package com.landlens.ai.controller;

import com.landlens.ai.dto.AiConversationResponseDto;
import com.landlens.ai.dto.AiMessageResponseDto;
import com.landlens.ai.mapper.AiMapper;
import com.landlens.ai.model.AiConversation;
import com.landlens.ai.model.AiMessage;
import com.landlens.ai.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai/conversations")
@Transactional
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    @PostMapping
    public ResponseEntity<AiConversationResponseDto> startConversation(
            @RequestParam(required = false) String title,
            Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        AiConversation conversation = aiChatService.startConversation(userId, title);
        return ResponseEntity.ok(AiMapper.toResponseDto(conversation));
    }

    @GetMapping
    public ResponseEntity<List<AiConversationResponseDto>> getMyConversations(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        List<AiConversation> list = aiChatService.getUserConversations(userId);
        List<AiConversationResponseDto> dtoList = list.stream()
                .map(AiMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<AiMessageResponseDto> sendMessage(
            @PathVariable UUID id,
            @RequestBody String content) {
        if (content.startsWith("\"") && content.endsWith("\"")) {
            content = content.substring(1, content.length() - 1);
        }
        AiMessage reply = aiChatService.sendMessage(id, content);
        return ResponseEntity.ok(AiMapper.toResponseDto(reply));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<AiMessageResponseDto>> getMessages(@PathVariable UUID id) {
        List<AiMessage> messages = aiChatService.getMessages(id);
        List<AiMessageResponseDto> dtoList = messages.stream()
                .map(AiMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(dtoList);
    }
}
