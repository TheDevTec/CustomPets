package me.devtec.custompets;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.devtec.custompets.pets.constructors.Pet;

public class PetOwner {
	private final UUID uuid;
	private String pet;
	private List<String> pets;
	
	private Pet ticking;
	
	public PetOwner(UUID uuid) {
		this.uuid=uuid;
		pet=PetsManager.petDatas.getString(uuid+".pet");
		pets=PetsManager.petDatas.getStringList(uuid+".pets");
		
	}
	
	public UUID getOwner() {
		return uuid;
	}

	@Nullable
	public String getPet() {
		return ticking!=null?pet=ticking.asString():pet;
	}
	
	public List<String> getPets() {
		return pets;
	}
	
	public void switchPet(String to) {
		if(pet.equals(to))return;
		if(pet!=null) {
			pets.remove(to);
			pets.add(pet);
			pet=to;
		}
	}
	
	@Nullable
	public Pet getTicking() {
		return ticking;
	}
	
	@Nullable
	public Pet activePet() {
		if(pet==null)return null;
		Player p = Bukkit.getPlayer(uuid);
		if(p==null)return null;
		ticking=PetsManager.parsePet(p, getPet());
		PetsManager.ticking.add(ticking);
		return ticking;
	}
	
	public boolean deactivePet() {
		if(ticking==null)return false;
		ticking.despawn();
		PetsManager.ticking.remove(ticking);
		pet=ticking.asString();
		ticking=null;
		return true;
	}

	public void setPet(String s) {
		boolean active = ticking!=null;
		deactivePet();
		pet=s;
		if(active)activePet();
	}
	
	public void save() {
		PetsManager.petDatas.set(uuid.toString()+".pets", pets);
		PetsManager.petDatas.set(uuid.toString()+".pet", getPet());
		PetsManager.petDatas.set(uuid.toString()+".spawned", ticking!=null && ticking.isAlive());
	}
}
