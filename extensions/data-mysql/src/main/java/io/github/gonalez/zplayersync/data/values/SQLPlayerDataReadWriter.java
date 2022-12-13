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

import com.google.common.collect.ImmutableList;
import io.github.gonalez.zplayersync.data.serializer.ObjectSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public abstract class SQLPlayerDataReadWriter implements PlayerDataReadWriter {
  private final ConnectionFactory connectionProvider;

  public SQLPlayerDataReadWriter(ConnectionFactory connectionProvider) {
    this.connectionProvider = connectionProvider;
  }

  @Nullable
  protected abstract <T> ObjectSerializer<T> findSerializerOfType(Class<T> type);

  /** List of all available {@link PlayersValueApi} to be used. */
  protected abstract ImmutableList<PlayersValueApi<?>> providePlayerValues();

  public Player getPlayer(UUID uuid) {
    return Bukkit.getPlayer(uuid);
  }

  @Override
  public ImmutableList<PlayersValueApi<?>> read(UUID uuid) {
    ImmutableList.Builder<PlayersValueApi<?>> apiBuilder = ImmutableList.builder();
    try (Connection connection = connectionProvider.create()) {
      for (PlayersValueApi valueApi : providePlayerValues()) {
        PreparedStatement preparedStatement =
            connection.prepareStatement(String.format(
                "SELECT data FROM %s WHERE uuid = ?", valueApi.identifier()));
        preparedStatement.setString(1, uuid.toString());
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
          ObjectSerializer<?> objectSerializer = findSerializerOfType(valueApi.type());
          if (objectSerializer == null) {
            continue;
          }

          Object data = objectSerializer.deserialize(rs.getString(1));
          PlayersValueApi<?> playersValueApi = new PlayersValueApi<Object>() {
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
              valueApi.set(input, data);
            }

            @Override
            public boolean isStandalone() {
              return true;
            }
          };
          apiBuilder.add(playersValueApi);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return apiBuilder.build();
  }

  private void setupPlayerDataValueTables(
      Connection connection, PlayersValueApi<?> value) throws SQLException {
    connection
        .createStatement()
        .executeUpdate(String.format(
            "CREATE TABLE IF NOT EXISTS %s "
                + "(uuid BLOB PRIMARY KEY NOT NULL, "
                + "data BLOB NOT NULL) "
                + "WITHOUT ROWID", value.identifier()));
  }

  @Override
  public void write(UUID uuid) {
    Player player = getPlayer(uuid);
    if (player == null) {
      return;
    }
    try (Connection connection = connectionProvider.create()) {
      for (PlayersValueApi<?> value : providePlayerValues()) {
        setupPlayerDataValueTables(connection, value);

        ObjectSerializer serializer = findSerializerOfType(value.type());
        if (serializer == null) {
          continue;
        }


        String serializedValue = serializer.serialize(value.read(player));
        if (serializedValue == null) {
         continue;
        }

        PreparedStatement preparedStatement =
            connection.prepareStatement(String.format(
                "INSERT OR REPLACE into %s (uuid, data) VALUES (?, ?)", value.identifier()));
        preparedStatement.setString(1, uuid.toString());
        preparedStatement.setString(2, serializedValue);
        preparedStatement.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
