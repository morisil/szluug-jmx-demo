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

/**
 * JMX Management bean definition for the {@link HttpServer}.
 *
 * @author morisil
 */
public interface HttpServerMBean {

  /**
   * Returns number of HTTP client request since start of the server.
   *
   * @return the request count.
   */
  int getRequestCount();

  /**
   * Resets the request counter with given {@code value}.
   *
   * @param value the value of request counter to set.
   */
  void setRequestCount(int value);

  /**
   * Returns number of concurrent threads handling client requests.
   *
   * @return the thread count.
   */
  int getThreadCount();

}
