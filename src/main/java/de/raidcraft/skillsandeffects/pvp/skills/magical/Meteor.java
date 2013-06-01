package de.raidcraft.skillsandeffects.pvp.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
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
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
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

    public Meteor(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        amount = data.getConfigurationSection("amount");
    }

    public int getAmount() {

        int value = (int) ConfigUtil.getTotalValue(this, amount);
        return value > 0 ? value : 1;
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        dropMeteor(getBlockTarget());
    }

    public void dropMeteor(Location location) throws CombatException {

        // lets get a position above the location with a random offset
        Location origin = location.clone();

        for (int i = 0; i < getAmount(); i++) {
            origin = origin.add(MathUtil.RANDOM.nextInt(3), MathUtil.RANDOM.nextInt(5) + 3, MathUtil.RANDOM.nextInt(3));
            RangedAttack<ProjectileCallback> attack = new RangedAttack<>(getHolder(), ProjectileType.LARGE_FIREBALL, getTotalDamage());
            org.bukkit.entity.Fireball projectile = location.getWorld().spawn(origin, org.bukkit.entity.Fireball.class);
            projectile.setShooter(getHolder().getPlayer());
            projectile.setIsIncendiary(true);
            projectile.setFireTicks(100);
            attack.setProjectile(projectile);

            Vector direction = new Vector(location.getX() - origin.getX(), location.getY() - origin.getY(), location.getZ() - origin.getZ());
            direction = direction.normalize().multiply(MathUtil.RANDOM.nextInt(i));
            attack.setVelocity(direction);
            attack.run();
        }
    }
}
