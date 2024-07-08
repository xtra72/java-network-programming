package com.nhnacademy.aiot;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nhnacademy.quiz.quiz10.Server;

/**
 * Hello world!
 *
 */
public class SimpleHttpServer extends Thread {
    Logger log;
    int port = 8080;
    List<ServiceHandler> serviceList = new LinkedList<>();

    public SimpleHttpServer() {
        log = LogManager.getLogger(this.getClass().getSimpleName());
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                    System.out.println("Shutting down ...");
                    SimpleHttpServer.this.interrupt();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        log.trace("Start server : {}", port);
        while (!Thread.currentThread().isInterrupted()) {
            try (ServerSocket socket = new ServerSocket(port)) {
                ServiceHandler handler = new ServiceHandler(socket.accept());

                serviceList.add(handler);
                handler.start();

            } catch (IOException ignore) {
                //
            }
        }

        for (ServiceHandler handler : serviceList) {
            handler.stop();
        }

        log.trace("Stopped server : {}", port);
    }

    public static void main(String[] args) throws InterruptedException {
        SimpleHttpServer server = new SimpleHttpServer();

        server.start();

        server.join();
    }
}
