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
import io.github.gonalez.zplayersync.data.values.SQLiteConnectionFactory;
import io.github.gonalez.zplayersync.data.values.PlayersValueApi;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.IOException;

public class PlayerSyncPlugin extends JavaPlugin implements Listener {
  @Nullable
  private PlayerSyncModule pluginModule;

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);

    try {
      pluginModule = new PlayerSyncPluginModule(
          new SQLiteConnectionFactory(getDataFolder().toPath().resolve("test.db")),
          new Gson());

      try {
        pluginModule.init();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      pluginModule.initializePlayerValueApi(new PlayersValueApi<Double>() {
        @Override
        public Class<Double> type() {
          return double.class;
        }

        @Override
        public String identifier() {
          return "health";
        }

        @Override
        public Double read(Player input) {
          return input.getHealth();
        }

        @Override
        public void set(Player input, Double aDouble) {
          input.setHealth(aDouble);
        }
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
