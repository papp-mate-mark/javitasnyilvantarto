package otvosuzlet.javitasnyilntarto.config;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import otvosuzlet.javitasnyilntarto.service.JWTService;
import otvosuzlet.javitasnyilntarto.service.MyUserDetailsService;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JWTService jwtService;
    private final MyUserDetailsService userDetailsService;

    public WebSocketAuthChannelInterceptor(JWTService jwtService, MyUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            Authentication authentication = authenticateFromHeaders(accessor);
            if (authentication == null) {
                throw new AccessDeniedException("Missing or invalid Authorization token");
            }
            accessor.setUser(authentication);
        }

        // Require an authenticated Principal for any further interaction.
        if (StompCommand.SUBSCRIBE.equals(command) || StompCommand.SEND.equals(command) || StompCommand.UNSUBSCRIBE.equals(command)) {
            if (accessor.getUser() == null) {
                throw new AccessDeniedException("Unauthenticated WebSocket session");
            }
        }

        return message;
    }

    private Authentication authenticateFromHeaders(StompHeaderAccessor accessor) {
        String authHeader = firstNativeHeader(accessor, "Authorization");
        if (authHeader == null) {
            authHeader = firstNativeHeader(accessor, "authorization");
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUserName(token);
        if (username == null || username.isBlank()) {
            return null;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtService.validateAccessToken(token, userDetails)) {
            return null;
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private String firstNativeHeader(StompHeaderAccessor accessor, String name) {
        List<String> values = accessor.getNativeHeader(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }
}
