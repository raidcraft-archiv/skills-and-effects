package de.raidcraft.skillsandeffects.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.MagicalAttackType;
import de.raidcraft.skills.api.combat.callback.RangedCallback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.effects.debuff.LivingBombEffect;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Living Bomb",
        desc = "Sprengt alle Gegner nach Ablauf der Zeit um das Ziel.",
        types = {EffectType.DAMAGING, EffectType.DEBUFF, EffectType.MAGICAL, EffectType.SILENCABLE},
        elements = {EffectElement.FIRE}
)
public class LivingBomb extends AbstractSkill implements CommandTriggered {

    public LivingBomb(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        magicalAttack(MagicalAttackType.FIRE, new RangedCallback() {
            @Override
            public void run(CharacterTemplate target) throws CombatException {

                LivingBomb.this.addEffect(target, LivingBombEffect.class);
            }
        });
    }
}
