package me.HeyAwesomePeople.party;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NewParty {

	private Party plugin = Party.instance;

	private String party = ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "Party" + ChatColor.DARK_GREEN + "]";
	private Integer id;
	private List<Player> players = new ArrayList<Player>();
	public List<Player> invites = new ArrayList<Player>();
	public List<Player> partyChat = new ArrayList<Player>();
	private Player leader;
	private Boolean friendlyfire;

	public NewParty(Player p, Integer id) {
		this.id = id;
		this.players.add(p);
		this.leader = p;
		this.friendlyfire = false;
		plugin.party.put(p, id);
	}

	/*************/

	public Integer getId() {
		return this.id;
	}

	public List<Player> getPlayers() {
		return this.players;
	}

	public Player getLeader() {
		return this.leader;
	}

	public Boolean isFriendlyFireOn() {
		return this.friendlyfire;
	}

	/**************/

	public void promote(Player p) {
		if (this.players.contains(p)) {
			this.leader = p;
			partyMessage(ChatColor.GOLD + ChatColor.stripColor(ChatColor.stripColor(p.getName())) + " is your new party leader!");
		} else {
			this.leader.sendMessage(ChatColor.RED + " New leader must be in the party!");
		}

	}

	public void party(Player p) {
		if (this.players.contains(p)) {
			List<String> ps = new ArrayList<String>();
			for (Player p1 : this.players) {
				if (p1 == this.leader) {
					ps.add(ChatColor.DARK_AQUA + p1.getName() + ChatColor.AQUA);
				} else {
					ps.add(ChatColor.AQUA + p1.getName() + ChatColor.AQUA);
				}
			}
			p.sendMessage(this.party + ChatColor.AQUA + " Party Info");
			p.sendMessage(this.party + ChatColor.AQUA + " Players: " + Arrays.toString(ps.toArray()));
			p.sendMessage(this.party + ChatColor.AQUA + " Friendly Fire: " + ChatColor.DARK_AQUA + isFriendlyFireOn());
		}
	}

	public void kick(Player p) {
		if (this.players.contains(p)) {
			if (this.leader.equals(p)) {
				this.disband(p);
				return;
			}
			this.players.remove(p);
			plugin.party.remove(p);
			p.sendMessage(this.party + ChatColor.GOLD + " You are kicked from your party!");
			partyMessage(ChatColor.GOLD + ChatColor.stripColor(ChatColor.stripColor(p.getName())) + " was kicked from party!");
		}
	}

	public void removeInvite(Player p) {
		this.invites.remove(p);
	}

	public void invite(final Player p) { // expires after 3 minutes
		if (!this.players.contains(p)) {
			for (NewParty s : plugin.nParty.values()) {
				if (s.invites.contains(p)) {
					partyMessage(ChatColor.BLUE + ChatColor.stripColor(p.getName()) + " has another pending invite.");
					return;
				}
			}
			this.invites.add(p);
			p.sendMessage(this.party + ChatColor.GOLD + " You have been invited to " + this.leader.getName() + "'s party! Join with /party join");
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					if (!invites.contains(p)) {
						return;
					}
					removeInvite(p);
					p.sendMessage(party + ChatColor.GOLD + " The invite to " + leader.getName() + "'s party has expired!");
					partyMessage(ChatColor.RED + "Invite to " + ChatColor.stripColor(p.getName()) + " has expired!");
				}
			}, 20 * 60 * 3L);
			partyMessage(ChatColor.GOLD + ChatColor.stripColor(p.getName()) + " was invited into the party!");
		} else {
			partyMessage(ChatColor.GOLD + ChatColor.stripColor(p.getName()) + " is already in the party, cannot invite");
			return;
		}
	}

	public void leave(Player p) {
		if (this.players.contains(p)) {
			if (this.leader == p) {
				this.disband(p);
				return;
			}
			this.players.remove(p);
			plugin.party.remove(p);
			p.sendMessage(this.party + ChatColor.RED + " You have left the party!");
			partyMessage(ChatColor.RED + ChatColor.stripColor(p.getName()) + " has left the party!");
		} else {
			p.sendMessage(this.party + ChatColor.RED + " Cannot leave a party you are not in!");
		}
	}

	public void join(Player p) {
		if (!this.players.contains(p)) {
			if (this.invites.contains(p)) {
				this.players.add(p);
				plugin.party.put(p, this.getId());
				removeInvite(p);
				partyMessage(ChatColor.BLUE + ChatColor.stripColor(p.getName()) + " has joined the party!");
				return;
			} else {
				p.sendMessage(this.party + ChatColor.RED + " Cannot join party without invitation!");
				return;
			}
		} else {
			partyMessage(ChatColor.RED + ChatColor.stripColor(p.getName()) + " has attempted to join the party, though they are already in it.");
			return;
		}
	}

	public void disband(Player p) {
		if (this.leader.equals(p)) {
			for (Player p1 : this.players) {
				plugin.party.remove(p1);
				plugin.nParty.remove(id);
				p1.sendMessage(this.party + ChatColor.GOLD + " Party has been disbanded!");
			}
			this.players.clear();
			this.invites.clear();
		} else {
			p.sendMessage(this.party + ChatColor.RED + " Cannot disband party!");
		}
	}

	public void togglePartyChat(Player p) {
		if (this.players.contains(p)) {
			if (this.partyChat.contains(p)) {
				this.partyChat.remove(p);
				p.sendMessage(ChatColor.GOLD + "Party chat was turned off!");
			} else {
				this.partyChat.add(p);
				p.sendMessage(ChatColor.GOLD + "Party chat was turned on!");
			}
		}
	}

	public void toggleFriendlyFire(Player p) {
		if (this.players.contains(p)) {
			if (this.leader.equals(p)) {
				if (this.isFriendlyFireOn()) {
					this.friendlyfire = false;
					partyMessage(ChatColor.GOLD + "Friendly fire was disabled!");
				} else {
					this.friendlyfire = true;
					partyMessage(ChatColor.GOLD + "Friendly fire was enabled!");
				}
				return;
			} else {
				p.sendMessage(this.party + ChatColor.GOLD + " No permission to do this! You are not leader!");
				return;
			}
		}
	}

	public void partyChat(Player p, String s) {
		for (Player p1 : this.players) {
			if (p.equals(this.leader)) {
				p1.sendMessage(ChatColor.GOLD + "[" + ChatColor.YELLOW + ChatColor.stripColor(p.getName()) + ChatColor.GOLD + "] " + ChatColor.WHITE + s);	
			} else {
				p1.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + ChatColor.stripColor(p.getName()) + ChatColor.YELLOW + "] " + ChatColor.WHITE + s);
			}
		}
	}

	public void partyMessage(String s) {
		for (Player p : this.players) {
			p.sendMessage(this.party + " " + s);
		}
	}

}
