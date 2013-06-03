package de.raidcraft.skillsandeffects.pvp.effects.buffs.avatar;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.HealTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.skills.buffs.Avatar;
import de.raidcraft.util.MathUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Bloodmage Avatar",
        description = "Verwandelt dich in einen Engel des Lichts mit erhöhter Verteidigung.",
        types = {EffectType.AVATAR, EffectType.BUFF, EffectType.HELPFUL}
)
public class BloodmageAvatar extends AbstractAvatar implements Triggered {

    private double healthIncreasePercent = 0.50;
    private double healPercentage = 0.50;
    private int healthIncrease = 20;

    public BloodmageAvatar(Avatar source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        healthIncreasePercent = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("health-increase"));
        healPercentage = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("heal-increase"));
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        healthIncrease = (int) (target.getMaxHealth() * healthIncreasePercent);
        target.increaseMaxHealth(healthIncrease);
        getSource().getHolder().combatLog("Maximale Leben um " + healthIncreasePercent * 100 + "% (" + (healthIncrease) + ") erhöht.");
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        target.decreaseMaxHealth(healthIncrease);
        getSource().getHolder().combatLog("Leben um " + healthIncreasePercent * 100 + "% (" + (healthIncrease) + ") verringert.");
        healthIncrease = 20;
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onHeal(HealTrigger trigger) {

        // increase the gained heal by the defined amount
        int oldHeal = trigger.getAmount();
        int newHeal = (int) (oldHeal + oldHeal * healPercentage);
        combatLog("Erhaltene Heilung um " + (newHeal - oldHeal) + "(" + MathUtil.toPercent(healPercentage) + "%) erhöht.");
        trigger.setAmount(newHeal);
    }
}
