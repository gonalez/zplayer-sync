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
import org.bukkit.inventory.Inventory;

/** Value for inventory of players. */
public class InventoryPlayersValueApi implements PlayersValueApi<Inventory> {
  public static final String IDENTIFIER = "inventory";

  @Override
  public Class<Inventory> type() {
    return Inventory.class;
  }

  @Override
  public String identifier() {
    return IDENTIFIER;
  }

  @Override
  public Inventory read(Player input) {
    return input.getInventory();
  }

  @Override
  public void set(Player input, Inventory inventory) {
    input.getInventory().setContents(inventory.getContents());
  }
}
