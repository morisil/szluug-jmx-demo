/*
 * szluug-jmx-demo - Java Management Extensions demo for szluug.org
 * Copyright (C) 2012 Xemantic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xemantic.demo.szluug.jmx;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The simplest possible HTTP daemon which serves all the filesystem
 * as Content-Type {@code text/plain}.
 *
 * @author morisil
 */
public class HttpServer implements HttpServerMBean {

  /*
   * Thanks to encapsulating state in AtomicIntegers we don't have to
   * bother with synchronization.
   */
  private final AtomicInteger threadCounter = new AtomicInteger(0);

  private final AtomicInteger requestCounter = new AtomicInteger(0);

  private final int port;
  private final Path rootDir;


  /**
   * Creates HTTP server instance on give {@code port}.
   *
   * @param port the port to bind to.
   * @param rootDir the root directory for serving files from
   */
  public HttpServer(int port, Path rootDir) {
    this.port = port;
    this.rootDir = rootDir;
  }

  /**
   * Starts the server.
   */
  public void start() {
    (new Thread(this::dispatchRequests, "RequestDispatcher")).start();
  }

  private void dispatchRequests() {
    try (ServerSocket socket = new ServerSocket(port)) {
      handleConnections(socket);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void handleConnections(ServerSocket socket) {
    while (true) {
      waitForConnection(socket);
    }
  }

  private void waitForConnection(ServerSocket socket) {
    try {
      handleClient(socket.accept());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void handleClient(Socket socket) {
    int requestNumber = requestCounter.incrementAndGet();
    System.out.println("Client connected, starting handler thread: " + requestNumber);
    (new Thread(() -> handleProtocol(socket), "Handler-" + requestNumber)).start();
  }

  private void handleProtocol(Socket clientSocket) {
    threadCounter.incrementAndGet();
    try (Socket socket = clientSocket) { // we want this thread to close the socket after handling the request
      process(socket);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      threadCounter.decrementAndGet();
    }
  }

  private void process(Socket socket) throws IOException {
    try (InputStream in = socket.getInputStream()) {
      String request = readRequest(in);
      String[] split = request.split(" ");
      System.out.println(request); // debugging
      if (!("GET".equals(split[0]) || (split.length >= 2))) {
        sendInvalidRequest(socket, request);
      } else {
        sendFile(split[1].replaceAll("^/", ""), socket);
      }
    }
  }

  private String readRequest(InputStream in) throws IOException {
    BufferedReader input = new BufferedReader(new InputStreamReader(in));
    return input.readLine();
  }

  private void sendFile(String file, Socket socket) throws IOException {
    Path path = rootDir.resolve(file);
    try (OutputStream out = socket.getOutputStream()) {
      if (path.toFile().exists()) {
        write(out, 200, "OK");
        Files.copy(path, out);
      } else {
        write(out, 404, "No such file: " + file);
      }
    }
  }

  private void sendInvalidRequest(Socket socket, String request)
      throws IOException {

    try (OutputStream out = socket.getOutputStream()) {
      write(out, 400, "Invalid request: " + request);
    }
  }

  private void write(OutputStream out, int code, String reason)
      throws IOException {

    PrintWriter writer = new PrintWriter(out, true);
    writer.println("HTTP/1.1 " + code + " " + reason);
    writer.println("Content-Type: text/plain\n");
    writer.println();
    if (code != 200) {
      writer.println(reason);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int getRequestCount() {
    return requestCounter.get();
  }

  /** {@inheritDoc} */
  @Override
  public void setRequestCount(int value) {
    requestCounter.set(value);
  }

  /** {@inheritDoc} */
  @Override
  public int getThreadCount() {
    return threadCounter.get();
  }

}
