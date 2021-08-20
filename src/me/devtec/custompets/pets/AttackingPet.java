package me.devtec.custompets.pets;

import me.devtec.custompets.pathfinders.PathfinderGoalAttackMobs;
import me.devtec.custompets.pathfinders.PathfinderGoalFollowPlayer;
import me.devtec.custompets.pathfinders.PathfinderGoalHurtByTarget;
import me.devtec.custompets.pathfinders.PathfinderGoalOwnerHurtByTarget;
import me.devtec.custompets.pets.constructors.Attackable;
import me.devtec.custompets.pets.constructors.Defendable;
import me.devtec.custompets.pets.constructors.Pet;
import me.devtec.theapi.TheAPI;
import me.devtec.theapi.guiapi.GUI;
import me.devtec.theapi.scheduler.Scheduler;
import me.devtec.theapi.scheduler.Tasker;
import me.devtec.theapi.utils.json.Writer;
import me.devtec.theapi.utils.reflections.Ref;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftCreature;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AttackingPet implements Pet, Defendable, Attackable {

    protected final EntityPlayer owner;
    protected PathfinderGoalSelector pathSelector;
    protected EntityCreature entityCreature;
    protected Creature creature;
    protected double food = 20, defaultFood = 20, defaultHealth = 20, respawnCost;
    protected PathfinderGoal df, dfo, at;

    protected boolean defend, defendOwner, attack;

    public AttackingPet(Player owner, EntityType type) {
        Creature creature = (Creature) owner.getWorld().spawnEntity(owner.getLocation(), type);
        this.creature = creature;

        entityCreature = (((CraftCreature) creature).getHandle());
        this.owner = (EntityPlayer) Ref.player(owner);
        pathSelector = entityCreature.bP;
        entityCreature.craftAttributes.registerAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        at = new PathfinderGoalNearestAttackableTarget<>(entityCreature, EntityInsentient.class, 6, true, false, this::tryAttack);
        df = new PathfinderGoalHurtByTarget(this);
        dfo = new PathfinderGoalOwnerHurtByTarget(this);
        pathSelector.a(); //clear paths
        pathSelector.a(0, new PathfinderGoalFloat(entityCreature)); //water
        pathSelector.a(1, new PathfinderGoalAttackMobs(entityCreature, 1, false)); //attack
        pathSelector.a(3, new PathfinderGoalFollowPlayer(this)); //follow owner
    }

    public boolean tryAttack(EntityLiving entityliving) {
        return entityliving != this.owner;
    }

    public void teleport(Location loc) {
        if (creature.getWorld() != loc.getWorld()) {
            creature.teleport(loc);
            creature.setNoDamageTicks(40);
            entityCreature = (((CraftCreature) creature).getHandle());

            pathSelector = entityCreature.bP;
            entityCreature.craftAttributes.registerAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            setDamage(getDamage());
            at = new PathfinderGoalNearestAttackableTarget<>(entityCreature, EntityInsentient.class, 6, true, false, this::tryAttack);
            df = new PathfinderGoalHurtByTarget(this);
            dfo = new PathfinderGoalOwnerHurtByTarget(this);
            pathSelector.a(); //clear paths
            pathSelector.a(0, new PathfinderGoalFloat(entityCreature)); //water
            pathSelector.a(1, new PathfinderGoalAttackMobs(entityCreature, 1, false)); //attack
            pathSelector.a(3, new PathfinderGoalFollowPlayer(this)); //follow owner
        } else
            creature.teleport(loc);
    }

    public boolean isAlive() {
        return !creature.isDead();
    }

    public void despawn() {
        creature.remove();
    }

    public void respawn() {
        food = defaultFood;

        double d = getDamage();
        Creature creature = (Creature) this.owner.getBukkitEntity().getWorld().spawnEntity(this.owner.getBukkitEntity().getLocation(), this.creature.getType());
        this.creature = creature;
        creature.setMaxHealth(defaultHealth);
        creature.setHealth(defaultHealth);
        entityCreature = (((CraftCreature) creature).getHandle());

        pathSelector = entityCreature.bP;
        entityCreature.craftAttributes.registerAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        setDamage(d);
        at = new PathfinderGoalNearestAttackableTarget<>(entityCreature, EntityInsentient.class, 6, true, false, this::tryAttack);
        df = new PathfinderGoalHurtByTarget(this);
        dfo = new PathfinderGoalOwnerHurtByTarget(this);
        pathSelector.a(); //clear paths
        pathSelector.a(0, new PathfinderGoalFloat(entityCreature)); //water
        pathSelector.a(1, new PathfinderGoalAttackMobs(entityCreature, 1, false)); //attack
        pathSelector.a(3, new PathfinderGoalFollowPlayer(this)); //follow owner
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        creature.setCustomName(TheAPI.colorize(name));
        creature.setCustomNameVisible(false);
    }

    public double getHealth() {
        return creature.getHealth();
    }

    public void setHealth(double hp) {
        creature.setHealth(hp);
    }

    public double getFood() {
        return food;
    }

    public void setFood(double food) {
        this.food = food;
    }

    public Location getLocation() {
        return creature.getLocation();
    }

    public Creature getEntity() {
        return creature;
    }

    public EntityPlayer getOwner() {
        return owner;
    }

    public PathfinderGoalSelector getPathSelector() {
        return pathSelector;
    }

    public EntityCreature getNmsEntity() {
        return entityCreature;
    }

    public boolean isAttacking() {
        return entityCreature.isAggressive();
    }

    public void setDefending(boolean status) {
        defend = status;
        if (status) {
            pathSelector.a(2, df);
        } else {
            pathSelector.a(df);
        }
    }

    public boolean isDefending() {
        return defend;
    }

    public void setDefendingOwner(boolean status) {
        defendOwner = status;
        if (status) {
            pathSelector.a(1, dfo);
        } else {
            pathSelector.a(dfo);
        }
    }

    public boolean isDefendingOwner() {
        return defendOwner;
    }

    public void setAggresive(boolean status) {
        attack = status;
        if (status) {
            pathSelector.a(1, at);
        } else {
            pathSelector.a(at);
        }
    }

    public boolean isAggresive() {
        return attack;
    }

    public void damageByHunger() {
        getEntity().damage(0.05);
    }

    public String toString() {
        return asString();
    }

    public String asString() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "ATTACKING");
        map.put("name", getName() + "");
        map.put("food", food);
        map.put("health", creature.getHealth());
        map.put("max_health", defaultHealth);
        map.put("max_food", defaultFood);
        map.put("aggresive", attack);
        map.put("defend", defend);
        map.put("damage", getDamage());
        map.put("respawn_cost", respawnCost);
        map.put("defend_owner", defendOwner);
        map.put("entity", creature.getType().name());
        return Writer.write(map);
    }

    public static Pet fromString(Player owner, Map<String, Object> json) {
        AttackingPet pet = new AttackingPet(owner, EntityType.fromName(json.get("entity") + ""));
        if ((boolean) json.getOrDefault("aggresive", false))
            pet.setAggresive(true);
        if ((boolean) json.getOrDefault("defend", true))
            pet.setDefending(true);
        if ((boolean) json.getOrDefault("defendOwner", true))
            pet.setDefendingOwner(true);
        pet.defaultHealth = (double) json.get("max_health");
        pet.defaultFood = (double) json.get("max_food");
        pet.respawnCost = (double) json.get("respawn_cost");
        pet.setDamage((double) json.get("damage"));
        pet.getEntity().setMaxHealth(pet.defaultHealth);
        pet.setName("" + json.get("name"));
        if ((double) json.get("health") <= 0) {
            pet.setHealth(0);
            pet.despawn();
        } else
            pet.setHealth((double) json.get("health"));
        pet.food = (double) json.get("food");
        return pet;
    }

    public double getRespawnCost() {
        return respawnCost;
    }

    public void setRespawnCost(double cost) {
        respawnCost = cost;
    }

    public double getDamage() {
        return entityCreature.craftAttributes.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue();
    }

    public void setDamage(double damage) {
        entityCreature.craftAttributes.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage);
    }

    private int opened;
    private GUI stats;
    private int updateTask;

    public void openStats(Player player) {
        ++opened;
        if(opened==1) {
            updateStats();
        }
        stats.open(player);
    }

    private void updateStats() {
        if(stats==null){
            stats=new GUI("&eInformace o tvém mazlíčkovi", 54) {
                @Override
                public void onClose(Player p){
                    onCloseStats(p);
                }
            };
            updateTask=new Tasker(){
                @Override
                public void run() {
                    updateStats();
                }
            }.runRepeating(20, 20);
            //TODO setup gui
        }
        //TODO update stats in gui
    }

    public void onCloseStats(Player player) {
        --opened;
        if(opened==0) {
            stats.clear(); //clear cache from memory
            stats=null;
            Scheduler.cancelTask(updateTask);
        }
    }
}
