package me.devtec.custompets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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

import me.devtec.coins.API;
import me.devtec.custompets.pets.PickupingPet;
import me.devtec.custompets.pets.constructors.Pet;
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
import me.devtec.theapi.utils.json.Reader;
import me.devtec.theapi.utils.json.Writer;
import me.devtec.theapi.utils.nms.NMSAPI;

public class Loader extends JavaPlugin implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(PetsManager.petDatas.getBoolean(e.getPlayer().getUniqueId().toString()+".spawned"))
			PetsManager.loadPet(e.getPlayer());
	}
	
	@EventHandler
	public void onJoin(PlayerQuitEvent e) {
		PetOwner owner = PetsManager.cache.remove(e.getPlayer().getUniqueId());
		if(owner==null)return;
		owner.save();
		PetsManager.petDatas.set(e.getPlayer().getUniqueId().toString()+".spawned", owner.deactivePet());
	}
	
	@EventHandler
	public void onRightClick(PlayerInteractEntityEvent e) {
		Pet p = PetsManager.getTicking(e.getRightClicked());
		if(p!=null) {
			if(p instanceof PickupingPet)
				e.getPlayer().openInventory(((PickupingPet)p).getLoot());
		}
	}
	
	@EventHandler
	public void onTp(EntityTeleportEvent e) {
		if(!(e.getEntity() instanceof Player))return;
		if(e.isCancelled())return;
		PetOwner owner = PetsManager.getPetOwner(e.getEntity().getUniqueId());
		if(owner==null)return;
		if(owner.getTicking()!=null)owner.getTicking().teleport(e.getTo());
	}
	
	@EventHandler
	public void onTp(PlayerTeleportEvent e) {
		if(e.isCancelled())return;
		PetOwner owner = PetsManager.getPetOwner(e.getPlayer().getUniqueId());
		if(owner==null)return;
		if(owner.getTicking()!=null)owner.getTicking().teleport(e.getTo());
	}
	
	public void onEnable() {
		if(TheAPI.isOlderThan(17)) {
			TheAPI.msg("&cPlugin require server version 1.17 or newer to run.", TheAPI.getConsole());
			TheAPI.msg("&cDisabling plugin...", TheAPI.getConsole());
			setNaggable(true);
			return;
		}
		Bukkit.getPluginManager().registerEvents(this,this);
		Config c = Config.loadConfig(this, "config.yml", "CustomPets/config.yml");
		PetsManager.loadPets();
		PetsManager.loadTasks();
		
		GUI main = new GUI("&eZverimex 1/"+(((int)c.getKeys("PetShop").size()/45)+1), 54);
		prepare(main);
		main.setItem(47, new ItemGUI(ItemCreatorAPI.create(Material.RED_STAINED_GLASS, 1, "&cOdejít")) {
			public void onClick(Player player, HolderGUI gui, ClickType click) {
				gui.close(player);
			}
		});
		GUI g = main;
		for(String s : c.getKeys("PetShop")) {
			if(g.isFull()) {
				GUI old = g;
				g=new GUI("&eZverimex 1/"+(((int)c.getKeys("PetShop").size()/45)+1), 54);
				prepare(g);
				GUI next = g;
				old.setItem(51, new ItemGUI(ItemCreatorAPI.create(Material.ARROW, 1, "&eDalší &8&l»")) {
					public void onClick(Player player, HolderGUI gui, ClickType click) {
						next.open(player);
					}
				});
				next.setItem(47, new ItemGUI(ItemCreatorAPI.create(Material.ARROW, 1, "&8&l« &ePředchozí")) {
					public void onClick(Player player, HolderGUI gui, ClickType click) {
						old.open(player);
					}
				});
			}
			g.add(new ItemGUI(create(c.getSection("PetShop."+s+".display"))) {
				String parse = preparePet(c.getSection("PetShop."+s));
				public void onClick(Player player, HolderGUI gui, ClickType click) {
					if(API.get(player.getName())>=c.getDouble("PetShop."+s+".cost")) {
						API.set(player.getName(), API.get(player.getName())-c.getDouble("PetShop."+s+".cost"));
						List<String> pets = PetsManager.getPets(player.getUniqueId());
						pets.add(parse);
						for(String s : c.getStringList("PetShop."+s+".msgs"))
							TheAPI.msg(PlaceholderAPI.setPlaceholders(player, s), player);
						return;
					}
					for(String s : c.getStringList("PetShop."+s+".no-coins"))
						TheAPI.msg(PlaceholderAPI.setPlaceholders(player, s), player);
				}
			});
		}
		
		TheAPI.createAndRegisterCommand("pets", null, new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sd, Command arg1, String arg2, String[] args) {
				if(!sd.hasPermission("op"))return true;
				List<String> pets = new ArrayList<>(PetsManager.getPets(((Player)sd).getUniqueId()));
				GUI main = new GUI("&eTvoji mazlíčci 1/"+(((int)pets.size()/45)+1), 54);
				prepare(main);
				main.setItem(47, new ItemGUI(ItemCreatorAPI.create(Material.RED_STAINED_GLASS, 1, "&cOdejít")) {
					public void onClick(Player player, HolderGUI gui, ClickType click) {
						gui.close(player);
					}
				});
				GUI g = main;
				String active = PetsManager.getPet(((Player)sd).getUniqueId());
				pets.add(active);
				for(String s : pets) {
					if(g.isFull()) {
						GUI old = g;
						g=new GUI("&eTvoji mazlíčci 1/"+(((int)pets.size()/45)+1), 54);
						prepare(g);
						GUI next = g;
						old.setItem(51, new ItemGUI(ItemCreatorAPI.create(Material.ARROW, 1, "&eDalší &8&l»")) {
							public void onClick(Player player, HolderGUI gui, ClickType click) {
								next.open(player);
							}
						});
						next.setItem(47, new ItemGUI(ItemCreatorAPI.create(Material.ARROW, 1, "&8&l« &ePředchozí")) {
							public void onClick(Player player, HolderGUI gui, ClickType click) {
								old.open(player);
							}
						});
					}
					g.add(new ItemGUI(active!=null&&active.equals(s)?e(create(s)):create(s)) {
						boolean clickToAgree = false;
						public void onClick(Player player, HolderGUI gui, ClickType click) {
							if(click==ClickType.LEFT_DROP||click==ClickType.LEFT_PICKUP) {
								PetOwner active = PetsManager.getPetOwner(player.getUniqueId());
								active.switchPet(s);
								NMSAPI.postToMainThread(() -> active.setPet(s));
								@SuppressWarnings("unchecked")
								Map<String,Object> r = (Map<String, Object>) Reader.read(s);
								if((double)r.get("health")>0)
									NMSAPI.postToMainThread(() -> active.activePet());
								for(String s : c.getStringList("Msgs.Selected"))
									TheAPI.msg(PlaceholderAPI.setPlaceholders(player, s), player);
								return;
							}
							if(click==ClickType.RIGHT_DROP||click==ClickType.RIGHT_PICKUP) {
								if(API.get(player.getName())>=c.getDouble("PetShop."+s+".cost")) {
								PetOwner active = PetsManager.getPetOwner(player.getUniqueId());
								@SuppressWarnings("unchecked")
								Map<String,Object> r = (Map<String, Object>) Reader.read(active.getPet());
								if(active.getPet()==null || active.getTicking()!=null && active.getTicking().getHealth()>0 || (double)r.get("health")>0) {
									for(String s : c.getStringList("Msgs.AliveOrInvalid"))
										TheAPI.msg(PlaceholderAPI.setPlaceholders(player, s), player);
									return;
								}
								if(!clickToAgree) {
									clickToAgree=true;
									for(String s : c.getStringList("Msgs.Agree-Respawn"))
										TheAPI.msg(PlaceholderAPI.setPlaceholders(player, s), player);
									return;
								}
								clickToAgree=false;
								gui.close(player);
								API.set(player.getName(), API.get(player.getName())-(double)r.get("respawn_cost"));
								NMSAPI.postToMainThread(() -> {
									if(active.activePet()!=null)
										active.getTicking().respawn();
								});
								for(String s : c.getStringList("Msgs.Respawned"))
									TheAPI.msg(PlaceholderAPI.setPlaceholders(player, s), player);
								return;
								}
								for(String s : c.getStringList("PetShop."+s+".no-coins"))
									TheAPI.msg(PlaceholderAPI.setPlaceholders(player, s), player);
							}
						}
					});
				}
				main.open((Player)sd);
				return true;
			}
		});
		
		TheAPI.createAndRegisterCommand("petshop", null, new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command arg1, String arg2, String[] args) {
				if(!s.hasPermission("op"))return true;
				main.open((Player)s);
				return true;
			}
		},"shoppet","zverimex");
	}

	ItemGUI empty = new EmptyItemGUI(ItemCreatorAPI.create(Material.BLACK_STAINED_GLASS_PANE, 1, "&c"));
	private void prepare(GUI main) {
		for(int i = 0; i < 9; ++i)main.setItem(i, empty);
		for(int i = 45; i < 54; ++i)main.setItem(i, empty);
	}

	protected ItemStack e(ItemStack create) {
		create.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 0);
		create.addItemFlags(ItemFlag.HIDE_ENCHANTS,ItemFlag.HIDE_ATTRIBUTES);
		return create;
	}

	protected ItemStack create(String json) {
		@SuppressWarnings("unchecked")
		Map<String,Object> r = (Map<String, Object>) Reader.read(json);
		return ItemCreatorAPI.create(Material.getMaterial(r.get("entity").toString().toUpperCase()+"_SPAWN_EGG"), 1, "&8&l» &f"+r.get("name"), Arrays.asList("","&8&l| &6T&fyp: &e"+niceType(r.get("type").toString()),"&8&l| &6E&fntita: &e"+niceEntity(r.get("entity").toString()),""
				,"&8&l| &6Ž&fivoty: &e"+r.get("health")+"/"+r.get("max_health")
				,"&8&l| &6J&fídlo: &e"+r.get("food")+"/"+r.get("max_food"),""
				,"&8&l| &6C&fena &6R&fespawnu: &e"+r.get("respawn_cost")+" coinů",""));
	}

	private String niceEntity(String string) {
		switch(string) {
		case "CHICKEN":
			return "Slepice";
		case "PIG":
			return "Prase";
		case "COW":
			return "Kráva";
		case "CAT":
			return "Kočka";
		case "WOLF":
			return "Vlk";
		case "LLAMA":
			return "Lama";
		case "OCELOT":
			return "Ocelot";
		case "MAGMA_CUBE":
			return "Magma Cube";
		case "SLIME":
			return "Slizoun";
		case "VILLAGER":
			return "Vesničan";
		case "RABBIT":
			return "Králík";
		}
		return string;
	}

	private String niceType(String string) {
		switch(string.toUpperCase()) {
		case "ATTACKING":
			return "Bojovník";
		case "DEFENDING":
			return "Ochránce";
		case "PICKUPING":
			return "Sběratel";
		case "FOLLOWING":
			return "Mazlíček";
		}
		return string;
	}

	protected String preparePet(Section section) {
		Map<String, Object> json = new HashMap<>();
		json.put("type", section.getString("type").toUpperCase());
		json.put("health", section.getDouble("health"));
		json.put("max_health", section.getDouble("health"));
		json.put("food", section.getDouble("food"));
		json.put("max_food", section.getDouble("food"));
		json.put("name", section.getString("name"));
		json.put("entity", section.getString("entity").toUpperCase());
		json.put("respawn_cost", section.getDouble("respawn_cost"));
		switch(section.getString("type").toUpperCase()) {
		case "ATTACKING":
		case "DEFENDING":
			json.put("damage", section.getDouble("damage"));
			break;
		}
		return Writer.write(json);
	}

	private ItemStack create(Section section) {
		return ItemCreatorAPI.create(Material.valueOf(section.getString("item").toUpperCase()), 1, section.getString("name"), section.getStringList("lore"));
	}

	public void onDisable() {
		PetsManager.unloadTasks();
		PetsManager.savePets();
		PetsManager.unloadPets();
	}
}