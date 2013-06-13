package de.raidcraft.skillsandeffects.pvp.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.ProjectileType;
import de.raidcraft.skills.api.combat.action.RangedAttack;
import de.raidcraft.skills.api.combat.callback.ProjectileCallback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.MathUtil;
import de.raidcraft.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LargeFireball;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

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

    private ConfigurationSection amount;
    private BukkitTask meteorTask;
    private int firedMeteors = 0;
    private double minSpeed;
    private double maxSpeed;
    private long interval;
    private long delay;

    public Meteor(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        amount = data.getConfigurationSection("amount");
        minSpeed = data.getDouble("min-speed", 0.01);
        maxSpeed = data.getDouble("max-speed", 0.75);
        interval = TimeUtil.secondsToTicks(data.getDouble("interval", 1.0));
        delay = TimeUtil.secondsToTicks(data.getDouble("delay", 1.0));
    }

    public int getAmount() {

        int value = (int) ConfigUtil.getTotalValue(this, amount);
        return value > 0 ? value : 1;
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        dropMeteor(getBlockTarget());
    }

    public void dropMeteor(final Location location) throws CombatException {

        // lets get a position above the location with a random offset
        final Location origin = location.clone();
        final Vector direction = new Vector(location.getX() - origin.getX(),
                location.getY() - origin.getY(),
                location.getZ() - origin.getZ()).normalize();
        // lets start a repeating task to not spawn all meteors at once
        final int amount = getAmount();
        firedMeteors = 0;
        meteorTask = Bukkit.getScheduler().runTaskTimer(RaidCraft.getComponent(SkillsPlugin.class), new Runnable() {
            @Override
            public void run() {

                if (firedMeteors >= amount) {
                    // cancel the task
                    meteorTask.cancel();
                    return;
                }
                try {
                    origin.add(MathUtil.RANDOM.nextInt(3), MathUtil.RANDOM.nextInt(5) + 3, MathUtil.RANDOM.nextInt(3));
                    RangedAttack<ProjectileCallback> attack = new RangedAttack<>(getHolder(), ProjectileType.LARGE_FIREBALL, getTotalDamage());
                    LargeFireball projectile = location.getWorld().spawn(origin, LargeFireball.class);
                    projectile.setShooter(getHolder().getPlayer());
                    projectile.setIsIncendiary(true);
                    projectile.setFireTicks(100);
                    attack.setProjectile(projectile);
                    if (amount > 1) {
                        double speedmult = MathUtil.RANDOM.nextDouble() * (maxSpeed - minSpeed) + minSpeed;
                        // lets randomize the meteor a little if multiple are spawned
                        direction.multiply(speedmult);
                    }
                    attack.setVelocity(direction);
                    firedMeteors++;
                    attack.run();
                } catch (CombatException e) {
                    getHolder().sendMessage(ChatColor.RED + e.getMessage());
                }
            }
        }, delay, interval);
    }
}
