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
package io.github.gonalez.zplayersync;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.gonalez.zplayersync.data.ConnectionFactory;
import io.github.gonalez.zplayersync.data.PlayerDataReadWriter;
import io.github.gonalez.zplayersync.data.PlayerDataApi;
import io.github.gonalez.zplayersync.data.SQLPlayerDataReadWriter;
import io.github.gonalez.zplayersync.serializer.ObjectSerializer;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** <internal> */
public class PlayerSyncPluginModule extends PlayerSyncModule {
  private final Object lock = new Object();

  private final Map<Class<?>, ObjectSerializer<?>> serializers = new ConcurrentHashMap<>();

  @Nullable
  protected PlayerDataReadWriter playerDataReadWriter;

  private final ConnectionFactory connectionProvider;

  private final ArrayList<PlayerDataApi<?>> valueApis;

  private final Gson gson;

  private final ImmutableList<String> excludedValueApis;

  public PlayerSyncPluginModule(
      ConnectionFactory connectionProvider,
      @Nullable Gson gson,
      ImmutableList<String> excludedValueApis) {
    this.connectionProvider = connectionProvider;
    this.gson = gson;
    this.excludedValueApis = excludedValueApis;
    valueApis = new ArrayList<>();
  }

  public PlayerSyncPluginModule(
      ConnectionFactory connectionProvider) {
    this(connectionProvider, null, ImmutableList.of());
  }

  @Override
  public PlayerDataReadWriter getDataReadWriter() {
    return playerDataReadWriter;
  }

  @Override
  public void init() {
    playerDataReadWriter = new SQLPlayerDataReadWriter(connectionProvider) {
      @SuppressWarnings("unchecked")
      @Nullable
      @Override
      protected <T> ObjectSerializer<T> findSerializerOfType(Class<T> type) {
        if (!serializers.containsKey(type)) {
          if (gson != null) {
            ObjectSerializer<T> objectSerializer = createSerializerFromGson(type);
            serializers.put(type, objectSerializer);
            return objectSerializer;
          }
          return null;
        }
        return (ObjectSerializer<T>) serializers.get(type);
      }

      @Override
      protected ImmutableList<PlayerDataApi<?>> providePlayerValues() {
        synchronized (lock) {
          return ImmutableList.copyOf(valueApis);
        }
      }
    };
  }

  @Nullable
  <T> ObjectSerializer<T> createSerializerFromGson(Class<T> type) {
    if (gson == null)
      return null;
    Type type1 = TypeToken.get(type).getType();
    return new ObjectSerializer<T>() {
      @Override
      public String serialize(T value) {
        return gson.toJson(value, type1);
      }

      @Override
      public T deserialize(String data) {
        return gson.fromJson(data, type1);
      }
    };
  }

  @Override
  public <T> void initializePlayerValueApi(PlayerDataApi<T> valueApi) {
    if (!excludedValueApis.contains(valueApi.identifier())) {
      synchronized (lock) {
        valueApis.add(valueApi);
      }
    }
  }

  @Override
  public <T> void registerSerializer(Class<T> type, ObjectSerializer<T> serializer) {
    serializers.put(type, serializer);
  }
}
