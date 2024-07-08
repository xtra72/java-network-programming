package com.nhnacademy.practice;

import java.net.ServerSocket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.Console;
import java.net.Socket;
import java.io.IOException;

public class Exam04 {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(1234)) {
            String line;
            while (!Thread.currentThread().isInterrupted()) {
                try (Socket socket = serverSocket.accept();
                        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    System.out.println("Connected : " + socket.getInetAddress().getHostAddress());
                    while (!(line = input.readLine()).equals("exit")) {
                        System.out.println(line);
                        socket.getOutputStream().write(line.getBytes());
                        socket.getOutputStream().write("\n".getBytes());
                    }
                }
                try {

                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        } catch (

        Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
