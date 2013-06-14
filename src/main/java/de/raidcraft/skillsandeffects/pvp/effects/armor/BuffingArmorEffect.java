package de.raidcraft.skillsandeffects.pvp.effects.armor;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.PeriodicEffect;
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
public class BuffingArmorEffect extends PeriodicEffect<BuffingArmor> implements Triggered {

    private int healthIncrease;
    private double increasePercent;

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
            increasePercent = getSource().getHealthIncrease();
            healthIncrease = (int) (maxHealth * increasePercent);
            combatLog("Maximale Leben um " + healthIncrease + "(" + MathUtil.toPercent(increasePercent) + ") erhöht.");
            target.increaseMaxHealth(healthIncrease);
        }
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        if (getSource().hasType(BuffingArmor.Type.HEALTH_INCREASE)) {
            combatLog("Maximale Leben um " + healthIncrease + "(" + MathUtil.toPercent(increasePercent) + ") verringert.");
            target.decreaseMaxHealth(healthIncrease);
        }
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {


    }
}
