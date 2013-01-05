package de.raidcraft.skillsandeffects.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.ProjectileType;
import de.raidcraft.skills.api.combat.action.RangedAttack;
import de.raidcraft.skills.api.combat.callback.RangedCallback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.effects.damaging.Burn;
import de.raidcraft.skills.tables.THeroSkill;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Fireball",
        desc = "Schießt einen Feuerball auf den Gegener",
        types = {EffectType.MAGICAL, EffectType.DAMAGING, EffectType.SILENCABLE, EffectType.HARMFUL},
        elements = {EffectElement.FIRE},
        triggerCombat = true
)
public class Fireball extends AbstractLevelableSkill implements CommandTriggered {

    public Fireball(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        new RangedAttack(getHero(), ProjectileType.FIREBALL, new RangedCallback() {
            @Override
            public void run(CharacterTemplate target) throws CombatException {

                Fireball.this.addEffect(target, Burn.class);
            }
        }).run();
    }
}
