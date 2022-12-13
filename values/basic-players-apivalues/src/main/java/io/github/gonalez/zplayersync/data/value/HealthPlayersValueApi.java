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

import org.bukkit.entity.Player;

/** Value for health level of players. */
public class HealthPlayersValueApi implements PlayersValueApi<Double> {
  public static final String IDENTIFIER = "health";

  @Override
  public Class<Double> type() {
    return Double.class;
  }

  @Override
  public String identifier() {
    return IDENTIFIER;
  }

  @Override
  public Double read(Player input) {
    return input.getHealth();
  }

  @Override
  public void set(Player input, Double aDouble) {
    input.setHealth(aDouble);
  }
}
