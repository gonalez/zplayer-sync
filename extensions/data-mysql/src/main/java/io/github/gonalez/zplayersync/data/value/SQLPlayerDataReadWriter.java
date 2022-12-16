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

import com.google.common.collect.ImmutableList;
import io.github.gonalez.zplayersync.serializer.ObjectSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/** Base class for PlayerDataReadWriter which reads and writes via sql. */
public abstract class SQLPlayerDataReadWriter implements PlayerDataReadWriter {
  private static final String SELECT_DATA_SQL = "SELECT data FROM %s WHERE uuid = ? FOR UPDATE";
  private static final String UPDATE_DATA_SQL = "%s INTO %s (uuid, data) VALUES (?, ?)";

  private final ConnectionFactory connectionProvider;

  private volatile boolean opened;

  protected Connection connection;

  public SQLPlayerDataReadWriter(ConnectionFactory connectionProvider) {
    this.connectionProvider = connectionProvider;
  }

  public synchronized boolean isOpened() {
    return opened;
  }

  @Override
  public synchronized void open() {
    if (isOpened())
      return;

    try {
      connection = connectionProvider.create();
      connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

      connection.setAutoCommit(false);
    } catch (SQLException sqlException) {
      throw new RuntimeException(sqlException);
    }

    opened = true;
  }

  @Override
  public synchronized void close() {
    if (!isOpened())
      return;

    try {
      connection.close();
    } catch (SQLException sqlException) {
      throw new RuntimeException(sqlException);
    }

    opened = false;
  }

  /** Finds the appropriate object serializer of the given class or {@code null} if not found. */
  @Nullable
  protected abstract <T> ObjectSerializer<T> findSerializerOfType(Class<T> type);

  /** List of all available {@link PlayerDataApi} to be used. */
  protected abstract ImmutableList<PlayerDataApi<?>> providePlayerValues();

  /** @return the online-player matching the given uuid. */
  public Player getPlayer(UUID uuid) {
    return Bukkit.getPlayer(uuid);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public synchronized ImmutableList<PlayerDataApi<?>> read(UUID uuid) {
    open();

    ImmutableList.Builder<PlayerDataApi<?>> dataApiBuilder = ImmutableList.builder();
    try {
      for (PlayerDataApi dataApi : providePlayerValues()) {
        // Create tables
        createPlayersValueTable(dataApi);

        try (PreparedStatement preparedStatement = connection.prepareStatement(
            String.format(SELECT_DATA_SQL, dataApi.identifier()))) {
          preparedStatement.setString(1, uuid.toString());
          try (ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
              ObjectSerializer<?> objectSerializer = findSerializerOfType(dataApi.type());
              if (objectSerializer != null) {
                Object data = objectSerializer.deserialize(resultSet.getString(1));
                dataApiBuilder.add(new PlayerDataApi<Object>() {
                  @Override
                  public Class<Object> type() {
                    return (Class<Object>) dataApi.type();
                  }

                  @Override
                  public String identifier() {
                    return dataApi.identifier();
                  }

                  @Nullable
                  @Override
                  public Object read(@Nullable Player input) {
                    return data;
                  }

                  @Override
                  public void set(Player input, Object value) {
                    set(input);
                  }

                  @Override
                  public void set(Player input) {
                    dataApi.set(input, data);
                  }

                  @Override
                  public boolean isStandalone() {
                    return true;
                  }
                });
              }
            }
          }
          connection.commit();
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return dataApiBuilder.build();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public synchronized void write(UUID uuid) {
    open();

    // Check the player for the given uuid. If no player is connected we can
    // skip writing since we need the player to get the appropriate values.
    Player player = getPlayer(uuid);
    if (player == null) {
      return;
    }

    try {
      connection.setAutoCommit(false);
      for (PlayerDataApi<?> dataApi : providePlayerValues()) {
        // Create tables
        createPlayersValueTable(dataApi);

        ObjectSerializer serializer = findSerializerOfType(dataApi.type());
        if (serializer != null) {
          // Serialize the value from the player into a string
          String serializedValue = serializer.serialize(dataApi.read(player));
          if (serializedValue == null) {
            // Skip this value ( no serializer )
            continue;
          }
          try (PreparedStatement preparedStatement = connection.prepareStatement(
              String.format(SELECT_DATA_SQL, dataApi.identifier()))) {
            preparedStatement.setString(1, uuid.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
              try (PreparedStatement insertStatement = connection.prepareStatement(
                  String.format(UPDATE_DATA_SQL,
                      resultSet.next() ? "REPLACE" : "INSERT",
                      dataApi.identifier()))) {
                insertStatement.setString(1, uuid.toString());
                insertStatement.setString(2, serializedValue);
                insertStatement.executeUpdate();
              }
            }
          }
        }
      }
      connection.commit();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void createPlayersValueTable(PlayerDataApi<?> value) throws SQLException {
    String createTableSql =
        "CREATE TABLE IF NOT EXISTS " + value.identifier() + " "
            + "(uuid VARCHAR(36) PRIMARY KEY NOT NULL, "
            + "data BLOB NOT NULL)";
    try (Statement statement = connection.createStatement()) {
      statement.execute(createTableSql);
    }
  }
}
