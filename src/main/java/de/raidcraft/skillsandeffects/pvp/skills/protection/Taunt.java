package de.raidcraft.skillsandeffects.pvp.skills.protection;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.pvp.effects.movement.Taunted;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Taunt",
        description = "Zwingt alle Ziele im Umkreis dazu dich anzugreifen.",
        types = {EffectType.DEBUFF, EffectType.AREA},
        triggerCombat = true
)
public class Taunt extends AbstractSkill implements CommandTriggered {

    public Taunt(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        for (CharacterTemplate target : getSafeNearbyTargets(false)) {
            if (!target.isFriendly(getHolder())) {
                addEffect(getHolder(), target, Taunted.class);
            }
        }
    }
}
