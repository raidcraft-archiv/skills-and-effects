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
import de.raidcraft.skillsandeffects.pvp.skills.buffs.BoneShield;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Bone Shield",
        description = "Schütz das Ziel mit Knochen und absorbiert eingehenden Schaden.",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.PURGEABLE, EffectType.ABSORBING}
)
public class BoneShieldEffect extends ExpirableEffect<BoneShield> implements Triggered {

    private int damageToAbsorb;

    public BoneShieldEffect(BoneShield source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (damageToAbsorb <= 0) {
            remove();
            return;
        }
        double damage = trigger.getAttack().getDamage();
        double newDamage = damage - damageToAbsorb;
        if (newDamage < 0) {
            newDamage = 0;
        }
        damageToAbsorb -= damage;
        combatLog(damage - newDamage + " von " + damage + " Schaden absorbiert.");
        trigger.getAttack().setDamage(newDamage);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        renew(target);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        damageToAbsorb = (int) getSource().getAbsorbtion();
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {


    }
}
