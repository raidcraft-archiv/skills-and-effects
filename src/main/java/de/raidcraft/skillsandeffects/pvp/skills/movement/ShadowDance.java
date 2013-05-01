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
import de.raidcraft.skillsandeffects.pvp.effects.movement.ShadowDanceEffect;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Shadow Dance",
        description = "Springt mit Shadow Step von Ziel zu Ziel.",
        types = {EffectType.HELPFUL, EffectType.BUFF, EffectType.DAMAGING, EffectType.MOVEMENT}
)
public class ShadowDance extends AbstractSkill implements CommandTriggered {

    public ShadowDance(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(getHolder(), ShadowDanceEffect.class);
    }
}
