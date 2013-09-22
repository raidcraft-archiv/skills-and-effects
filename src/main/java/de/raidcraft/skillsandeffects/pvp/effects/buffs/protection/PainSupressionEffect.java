package de.raidcraft.skillsandeffects.pvp.effects.buffs.protection;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skillsandeffects.pvp.skills.protection.PainSupression;
import de.raidcraft.util.MathUtil;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Pain Supression",
        description = "Verringert den erlittenen Schaden enorm.",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.HELPFUL, EffectType.ABSORBING, EffectType.PROTECTION, EffectType.PURGEABLE}
)
public class PainSupressionEffect extends ExpirableEffect<PainSupression> implements Triggered {

    private double reduction;

    public PainSupressionEffect(PainSupression source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        reduction = getSource().getDamageReduction();
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.HIGHEST)
    public void onDamage(DamageTrigger trigger) {

        double oldDamage = trigger.getAttack().getDamage();
        double newDamage = oldDamage - oldDamage * reduction;
        combatLog("Schaden um " + (oldDamage - newDamage) + "(" + MathUtil.toPercent(reduction) + "%) verringert.");
        trigger.getAttack().setDamage(newDamage);
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
