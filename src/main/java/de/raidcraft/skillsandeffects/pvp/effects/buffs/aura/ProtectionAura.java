package de.raidcraft.skillsandeffects.pvp.effects.buffs.aura;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.skills.buffs.Aura;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Protection Aura",
        description = "Schützt dich und deine Gruppe vor Schaden.",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.REDUCING, EffectType.AURA},
        elements = {EffectElement.HOLY},
        priority = 1.0
)
public class ProtectionAura extends AbstractAura implements Triggered {

    private ConfigurationSection reduction;

    public ProtectionAura(Aura source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        reduction = data.getConfigurationSection("reduction");
    }

    private double getDamageReduction() {

        return ConfigUtil.getTotalValue(getSource(), reduction);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onDamage(DamageTrigger trigger) {

        double oldDamage = trigger.getAttack().getDamage();
        double newDamage = oldDamage - oldDamage * getDamageReduction();
        trigger.getAttack().setDamage(newDamage);
        getSource().getHolder().combatLog(this,
                "Schaden um " + (int) (getDamageReduction() * 100) + "% (" + (oldDamage - newDamage) + ") veringert.");
    }
}
