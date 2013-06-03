package de.raidcraft.skillsandeffects.pvp.skills.movement;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.common.Combat;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Retreat",
        description = "Zieht sich durch einen gekonnten Sprung aus dem Kampf zurück.",
        types = {EffectType.MOVEMENT, EffectType.HELPFUL}
)
public class Retreat extends AbstractSkill implements CommandTriggered {

    private boolean removeCombat = false;

    public Retreat(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        removeCombat = data.getBoolean("remove-combat", false);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        Vector direction = getHolder().getEntity().getEyeLocation().toVector();
        // flip the direction the player is looking at
        direction.multiply(-1.0);
        // add some height
        direction.add(new Vector(0, 3, 0));
        // multiply it to increase the force
        direction.multiply(2.0);
        // and finally apply it to the player
        getHolder().getEntity().setVelocity(direction);
        // also remove the combat effect
        if (removeCombat) {
            getHolder().removeEffect(Combat.class);
        }
    }
}
