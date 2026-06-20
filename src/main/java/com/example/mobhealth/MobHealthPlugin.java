package com.example.mobhealth;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MobHealthPlugin extends JavaPlugin {

    /** Maximum distance a player can be looking at a mob for the boss bar to show. */
    private static final double BOSS_BAR_RANGE = 32.0;

    /** How often (in ticks) to refresh each player's boss bar. */
    private static final long UPDATE_INTERVAL = 4L;

    /** Tag used on old floating ArmorStand labels so they can be cleaned up. */
    private static final String LABEL_TAG = "mobhealth_label";

    /** player UUID -> boss bar */
    private final Map<UUID, BossBar> playerBossBars = new ConcurrentHashMap<>();

    private BukkitRunnable bossBarTask;

    @Override
    public void onEnable() {
        cleanupOldLabels();
        startBossBarTask();
        getLogger().info("MobHealth enabled.");
    }

    @Override
    public void onDisable() {
        if (bossBarTask != null) {
            bossBarTask.cancel();
        }
        for (Map.Entry<UUID, BossBar> entry : playerBossBars.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                player.hideBossBar(entry.getValue());
            }
        }
        playerBossBars.clear();
        getLogger().info("MobHealth disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && "cleanup".equalsIgnoreCase(args[0])) {
            int removed = cleanupOldLabels();
            sender.sendMessage("Removed " + removed + " old MobHealth labels.");
            return true;
        }
        sender.sendMessage("Usage: /mobhealth cleanup");
        return true;
    }

    /** Remove any leftover floating labels from previous versions. */
    private int cleanupOldLabels() {
        int removed = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof ArmorStand stand && stand.getScoreboardTags().contains(LABEL_TAG)) {
                    stand.remove();
                    removed++;
                }
            }
        }
        if (removed > 0) {
            getLogger().info("Cleaned up " + removed + " old MobHealth labels.");
        }
        return removed;
    }

    private void startBossBarTask() {
        bossBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // getTargetEntity performs a ray-trace on the main thread.
                    // Keep BOSS_BAR_RANGE as small as practical to limit cost.
                    Entity target = player.getTargetEntity((int) BOSS_BAR_RANGE);
                    if (target instanceof LivingEntity mob && !(mob instanceof Player) && mob.isValid()) {
                        showHealth(player, mob);
                    } else {
                        hideHealth(player);
                        player.sendActionBar(Component.empty());
                    }
                }
            }
        };
        bossBarTask.runTaskTimer(this, 1L, UPDATE_INTERVAL);
    }

    private void showHealth(Player player, LivingEntity mob) {
        double health = Math.max(0, mob.getHealth());
        var maxHealthAttr = mob.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : health;

        float progress = maxHealth <= 0 ? 0.0f : (float) Math.min(1.0, Math.max(0.0, health / maxHealth));
        BossBar.Color color = bossBarColor(health, maxHealth);
        NamedTextColor hColor = healthColor(health, maxHealth);
        Component name = Component.text()
                .append(Component.text(mob.getName() + " ", NamedTextColor.WHITE))
                .append(Component.text("❤ ", NamedTextColor.RED))
                .append(Component.text(formatHealth(health) + " / " + formatHealth(maxHealth), hColor))
                .build();

        BossBar bossBar = playerBossBars.get(player.getUniqueId());
        if (bossBar == null) {
            bossBar = BossBar.bossBar(name, progress, color, BossBar.Overlay.PROGRESS);
            playerBossBars.put(player.getUniqueId(), bossBar);
            player.showBossBar(bossBar);
        } else {
            bossBar.name(name);
            bossBar.progress(progress);
            bossBar.color(color);
        }

        // Action bar: show health above the hotbar
        Component actionBar = Component.text()
                .append(Component.text(mob.getName(), NamedTextColor.WHITE, TextDecoration.BOLD))
                .append(Component.text("  ❤ ", NamedTextColor.RED))
                .append(Component.text(formatHealth(health), hColor, TextDecoration.BOLD))
                .append(Component.text(" / " + formatHealth(maxHealth), NamedTextColor.GRAY))
                .build();
        player.sendActionBar(actionBar);
    }

    private void hideHealth(Player player) {
        BossBar bossBar = playerBossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            player.hideBossBar(bossBar);
        }
    }

    private NamedTextColor healthColor(double health, double maxHealth) {
        if (maxHealth <= 0) {
            return NamedTextColor.WHITE;
        }
        double ratio = health / maxHealth;
        if (ratio > 0.5) {
            return NamedTextColor.GREEN;
        } else if (ratio > 0.25) {
            return NamedTextColor.YELLOW;
        }
        return NamedTextColor.RED;
    }

    private BossBar.Color bossBarColor(double health, double maxHealth) {
        if (maxHealth <= 0) {
            return BossBar.Color.WHITE;
        }
        double ratio = health / maxHealth;
        if (ratio > 0.5) {
            return BossBar.Color.GREEN;
        } else if (ratio > 0.25) {
            return BossBar.Color.YELLOW;
        }
        return BossBar.Color.RED;
    }

    private String formatHealth(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }
        return String.format("%.1f", value);
    }
}
