package io.renren.crmchat.websocket;

import java.util.Map;

public interface BaseHandler {
    ChatWebSocketServer.SessionIdentity authenticate(Map<String, String> params);
}
