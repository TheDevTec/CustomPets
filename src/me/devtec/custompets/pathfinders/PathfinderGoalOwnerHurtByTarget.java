package me.devtec.custompets.pathfinders;

import java.util.EnumSet;

import org.bukkit.event.entity.EntityTargetEvent;

import me.devtec.custompets.pets.constructors.Damageable;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalTarget;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;


public class PathfinderGoalOwnerHurtByTarget extends PathfinderGoalTarget {
  private Damageable pet;
  
  private EntityLiving target;
  
  public PathfinderGoalOwnerHurtByTarget(Damageable pet) {
    super(pet.getNmsEntity(), false);
    this.pet=pet;
    a(EnumSet.of(PathfinderGoal.Type.d));
  }
  
  public boolean a() {
      if(!pet.tryAttack(pet.getOwner().getLastDamager()))return false;
      target = pet.getOwner().getLastDamager();
      return a(target, PathfinderTargetCondition.a);
  }
  
  public void c() {
    this.e.setGoalTarget(target, EntityTargetEvent.TargetReason.TARGET_ATTACKED_OWNER, true);
    super.c();
  }
}