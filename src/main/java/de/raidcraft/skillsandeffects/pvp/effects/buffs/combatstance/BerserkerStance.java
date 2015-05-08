package de.raidcraft.skillsandeffects.pvp.effects.buffs.combatstance;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.skills.buffs.CombatStance;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Berserker Stance",
        description = "Erhöht den ausgeteilten und eingesteckten Schaden.",
        types = {EffectType.COMBAT_STANCE, EffectType.HELPFUL, EffectType.BUFF, EffectType.MAGICAL},
        priority = 1.0
)
public class BerserkerStance extends AbstractCombatStance implements Triggered {

    private ConfigurationSection attackIncrease;
    private ConfigurationSection damageIncrease;
    private boolean physicalOnly = false;

    public BerserkerStance(CombatStance source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        attackIncrease = data.getConfigurationSection("attack-increase");
        damageIncrease = data.getConfigurationSection("damage-increase");
        physicalOnly = data.getBoolean("physical", false);
    }

    @TriggerHandler
    public void onAttack(AttackTrigger trigger) {

        if (physicalOnly && !trigger.getAttack().isOfAttackType(EffectType.PHYSICAL)) {
            return;
        }
        double oldDamage = trigger.getAttack().getDamage();
        double newDamage = oldDamage + oldDamage * getAttackIncrease();
        trigger.getAttack().setDamage(newDamage);
        getSource().getHolder().combatLog(this,
                "Schaden um " + (int) (getAttackIncrease() * 100) + "% (" + (newDamage - oldDamage) + ") erhöht.");
    }

    @TriggerHandler
    public void onDamage(DamageTrigger trigger) {

        if (physicalOnly && !trigger.getAttack().isOfAttackType(EffectType.PHYSICAL)) {
            return;
        }
        double oldDamage = trigger.getAttack().getDamage();
        double newDamage = oldDamage + oldDamage * getDamageIncrease();
        getSource().getHolder().combatLog(this,
                "Erlittener Schaden um " + (int) (getDamageIncrease() * 100) + "% (" + (newDamage - oldDamage) + ") erhöht.");
        trigger.getAttack().setDamage(newDamage);
    }

    private double getAttackIncrease() {

        return ConfigUtil.getTotalValue(getSource(), attackIncrease);
    }

    private double getDamageIncrease() {

        return ConfigUtil.getTotalValue(getSource(), damageIncrease);
    }
}
