package com.sithumya20220865.OOPCW;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HandleStopSessionUtil {

    private Message message;
    private JWTService jwtService;

    public HandleStopSessionUtil(Message message, JWTService jwtService) {
        this.message = message;
        this.jwtService = jwtService;
    }

    public ResponseEntity<?> execute() {

        if (!jwtService.getRole(message.getUserAuth().getCredentials().toString()).equalsIgnoreCase("ROLE_Admin")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only Admin can start a session");
        }

        //terminate session
        if (SessionConfiguration.getInstance() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No session to terminate");
        } else {
            SessionConfiguration.reset();
            GlobalUtil.setTicketpool(null);
            //terminate ticket release scheduler
            return ResponseEntity.status(HttpStatus.OK).body("Session terminated successfully");
        }
    }
}
