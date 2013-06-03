package de.raidcraft.skillsandeffects.pvp.effects.buffs.protection;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skillsandeffects.pvp.skills.protection.DamageTransfer;
import de.raidcraft.util.MathUtil;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Damage Transfer",
        description = "Transferiert genommenen Schaden auf den Zaubernden.",
        types = {EffectType.PURGEABLE, EffectType.BUFF, EffectType.MAGICAL, EffectType.PROTECTION, EffectType.HELPFUL}
)
public class DamageTransferEffect extends ExpirableEffect<DamageTransfer> implements Triggered {

    public DamageTransferEffect(DamageTransfer source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.HIGH)
    public void onDamage(DamageTrigger trigger) {

        double percent = getSource().getTransferedDamage();
        int transferedDamage = (int) (trigger.getAttack().getDamage() * percent);
        Hero holder = getSource().getHolder();
        combatLog(transferedDamage + "(" + MathUtil.toPercent(percent) + "%) Schaden auf " + holder + " transferiert.");
        trigger.getAttack().setDamage(trigger.getAttack().getDamage() - transferedDamage);
        // do pure direct damage to the source
        holder.combatLog(this, transferedDamage + " direkten Schaden erhalten.");
        holder.setHealth(holder.getHealth() - transferedDamage);
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
