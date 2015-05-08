package de.raidcraft.skillsandeffects.pvp.effects.buffs.combatstance;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.skills.buffs.CombatStance;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Defense Stance",
        description = "Verringert den eingesteckten Schaden.",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.REDUCING, EffectType.COMBAT_STANCE},
        priority = 1.0
)
public class DefenseStance extends AbstractCombatStance implements Triggered {

    private ConfigurationSection reduction;

    public DefenseStance(CombatStance source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        reduction = data.getConfigurationSection("reduction");
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onDamage(DamageTrigger trigger) {

        double oldDamage = trigger.getAttack().getDamage();
        double newDamage = oldDamage - oldDamage * getDamageReduction();
        trigger.getAttack().setDamage(newDamage);
        getSource().getHolder().combatLog(this,
                "Schaden um " + (int) (getDamageReduction() * 100) + "% (" + (oldDamage - newDamage) + ") veringert.");
    }

    private double getDamageReduction() {

        return ConfigUtil.getTotalValue(getSource(), reduction);
    }
}
