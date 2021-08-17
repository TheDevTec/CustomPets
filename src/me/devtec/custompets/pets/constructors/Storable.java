package me.devtec.custompets.pets.constructors;

import org.bukkit.inventory.Inventory;

import net.minecraft.world.entity.item.EntityItem;

public interface Storable {
	public boolean canPickup();
	
	public void allowPickup(boolean status);
	
	public Inventory getLoot();
	
	public void pickupLoot(EntityItem item);
	
	public EntityItem getPickupLoot();
}
