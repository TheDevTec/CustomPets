package me.devtec.custompets.pets.constructors;

import net.minecraft.world.entity.item.EntityItem;
import org.bukkit.inventory.Inventory;

public interface Storable {
    public boolean canPickup();

    public void allowPickup(boolean status);

    public Inventory getLoot();

    public void pickupLoot(EntityItem item);

    public EntityItem getPickupLoot();
}
