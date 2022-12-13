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
import java.util.Optional;
import java.util.UUID;

/** Base class for PlayerDataReadWriter which reads and writes via sql. */
public abstract class SQLPlayerDataReadWriter implements PlayerDataReadWriter {
  private final ConnectionFactory connectionProvider;

  public SQLPlayerDataReadWriter(ConnectionFactory connectionProvider) {
    this.connectionProvider = connectionProvider;
  }

  /** Finds the appropriate object serializer of the given class or {@code null} if not found. */
  @Nullable
  protected abstract <T> ObjectSerializer<T> findSerializerOfType(Class<T> type);

  /** List of all available {@link PlayersValueApi} to be used. */
  protected abstract ImmutableList<PlayersValueApi<?>> providePlayerValues();

  /** @return the online-player matching the given uuid. */
  public Player getPlayer(UUID uuid) {
    return Bukkit.getPlayer(uuid);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public synchronized ImmutableList<PlayersValueApi<?>> read(UUID uuid) {
    ImmutableList.Builder<PlayersValueApi<?>> apiBuilder = ImmutableList.builder();
    try (Connection connection = connectionProvider.create()) {
      for (PlayersValueApi valueApi : providePlayerValues()) {
        setupPlayerDataValueTables(connection, valueApi);

        Optional<String> optionalS = findData(connection, valueApi, uuid);
        if (optionalS.isPresent()) {
          ObjectSerializer<?> objectSerializer = findSerializerOfType(valueApi.type());
          if (objectSerializer == null) {
            continue;
          }
          Object data = objectSerializer.deserialize(optionalS.get());
          apiBuilder.add(new PlayersValueApi<Object>() {
            @Override
            public Class<Object> type() {
              return (Class<Object>) valueApi.type();
            }

            @Override
            public String identifier() {
              return valueApi.identifier();
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
              valueApi.set(input, data);
            }

            @Override
            public boolean isStandalone() {
              return true;
            }
          });
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return apiBuilder.build();
  }

  private Optional<String> findData(Connection connection, PlayersValueApi<?> valueApi, UUID uuid)
      throws SQLException {
    PreparedStatement preparedStatement =
        connection.prepareStatement(String.format(
            "SELECT data FROM %s WHERE uuid = ?", valueApi.identifier()));
    preparedStatement.setString(1, uuid.toString());
    ResultSet rs = preparedStatement.executeQuery();
    if (rs.next()) {
      return Optional.of(rs.getString(1));
    }
    return Optional.empty();
  }

  private void setupPlayerDataValueTables(
      Connection connection, PlayersValueApi<?> value) throws SQLException {
    connection
        .createStatement()
        .executeUpdate(String.format(
            "CREATE TABLE IF NOT EXISTS %s "
                + "(uuid VARCHAR(36) PRIMARY KEY NOT NULL, "
                + "data BLOB NOT NULL)", value.identifier()));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public synchronized void write(UUID uuid) {
    Player player = getPlayer(uuid);
    if (player == null) {
      return;
    }
    try (Connection connection = connectionProvider.create()) {
      for (PlayersValueApi<?> valueApi : providePlayerValues()) {
        setupPlayerDataValueTables(connection, valueApi);

        ObjectSerializer serializer = findSerializerOfType(valueApi.type());
        if (serializer == null) {
          continue;
        }

        String serializedValue = serializer.serialize(valueApi.read(player));
        if (serializedValue == null) {
         continue;
        }

        Optional<String> find = findData(connection, valueApi, uuid);
        String queryType = "INSERT";
        if (find.isPresent()) {
          queryType = "REPLACE";
        }

        PreparedStatement preparedStatement =
            connection.prepareStatement(String.format(
                "%s into %s (uuid, data) VALUES (?, ?)", queryType, valueApi.identifier()));
        preparedStatement.setString(1, uuid.toString());
        preparedStatement.setString(2, serializedValue);
        preparedStatement.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}