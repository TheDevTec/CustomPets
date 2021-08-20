package me.devtec.custompets.pets;

import com.google.common.collect.Queues;
import me.devtec.custompets.pathfinders.PathfinderGoalFollowLoot;
import me.devtec.custompets.pathfinders.PathfinderGoalFollowPlayer;
import me.devtec.custompets.pets.constructors.Pet;
import me.devtec.custompets.pets.constructors.Storable;
import me.devtec.theapi.TheAPI;
import me.devtec.theapi.guiapi.GUI;
import me.devtec.theapi.scheduler.Scheduler;
import me.devtec.theapi.scheduler.Tasker;
import me.devtec.theapi.utils.json.Reader;
import me.devtec.theapi.utils.json.Writer;
import me.devtec.theapi.utils.reflections.Ref;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;
import net.minecraft.world.entity.item.EntityItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftItem;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PickupingPet implements Pet, Storable {

    protected final EntityPlayer owner;
    protected PathfinderGoalSelector pathSelector;
    protected EntityCreature entityCreature;
    protected Creature creature;
    protected double food = 20, defaultFood = 20, defaultHealth = 20, respawnCost;

    protected boolean pickup = true;
    protected Inventory inv = Bukkit.createInventory(null, 45);

    public PickupingPet(Player owner, EntityType type) {
        Creature creature = (Creature) owner.getWorld().spawnEntity(owner.getLocation(), type);
        this.creature = creature;

        entityCreature = (((CraftCreature) creature).getHandle());
        this.owner = (EntityPlayer) Ref.player(owner);
        pathSelector = entityCreature.bP;
        pathSelector.a(); //clear paths
        pathSelector.a(0, new PathfinderGoalFloat(entityCreature)); //water
        pathSelector.a(1, new PathfinderGoalFollowLoot(this)); //pickup loot
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
            pathSelector.a(1, new PathfinderGoalFollowLoot(this)); //pickup loot
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
        pathSelector.a(1, new PathfinderGoalFollowLoot(this)); //pickup loot
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
    public boolean canPickup() {
        return pickup;
    }

    @Override
    public void allowPickup(boolean status) {
        pickup = status;
    }

    @Override
    public Inventory getLoot() {
        return inv;
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
        map.put("type", "PICKUPING");
        map.put("name", getName() + "");
        map.put("food", food);
        map.put("health", creature.getHealth());
        map.put("max_health", defaultHealth);
        map.put("max_food", defaultFood);
        map.put("pickup", pickup);
        map.put("respawn_cost", respawnCost);
        map.put("loot", Writer.write(Arrays.asList(inv.getContents())));
        map.put("entity", creature.getType().name());
        return Writer.write(map);
    }

    @SuppressWarnings("unchecked")
    public static Pet fromString(Player owner, Map<String, Object> json) {
        PickupingPet pet = new PickupingPet(owner, EntityType.fromName(json.get("entity") + ""));
        pet.defaultHealth = (double) json.get("max_health");
        pet.defaultFood = (double) json.get("max_food");
        pet.getEntity().setMaxHealth(pet.defaultHealth);
        pet.setName("" + json.get("name"));
        if ((double) json.get("health") <= 0) {
            pet.setHealth(0);
            pet.despawn();
        } else
            pet.setHealth((double) json.get("health"));
        pet.pickup = (boolean) json.getOrDefault("pickup", true);
        pet.respawnCost = (double) json.get("respawn_cost");
        if (json.get("loot") != null)
            pet.inv.setContents(((List<ItemStack>) Reader.read((String) json.get("loot"))).toArray(new ItemStack[0]));
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

    private final Queue<EntityItem> queue = Queues.newLinkedBlockingDeque();

    public EntityItem getPickupLoot() {
        return queue.peek();
    }

    public void pickupedLoot(EntityItem item) {
        queue.remove(item);
        ItemStack added = add(getLoot(), ((CraftItem) item.getBukkitEntity()).getItemStack());
        if (added == null) item.getBukkitEntity().remove();
        else ((CraftItem) item.getBukkitEntity()).setItemStack(added);
    }

    public void removeLoot(EntityItem item) {
        queue.remove(item);
    }

    private ItemStack add(Inventory loot, ItemStack itemStack) {
        for (int i = 0; i < loot.getSize(); ++i) {
            ItemStack s = loot.getItem(i);
            if (s == null || s.getType() == Material.AIR) {
                loot.setItem(i, itemStack.clone());
                itemStack.setAmount(0);
                break;
            }
            if (s.isSimilar(itemStack)) {
                int amount = s.getAmount();
                if (amount < 64) {
                    amount += itemStack.getAmount();
                    if (amount < 64) {
                        itemStack.setAmount(0);
                        s.setAmount(amount);
                        loot.setItem(i, s);
                        break;
                    }
                    amount = -64;
                    itemStack.setAmount(amount);
                    s.setAmount(64);
                    loot.setItem(i, s);
                }
            }
        }
        return itemStack.getAmount() == 0 ? null : itemStack;
    }

    public void pickupLoot(EntityItem item) {
        if (!queue.contains(item))
            queue.add(item);
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
