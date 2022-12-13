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
package io.github.gonalez.zplayersync.serializer;

import io.github.gonalez.zplayersync.ObjectSerializerException;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/** Serializer & deserializer for bukkit inventories. */
public class InventorySerializer implements ObjectSerializer<Inventory> {

  @Override
  public String serialize(Inventory value) {
    int invSize = value.getSize();
    if (invSize % 9 != 0) invSize-=5;
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         BukkitObjectOutputStream outputStream1 = new BukkitObjectOutputStream(outputStream)) {
      outputStream1.writeInt(invSize);
      ItemStack[] items = value.getContents();
      for (int i = 0; i < invSize; i++) {
        outputStream1.writeObject(items[i]);
      }
      return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    } catch (IOException e) {
      throw new ObjectSerializerException(e);
    }
  }

  @Override
  public Inventory deserialize(String data) {
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
         BukkitObjectInputStream inputStream1 = new BukkitObjectInputStream(inputStream)) {
      int len = inputStream1.readInt();
      Inventory inventory = Bukkit.createInventory(null, len);
      for (int i = 0; i < len; i++) {
        inventory.setItem(i, (ItemStack) inputStream1.readObject());
      }
      return inventory;
    } catch (IOException | ClassNotFoundException ex) {
      throw new ObjectSerializerException(ex);
    }
  }
}
