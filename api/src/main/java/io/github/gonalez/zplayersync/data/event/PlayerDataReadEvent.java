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
package io.github.gonalez.zplayersync.data.event;

import io.github.gonalez.zplayersync.data.value.PlayersValueApi;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.UUID;

/** Event called when {@link io.github.gonalez.zplayersync.data.value.PlayerDataReadWriter#read(UUID)}. */
public class PlayerDataReadEvent extends Event implements Cancellable {
  private static final HandlerList handlers = new HandlerList();

  private final UUID playerUUID;
  private final List<PlayersValueApi<?>> valueApis;

  private boolean cancelled = false;

  public PlayerDataReadEvent(
      UUID playerUUID,
      List<PlayersValueApi<?>> valueApis) {
    this.playerUUID = playerUUID;
    this.valueApis = valueApis;
  }

  public UUID getPlayerUUID() {
    return playerUUID;
  }

  public List<PlayersValueApi<?>> getValueApis() {
    return valueApis;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.cancelled = cancel;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
