package com.nhnacademy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

public class SimpleNC {
    public static void main(String[] args) {
        Options options = new Options();

        options.addOption("l", true, "Listen");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine commandLine = parser.parse(options, args);

            if (commandLine.hasOption("l")) {
                List<Thread> clientHandlerList = new LinkedList<>();
                List<NetCat> netcatList = new LinkedList<>();

                Thread inputAgent = new Thread(() -> {
                    try {
                        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                        String line;
                        while ((line = input.readLine()) != null) {
                            synchronized (netcatList) {
                                for (NetCat netcat : netcatList) {
                                    netcat.send(line + "\n");
                                }
                            }
                        }
                    } catch (IOException e) {
                    }
                });

                Thread outputAgent = new Thread(() -> {
                    while (!Thread.currentThread().isInterrupted()) {
                        synchronized (netcatList) {
                            for (NetCat netcat : netcatList) {
                                if (!netcat.isEmptyReceiveQueue()) {
                                    String line = netcat.receive();
                                    System.out.println(line);
                                }
                            }
                        }

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ignore) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });

                inputAgent.start();
                outputAgent.start();

                ServerSocket serverSocket = new ServerSocket(1234);
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        NetCat netcat = new NetCat(serverSocket.accept());
                        Thread thread = new Thread(netcat);
                        thread.start();
                        clientHandlerList.add(thread);
                        netcatList.add(netcat);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                }
                serverSocket.close();
            } else {
                try (Socket socket = new Socket("localhost", 1234)) {
                    NetCat netcat = new NetCat(socket);
                    Thread thread = new Thread(netcat);
                    thread.start();

                    Thread inputAgent = new Thread(() -> {
                        try {
                            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                            String line;
                            while ((line = input.readLine()) != null) {
                                netcat.send(line + "\n");
                            }
                        } catch (IOException e) {
                        }
                    });

                    Thread outputAgent = new Thread(() -> {
                        while (!Thread.currentThread().isInterrupted()) {
                            if (!netcat.isEmptyReceiveQueue()) {
                                String line = netcat.receive();
                                System.out.println(line);
                            }
                        }
                    });

                    inputAgent.start();
                    outputAgent.start();
                    thread.join();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        } catch (org.apache.commons.cli.ParseException | IOException e) {
        }
    }
}