package com.nhnacademy.practice;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.Reader;
import java.net.Socket;

public class Exam01 {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 1234;

        try (Socket socket = new Socket(host, port)) {
            System.out.println("서버에 연결되었습니다.");

            Thread thread = new Thread(() -> {
                try {
                    String line;
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (!(line = input.readLine()).equals("exit")) {
                        System.out.println(line);
                        // socket.getOutputStream().write(line.getBytes());
                        // socket.getOutputStream().write("\n".getBytes());
                    }
                } catch (IOException ignore) {
                }
            });
            thread.start();

            String line;

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (!(line = reader.readLine()).equals("exit")) {
                socket.getOutputStream().write(line.getBytes());
                socket.getOutputStream().write("\n".getBytes());
            }

        } catch (Exception ignore) {
            System.err.println(host + ":" + port + "에 연결할 수 없습니다.");
        }
    }
}
