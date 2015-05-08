package de.raidcraft.skillsandeffects.pvp.effects.buffs.combatstance;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.skills.buffs.CombatStance;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Attack Stance",
        description = "Erhöht den ausgeteilten Schaden.",
        types = {EffectType.COMBAT_STANCE, EffectType.HELPFUL, EffectType.BUFF, EffectType.MAGICAL},
        priority = 1.0
)
public class AttackStance extends AbstractCombatStance implements Triggered {

    private ConfigurationSection attackIncrease;
    private boolean physicalOnly = false;

    public AttackStance(CombatStance source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        attackIncrease = data.getConfigurationSection("attack-increase");
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

    private double getAttackIncrease() {

        return ConfigUtil.getTotalValue(getSource(), attackIncrease);
    }
}
