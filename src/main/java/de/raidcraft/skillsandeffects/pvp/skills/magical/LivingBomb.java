package de.raidcraft.skillsandeffects.pvp.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.MagicalAttackType;
import de.raidcraft.skills.api.combat.action.EntityAttack;
import de.raidcraft.skills.api.combat.callback.EntityAttackCallback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.pvp.effects.debuff.LivingBombEffect;

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

        magicalAttack(MagicalAttackType.FIRE, new EntityAttackCallback() {
            @Override
            public void run(EntityAttack attack) throws CombatException {

                LivingBomb.this.addEffect(attack.getTarget(), LivingBombEffect.class);
            }
        });
    }
}
