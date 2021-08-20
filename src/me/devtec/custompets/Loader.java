package me.devtec.custompets;

import me.devtec.coins.API;
import me.devtec.custompets.pets.constructors.Pet;
import me.devtec.custompets.utility.GuiManager;
import me.devtec.custompets.utility.PetOwner;
import me.devtec.custompets.utility.PetsManager;
import me.devtec.theapi.TheAPI;
import me.devtec.theapi.apis.ItemCreatorAPI;
import me.devtec.theapi.configapi.Config;
import me.devtec.theapi.configapi.Section;
import me.devtec.theapi.guiapi.EmptyItemGUI;
import me.devtec.theapi.guiapi.GUI;
import me.devtec.theapi.guiapi.GUI.ClickType;
import me.devtec.theapi.guiapi.HolderGUI;
import me.devtec.theapi.guiapi.ItemGUI;
import me.devtec.theapi.placeholderapi.PlaceholderAPI;
import me.devtec.theapi.utils.datakeeper.Data;
import me.devtec.theapi.utils.json.Reader;
import me.devtec.theapi.utils.json.Writer;
import me.devtec.theapi.utils.nms.NMSAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Loader extends JavaPlugin implements Listener {

    public static Config c;
    public static Data petDatas = new Data("plugins/CustomPets/data.dat");

    public void onEnable() {
        if (TheAPI.isOlderThan(17)) {
            TheAPI.msg("&cPlugin require server version 1.17 or newer to run.", TheAPI.getConsole());
            TheAPI.msg("&cDisabling plugin...", TheAPI.getConsole());
            setNaggable(true);
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        c = Config.loadConfig(this, "config.yml", "CustomPets/config.yml");
        PetsManager.loadPets();
        PetsManager.loadTasks();

        TheAPI.createAndRegisterCommand("pets", null, (s, arg1, arg2, args) -> {
            if (!s.hasPermission("custompets.inventory")) return true;
            GuiManager.openInventory((Player)s);
            return true;
        });

        TheAPI.createAndRegisterCommand("petshop", null, (s, arg1, arg2, args) -> {
            if (!s.hasPermission("custompets.petshop")) return true;
            GuiManager.openShop((Player)s);
            return true;
        }, "shoppet", "zverimex");
    }

    public void onDisable() {
        PetsManager.unloadTasks();
        PetsManager.savePets();
        PetsManager.unloadPets();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (petDatas.getBoolean(e.getPlayer().getUniqueId() + ".spawned"))
            PetsManager.loadPet(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        PetOwner owner = PetsManager.cache.remove(e.getPlayer().getUniqueId());
        if (owner == null) return;
        owner.save();
        petDatas.set(e.getPlayer().getUniqueId() + ".spawned", owner.deactivePet());
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent e) {
        Pet p = PetsManager.getTicking(e.getRightClicked());
        if(e.getPlayer().isSneaking())
            GuiManager.openStats(p, e.getPlayer());
    }

    @EventHandler
    public void onTeleport(EntityTeleportEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (e.isCancelled()) return;
        PetOwner owner = PetsManager.getPetOwner(e.getEntity().getUniqueId());
        if (owner == null) return;
        if (owner.getTicking() != null) owner.getTicking().teleport(e.getTo());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.isCancelled()) return;
        PetOwner owner = PetsManager.getPetOwner(e.getPlayer().getUniqueId());
        if (owner == null) return;
        if (owner.getTicking() != null) owner.getTicking().teleport(e.getTo());
    }
}