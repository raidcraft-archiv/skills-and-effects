package de.raidcraft.skillsandeffects.pvp.skills.physical;

import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skillsandeffects.pvp.effects.misc.ParryEffect;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Counter Attack",
        description = "Der nächste Schlag nach einer Parrade fügt zusätzlichen Schaden zu.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING}
)
public class CounterAttack extends AbstractSkill implements Triggered {

    public CounterAttack(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.HIGHEST)
    public void onAttack(AttackTrigger trigger) {

        if (trigger.getAttack().isOfAttackType(EffectType.DEFAULT_ATTACK)
                && trigger.getAttack().isOfAttackType(EffectType.PHYSICAL)
                && getHero().hasEffect(ParryEffect.class)
                && canUseSkill()) {
            if (trigger.getAttack().getDamage() < getTotalDamage()) {
                trigger.getAttack().setDamage(getTotalDamage());
                substractUsageCost();
            }
        }
    }
}
