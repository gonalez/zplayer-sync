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

import io.github.gonalez.zplayersync.data.value.PlayerDataReadWriter;
import io.github.gonalez.zplayersync.data.value.PlayersValueApi;
import io.github.gonalez.zplayersync.serializer.ObjectSerializer;

// TODO: javadoc
public abstract class PlayerSyncModule {

  /** Initializes the module. */
  public void init() throws Exception {}

  public PlayerDataReadWriter getDataReadWriter() {
    return null;
  }

  /** Sets up the given value api into this module. */
  public <T> void initializePlayerValueApi(PlayersValueApi<T> valueApi) {}

  public <T> void registerSerializer(Class<T> type, ObjectSerializer<T> serializer) {}
}
