package de.raidcraft.skillsandeffects.pvp.effects.armor;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.PeriodicExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.resource.Resource;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.HealTrigger;
import de.raidcraft.skillsandeffects.pvp.skills.armor.BuffingArmor;
import de.raidcraft.util.MathUtil;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Buffing Armor",
        description = "Schütz den Zaubernden vor Effekten und gibt Boni.",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.HELPFUL}
)
public class BuffingArmorEffect extends PeriodicExpirableEffect<BuffingArmor> implements Triggered {

    public BuffingArmorEffect(BuffingArmor source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onHeal(HealTrigger trigger) {

        if (!getSource().hasType(BuffingArmor.Type.HEAL_INCREASE)
                || !trigger.getTarget().equals(getTarget())) {
            return;
        }
        int amount = trigger.getAmount();
        double healIncrease = getSource().getHealIncrease();
        int newAmount = (int) (amount + amount * healIncrease);
        combatLog("Erhaltene Heilung um " + (newAmount - amount) + "(" + MathUtil.toPercent(healIncrease) + ") erhöht.");
        trigger.setAmount(newAmount);
    }

    @Override
    protected void tick(CharacterTemplate target) throws CombatException {

        if (!getSource().hasType(BuffingArmor.Type.RESOURCE_REGAIN)) {
            return;
        }
        Resource resource = getSource().getResource();
        resource.setCurrent((int) (resource.getCurrent() + getSource().getResourceRegain()));
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        if (getSource().hasType(BuffingArmor.Type.HEALTH_INCREASE)) {
            int maxHealth = target.getMaxHealth();
            double healthIncrease = getSource().getHealthIncrease();
            int increase = (int) (maxHealth * healthIncrease);
            combatLog("Maximale Leben um " + increase + "(" + MathUtil.toPercent(healthIncrease) + ") erhöht.");
            target.increaseMaxHealth(increase);
        }
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        if (getSource().hasType(BuffingArmor.Type.HEALTH_INCREASE)) {
            int maxHealth = target.getMaxHealth();
            double healthDecrease = getSource().getHealthIncrease();
            int decrease = (int) (maxHealth * healthDecrease);
            combatLog("Maximale Leben um " + decrease + "(" + MathUtil.toPercent(healthDecrease) + ") verringert.");
            target.decreaseMaxHealth(decrease);
        }
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {


    }
}
