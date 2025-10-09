package io.renren.modules.ai.controller;

import io.renren.common.utils.Result;
import io.renren.modules.ai.dto.AiChatConversationDTO;
import io.renren.modules.ai.service.AiChatConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI聊天对话
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("/ai/chat/conversation")
@Tag(name = "AI聊天对话")
@AllArgsConstructor
public class AiChatConversationController {
    private final AiChatConversationService aiChatConversationService;

    @GetMapping("list")
    @Operation(summary = "列表")
    @RequiresPermissions("ai:chat")
    public Result<List<AiChatConversationDTO>> list() {
        List<AiChatConversationDTO> list = aiChatConversationService.getList();

        return new Result<List<AiChatConversationDTO>>().ok(list);
    }

    @PostMapping
    @Operation(summary = "保存")
    @RequiresPermissions("ai:chat")
    public Result<AiChatConversationDTO> save(@RequestBody AiChatConversationDTO vo) {
        AiChatConversationDTO result = aiChatConversationService.save(vo);

        return new Result<AiChatConversationDTO>().ok(result);
    }

    @PutMapping
    @Operation(summary = "修改")
    @RequiresPermissions("ai:chat")
    public Result<String> update(@RequestBody @Valid AiChatConversationDTO vo) {
        aiChatConversationService.update(vo);

        return new Result<>();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @RequiresPermissions("ai:chat")
    public Result<String> delete(@RequestBody List<Long> idList) {
        aiChatConversationService.delete(idList);

        return new Result<>();
    }
}