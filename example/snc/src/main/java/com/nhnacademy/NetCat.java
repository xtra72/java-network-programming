package com.nhnacademy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class NetCat implements Runnable {
    Socket socket;
    Queue<String> receiveQueue = new LinkedList<>();
    Queue<String> sendQueue = new LinkedList<>();

    public NetCat(Socket socket) {
        this.socket = socket;
    }

    public void send(String message) {
        synchronized (sendQueue) {
            sendQueue.add(message);
        }
    }

    public boolean isEmptyReceiveQueue() {
        synchronized (receiveQueue) {
            return receiveQueue.isEmpty();
        }
    }

    public String receive() {
        synchronized (receiveQueue) {
            return receiveQueue.poll();
        }
    }

    public void run() {
        try (BufferedReader inputRemote = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter outputRemote = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            Thread receiver = new Thread(() -> {
                try {
                    String line;
                    while ((line = inputRemote.readLine()) != null) {
                        synchronized (receiveQueue) {
                            receiveQueue.add(line);
                        }
                    }
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            });

            Thread sender = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        synchronized (sendQueue) {
                            if (!sendQueue.isEmpty()) {
                                outputRemote.write(sendQueue.poll());
                                outputRemote.flush();
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            });

            receiver.start();
            sender.start();

            receiver.join();
            sender.join();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
        }
    }
}
