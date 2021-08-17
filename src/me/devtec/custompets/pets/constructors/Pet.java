package me.devtec.custompets.pets.constructors;

import org.bukkit.Location;
import org.bukkit.entity.Creature;

import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;

public interface Pet {
	public void teleport(Location location);
	
	public String getName();
	
	public void setName(String name);
	
	public double getHealth();
	
	public void setHealth(double hp);
	
	public double getFood();
	
	public void setFood(double food);
	
	public Location getLocation();
	
	public Creature getEntity();
	
	public EntityPlayer getOwner();
	
	public PathfinderGoalSelector getPathSelector();
	
	public EntityCreature getNmsEntity();
	
	public void despawn();
	
	public void respawn();
	
	public boolean isAlive();

	public void damageByHunger();

	public String asString();
	
	public double getRespawnCost();
	
	public void setRespawnCost(double cost);
}
