package de.raidcraft.skillsandeffects.pvp.effects.buffs.generic;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.MaxHealthChangeTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.MathUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Health Buff",
        description = "Erhöht die maximale Lebensenergie.",
        types = {EffectType.HELPFUL, EffectType.MAGICAL, EffectType.BUFF, EffectType.PURGEABLE},
        global = true
)
public class HealthBuff extends ExpirableEffect<Skill> implements Triggered {

    private int increasedHealth;
    private double modifier;

    public HealthBuff(Skill source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onMaxHealthChange(MaxHealthChangeTrigger trigger) {

        double value = trigger.getEvent().getValue();
        trigger.getEvent().setValue(value + (value * modifier));
        increasedHealth += value * modifier;
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        increasedHealth = (int) (target.getMaxHealth() * modifier);
        combatLog("Maximale Leben um " + increasedHealth + "(" + MathUtil.toPercent(modifier) + ") erhöht.");
        target.increaseMaxHealth(increasedHealth);
    }

    @Override
    public void load(ConfigurationSection data) {

        modifier = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("health"));
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {


    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        combatLog("Maximale Leben um " + increasedHealth + "(" + MathUtil.toPercent(modifier) + ") verringert.");
        target.decreaseMaxHealth(increasedHealth);
    }
}
