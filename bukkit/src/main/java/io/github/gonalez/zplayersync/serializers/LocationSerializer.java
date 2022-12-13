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
package io.github.gonalez.zplayersync.serializers;

import com.google.common.base.Splitter;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Type;
import java.util.List;

/** Gson deserializer & serializer for bukkit locations. */
public class LocationSerializer implements JsonSerializer<Location>, JsonDeserializer<Location> {

  private static final char SPLIT_CH = ':';
  private static final Splitter SPLITTER = Splitter.on(SPLIT_CH);

  @Override
  public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    List<String> splitJsonToList = SPLITTER.splitToList(json.getAsString());
    return new Location(
        Bukkit.getWorld(splitJsonToList.get(0)),
        Double.parseDouble(splitJsonToList.get(1)),
        Double.parseDouble(splitJsonToList.get(2)),
        Double.parseDouble(splitJsonToList.get(3)),
        Float.parseFloat(splitJsonToList.get(4)),
        Float.parseFloat(splitJsonToList.get(5)));
  }

  @Override
  public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(
        new StringBuilder(src.getWorld().getName())
            .append(SPLIT_CH)
            .append(src.getX())
            .append(SPLIT_CH)
            .append(src.getY())
            .append(SPLIT_CH)
            .append(src.getZ())
            .append(SPLIT_CH)
            .append(src.getYaw())
            .append(SPLIT_CH)
            .append(src.getPitch())
            .toString());
  }
}
