package de.raidcraft.skillsandeffects.pvp.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
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

import java.util.List;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Bloody Sacrifice",
        description = "Heilt die Gruppe des Blutmagiers um die Menge der geopferten Leben und fügt allen Gegnern im Umkreis von 10m den selben Schaden zu. Es werden die aktuellen Leben verrechnet, nicht die max. Leben.",
        types = {EffectType.MAGICAL, EffectType.DAMAGING, EffectType.HEALING},
        elements = {EffectElement.DARK}
)
public class BloodySacrifice extends AbstractSkill implements CommandTriggered {

    public BloodySacrifice(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        List<CharacterTemplate> nearbyTargets = getNearbyTargets();
        for (CharacterTemplate target : nearbyTargets) {
            if (target.equals(getHolder())) {
                continue;
            }
            if (target.isFriendly(getHolder())) {
                new HealAction<>(this, target, getTotalDamage()).run();
            } else {
                magicalAttack(target, getTotalDamage());
            }
        }
    }
}
