package de.raidcraft.skillsandeffects.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.MagicalAttack;
import de.raidcraft.skills.api.combat.callback.RangedCallback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.effects.disabling.Root;
import de.raidcraft.skills.tables.THeroSkill;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Frostnova",
        desc = "Frostet alle Gegner im Umkreis am Boden fest.",
        types = {EffectType.HARMFUL, EffectType.MAGICAL, EffectType.DAMAGING, EffectType.MOVEMENT},
        elements = {EffectElement.ICE}
)
public class Frostnova extends AbstractSkill implements CommandTriggered {

    public Frostnova(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        for (CharacterTemplate target : getNearbyTargets()) {
            new MagicalAttack(getHero(), target, getTotalDamage(), new RangedCallback() {
                @Override
                public void run(CharacterTemplate trigger) throws CombatException {

                    Frostnova.this.addEffect(trigger, Root.class);
                }
            }).run();
        }
    }
}
