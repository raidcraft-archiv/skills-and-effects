package de.raidcraft.skillsandeffects.pvp.effects.buffs.protection;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skillsandeffects.pvp.skills.protection.Fade;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Fade",
        description = "Verblasst und verringert die Ausweichenchance.",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.PURGEABLE, EffectType.PROTECTION}
)
public class FadeEffect extends ExpirableEffect<Fade> implements Triggered {

    private double chance;

    public FadeEffect(Fade source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        chance = getSource().getEvadeChance();
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (!trigger.getAttack().isOfAttackType(EffectType.PHYSICAL)) {
            return;
        }
        if (Math.random() < chance) {
            throw new CombatException(CombatException.Type.EVADED);
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
