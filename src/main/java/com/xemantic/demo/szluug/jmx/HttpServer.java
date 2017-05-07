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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.io.ByteStreams;

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


  /**
   * Creates HTTP server instance on give {@code port}.
   *
   * @param port the port to bind to.
   */
  public HttpServer(int port) {
    this.port = port;
  }

  /**
   * Starts the server.
   */
  public void start() {
    (new Thread("RequestDispatcher") {
      @Override
      public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
          while (true) {
            handleClient(serverSocket.accept());
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  private void handleClient(final Socket clientSocket) {
    requestCounter.incrementAndGet();
    (new Thread("Handler-" + threadCounter.getAndIncrement()) {
      @Override
      public void run() {
        System.out.println("Client connected, starting thread: " + getName());
        try (Socket socket = clientSocket) {
          try (InputStream in = socket.getInputStream()) {
            String[] request = readRequest(in);
            System.out.println(Arrays.asList(request));
            if (!("GET".equals(request[0]) || (request.length >= 2))) {
              respondWithError(socket, "Invalid request: " + request, 400);
            } else {
              sendFile(request[1], clientSocket);
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          threadCounter.decrementAndGet();
        }
      }
    }).start();
  }

  private String[] readRequest(InputStream in) throws IOException {
    BufferedReader input = new BufferedReader(new InputStreamReader(in));
    return input.readLine().split(" ");
  }

  private void sendFile(String file, Socket socket) throws IOException {
    try (FileInputStream in = new FileInputStream(file)) {
      try (OutputStream out = socket.getOutputStream()) {
        writeHeaders(out, 200, "OK");
        ByteStreams.copy(in, out);
      }
    } catch (FileNotFoundException e) {
      respondWithError(socket, e.getMessage(), 404);
    }
  }

  private void respondWithError(Socket socket, String error, int code)
      throws IOException {

    try (OutputStream out = socket.getOutputStream()) {
      writeHeaders(out, code, error);
      out.write(error.getBytes());
    }
  }

  private void writeHeaders(OutputStream out, int status, String reason)
      throws IOException {

    out.write(("HTTP/1.1 " + status + " " + reason).getBytes());
    out.write("Content-Type: text/plain\n".getBytes());
    out.write("\n".getBytes());
  }

  @Override
  public int getRequestCounter() {
    return requestCounter.get();
  }

  @Override
  public void resetRequestCounter(int value) {
    requestCounter.set(value);
  }

}
