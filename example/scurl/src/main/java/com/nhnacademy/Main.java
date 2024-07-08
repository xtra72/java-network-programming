package com.nhnacademy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();
        String version = "HTTP/1.1";
        String command = "GET";
        String location = "/get";
        int port = 80;

        options.addOption(Option.builder("X")
                .longOpt("request")
                .hasArg()
                .argName("command")
                .desc("(HTTP) Specifies a custom request method to use when communicating with the HTTP server. "
                        + " The specified request method will be used instead of the method otherwise used (which defaults to GET). "
                        + "Read the HTTP 1.1 specification for details and explanations."
                        + "Common additional HTTP requests include PUT and DELETE, "
                        + "but related technologies like WebDAV offers PROPFIND, COPY, MOVE and more.")
                .build());

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("X")) {
                command = line.getOptionValue("X");
            }

            if (line.getArgs().length > 0) {
                String host = line.getArgs()[0];

                Socket socket = new Socket(host, port);

                PrintStream writer = new PrintStream(socket.getOutputStream());

                writer.printf("%s %s %s\r\n", command, location, version);
                writer.printf("Host: %s\r\n", host);
                writer.printf("\r\n");

                Thread receiver = new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                        String inputLine;
                        while ((inputLine = reader.readLine()) != null) {
                            System.out.println(inputLine);
                        }
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                });

                receiver.start();
            } else {
                System.err.println("URL이 필요합니다.");
            }
        } catch (IOException | ParseException e) {
            System.err.println(e.getMessage());
        }

    }
}