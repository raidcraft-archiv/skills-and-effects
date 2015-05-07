package de.raidcraft.skillsandeffects.pvp.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.ProjectileType;
import de.raidcraft.skills.api.combat.action.RangedAttack;
import de.raidcraft.skills.api.combat.callback.LocationCallback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.LocationUtil;
import de.raidcraft.util.MathUtil;
import de.raidcraft.util.TimeUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Meteor",
        description = "Lässt einen Meteor auf das Ziel herab regnen.",
        types = {EffectType.MAGICAL, EffectType.DAMAGING},
        elements = {EffectElement.FIRE}
)
public class Meteor extends AbstractSkill implements CommandTriggered {

    @Getter
    private int amount;
    private BukkitTask meteorTask;
    private int firedMeteors = 0;
    private double speed;
    private long interval;
    private long delay;

    public Meteor(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        amount = (int) ConfigUtil.getTotalValue(this, data.getConfigurationSection("amount"));
        if (amount < 1) {
            amount = 1;
        }
        speed = data.getDouble("speed", 1.0);
        interval = TimeUtil.secondsToTicks(data.getDouble("interval", 0.3));
        delay = TimeUtil.secondsToTicks(data.getDouble("delay", 1.0));
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        dropMeteors(getTargetBlock().clone());
    }

    public void dropMeteors(final Location targetLocation) throws CombatException {

        // lets start a repeating task to not spawn all meteors at once
        final int amount = getAmount();
        firedMeteors = 0;
        meteorTask = Bukkit.getScheduler().runTaskTimer(RaidCraft.getComponent(SkillsPlugin.class), () -> {
            if (firedMeteors >= amount) {
                // cancel the task
                meteorTask.cancel();
                return;
            }

            Location top = targetLocation.clone();
            // frst Meteor hits exactly
            if (firedMeteors == 0) {
                top = top.clone().add(0.5, MathUtil.RANDOM.nextInt(3) + 3, 0.5);
            } else {
                top = top.add(MathUtil.RANDOM.nextInt(7) - 3 + MathUtil.RANDOM.nextDouble(),
                        MathUtil.RANDOM.nextInt(3) + 3,
                        MathUtil.RANDOM.nextInt(7) - 3 + MathUtil.RANDOM.nextDouble());
            }
            // TODO: set custom damage
            try {
                RangedAttack<LocationCallback> attack = rangedAttack(ProjectileType.LARGE_FIREBALL);
                attack.setSpawnLocation(top);
                attack.setVelocity(LocationUtil.getDirection(top, targetLocation).multiply(speed));
                attack.run();
                firedMeteors++;
            } catch (CombatException ignored) {
            }
        }, delay, interval);
    }
}
