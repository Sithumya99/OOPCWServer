package com.sithumya20220865.OOPCW;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sithumya20220865.OOPCW.Configs.SessionConfiguration;
import com.sithumya20220865.OOPCW.Controller.MessageServer;
import com.sithumya20220865.OOPCW.Services.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServerTest {

    @InjectMocks
    private MessageServer messageServer;

    @Mock
    private JwtAuthenticationService jwtAuthenticationService;

    @Mock
    private TicketPoolService ticketPoolService;

    @Mock
    private HttpServletRequest httpServletRequest;


    @Test
    void testExecCommandGet_GetTickets_Success() {
        String command = "gettickets";
        Authentication mockAuth = Mockito.mock(Authentication.class);

        when(jwtAuthenticationService.authenticate(httpServletRequest)).thenReturn(mockAuth);
        when(mockAuth.getPrincipal()).thenReturn("Tom");
        when(mockAuth.getCredentials()).thenReturn("mockToken");
        SessionConfiguration.initialize(10, 1000, 200, 30);

        CompletableFuture<ResponseEntity<?>> response = messageServer.executeCommandGet(command, httpServletRequest);

        ResponseEntity<?> finalRes = response.join();
        assertEquals(HttpStatus.OK, finalRes.getStatusCode());
        verify(ticketPoolService, times(1)).writeTicketPool(any());
    }

    @Test
    void testExecCommandGet_GetTickets_Unauthorized() {
        String command = "gettickets";
        when(jwtAuthenticationService.authenticate(httpServletRequest)).thenReturn(null);

        CompletableFuture<ResponseEntity<?>> response = messageServer.executeCommandGet(command, httpServletRequest);

        ResponseEntity<?> finalRes = response.join();
        assertEquals(HttpStatus.UNAUTHORIZED, finalRes.getStatusCode());
        assertTrue(((ObjectNode) finalRes.getBody()).get("message").asText().contains("Authorization failed."));
    }

    @Test
    void testExecCommandGet_GetTickets_NoActiveSession() {
        String command = "gettickets";
        Authentication mockAuth = Mockito.mock(Authentication.class);

        when(jwtAuthenticationService.authenticate(httpServletRequest)).thenReturn(mockAuth);
        when(mockAuth.getPrincipal()).thenReturn("Tom");

        CompletableFuture<ResponseEntity<?>> response = messageServer.executeCommandGet(command, httpServletRequest);

        ResponseEntity<?> finalRes = response.join();
        assertEquals(HttpStatus.BAD_REQUEST, finalRes.getStatusCode());
        assertTrue(((ObjectNode) finalRes.getBody()).get("message").asText().contains("Ticket session is not active"));
    }
}
