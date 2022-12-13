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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import io.github.gonalez.zplayersync.data.event.PlayerDataReadEvent;
import io.github.gonalez.zplayersync.data.value.PlayerDataReadWriter;
import io.github.gonalez.zplayersync.data.value.PlayersValueApi;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/** Listener to {@link PlayerDataReadWriter} write & read the values of players when entering and leaving the server. */
public class PlayerSyncListener implements Listener {
  private final PlayerDataReadWriter dataReadWriter;

  public PlayerSyncListener(PlayerDataReadWriter dataReadWriter) {
    this.dataReadWriter = checkNotNull(dataReadWriter);
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent joinEvent) {
    UUID playerUUID = joinEvent.getPlayer().getUniqueId();
    ImmutableList<PlayersValueApi<?>> valueApis =
        dataReadWriter.read(playerUUID);

    PlayerDataReadEvent dataWriteEvent = new PlayerDataReadEvent(playerUUID, valueApis);
    Bukkit.getServer().getPluginManager().callEvent(dataWriteEvent);

    if (!dataWriteEvent.isCancelled()) {
      valueApis.stream()
          .filter(PlayersValueApi::isStandalone)
          .forEach(playersValueApi -> playersValueApi.set(joinEvent.getPlayer()));
    }
  }

  @EventHandler
  public void onLeave(PlayerQuitEvent quitEvent) {
    dataReadWriter.write(quitEvent.getPlayer().getUniqueId());
  }
}
