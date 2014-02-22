package de.raidcraft.skillsandeffects.pvp.skills.physical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.ProjectileType;
import de.raidcraft.skills.api.combat.callback.RangedCallback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Pull",
        description = "Wirft die aktuelle Waffe in Richtung Gegner.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING}
)
public class Pull extends AbstractSkill implements CommandTriggered {

    public Pull(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);

    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        Projectile projectile = rangedAttack(ProjectileType.FISH, new RangedCallback() {
            @Override
            public void run(CharacterTemplate target) throws CombatException {

                if (LocationUtil.isSafeZone(target.getEntity().getLocation())) {
                    throw new CombatException(CombatException.Type.INVALID_TARGET);
                }
                // calculate the velocity and pull the target towards the caster
                Location sourceLoc = target.getEntity().getLocation();
                Location targetLoc = getHolder().getEntity().getLocation();

                Vector direction = new Vector(
                        targetLoc.getX() - sourceLoc.getX(),
                        targetLoc.getY() - sourceLoc.getY(),
                        targetLoc.getZ() - sourceLoc.getZ()
                );

                direction.add(new Vector(0, 1.5, 0));
                direction.multiply(1.5F);

                target.getEntity().setVelocity(direction);
            }
        }).getProjectile();
        projectile.setVelocity(projectile.getVelocity().multiply(5.0));
    }
}
