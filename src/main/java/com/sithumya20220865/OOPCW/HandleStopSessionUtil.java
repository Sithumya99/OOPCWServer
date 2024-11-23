package com.sithumya20220865.OOPCW;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HandleStopSessionUtil {

    private Message message;

    public HandleStopSessionUtil(Message message) {this.message = message;}

    public ResponseEntity<?> execute() {

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
