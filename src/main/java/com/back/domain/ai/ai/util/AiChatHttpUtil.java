package com.back.domain.ai.ai.util;

import java.io.IOException;

public final class AiChatHttpUtil {
    private AiChatHttpUtil() {
    }

    public static boolean isClientDisconnect(Throwable ex) {
        Throwable t = ex;
        while (t != null) {
            String name = t.getClass().getName();
            String msg = t.getMessage() != null ? t.getMessage().toLowerCase() : "";

            // Tomcat
            if (name.contains("ClientAbortException")) return true;

            // Reactor Netty / WebClient
            if (name.contains("AbortedException")) return true;

            // 소켓 끊김 메시지
            if (msg.contains("broken pipe")) return true;
            if (msg.contains("connection reset by peer")) return true;
            if (msg.contains("connection aborted")) return true;

            // 직접 응답 중지
            if (t instanceof IOException) return true;

            t = t.getCause();
        }
        return false;
    }

}
