package de.raidcraft.skillsandeffects.pvp.effects.buffs.protection;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.Effect;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.trigger.PlayerGainedEffectTrigger;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Magic Immunity",
        description = "Schütz vor allen magischen Attacken.",
        types = {EffectType.HELPFUL, EffectType.MAGICAL, EffectType.PROTECTION}
)
public class MagicImmunity extends ExpirableEffect<Skill> implements Triggered {

    public MagicImmunity(Skill source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (trigger.getAttack().isOfAttackType(EffectType.MAGICAL)) {
            throw new CombatException("Ziel ist immun gegen Zauber!");
        }
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onEffectGain(PlayerGainedEffectTrigger trigger) throws CombatException {

        Effect<?> effect = trigger.getEffect();
        if ((effect.isOfType(EffectType.HARMFUL) || effect.isOfType(EffectType.DAMAGING)) && effect.isOfType(EffectType.MAGICAL)) {
            throw new CombatException("Ziel ist immun gegen Zauber!");
        }
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {


    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {


    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {


    }
}
