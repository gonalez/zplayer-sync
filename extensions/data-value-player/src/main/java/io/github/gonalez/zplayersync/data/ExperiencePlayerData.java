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

import org.bukkit.entity.Player;

/** Data for level experience of players. */
public class ExperiencePlayerData implements PlayerDataApi<Float> {
  public static final String IDENTIFIER = "experience";


  @Override
  public Class<Float> type() {
    return Float.class;
  }

  @Override
  public String identifier() {
    return IDENTIFIER;
  }

  @Override
  public Float read(Player input) {
    return input.getExp();
  }

  @Override
  public void set(Player input, Float aFloat) {
    input.setExp(aFloat);
  }
}
