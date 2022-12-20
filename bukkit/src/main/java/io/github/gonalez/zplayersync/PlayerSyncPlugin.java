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
import io.github.gonalez.zplayersync.data.*;
import io.github.gonalez.zplayersync.serializer.InventorySerializer;
import io.github.gonalez.zplayersync.serializer.LocationSerializer;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
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
      switch (DatabaseType.valueOf(fileConfiguration.getString("database.type"))) {
        case MYSQL:
          ConnectionFactory connectionFactory =
              new MySQLConnectionFactory(
                  fileConfiguration.getString("database.url"),
                  fileConfiguration.getString("database.user"),
                  fileConfiguration.getString("database.pass"));
          pluginModule = new PlayerSyncPluginModule(connectionFactory,
              new Gson(),
              ImmutableList.copyOf(
                  fileConfiguration.getConfigurationSection("enabled_values").getKeys(false)
                      .stream()
                      .filter(s -> !fileConfiguration.getBoolean("enabled_values." + s))
                      .collect(Collectors.toList())));
          break;
      }

      // built-in serializers
      pluginModule.registerSerializer(Location.class, new LocationSerializer());
      pluginModule.registerSerializer(Inventory.class, new InventorySerializer());

      ImmutableList.of(
          new HealthPlayerData(),
          new FoodPlayerData(),
          new LocationPlayerData(),
          new InventoryPlayerData(),
          new LevelPlayerData(),
          new ExperiencePlayerData())
          .forEach(valueApi ->
              pluginModule.initializePlayerValueApi(valueApi));

      pluginModule.init();

      PlayerDataReadWriter playerDataReadWriter = pluginModule.getDataReadWriter();
      if (playerDataReadWriter == null) {
        String msg = "PlayerDataReadWriter not found, could not fully initialize the plugin.";
        getLogger().log(Level.WARNING, msg);
      } else {
        playerDataReadWriter.open();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(
            new PlayerSyncListener(playerDataReadWriter, this), this);
      }
    } catch (Exception e) {
      throw new RuntimeException("Cannot initialize plugin", e);
    }
  }

  @Override
  public void onDisable() {
    if (pluginModule != null) {
      PlayerDataReadWriter dataReadWriter = pluginModule.getDataReadWriter();
      if (dataReadWriter != null) {
        dataReadWriter.close();
      }
    }
  }

  @Nullable
  public PlayerSyncModule getPluginModule() {
    return pluginModule;
  }
}
