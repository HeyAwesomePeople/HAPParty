package me.HeyAwesomePeople.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Party extends JavaPlugin implements Listener, CommandExecutor {
	public static Party instance;

	private String partyN = ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "Party" + ChatColor.DARK_GREEN + "] ";
	public HashMap<Player, Integer> party = new HashMap<Player, Integer>();
	public HashMap<Integer, NewParty> nParty = new HashMap<Integer, NewParty>();
	public List<Player> spying = new ArrayList<Player>();

	@Override
	public void onEnable() {
		instance = this;

		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerComands(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		String[] args = e.getMessage().split(" ");
		String commandLabel = args[0].replace("/", "");
		e.setCancelled(true);
		
		if (commandLabel.equalsIgnoreCase("party")) {
			if (args.length == 1) {
				p.sendMessage(partyN);
				p.sendMessage(ChatColor.AQUA + "/party create");
				p.sendMessage(ChatColor.AQUA + "/party join");
				p.sendMessage(ChatColor.AQUA + "/party deny");
				p.sendMessage(ChatColor.AQUA + "/party info");
				p.sendMessage(ChatColor.AQUA + "/party promote <name>");
				p.sendMessage(ChatColor.AQUA + "/party invite <name>");
				p.sendMessage(ChatColor.AQUA + "/party kick <name>");
				p.sendMessage(ChatColor.AQUA + "/party leave");
				p.sendMessage(ChatColor.AQUA + "/party disband");
				p.sendMessage(ChatColor.AQUA + "/party friendlyfire");
				p.sendMessage(ChatColor.AQUA + "/party gui");
			} else if (args.length > 1) {
				if (!(p instanceof Player)) {
					p.sendMessage(partyN + ChatColor.RED + "Only players can perform this command!");
					e.setCancelled(true);
					return;
				}
				if (p.hasPermission("party.player")) {
					if (args[1].equalsIgnoreCase("create")) {
						if (args.length == 2) {
							if (!isPlayerInParty(p)) {
								int id = getUnusedPartyId();
								this.nParty.put(id, new NewParty(p, id));
								p.sendMessage(partyN + ChatColor.BLUE + "Party successfully created!");
								e.setCancelled(true);
								return;
							}
							p.sendMessage(partyN + ChatColor.RED + "Cannot create party, already in one!");
							e.setCancelled(true);
							return;
						}
						p.sendMessage(partyN + ChatColor.AQUA + "[Usage] /party create");
						e.setCancelled(true);
						return;
					}
					if (args[1].equalsIgnoreCase("promote")) {
						if (args.length == 3) {
							if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[2]))) {
								p.sendMessage(this.partyN + ChatColor.RED + "Player not online!");
								e.setCancelled(true);
								return;
							}
							if (isPlayerInParty(p)) {
								NewParty np = this.nParty.get(party.get(p));
								if (np.getLeader().equals(p)) {
									np.promote(Bukkit.getPlayer(args[2]));
									e.setCancelled(true);
									return;
								} else {
									p.sendMessage(this.partyN + ChatColor.RED + "No permission to do this! You are not leader!");
								}
							} else {
								p.sendMessage(this.partyN + ChatColor.RED + "You are not in a party!");
							}
						} else {
							p.sendMessage(partyN + ChatColor.AQUA + "[Usage] /party promote <name>");
						}
					}
					if (args[1].equalsIgnoreCase("invite")) {
						if (args.length == 3) {
							if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[2]))) {
								p.sendMessage(this.partyN + ChatColor.RED + "Player not online!");
								e.setCancelled(true);
								return;
							}
							if (isPlayerInParty(p)) {
								NewParty np = this.nParty.get(party.get(p));
								np.invite(Bukkit.getPlayer(args[2]));
								e.setCancelled(true);
								return;
							} else {
								p.sendMessage(this.partyN + ChatColor.RED + "You are not in a party!");
								e.setCancelled(true);
								return;
							}
						} else {
							p.sendMessage(partyN + ChatColor.AQUA + "[Usage] /party invite <name>");
						}

					}
					if (args[1].equalsIgnoreCase("kick")) {
						if (args.length == 3) {
							if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[2]))) {
								p.sendMessage(this.partyN + ChatColor.RED + "Player not online!");
								e.setCancelled(true);
								return;
							}
							if (isPlayerInParty(p)) {
								NewParty np = this.nParty.get(party.get(p));
								if (np.getLeader().equals(p)) {
									np.kick(Bukkit.getPlayer(args[2]));
									e.setCancelled(true);
									return;
								} else {
									p.sendMessage(this.partyN + ChatColor.RED + "No permission to do this! You are not leader!");
								}
							} else {
								p.sendMessage(this.partyN + ChatColor.RED + "You are not in a party!");
							}
						} else {
							p.sendMessage(partyN + ChatColor.AQUA + "[Usage] /party kick <name>");
						}

					}
					if (args[1].equalsIgnoreCase("leave")) {
						if (args.length == 2) {
							if (isPlayerInParty(p)) {
								nParty.get(party.get(p)).leave(p);
								e.setCancelled(true);
								return;
							}
							p.sendMessage(partyN + ChatColor.RED + "Cannot leave a party when you are not in one!");
							e.setCancelled(true);
							return;
						}
						p.sendMessage(partyN + ChatColor.AQUA + "[Usage] /party leave");
						e.setCancelled(true);
						return;
					}
					if (args[1].equalsIgnoreCase("disband")) {
						if (args.length == 2) {
							if (isPlayerInParty(p)) {
								nParty.get(party.get(p)).disband(p);
								e.setCancelled(true);
								return;
							}
							p.sendMessage(partyN + ChatColor.RED + "Cannot disband a party when you are not in one!");
							e.setCancelled(true);
							return;
						}
						p.sendMessage(partyN + ChatColor.AQUA + "[Usage] /party disband");
						e.setCancelled(true);
						return;
					}
					if (args[1].equalsIgnoreCase("friendlyfire")) {
						if (args.length == 2) {
							if (isPlayerInParty(p)) {
								nParty.get(party.get(p)).toggleFriendlyFire(p);
								e.setCancelled(true);
								return;
							}
							p.sendMessage(partyN + ChatColor.RED + "Cannot edit a party when you are not in one!");
							e.setCancelled(true);
							return;
						}
						p.sendMessage(partyN + ChatColor.AQUA + "[Usage] /party friendlyfire");
						e.setCancelled(true);
						return;
					}
					if (args[1].equalsIgnoreCase("join")) {
						if (args.length == 2) {
							if (!isPlayerInParty(p)) {
								for (NewParty s : nParty.values()) {
									if (s.invites.contains(p)) {
										s.join(p);
										e.setCancelled(true);
										return;
									}
									continue;
								}
								p.sendMessage(partyN + ChatColor.RED + "No pending invites!");
							} else {
								p.sendMessage(partyN + ChatColor.RED + "Cannot join party, already in one!");
								e.setCancelled(true);
								return;
							}
						} else {
							p.sendMessage(partyN + ChatColor.AQUA + "[Usage] /party join");
							e.setCancelled(true);
							return;
						}
					}
					if (args[1].equalsIgnoreCase("deny")) {
						if (args.length == 2) {
							for (NewParty s : nParty.values()) {
								if (s.invites.contains(p)) {
									s.removeInvite(p);
									s.getLeader().sendMessage(partyN + ChatColor.BLUE + ChatColor.stripColor(p.getName()) + " has declined the invite to your party!");
									p.sendMessage(partyN + ChatColor.BLUE + "Invite declined.");
									e.setCancelled(true);
									return;
								}
								continue;
							}
							p.sendMessage(partyN + ChatColor.RED + "Cannot deny party, no invites!");
						} else {
							p.sendMessage(partyN + ChatColor.AQUA + "[Usage] /party deny");
							e.setCancelled(true);
							return;
						}
					}
					if (args[1].equalsIgnoreCase("info")) {
						if (args.length == 2) {
							if (isPlayerInParty(p)) {
								nParty.get(party.get(p)).party(p);
								e.setCancelled(true);
								return;
							} else {
								p.sendMessage(partyN + ChatColor.RED + "Cannot get party info, not in party!");
								e.setCancelled(true);
								return;
							}
						} else {
							p.sendMessage(partyN + ChatColor.AQUA + "[Usage] /party deny");
							e.setCancelled(true);
							return;
						}
					}
					if (args[1].equalsIgnoreCase("gui")) {
						if (args.length == 2) {
							if (isPlayerInParty(p)) {
								nParty.get(party.get(p)).party(p);
								e.setCancelled(true);
								return;
							} else {
								p.sendMessage(partyN + ChatColor.RED + "Cannot get party info, not in party!");
								e.setCancelled(true);
								return;
							}
						} else {
							p.sendMessage(partyN + ChatColor.AQUA + "[Usage] /party deny");
							e.setCancelled(true);
							return;
						}
					}
				} else {
					p.sendMessage(this.partyN + ChatColor.RED + "No permissions!");
					return;
				}
			}
			e.setCancelled(true);
			return;
		}
		if (commandLabel.equalsIgnoreCase("partychat") || commandLabel.equalsIgnoreCase("pc")) {
			if (!(p instanceof Player)) {
				p.sendMessage(partyN + ChatColor.RED + "Only players can perform this command!");
				e.setCancelled(true);
				return;
			}
			if (args.length == 1) {
				if (isPlayerInParty(p)) {
					this.nParty.get(party.get(p)).togglePartyChat(p);
				} else {
					p.sendMessage(this.partyN + ChatColor.RED + "You are not in a party!");
					e.setCancelled(true);
					return;
				}
			} else if (args.length > 1) {
				if (isPlayerInParty(p)) {
					this.nParty.get(party.get(p)).partyChat(p, StringUtils.join(args, ' ', 1, args.length));
					for (Player p2 : Bukkit.getOnlinePlayers()) {
						if (p2.hasPermission("party.admin.spy") && !this.nParty.get(party.get(p)).getPlayers().contains(p2)) {
							p2.sendMessage(ChatColor.GRAY + "[" + e.getPlayer().getName() + " --> " + this.nParty.get(party.get(p)).getLeader().getName() + "'s Party] " + StringUtils.join(args, ' ', 1, args.length));
						}
					}
					e.setCancelled(true);
					return;
				} else {
					p.sendMessage(this.partyN + ChatColor.RED + "You are not in a party!");
					e.setCancelled(true);
					return;
				}
			}
			e.setCancelled(true);
			return;
		}

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerChatEvent(AsyncPlayerChatEvent e) {
		if (this.party.containsKey(e.getPlayer())) {
			Integer party = this.party.get(e.getPlayer());
			NewParty np = nParty.get(party);
			if (!np.partyChat.contains(e.getPlayer())) {
				return;
			}
			np.partyChat(e.getPlayer(), e.getMessage());
			for (Player p2 : Bukkit.getOnlinePlayers()) {
				if (p2.hasPermission("party.admin.spy") && !np.getPlayers().contains(p2)) {
					p2.sendMessage(ChatColor.GRAY + "[" + e.getPlayer().getName() + " --> " + np.getLeader().getName() + "'s Party" + ChatColor.GRAY + "] " + e.getMessage());
				}
			}
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void playerHitEvent(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			if (this.party.containsKey((Player) e.getEntity())) {
				Integer party = this.party.get((Player) e.getEntity());
				if (this.nParty.get(party).getPlayers().contains(e.getDamager())) {
					NewParty np = this.nParty.get(party);
					if (np.isFriendlyFireOn()) {
						if (e.getCause().equals(DamageCause.PROJECTILE)) {
							e.setCancelled(true);
						}
						return;
					} else {
						e.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	@EventHandler
	public void playerLeave(PlayerQuitEvent e) {
		if (this.party.containsKey(e.getPlayer())) {
			Integer party = this.party.get(e.getPlayer());
			if (this.nParty.get(party).getPlayers().contains(e.getPlayer())) {
				this.nParty.get(party).leave(e.getPlayer());
			}
		}
		spying.remove(e.getPlayer());
	}

	@EventHandler
	public void playerLeave(PlayerKickEvent e) {
		if (this.party.containsKey(e.getPlayer())) {
			Integer party = this.party.get(e.getPlayer());
			if (this.nParty.get(party).getPlayers().contains(e.getPlayer())) {
				this.nParty.get(party).leave(e.getPlayer());
				return;
			}
		}
		spying.remove(e.getPlayer());
	}

	public Boolean isPlayerInParty(Player p) {
		if (party.containsKey(p) && nParty.containsKey(party.get(p))) {
			return true;
		}
		return false;
	}

	public Integer getUnusedPartyId() {
		int a = 1;
		if (nParty.isEmpty()) {
			return a;
		} else {
			for (Integer i : nParty.keySet()) {
				if (i.equals(a)) {
					a++;
					continue;
				}
			}
			return a;
		}
	}

	public boolean onCommand(final CommandSender d, Command cmd,
			String commandLabel, final String[] args) {
		return false;
	}

}
