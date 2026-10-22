package smp.jalebi.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class JalebiScoreboard extends JavaPlugin implements Listener {
    private DataStore data;
    private BoardManager boards;
    private ScoreboardGui gui;

    @Override
    public void onEnable() {
        data = new DataStore(this);
        boards = new BoardManager(this);
        gui = new ScoreboardGui(this);

        Bukkit.getPluginManager().registerEvents(this, this);

        // refresh every second
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) boards.update(p);
        }, 20L, 20L);
    }

    @Override
    public void onDisable() {
        data.save();
    }

    public DataStore data() { return data; }
    public BoardManager boards() { return boards; }

    /** Reads the player's team name from the JalebiTeams plugin (if installed). "N/A" if none. */
    public String teamNameOf(Player p) {
        Plugin teams = Bukkit.getPluginManager().getPlugin("JalebiTeams");
        if (teams != null && teams.isEnabled()) {
            try {
                Object r = teams.getClass().getMethod("getTeamName", UUID.class).invoke(teams, p.getUniqueId());
                if (r instanceof String s && !s.isBlank()) return s;
            } catch (ReflectiveOperationException ignored) {
                // JalebiTeams is a different version - fall through to N/A
            }
        }
        return "N/A";
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLater(this, () -> boards.update(e.getPlayer()), 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        boards.forget(e.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] a) {
        if (!(s instanceof Player p)) {
            s.sendMessage("Players only.");
            return true;
        }
        gui.open(p);
        return true;
    }
}
