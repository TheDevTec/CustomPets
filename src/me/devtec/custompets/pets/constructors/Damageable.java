package me.devtec.custompets.pets.constructors;

import net.minecraft.world.entity.EntityLiving;

public interface Damageable extends Pet {
	
	public boolean tryAttack(EntityLiving entity);
	
	public double getDamage();
	
	public void setDamage(double damage);
	
}
