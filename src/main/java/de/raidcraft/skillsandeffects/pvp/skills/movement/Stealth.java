package de.raidcraft.skillsandeffects.pvp.skills.movement;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.pvp.effects.potion.Invisibility;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Stealth",
        description = "Du schwindest in die Schatten und wirst für eine Zeit unsichtbar.",
        types = {EffectType.MOVEMENT, EffectType.BUFF, EffectType.HELPFUL},
        triggerCombat = false
)
public class Stealth extends AbstractSkill implements CommandTriggered {

    public Stealth(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (getHolder().hasEffect(Invisibility.class)) {
            removeEffect(Invisibility.class);
        } else {
            addEffect(Invisibility.class);
        }
    }
}
