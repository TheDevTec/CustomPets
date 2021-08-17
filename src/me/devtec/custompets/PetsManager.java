package me.devtec.custompets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.craftbukkit.v1_17_R1.entity.CraftItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import me.devtec.custompets.pets.AttackingPet;
import me.devtec.custompets.pets.DefendingPet;
import me.devtec.custompets.pets.FollowingPet;
import me.devtec.custompets.pets.PickupingPet;
import me.devtec.custompets.pets.constructors.Pet;
import me.devtec.theapi.TheAPI;
import me.devtec.theapi.scheduler.Scheduler;
import me.devtec.theapi.scheduler.Tasker;
import me.devtec.theapi.utils.datakeeper.Data;
import me.devtec.theapi.utils.datakeeper.DataType;
import me.devtec.theapi.utils.json.Reader;
import net.minecraft.world.entity.item.EntityItem;

public class PetsManager {
	public static Map<UUID, PetOwner> cache = new HashMap<>();
	public static List<Pet> ticking = new ArrayList<>();
	
	public static PetOwner getPetOwner(UUID uuid) {
		PetOwner owner = cache.get(uuid);
		if(owner==null) {
			cache.put(uuid, owner=new PetOwner(uuid));
		}
		return owner;
	}
	
	public static Pet getTicking(Entity e) {
		for(Pet p : ticking)if(p.getEntity()==e)return p;
		return null;
	}
	
	public static String getPet(UUID uuid) {
		return getPetOwner(uuid).getPet();
	}
	
	public static List<String> getPets(UUID uuid){
		return getPetOwner(uuid).getPets();
	}

	private static List<Integer> tasks = new ArrayList<>();
	
	public static void loadTasks() {
		//hunger
		tasks.add(new Tasker() {
			public void run() {
				for(Pet p : Collections.unmodifiableList(ticking)) {
					if(!p.isAlive())continue;
					if(p.getFood()<=0) {
						if(p.getHealth()<=0.005) { //dead
							p.despawn();
							continue;
						}
						p.damageByHunger();
					}else
						p.setFood(p.getFood()-0.0005);
				}
			}
		}.runRepeating(20,20));
		//pickup
		tasks.add(new Tasker() {
			public void run() {
				for(Pet p : Collections.unmodifiableList(ticking)) {
					if(!p.isAlive())continue;
					if(p instanceof PickupingPet) {
						if(((PickupingPet) p).canPickup()) {
							for(Item i : p.getLocation().getNearbyEntitiesByType(Item.class, 6)) {
								if(i.getPickupDelay()==0) {
									((PickupingPet)p).pickupLoot((EntityItem) ((CraftItem)i).getHandle());
								}
							}
						}
					}
				}
			}
		}.runRepeatingSync(5, 5));
	}
	
	public static void unloadTasks() {
		for(int i : tasks)Scheduler.cancelTask(i);
	}
	
	protected static Data petDatas = new Data("plugins/CustomPets/data.dat");

	public static void savePets() {
		for(PetOwner e : cache.values()) {
			e.save();
		}
		petDatas.save(DataType.YAML);
	}

	public static void loadPets() {
		for(Player p : TheAPI.getOnlinePlayers())
			if(petDatas.getBoolean(p.getUniqueId().toString()+".spawned"))
				loadPet(p);
	}
	
	public static void loadPet(PetOwner player) {
		player.activePet();
	}
	
	public static void loadPet(Player player) {
		loadPet(getPetOwner(player.getUniqueId()));
	}
	
	public static Pet parsePet(Player owner, Map<String, Object> parsed) {
		switch(((String)parsed.get("type")).toUpperCase()) {
		case "ATTACKING":
			return AttackingPet.fromString(owner, parsed);
		case "DEFENDING":
			return DefendingPet.fromString(owner, parsed);
		case "PICKUPING":
			return PickupingPet.fromString(owner, parsed);
		case "FOLLOWING":
			return FollowingPet.fromString(owner, parsed);
		}
		return null;
	}
	
	public static Pet parsePet(Player owner, String json) {
		@SuppressWarnings("unchecked")
		Map<String, Object> parsed = (Map<String, Object>) Reader.read(json);
		switch(((String)parsed.get("type")).toUpperCase()) {
		case "ATTACKING":
			return AttackingPet.fromString(owner, parsed);
		case "DEFENDING":
			return DefendingPet.fromString(owner, parsed);
		case "PICKUPING":
			return PickupingPet.fromString(owner, parsed);
		case "FOLLOWING":
			return FollowingPet.fromString(owner, parsed);
		}
		return null;
	}

	public static void unloadPets() {
		for(Pet tick : ticking)
			tick.despawn();
		ticking.clear();
		cache.clear();
	}
}
