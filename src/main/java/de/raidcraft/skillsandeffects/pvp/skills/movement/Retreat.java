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
import de.raidcraft.skillsandeffects.pvp.effects.misc.RetreatEffect;
import org.bukkit.Location;
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

    public boolean isRemovingCombat() {

        return removeCombat;
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        Location targetLoc = getTargetBlock();
        Location playerLoc = getHolder().getEntity().getLocation();
        double xDir = targetLoc.getX() - playerLoc.getX();
        double zDir = targetLoc.getZ() - playerLoc.getZ();
        // set the target block and flip it
        Vector vector = new Vector(xDir, 1.5D, zDir).multiply(-1);
        // lets first null the velocity of the player to stop movement
        getHolder().getEntity().setVelocity(new Vector(0, 0, 0));
        // and finally apply it to the player
        getHolder().getEntity().setVelocity(vector);
        // also remove the combat effect
        if (removeCombat) {
            getHolder().removeEffect(Combat.class);
        }
        // and add the retreat effect
        addEffect(getHolder(), RetreatEffect.class);
    }
}
