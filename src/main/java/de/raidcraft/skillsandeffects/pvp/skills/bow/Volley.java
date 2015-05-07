package de.raidcraft.skillsandeffects.pvp.skills.bow;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.ProjectileType;
import de.raidcraft.skills.api.combat.action.RangedAttack;
import de.raidcraft.skills.api.combat.callback.LocationCallback;
import de.raidcraft.skills.api.effect.common.QueuedProjectile;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.LocationUtil;
import de.raidcraft.util.MathUtil;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.util.stream.IntStream;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Volley",
        description = "Schießt einen Pfeilreigen ab der alles im Umkreis mit Pfeilen eindeckt.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING}
)
public class Volley extends AbstractLevelableSkill implements CommandTriggered {

    private final int[] angles = IntStream.range(-15, 15).toArray();
    private ConfigurationSection amount;

    public Volley(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        amount = data.getConfigurationSection("amount");
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(QueuedProjectile.class).addCallback(location -> {


            for (int i = 0; i < getAmount(); i++) {
                Location top = location.clone();
                // frst Meteor hits exactly
                if (i == 0) {
                    top = top.clone().add(0.5, MathUtil.RANDOM.nextInt(3) + 3, 0.5);
                } else {
                    top = top.add(MathUtil.RANDOM.nextInt(7) - 3 + MathUtil.RANDOM.nextDouble(),
                            MathUtil.RANDOM.nextInt(3) + 3,
                            MathUtil.RANDOM.nextInt(7) - 3 + MathUtil.RANDOM.nextDouble());
                }
                try {
                    RangedAttack<LocationCallback> attack = rangedAttack(ProjectileType.ARROW);
                    attack.setSpawnLocation(top);
                    attack.setVelocity(LocationUtil.getDirection(top, location));
                    attack.run();
                } catch (CombatException ignored) {
                }
            }
        });
    }

    public int getAmount() {

        return (int) ConfigUtil.getTotalValue(this, amount);
    }

    // the shooting player, his eyelocation, his view direction (player.getEyeLocation().getDirection()) and the speed of the spawned snowball:
    public Vector calculateDirection(Location location, Vector direction) {
        // making sure that the vector is length 1
        direction.normalize();
        // some trick, to get a vector pointing in the player's view direction, but on the x-z-plane only and without problems when looking straight up (x, z = 0 then)
        Vector dirY = (new Location(location.getWorld(), 0, 0, 0, location.getYaw(), 0)).getDirection().normalize();
        Vector vec;
        int angle = angles[RandomUtils.nextInt(angles.length)];
        if (angle != 0) {
            vec = rotateYAxis(dirY, angle);
            vec.multiply(Math.sqrt(vec.getX() * vec.getX() + vec.getZ() * vec.getZ())).subtract(dirY);
            vec = direction.clone().add(vec).normalize();
        } else {
            vec = direction.clone();
        }
        return vec;
    }

    public Vector rotateYAxis(Vector dir, double angleD) {

        double angleR = Math.toRadians(angleD);
        double x = dir.getX();
        double z = dir.getZ();
        double cos = Math.cos(angleR);
        double sin = Math.sin(angleR);
        return (new Vector(x * cos + z * (-sin), 0.0, x * sin + z * cos)).normalize();
    }
}
