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

import java.io.File;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * The {@link HttpServer} launcher.
 *
 * @author morisil
 */
public class HttpServerLauncher {

  /**
   * Creates the {@link HttpServer} instance and starts it.
   *
   * @param args one integer value denoting HTTP server port.
   * @throws Exception in case of JMX related error.
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.out.println("please specify port of the HTTP server and root dir");
      System.exit(1);
    }
    int port = Integer.parseInt(args[0]);
    File rootDir = new File(args[1]);
    if (!rootDir.exists()) {
      System.out.println("root dir doesn't exist: " + rootDir.getAbsolutePath());
    }
    HttpServer httpServer = new HttpServer(port, rootDir.toPath());
    httpServer.start();
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    ObjectName name = new ObjectName("org.szluug:type=HttpServerMBean");
    mbs.registerMBean(httpServer, name);
  }

}
