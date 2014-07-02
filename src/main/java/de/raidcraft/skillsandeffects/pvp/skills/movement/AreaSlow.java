package de.raidcraft.skillsandeffects.pvp.skills.movement;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.effects.Slow;
import de.raidcraft.skills.tables.THeroSkill;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Area Slow",
        description = "Verlangsammt alle Gegner um dich herum.",
        types = {EffectType.MAGICAL, EffectType.DEBUFF, EffectType.MOVEMENT, EffectType.HARMFUL}
)
public class AreaSlow extends AbstractLevelableSkill implements CommandTriggered {

    public AreaSlow(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        for (CharacterTemplate target : getSafeNearbyTargets(false)) {
            addEffect(target, Slow.class);
        }
    }
}
