package com.nhnacademy.aiot;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServiceHandler implements Runnable {
    static final String CRLF = "\r\n";
    Thread thread;
    Socket socket;
    Logger log;

    public ServiceHandler(Socket socket) {
        this.socket = socket;
        thread = new Thread(this);
        log = LogManager.getLogger(this.getClass().getSimpleName());
    }

    String getFileList(Path path) {
        StringBuilder builder = new StringBuilder();

        try (Stream<Path> stream = Files.list(path)) {
            stream.filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .forEach(p -> builder.append(p.toString()).append(CRLF));
        } catch (IOException ignore) {
            throw new InvalidStatusException(403);
        }

        return builder.toString();
    }

    String getFile(Path path) {
        StringBuilder builder = new StringBuilder();

        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(x -> builder.append(x).append(CRLF));
        } catch (IOException e) {
            throw new InvalidStatusException(403);
        }

        return builder.toString();
    }

    public Response process(Request request) {
        try {
            if (request.getMethod().equals("GET")) {
                Path relativePath = Paths.get("." + request.getPath());

                Response response = new Response(request.getVersion(), 200, "OK");
                StringBuilder contentType = new StringBuilder();
                contentType.append("text");
                if (Files.isDirectory(relativePath)) {
                    contentType.append("; charset=utf-8");

                    response.setBody(getFileList(relativePath).getBytes(StandardCharsets.UTF_8));
                } else if (Files.isRegularFile(relativePath)) {
                    String filename = relativePath.getFileName().toString();
                    if (filename.contains(".")) {
                        throw new UnknownContentTypeException();
                    }

                    contentType.append("/")
                            .append(filename.substring(filename.lastIndexOf(".") + 1))
                            .append("; charset=utf-8");

                    response.setBody(getFile(relativePath).getBytes(StandardCharsets.UTF_8));
                }
                response.addField("content-type", contentType.toString());

                return response;
            }

            throw new InvalidStatusException(400);
        } catch (InvalidStatusException e) {
            return new Response(request.getVersion(), e.getCode());
        }
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        thread.interrupt();
        thread.join();
    }

    @Override
    public void run() {
        log.trace("Start thread : {}", thread.getId());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedOutputStream writer = new BufferedOutputStream(socket.getOutputStream())) {
            while (!Thread.currentThread().isInterrupted()) {
                String requestLine = reader.readLine();
                if (requestLine == null) {
                    break;
                }

                String[] fields = requestLine.split("\\s", 3);
                if (fields.length != 3) {
                    throw new InvalidHttpRequestException();
                }

                Request request = new Request(fields[0], fields[1], fields[2]);

                String fieldLine;
                while ((fieldLine = reader.readLine()) != null) {
                    if (fieldLine.length() == 0) {
                        break;
                    }
                    request.addField(fieldLine);
                }

                if (request.hasField(Request.FIELD_CONTENT_LENGTH)) {
                    char[] buffer = new char[request.getContentLength()];

                    int bodyLength = reader.read(buffer, 0, request.getContentLength());
                    if (bodyLength == request.getContentLength()) {
                        request.setBody(buffer);
                    }
                }

                Response response = process(request);
                log.trace(response);

                writer.write(response.getBytes());
                writer.flush();
            }
        } catch (Exception ignore) {
            //
        } finally {
            try {
                socket.close();
            } catch (IOException ignore) {
                //
            }
        }

        log.trace("Finished thread : {}", thread.getId());
    }
}
