package de.raidcraft.skillsandeffects.pvp.skills.healing;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.HealAction;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Area Heal",
        description = "Heilt dich und deine Gruppe.",
        types = {EffectType.MAGICAL, EffectType.SILENCABLE, EffectType.HEALING, EffectType.HELPFUL, EffectType.AREA}
)
public class AreaHeal extends AbstractSkill implements CommandTriggered {

    public AreaHeal(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        for (CharacterTemplate target : getSafeNearbyTargets(true)) {
            try {
                new HealAction<>(this, target, getTotalDamage()).run();
            } catch (CombatException ignored) {
            }
        }
        // also heal ourselves
        new HealAction<>(this, getHolder(), getTotalDamage()).run();
    }
}
