package me.devtec.custompets.pets;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftCreature;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.devtec.custompets.pathfinders.PathfinderGoalAttackMobs;
import me.devtec.custompets.pathfinders.PathfinderGoalFollowPlayer;
import me.devtec.custompets.pathfinders.PathfinderGoalHurtByTarget;
import me.devtec.custompets.pathfinders.PathfinderGoalOwnerHurtByTarget;
import me.devtec.custompets.pets.constructors.Defendable;
import me.devtec.custompets.pets.constructors.Pet;
import me.devtec.theapi.TheAPI;
import me.devtec.theapi.utils.json.Writer;
import me.devtec.theapi.utils.reflections.Ref;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;

public class DefendingPet implements Pet, Defendable {

	protected final EntityPlayer owner;
	protected PathfinderGoalSelector pathSelector;
	protected EntityCreature entityCreature;
	protected Creature creature;
	protected double food = 20, defaultFood = 20, defaultHealth = 20, respawnCost;
	protected PathfinderGoal df, dfo;
	
	protected boolean defend, defendOwner;

	public DefendingPet(Player owner, EntityType type) {
		Creature creature = (Creature)owner.getWorld().spawnEntity(owner.getLocation(), type);
		this.creature=creature;
		
		entityCreature=(((CraftCreature)creature).getHandle());
		this.owner=(EntityPlayer)Ref.player(owner);
		pathSelector=entityCreature.bP;
		entityCreature.craftAttributes.registerAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
		df=new PathfinderGoalHurtByTarget(this);
		dfo=new PathfinderGoalOwnerHurtByTarget(this);
		pathSelector.a(); //clear paths
		pathSelector.a(0, new PathfinderGoalFloat(entityCreature)); //water
		pathSelector.a(1, new PathfinderGoalAttackMobs(entityCreature, 1, false)); //attack
		pathSelector.a(3, new PathfinderGoalFollowPlayer(this)); //follow owner
	}
	
	public boolean tryAttack(EntityLiving entityliving) {
		return entityliving != this.owner;
	}
	
	public void teleport(Location loc) {
		if(creature.getWorld()!=loc.getWorld()) {
			creature.teleport(loc);
			creature.setNoDamageTicks(40);
			entityCreature=(((CraftCreature)creature).getHandle());
			
			pathSelector=entityCreature.bP;
			entityCreature.craftAttributes.registerAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
			setDamage(getDamage());
			df=new PathfinderGoalHurtByTarget(this);
			dfo=new PathfinderGoalOwnerHurtByTarget(this);
			pathSelector.a(); //clear paths
			pathSelector.a(0, new PathfinderGoalFloat(entityCreature)); //water
			pathSelector.a(1, new PathfinderGoalAttackMobs(entityCreature, 1, false)); //attack
			pathSelector.a(3, new PathfinderGoalFollowPlayer(this)); //follow owner
		}else
		creature.teleport(loc);
	}
	
	public boolean isAlive() {
		return !creature.isDead();
	}
	
	public void despawn() {
		creature.remove();
	}
	
	public void respawn() {
		food=defaultFood;
		
		double d = getDamage();
		Creature creature = (Creature)this.owner.getBukkitEntity().getWorld().spawnEntity(this.owner.getBukkitEntity().getLocation(), this.creature.getType());
		this.creature=creature;
		creature.setMaxHealth(defaultHealth);
		creature.setHealth(defaultHealth);
		entityCreature=(((CraftCreature)creature).getHandle());
		
		pathSelector=entityCreature.bP;
		entityCreature.craftAttributes.registerAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
		setDamage(d);
		df=new PathfinderGoalHurtByTarget(this);
		dfo=new PathfinderGoalOwnerHurtByTarget(this);
		pathSelector.a(); //clear paths
		pathSelector.a(0, new PathfinderGoalFloat(entityCreature)); //water
		pathSelector.a(1, new PathfinderGoalAttackMobs(entityCreature, 1, false)); //attack
		pathSelector.a(3, new PathfinderGoalFollowPlayer(this)); //follow owner
	}
	
	private String name;
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name=name;
		creature.setCustomName(TheAPI.colorize(name));
		creature.setCustomNameVisible(false);
	}

	@Override
	public double getHealth() {
		return creature.getHealth();
	}

	@Override
	public void setHealth(double hp) {
		creature.setHealth(hp);
	}

	@Override
	public double getFood() {
		return food;
	}

	@Override
	public void setFood(double food) {
		this.food=food;
	}

	@Override
	public Location getLocation() {
		return creature.getLocation();
	}

	@Override
	public Creature getEntity() {
		return creature;
	}

	@Override
	public EntityPlayer getOwner() {
		return owner;
	}

	@Override
	public PathfinderGoalSelector getPathSelector() {
		return pathSelector;
	}

	@Override
	public EntityCreature getNmsEntity() {
		return entityCreature;
	}

	@Override
	public boolean isAttacking() {
		return entityCreature.isAggressive();
	}

	@Override
	public void setDefending(boolean status) {
		defend=status;
		if(status) {
			pathSelector.a(2, df);
		}else {
			pathSelector.a(df);
		}
	}

	@Override
	public boolean isDefending() {
		return defend;
	}

	@Override
	public void setDefendingOwner(boolean status) {
		defendOwner=status;
		if(status) {
			pathSelector.a(1, dfo);
		}else {
			pathSelector.a(dfo);
		}
	}

	@Override
	public boolean isDefendingOwner() {
		return defendOwner;
	}
	
	@Override
	public void damageByHunger() {
		getEntity().damage(0.05);
	}
	
	public String toString() {
		return asString();
	}

	@Override
	public String asString() {
		Map<String, Object> map = new HashMap<>();
		map.put("type", "DEFENDING");
		map.put("name", getName()+"");
		map.put("food", food);
		map.put("health", creature.getHealth());
		map.put("max_health", defaultHealth);
		map.put("max_food", defaultFood);
		map.put("defend", defend);
		map.put("respawn_cost", respawnCost);
		map.put("damage", getDamage());
		map.put("defend_owner", defendOwner);
		map.put("entity", creature.getType().name());
		return Writer.write(map);
	}
	
	public static Pet fromString(Player owner, Map<String, Object> json) {
		DefendingPet pet = new DefendingPet(owner, EntityType.fromName(json.get("entity")+""));
		if((boolean)json.getOrDefault("defend",true))
			pet.setDefending(true);
		if((boolean)json.getOrDefault("defendOwner",true))
			pet.setDefendingOwner(true);
		pet.defaultHealth=(double)json.get("max_health");
		pet.defaultFood=(double)json.get("max_food");
		pet.getEntity().setMaxHealth(pet.defaultHealth);
		pet.setName(""+json.get("name"));
		if((double)json.get("health")<=0) {
			pet.setHealth(0);
			pet.despawn();
		}else
			pet.setHealth((double)json.get("health"));
		pet.setDamage((double)json.get("damage"));
		pet.respawnCost=(double)json.get("respawn_cost");
		pet.food=(double)json.get("food");
		return pet;
	}

	@Override
	public double getRespawnCost() {
		return respawnCost;
	}

	@Override
	public void setRespawnCost(double cost) {
		respawnCost=cost;
	}

	@Override
	public double getDamage() {
		return entityCreature.craftAttributes.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue();
	}

	@Override
	public void setDamage(double damage) {
		entityCreature.craftAttributes.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage);
	}

}
