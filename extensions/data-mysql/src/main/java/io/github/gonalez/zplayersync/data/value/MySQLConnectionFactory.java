/*
 * Copyright 2022 - Gaston Gonzalez (Gonalez). and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.gonalez.zplayersync.data.value;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Implementation of connection factory for mysql connections. */
public class MySQLConnectionFactory implements ConnectionFactory {
  private final String url, username, password;

  public MySQLConnectionFactory(String url, String username, String password) throws IOException {
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException classNotFoundException) {
      throw new IOException(classNotFoundException);
    }
    this.url = url;
    this.username = username;
    this.password = password;
  }

  @Override
  public Connection create() throws SQLException {
    return DriverManager.getConnection(url, username, password);
  }
}
