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
import com.google.gson.GsonBuilder;
import io.github.gonalez.zplayersync.data.value.FoodPlayersValueApi;
import io.github.gonalez.zplayersync.data.value.HealthPlayersValueApi;
import io.github.gonalez.zplayersync.data.value.LocationPlayersValueApi;
import io.github.gonalez.zplayersync.data.value.MySQLConnectionFactory;
import io.github.gonalez.zplayersync.data.value.PlayerDataReadWriter;
import io.github.gonalez.zplayersync.serializers.LocationSerializer;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.logging.Level;
import java.util.stream.Collectors;

/** The main class of the plugin. */
public class PlayerSyncPlugin extends JavaPlugin {

  @Nullable
  private PlayerSyncModule pluginModule;

  @Override
  public void onEnable() {
    FileConfiguration fileConfiguration = getConfig();

    getConfig().options().copyDefaults(true);
    saveConfig();

    try {
      final DatabaseType databaseType;
      try {
        databaseType = DatabaseType.valueOf(fileConfiguration.getString("database.type"));
      } catch (IllegalArgumentException exception) {
        throw exception;
      }

      switch (databaseType) {
        case MYSQL:
          pluginModule = new PlayerSyncPluginModule(
              new MySQLConnectionFactory(
                  getDataFolder().toPath().resolve("playersync.db"),
                  fileConfiguration.getString("database.url"),
                  fileConfiguration.getString("database.user"),
                  fileConfiguration.getString("database.pass")),
              new GsonBuilder()
                  .registerTypeAdapter(Location.class, new LocationSerializer())
                  .create(),
              ImmutableList.copyOf(
                  fileConfiguration.getConfigurationSection("enabled_values").getKeys(false)
                      .stream()
                      .filter(s -> !fileConfiguration.getBoolean("enabled_values." + s))
                      .collect(Collectors.toList())));
          break;
      }

      ImmutableList.of(
          new HealthPlayersValueApi(),
          new FoodPlayersValueApi(),
          new LocationPlayersValueApi())
          .forEach(valueApi ->
              pluginModule.initializePlayerValueApi(valueApi));

      pluginModule.init();

      PluginManager pluginManager = getServer().getPluginManager();

      PlayerDataReadWriter playerDataReadWriter = pluginModule.getDataReadWriter();
      if (playerDataReadWriter == null) {
        String msg = "PlayerDataReadWriter not found, could not fully initialize the plugin.";
        getLogger().log(Level.WARNING, msg);
      } else {
        pluginManager.registerEvents(new PlayerSyncListener(playerDataReadWriter), this);
      }
    } catch (Exception e) {
      throw new RuntimeException("Cannot initialize plugin", e);
    }
  }
}
