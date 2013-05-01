package de.raidcraft.skillsandeffects.pvp.skills.buffs;

import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.damage.BloodlustEffect;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Bloodlust",
        description = "Erhöht deinen Schaden mit jedem erlittenen Treffer.",
        types = {EffectType.BUFF, EffectType.PHYSICAL, EffectType.HELPFUL}
)
public class Bloodlust extends AbstractSkill implements Triggered {

    public Bloodlust(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @TriggerHandler
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (!getHolder().isInCombat()) {
            return;
        }
        if (isOnCooldown()) {
            return;
        }
        if (getHolder().hasEffect(BloodlustEffect.class)) {
            getHolder().getEffect(BloodlustEffect.class).renew();
        } else {
            addEffect(getHolder(), BloodlustEffect.class);
        }
    }
}
