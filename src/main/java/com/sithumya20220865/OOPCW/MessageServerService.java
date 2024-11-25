package com.sithumya20220865.OOPCW;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MessageServerService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private JWTService jwtService;  //for token generation

    @Autowired
    private MessageQueueService messageQueueService; //message queue for consumer

    @Autowired
    private TicketPoolService ticketPoolService;  //for adding and removing tickets

    @Autowired
    private TicketWebSocketHandler ticketWebSocketHandler;  //handle changes in ticketpool for websockets

    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    @PostConstruct
    public void startExecutingQueue() {
        //thread to retrieve and execute tasks from queue
        Thread taskThread = new Thread(() -> {
            while (true) {
                try {
                    // Dequeue tasks from the queue
                    Message task = messageQueueService.dequeue();

                    // Submit the task to the thread pool
                    threadPool.submit(() -> {
                        ResponseEntity<?> finalResponse = null;
                        try {
                            //set context for thread
                            Authentication taskAuth = task.getUserAuth();
                            if (taskAuth != null) {
                                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                                securityContext.setAuthentication(taskAuth);
                                SecurityContextHolder.setContext(securityContext);
                            }

                            // Execute the command
                            finalResponse = executeCommandService(task);
                            if(finalResponse != null) {
                                task.getResponse().complete(finalResponse);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        finally {
                            SecurityContextHolder.clearContext();
                        }
                    });

                } catch (InterruptedException e) {
                    System.out.println("Dispatcher thread interrupted.");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        taskThread.setDaemon(false);
        taskThread.start();
    }


    public ResponseEntity<?> executeCommandService(Message message) {
        try {
            System.out.println("process sec cont: " + SecurityContextHolder.getContext().getAuthentication());
            //process message
            if ("register".equalsIgnoreCase(message.getCommand())) {
                //register new user
                HandleRegisterUserUtil newUserRegister = new HandleRegisterUserUtil(message, repositoryService, jwtService);
                return newUserRegister.execute();
            } else if ("login".equalsIgnoreCase(message.getCommand())) {
                //authenticate user
                HandleLoginUserUtil loginUser = new HandleLoginUserUtil(message, repositoryService, jwtService);
                return loginUser.execute();
            } else if ("startsession".equalsIgnoreCase(message.getCommand())) {
                //start ticket session
                HandleStartSessionUtil newSession = new HandleStartSessionUtil(message, jwtService, ticketWebSocketHandler, ticketPoolService);
                return newSession.execute();
            } else if ("stopsession".equalsIgnoreCase(message.getCommand())) {
                //stop ticket session
                System.out.println("stop session process: start");
                HandleStopSessionUtil stopSession = new HandleStopSessionUtil(message, jwtService, ticketWebSocketHandler, ticketPoolService);
                return stopSession.execute();
            } else if ("addticket".equalsIgnoreCase(message.getCommand())) {
                //add ticket
                HandleAddTicketUtil addNewTicket = new HandleAddTicketUtil(message, repositoryService, jwtService, ticketPoolService);
                return addNewTicket.execute();
            } else if ("buyticket".equalsIgnoreCase(message.getCommand())) {
                HandleBuyTicketUtil buyTicket = new HandleBuyTicketUtil(message, repositoryService, jwtService, ticketPoolService);
                return buyTicket.execute();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unknown command");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error handling request: " + e.getMessage());
        }
    }
}
