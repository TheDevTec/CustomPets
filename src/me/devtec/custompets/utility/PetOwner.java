package me.devtec.custompets.utility;

import me.devtec.custompets.Loader;
import me.devtec.custompets.pets.constructors.Pet;
import me.devtec.theapi.utils.json.Reader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PetOwner {
    private final UUID uuid;
    private String pet;
    private List<String> pets;

    private Pet ticking;

    public PetOwner(UUID uuid) {
        this.uuid = uuid;
        pet = Loader.petDatas.getString(uuid + ".pet");
        pets = Loader.petDatas.getStringList(uuid + ".pets");

    }

    public UUID getOwner() {
        return uuid;
    }

    @Nullable
    public String getPet() {
        return ticking != null ? pet = ticking.asString() : pet;
    }

    public List<String> getPets() {
        return pets;
    }

    public void switchPet(String to) {
        if (to.equals(pet)) return;
        if (pet != null) {
            pets.remove(to);
            pets.add(pet);
            pet = to;
        }
    }

    @Nullable
    public Pet getTicking() {
        return ticking;
    }

    @Nullable
    public Pet activePet() {
        if (pet == null) return null;
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) return null;
        Map<String, Object> r = (Map<String, Object>) Reader.read(getPet());
        if ((double) r.get("health") <= 0)return null;
            ticking = PetsManager.parsePet(p, r);
        PetsManager.ticking.add(ticking);
        return ticking;
    }

    public boolean deactivePet() {
        if (ticking == null) return false;
        ticking.despawn();
        PetsManager.ticking.remove(ticking);
        pet = ticking.asString();
        ticking = null;
        return true;
    }

    public void setPet(String s) {
        boolean active = ticking != null;
        deactivePet();
        pet = s;
        if (active) activePet();
    }

    public void save() {
        Loader.petDatas.set(uuid + ".pets", pets);
        Loader.petDatas.set(uuid + ".pet", getPet());
        Loader.petDatas.set(uuid + ".spawned", ticking != null && ticking.isAlive());
    }
}
