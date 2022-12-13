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

package io.github.gonalez.zplayersync.data.values;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnectionFactory implements ConnectionFactory {
  private final String path;

  public SQLiteConnectionFactory(Path path) throws IOException {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException classNotFoundException) {
      throw new IOException(classNotFoundException);
    }
    File parentFile = path.getParent().toFile();
    if (!parentFile.exists() && !parentFile.mkdirs()) {
      throw new IOException("Failed to create parent file for path: " + path);
    }
    this.path = path.toString();
  }

  @Override
  public Connection create() throws SQLException {
    return DriverManager.getConnection("jdbc:sqlite:" + path);
  }
}
