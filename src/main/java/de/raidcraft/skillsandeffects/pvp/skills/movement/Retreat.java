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
import de.raidcraft.util.LocationUtil;
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
    private double force = 2.0;
    private double height = 3.0;

    public Retreat(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        removeCombat = data.getBoolean("remove-combat", false);
        force = data.getDouble("force", 2.0);
        height = data.getDouble("height", 0.3);
    }

    public boolean isRemovingCombat() {

        return removeCombat;
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        Vector direction = LocationUtil.getRevertedViewDirection(getHolder().getEntity().getLocation());
        direction.normalize().multiply(force).clone().subtract(getHolder().getPlayer().getLocation().toVector()).add(new Vector(0, height, 0));
        getHolder().getEntity().setVelocity(direction);
        // also remove the combat effect
        if (removeCombat) {
            removeEffect(Combat.class);
        }
        // and add the retreat effect
        addEffect(RetreatEffect.class);
    }
}
