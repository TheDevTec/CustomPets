package me.devtec.custompets.pathfinders;

import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.ai.goal.PathfinderGoalMeleeAttack;

public class PathfinderGoalAttackMobs extends PathfinderGoalMeleeAttack {
	
	private final EntityCreature pet;
	
	private int ticks;
	
	public PathfinderGoalAttackMobs(EntityCreature mob, double speed, boolean pauseWhenMobIdle) {
		super(mob, speed, pauseWhenMobIdle);
	    this.pet = mob;
	}
	
	public void c() {
		super.c();
	    this.ticks = 0;
	}
	
	public void e() {
		if(pet.getGoalTarget()==null)return;
		super.e();
    	pet.setAggressive(ticks++ >= 5 && j() < k() / 2);
	}
}