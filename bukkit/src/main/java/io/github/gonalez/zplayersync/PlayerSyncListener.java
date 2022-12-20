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
import io.github.gonalez.zplayersync.data.PlayerDataReadWriter;
import io.github.gonalez.zplayersync.data.PlayerDataApi;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

/** Listener to {@link PlayerDataReadWriter} write & read the values of players when entering and leaving the server. */
class PlayerSyncListener implements Listener {
  private final PlayerDataReadWriter dataReadWriter;
  private final Plugin plugin;

  public PlayerSyncListener(
      PlayerDataReadWriter dataReadWriter,
      Plugin plugin) {
    this.dataReadWriter = checkNotNull(dataReadWriter);
    this.plugin = checkNotNull(plugin);
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent joinEvent) {
    // Wait a bit to get the latest data from mysql, even though we are using row locking when a player
    // goes from one server to another it can be so fast that mysql doesn't have time to acquire the
    // lock of rows when in multiple client. // TODO (gonalez): Find a better way for this...
    plugin.getServer().getScheduler()
        .runTaskLater(plugin, () -> {
          ImmutableList<PlayerDataApi<?>> playersValueApis =
              dataReadWriter.read(joinEvent.getPlayer().getUniqueId());

          PlayerDataReadEvent dataWriteEvent =
              new PlayerDataReadEvent(joinEvent.getPlayer().getUniqueId(), new ArrayList<>(playersValueApis));
          Bukkit.getServer().getPluginManager().callEvent(dataWriteEvent);

          if (!dataWriteEvent.isCancelled()) {
            dataWriteEvent.getValueApis().stream()
                .filter(PlayerDataApi::isStandalone)
                .forEach(valueApi -> valueApi.set(joinEvent.getPlayer()));
          }
          }, 2);
  }

  @EventHandler
  public void onLeave(PlayerQuitEvent quitEvent) {
    dataReadWriter.write(quitEvent.getPlayer().getUniqueId());
  }
}
