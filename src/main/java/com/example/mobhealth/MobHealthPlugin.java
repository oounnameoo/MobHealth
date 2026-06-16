package com.example.mobhealth;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.FluidCollisionMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MobHealthPlugin extends JavaPlugin implements Listener {

    /** How far the player can target a mob. */
    private static final int RAY_TRACE_DISTANCE = 6;

    /** Per-player HUD state. */
    private static final class HudState {
        final BossBar bar;

        HudState(BossBar bar) {
            this.bar = bar;
        }
    }

    private final Map<UUID, HudState> playerStates = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        startUpdateTask();
        getLogger().info("MobHealth enabled.");
    }

    @Override
    public void onDisable() {
        for (Map.Entry<UUID, HudState> entry : playerStates.entrySet()) {
            Player player = getServer().getPlayer(entry.getKey());
            if (player != null) {
                player.hideBossBar(entry.getValue().bar);
            }
        }
        playerStates.clear();
        getLogger().info("MobHealth disabled.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        BossBar bar = BossBar.bossBar(
                Component.empty(),
                1f,
                BossBar.Color.RED,
                BossBar.Overlay.PROGRESS
        );
        playerStates.put(player.getUniqueId(), new HudState(bar));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        HudState state = playerStates.remove(player.getUniqueId());
        if (state != null) {
            player.hideBossBar(state.bar);
        }
    }

    private void startUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    HudState state = playerStates.get(player.getUniqueId());
                    if (state == null) {
                        continue;
                    }

                    RayTraceResult result = player.rayTraceEntities(RAY_TRACE_DISTANCE, false);
                    Entity target = result != null ? result.getHitEntity() : null;

                    if (!(target instanceof LivingEntity mob) || target instanceof Player) {
                        player.hideBossBar(state.bar);
                        continue;
                    }

                    updateBossBar(player, state, mob);
                }
            }
        }.runTaskTimer(this, 0L, 1L);
    }

    private void updateBossBar(Player player, HudState state, LivingEntity mob) {
        double health = Math.max(0, mob.getHealth());
        double maxHealth = mob.getAttribute(Attribute.MAX_HEALTH) != null
                ? mob.getAttribute(Attribute.MAX_HEALTH).getValue()
                : health;

        float progress = maxHealth > 0 ? (float) Math.min(1.0, health / maxHealth) : 0f;

        Component name = mob.customName() != null
                ? mob.customName()
                : Component.translatable(mob.getType().translationKey());

        Component title = Component.text()
                .append(Component.text("❤ ", NamedTextColor.RED))
                .append(name.colorIfAbsent(NamedTextColor.WHITE).decorate(TextDecoration.BOLD))
                .append(Component.text("  " + formatHealth(health) + " / " + formatHealth(maxHealth), NamedTextColor.WHITE))
                .build();

        state.bar.name(title);
        state.bar.progress(progress);
        state.bar.color(barColor(progress));
        player.showBossBar(state.bar);
    }

    private String formatHealth(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }
        return String.format("%.1f", value);
    }

    private BossBar.Color barColor(float progress) {
        if (progress > 0.5f) return BossBar.Color.GREEN;
        if (progress > 0.25f) return BossBar.Color.YELLOW;
        return BossBar.Color.RED;
    }
}
