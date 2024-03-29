package de.raidcraft.skillsandeffects.pvp.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.effects.disabling.Immobilize;
import de.raidcraft.skills.tables.THeroSkill;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Frostnova",
        description = "Frostet alle Gegner im Umkreis am Boden fest.",
        types = {EffectType.HARMFUL, EffectType.MAGICAL, EffectType.DAMAGING, EffectType.MOVEMENT, EffectType.AREA},
        elements = {EffectElement.ICE}
)
public class Frostnova extends AbstractSkill implements CommandTriggered {

    public Frostnova(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        for (CharacterTemplate target : getSafeNearbyTargets(false)) {
            try {
                magicalAttack(target, attack -> {

                    Frostnova.this.addEffect(attack.getTarget(), Immobilize.class);
                }).run();
            } catch (CombatException ignored) {
            }
        }
    }
}
