package me.devtec.custompets.pets;

import me.devtec.custompets.pathfinders.PathfinderGoalFollowPlayer;
import me.devtec.custompets.pets.constructors.Pet;
import me.devtec.theapi.TheAPI;
import me.devtec.theapi.guiapi.GUI;
import me.devtec.theapi.scheduler.Scheduler;
import me.devtec.theapi.scheduler.Tasker;
import me.devtec.theapi.utils.json.Writer;
import me.devtec.theapi.utils.reflections.Ref;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftCreature;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class FollowingPet implements Pet {

    protected final EntityPlayer owner;
    protected PathfinderGoalSelector pathSelector;
    protected EntityCreature entityCreature;
    protected Creature creature;
    protected double food = 20, defaultFood = 20, defaultHealth = 20, respawnCost;

    public FollowingPet(Player owner, EntityType type) {
        Creature creature = (Creature) owner.getWorld().spawnEntity(owner.getLocation(), type);
        this.creature = creature;

        entityCreature = (((CraftCreature) creature).getHandle());
        this.owner = (EntityPlayer) Ref.player(owner);
        pathSelector = entityCreature.bP;
        pathSelector.a(); //clear paths
        pathSelector.a(0, new PathfinderGoalFloat(entityCreature)); //water
        pathSelector.a(1, new PathfinderGoalFollowPlayer(this)); //follow owner
    }

    public void teleport(Location loc) {
        if (creature.getWorld() != loc.getWorld()) {
            creature.teleport(loc);
            creature.setNoDamageTicks(40);
            entityCreature = (((CraftCreature) creature).getHandle());

            pathSelector = entityCreature.bP;
            pathSelector.a(); //clear paths
            pathSelector.a(0, new PathfinderGoalFloat(entityCreature)); //water
            pathSelector.a(1, new PathfinderGoalFollowPlayer(this)); //follow owner
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

        Creature creature = (Creature) this.owner.getBukkitEntity().getWorld().spawnEntity(this.owner.getBukkitEntity().getLocation(), this.creature.getType());
        this.creature = creature;
        creature.setMaxHealth(defaultHealth);
        creature.setHealth(defaultHealth);
        entityCreature = (((CraftCreature) creature).getHandle());

        pathSelector = entityCreature.bP;
        pathSelector.a(); //clear paths
        pathSelector.a(0, new PathfinderGoalFloat(entityCreature)); //water
        pathSelector.a(1, new PathfinderGoalFollowPlayer(this)); //follow owner
    }

    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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
        this.food = food;
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
    public void damageByHunger() {
        getEntity().damage(0.05);
    }

    public String toString() {
        return asString();
    }

    @Override
    public String asString() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "FOLLOWING");
        map.put("name", getName() + "");
        map.put("food", food);
        map.put("max_health", defaultHealth);
        map.put("max_food", defaultFood);
        map.put("respawn_cost", respawnCost);
        map.put("health", creature.getHealth());
        map.put("entity", creature.getType().name());
        return Writer.write(map);
    }

    public static Pet fromString(Player owner, Map<String, Object> json) {
        FollowingPet pet = new FollowingPet(owner, EntityType.fromName(json.get("entity") + ""));
        pet.defaultHealth = (double) json.get("max_health");
        pet.defaultFood = (double) json.get("max_food");
        pet.getEntity().setMaxHealth(pet.defaultHealth);
        pet.respawnCost = (double) json.get("respawn_cost");
        pet.setName("" + json.get("name"));
        if ((double) json.get("health") <= 0) {
            pet.setHealth(0);
            pet.despawn();
        } else
            pet.setHealth((double) json.get("health"));
        return pet;
    }

    @Override
    public double getRespawnCost() {
        return respawnCost;
    }

    @Override
    public void setRespawnCost(double cost) {
        respawnCost = cost;
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
