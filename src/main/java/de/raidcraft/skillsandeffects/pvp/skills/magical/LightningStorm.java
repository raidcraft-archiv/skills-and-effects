package de.raidcraft.skillsandeffects.pvp.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.util.EffectUtil;
import de.raidcraft.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Lightning Storm",
        description = "Lässt ein Blitzgewitter auf deine Feinde herab regnen.",
        types = {EffectType.MAGICAL, EffectType.SILENCABLE, EffectType.DAMAGING},
        elements = {EffectElement.LIGHTNING}
)
public class LightningStorm extends AbstractSkill implements CommandTriggered {

    private BukkitTask task;
    private int i = 0;

    public LightningStorm(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        cancel();

        final Location center = getHolder().getBlockTarget();
        final List<Location> circle = EffectUtil.circle(center, getTotalRange(), 1, true, false, 10);
        final World world = getHolder().getPlayer().getWorld();
        final FireworkEffect effect = FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.BLUE).build();

        task = Bukkit.getScheduler().runTaskTimer(RaidCraft.getComponent(SkillsPlugin.class), new Runnable() {
            @Override
            public void run() {

                if (i < circle.size()) {
                    EffectUtil.playFirework(world, circle.get(i), effect);
                    i++;
                } else {
                    world.strikeLightningEffect(center);
                    Entity[] entities = LocationUtil.getNearbyEntities(center, getTotalRange());
                    for (Entity entity : entities) {
                        if (entity instanceof LivingEntity) {
                            CharacterTemplate character = RaidCraft.getComponent(SkillsPlugin.class)
                                    .getCharacterManager().getCharacter((LivingEntity) entity);
                            if (character.isFriendly(getHolder())) {
                                continue;
                            }
                            try {
                                magicalAttack(character, getTotalDamage());
                                world.strikeLightningEffect(entity.getLocation());
                            } catch (CombatException e) {
                                getHolder().sendMessage(ChatColor.RED + e.getMessage());
                            }
                        }
                    }
                    cancel();
                }
            }
        }, 1L, 1L);
    }

    private void cancel() {

        i = 0;
        if (task != null) task.cancel();
        task = null;
    }
}
