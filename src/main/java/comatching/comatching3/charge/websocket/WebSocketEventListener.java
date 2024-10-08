package comatching.comatching3.charge.websocket;

import comatching.comatching3.admin.entity.Admin;
import comatching.comatching3.charge.dto.response.ChargePendingInfo;
import comatching.comatching3.charge.service.ChargeService;
import comatching.comatching3.users.auth.jwt.JwtUtil;
import comatching.comatching3.util.UUIDUtil;
import comatching.comatching3.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ChargeService chargeService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final JwtUtil jwtUtil;

    @EventListener
    public void handleWebSocketConnectListener(SessionSubscribeEvent event) {
        String destination = (String) event.getMessage().getHeaders().get("simpDestination");
        if ("/topic/chargeRequests".equals(destination)) {
            List<ChargePendingInfo> initialData = chargeService.getAllChargeRequests();
            simpMessagingTemplate.convertAndSend(destination, initialData);
        }
    }
}
