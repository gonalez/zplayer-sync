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

import org.bukkit.entity.Player;

import javax.annotation.Nullable;

/** A special value of a player that can be accessed and modified. */
public interface PlayersValueApi<VALUE_TYPE> {
  Class<VALUE_TYPE> type();

  /** @return the unique identifier of the value. */
  String identifier();

  VALUE_TYPE read(Player input);

  void set(Player input,  VALUE_TYPE value_type);

  /**
   * Applies a new value to the player, unlike {@link #set(Player, Object)} is not
   * necessary to explicitly set the value. By default, this method throws an,
   * {@link UnsupportedOperationException}.
   */
  default void set(Player input) {
    throw new UnsupportedOperationException("Not implemented");
  }

  default boolean isStandalone() {
    return false;
  }
}
