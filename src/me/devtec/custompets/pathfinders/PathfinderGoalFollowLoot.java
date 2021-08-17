package me.devtec.custompets.pathfinders;

import java.util.EnumSet;

import me.devtec.custompets.pets.PickupingPet;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfinderNormal;

public class PathfinderGoalFollowLoot extends PathfinderGoal {
	
	private final PickupingPet pet;
	
	private EntityItem following;
	private final NavigationAbstract navigation;
	
	private int ticks;
	private float penalty;
	
	public PathfinderGoalFollowLoot(PickupingPet pet) {
		this.pet=pet;
		navigation = pet.getNmsEntity().getNavigation();
    	a(EnumSet.of(PathfinderGoal.Type.a, PathfinderGoal.Type.b));
	}
	
 	public boolean a() {
	    EntityItem target = pet.getPickupLoot();
	    if(target==null)return false;
	    if (target.isSpectator())
	      return false;
	    following = target;
	    return true;
 	}
 	
 	public void checkOrRemove() {
		if(following.isRemoved()||!following.isAlive()||!following.valid) {
			pet.removeLoot(following);
			d();
		}
 	}
 	
	public boolean b() {
		if(navigation.m())return false;
		if(following==null)return false;
    	checkOrRemove();
		if(following==null)return false;
		return true;
	}
	
	public void c() {
		ticks = 0;
		penalty = pet.getNmsEntity().a(PathType.i);
		pet.getNmsEntity().a(PathType.i, 0);
	}
	
	public void d() {
		following = null;
		navigation.o();
		pet.getNmsEntity().a(PathType.i, penalty);
	}
	
	public void e() {
		if(following==null)return;
    	checkOrRemove();
		if(following==null)return;
		pet.getNmsEntity().getControllerLook().a(following, 10, pet.getNmsEntity().eZ());
		if (--ticks <= 0) {
			ticks = 10;
			if (!pet.getNmsEntity().isLeashed() && !pet.getNmsEntity().isPassenger()) {
				double r = pet.getNmsEntity().f(following);
				if(r<=3.9) {
					pet.pickupedLoot(following);
					d();
					return;
				}
				if (r >= 144) g();
				else navigation.a(following, dynamicSpeed(r));
			}
		}
	}
	
	public double dynamicSpeed(double range) {
		if(range<=40)return 1;
		if(range<=50)return 1.35;
		if(range<=60)return 1.5;
		if(range<=70)return 1.7;
		if(range<=80)return 1.8;
		if(range<=90)return 1.9;
		return 2;
	}
	
	private int a(int x, int z) {
		return pet.getNmsEntity().getRandom().nextInt(z-x+1)+x;
	}
	
	private void g() {
		BlockPosition blockposition = following.getChunkCoordinates();
		for (int i = 0; i < 10; i++) {
			int j = a(-3, 3);
    		int k = a(-1, 1);
     		int l = a(-3, 3);
     		if(a(blockposition.getX() + j, blockposition.getY() + k, blockposition.getZ() + l))break;
		}
	}
	
	private boolean a(int x, int y, int z) {
		if (Math.abs(x - following.locX()) < 2 && Math.abs(y - following.locZ()) < 2 || !a(new BlockPosition(x,y,z)))
			return false;
		pet.getNmsEntity().setPositionRotation(x+0.5, y, z+0.5, pet.getNmsEntity().getYRot(), pet.getNmsEntity().getXRot());
		navigation.o();
		return true;
	}
	
	private boolean a(BlockPosition blockposition) {
		PathType pathtype = PathfinderNormal.a(pet.getNmsEntity().t, blockposition.i());
		if (pathtype != PathType.c)
			return false;
		return pet.getNmsEntity().t.getCubes(pet.getNmsEntity(), pet.getNmsEntity().getBoundingBox().a(blockposition.e(pet.getNmsEntity().getChunkCoordinates())));
	}
}