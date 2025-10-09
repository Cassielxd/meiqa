package io.renren.crmchat.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket configuration enabling {@link jakarta.websocket.server.ServerEndpoint} support.
 * Mirrors PHP Swoole bootstrap by exposing a standalone WebSocket entry point.
 */
@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
