package com.kegekiss.kegeteamtag;

import net.milkbowl.vault.chat.Chat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class KegeTeamTag extends JavaPlugin {

	public static Chat chat = null;
	
	private Scoreboard scoreboard;
	
	@Override
    public void onEnable() {
		
		scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		
		if (!(setupChat())) {
            getLogger().severe(String.format("Problème majeur trouvé : Pas de plugin de formatage du tchat!"));
            Bukkit.getPluginManager().disablePlugin(this);
		} else {
			getLogger().info("Plugin de formatage trouvé! Création des teams selon les groupes...");
	        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				@SuppressWarnings("deprecation")
				@Override
				public void run() {
	            	if (checkTeams()) {
	            		for (Player player: Bukkit.getOnlinePlayers()) {
	            			checkPlayerGroup(player);
	            		}
	            	} else {
	            		createTeams();
	            	}
				}
	        }, 0L, 20L);
		}
	}
	
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }
    
    public void createTeams() {
    	removeTeams();
		for (String group: chat.getGroups()) {
			
			String KTT_group = "KTT_" + group;
			String prefix = chat.getGroupPrefix(Bukkit.getWorlds().get(0), group);
			String suffix = chat.getGroupSuffix(Bukkit.getWorlds().get(0), group);
			
			if (KTT_group.length() > 16 || prefix.length() > 16 || suffix.length() > 16) {
				getLogger().info("KegeTeamTag ne supporte pas les rangs, préfixes ou suffixes de plus de 16 lettres de longueurs, pour des limitations techniques!");
	            Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("KegeTeamTag"));
			} else {
				Team team = scoreboard.registerNewTeam(KTT_group);
				team.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix));
				team.setSuffix(ChatColor.translateAlternateColorCodes('&', suffix));
				team.setCanSeeFriendlyInvisibles(false);
				team.setAllowFriendlyFire(true);				
			}
		}
    }
    
    @SuppressWarnings("deprecation")
	public void removeTeams() {
		for (Team team : scoreboard.getTeams()) {
			if (team.getName().contains("KTT_")) {
				team.unregister();	
			}
		}
		for (Player player: Bukkit.getOnlinePlayers()) {
		removeTabName(player);
		}
    }
    
    public boolean checkTeams() {
    	
    	boolean checkTeams = false;
		for (String group: chat.getGroups()) {
			checkTeams = false;			
			for (Team team : scoreboard.getTeams()) {
				if (team.getName().contains("KTT_")) {
			    	if (group.equals(team.getName().replace("KTT_", ""))) {
			    		if (ChatColor.translateAlternateColorCodes('&', chat.getGroupPrefix(Bukkit.getWorlds().get(0), group)).equals(team.getPrefix())) {
			    			if (ChatColor.translateAlternateColorCodes('&', chat.getGroupSuffix(Bukkit.getWorlds().get(0), group)).equals(team.getSuffix())) {
					    		checkTeams = true;
					    		break;
			    			}
			    		}
			    	}	
				} 
			}
			if (!checkTeams) {
				return false;
			}
		}
		return true;   	
    }
    
    public void checkPlayerGroup(Player player) {
		for (Team team : scoreboard.getTeams()) {
			if (team.getName().contains("KTT_")) {
				if (team.hasPlayer(player)) {
					if ( !(chat.getPrimaryGroup(player).equals(team.getName().replace("KTT_", ""))) ) {
						addToTeam(player, scoreboard.getTeam("KTT_" + chat.getPrimaryGroup(player)));
						break;
					} else {
						setTabName(player);
					}
				} else if (scoreboard.getPlayerTeam(player) == null) {
    					addToTeam(player, scoreboard.getTeam("KTT_" + chat.getPrimaryGroup(player)));
    					break;
				}     					
			} else {
				if (team.hasPlayer(player)) {
					removeTabName(player);
				}
			}
		}
    }
    
    public void addToTeam(Player player, Team team) {
		for (Team oldTeam : scoreboard.getTeams()) {
			oldTeam.removePlayer(player);
		}
    	team.addPlayer(player);
    	setTabName(player);
    }
    
	public void setTabName(Player player) {
		
		Team team = scoreboard.getPlayerTeam(player);
		String playerName;
		
		if (scoreboard.getPlayerTeam(player) != null) {
			playerName = team.getPrefix() + player.getName() + team.getSuffix();
		} else {
			playerName = null;
		}			
		if (player.getPlayerListName().equals(playerName) || ChatColor.stripColor(player.getPlayerListName()).equals(player.getName())) {
			player.setPlayerListName(getColors('&', chat.getGroupPrefix(Bukkit.getWorlds().get(0), chat.getPrimaryGroup(player))) + player.getName());
		}			 
	}
    
	public void removeTabName(Player player) {
		
		Team team = scoreboard.getPlayerTeam(player);
		String playerName;
		
		if (scoreboard.getPlayerTeam(player) != null) {
			playerName = team.getPrefix() + player.getName() + team.getSuffix();
		} else {
			playerName = null;
		}		
		if (player.getPlayerListName().equals(playerName) || ChatColor.stripColor(player.getPlayerListName()).equals(player.getName())) {
			player.setPlayerListName(player.getName());
		}			 
    }
    
    public static String getColors(char colorChar, String text) {
    	
        String result = "";
        int length = text.length();

        for (int index = length - 1; index > -1; index--) {
            char section = text.charAt(index);
            if (section == colorChar && index < length - 1) {
                char c = text.charAt(index + 1);
              ChatColor color = ChatColor.getByChar(c);
                if (color != null) {
                	if (!color.equals(ChatColor.RESET)) {
                        result = color.toString() + result;	
                	}
                    if (color.isColor()) {
                        break;
                    }
                }
            }
        }
        return result;
    }
	   
	@Override
    public void onDisable() {
		getLogger().info("Désactivation de KegeTeamTag...");
		removeTeams();
	}
}