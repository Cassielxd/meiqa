package io.renren.crmchat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;

/**
 * Chat View Controller - 聊天界面控制器
 * Serves the chat interface HTML for embedded chat widget
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/chat")
@Tag(name = "Chat View - Chat Interface")
public class ChatViewController {

    /**
     * 聊天界面首页
     * GET /chat/index
     *
     * 返回嵌入式聊天界面 HTML
     * 由 customerServer.js 通过 iframe 加载
     *
     * URL Parameters (passed from customerServer.js):
     * - token: JWT authentication token (for registered users)
     * - appid: Application/Tenant ID (for visitor auto-login)
     * - kefuid: Specific customer service agent ID (optional)
     */
    @GetMapping(value = {"/index", "/index.html"}, produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Chat Interface Page")
    public ResponseEntity<String> index() {
        try {
            Resource resource = new ClassPathResource("static/chat/index.html");
            String content = new String(Files.readAllBytes(resource.getFile().toPath()));
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("<html><body><h1>Error loading chat interface</h1><p>" + e.getMessage() + "</p></body></html>");
        }
    }

    /**
     * 备用路由：PC端聊天界面
     * GET /chat/pc
     */
    @GetMapping(value = {"/pc", "/pc.html"}, produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "PC Chat Interface")
    public ResponseEntity<String> pc() {
        return index();
    }

    /**
     * 备用路由：移动端聊天界面
     * GET /chat/mobile
     */
    @GetMapping(value = {"/mobile", "/mobile.html"}, produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Mobile Chat Interface")
    public ResponseEntity<String> mobile() {
        return index();
    }
}
