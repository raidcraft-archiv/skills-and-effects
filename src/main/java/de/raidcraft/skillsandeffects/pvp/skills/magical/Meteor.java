package de.raidcraft.skillsandeffects.pvp.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
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
import de.raidcraft.util.MathUtil;
import lombok.Getter;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

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

    public Meteor(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        amount = (int) ConfigUtil.getTotalValue(this, data.getConfigurationSection("amount"));
        if (amount < 1) {
            amount = 1;
        }
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        dropMeteors(getTargetBlock().clone());
    }

    public void dropMeteors(final Location targetLocation) throws CombatException {

        // lets start a repeating task to not spawn all meteors at once
        for (int i = 0; i < getAmount(); i++) {
            Location top = targetLocation.clone();
            // first Meteor hits exactly
            if (i == 0) {
                top = top.add(0.5, MathUtil.RANDOM.nextInt(6) + 6, 0.5);
            } else {
                top = top.add(MathUtil.RANDOM.nextInt(7) - 3 + MathUtil.RANDOM.nextDouble(),
                        MathUtil.RANDOM.nextInt(6) + 6,
                        MathUtil.RANDOM.nextInt(7) - 3 + MathUtil.RANDOM.nextDouble());
            }
            try {
                RangedAttack<LocationCallback> attack = rangedAttack(ProjectileType.LARGE_FIREBALL, getTotalDamage(), location -> {
                    location.getWorld().playEffect(location, Effect.EXPLOSION_HUGE, 5);
                });
                attack.setSpawnLocation(top);
                attack.setVelocity(targetLocation.clone().subtract(top).toVector().multiply(0.2));
                //                attack.setVelocity(LocationUtil.getDirection(top, targetLocation).multiply(speed));
                attack.run();
            } catch (CombatException ignored) {
            }
        }
    }
}
