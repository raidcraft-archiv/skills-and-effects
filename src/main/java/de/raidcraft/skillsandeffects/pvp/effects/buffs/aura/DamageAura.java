package de.raidcraft.skillsandeffects.pvp.effects.buffs.aura;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.skills.buffs.Aura;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Damage Aura",
        description = "Erhöht deinen Schaden.",
        types = {EffectType.AURA, EffectType.HELPFUL, EffectType.BUFF, EffectType.MAGICAL},
        priority = 1.0
)
public class DamageAura extends AbstractAura implements Triggered {

    private ConfigurationSection damageIncrease;
    private boolean physicalOnly = false;

    public DamageAura(Aura source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        damageIncrease = data.getConfigurationSection("damage-increase");
        physicalOnly = data.getBoolean("physical", false);
    }

    @TriggerHandler
    public void onAttack(AttackTrigger trigger) {

        if (physicalOnly && !trigger.getAttack().isOfAttackType(EffectType.PHYSICAL)) {
            return;
        }
        double oldDamage = trigger.getAttack().getDamage();
        double newDamage = oldDamage + oldDamage * getDamageIncrease();
        trigger.getAttack().setDamage(newDamage);
        getSource().getHolder().combatLog(this,
                "Schaden um " + (int) (getDamageIncrease() * 100) + "% (" + (newDamage - oldDamage) + ") erhöht.");
    }

    private double getDamageIncrease() {

        return ConfigUtil.getTotalValue(getSource(), damageIncrease);
    }
}
