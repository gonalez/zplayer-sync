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
package io.github.gonalez.zplayersync.data;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/** Data for {@code Location}. */
public class LocationPlayerData implements PlayerDataApi<Location> {
  public static final String IDENTIFIER = "location";

  @Override
  public Class<Location> type() {
    return Location.class;
  }

  @Override
  public String identifier() {
    return IDENTIFIER;
  }

  @Override
  public Location read(Player input) {
    return input.getLocation();
  }

  @Override
  public void set(Player input, Location location) {
    input.teleport(location);
  }
}
