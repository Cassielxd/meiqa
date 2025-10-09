package io.renren.modules.ai.controller;

import io.renren.common.utils.Result;
import io.renren.modules.ai.dto.AiChatMessageDTO;
import io.renren.modules.ai.dto.AiChatRequestDTO;
import io.renren.modules.ai.service.AiChatMessageService;
import io.renren.modules.ai.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("ai/chat")
@Tag(name = "Ai对话")
@AllArgsConstructor
public class AiChatController {
    private final AiChatService aiChatService;
    private final AiChatMessageService aiChatMessageService;

    @PostMapping(value = "message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AiChatMessageDTO> message(@RequestBody AiChatRequestDTO requestDTO) {
        return aiChatService.sendChatMessage(requestDTO);
    }

    @GetMapping("message/list")
    @Operation(summary = "列表")
    @RequiresPermissions("ai:chat")
    public Result<List<AiChatMessageDTO>> list(Long conversationId) {
        List<AiChatMessageDTO> list = aiChatMessageService.getList(conversationId);

        return new Result<List<AiChatMessageDTO>>().ok(list);
    }

    @DeleteMapping("message/list")
    @Operation(summary = "清空列表")
    @RequiresPermissions("ai:chat")
    public Result<List<AiChatMessageDTO>> clearList(Long conversationId) {
        aiChatMessageService.clearList(conversationId);

        return new Result<>();
    }

}
