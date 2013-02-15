package de.raidcraft.skillsandeffects.effects.buffs.avatar;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.skills.buffs.Avatar;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Paladin Avatar",
        description = "Verwandelt dich in einen Engel des Lichts mit erhöhter Verteidigung.",
        types = {EffectType.AVATAR, EffectType.BUFF, EffectType.HELPFUL}
)
public class PaladinAvatar extends AbstractAvatar implements Triggered {

    private double healthIncrease = 0.25;
    private double damageReduction = 0.10;
    private double healPercentage = 0.10;
    private int oldMaxHealth = 20;

    public PaladinAvatar(Avatar source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        healthIncrease = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("health-increase"));
        damageReduction = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("damage-reduction"));
        healPercentage = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("heal-percentage"));
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        oldMaxHealth = target.getMaxHealth();
        int newMaxHealth = (int) (oldMaxHealth + oldMaxHealth * healthIncrease);
        target.setMaxHealth(newMaxHealth);
        getSource().getHero().combatLog("Leben um " + healthIncrease * 100 + "% (" + (newMaxHealth - oldMaxHealth) + ") erhöht.");
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        int newMaxHealth = target.getMaxHealth();
        target.setMaxHealth(oldMaxHealth);
        getSource().getHero().combatLog("Leben um " + healthIncrease * 100 + "% (" + (newMaxHealth - oldMaxHealth) + ") verringert.");
        oldMaxHealth = 20;
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onAttack(AttackTrigger trigger) throws CombatException {

        if (!trigger.getAttack().isOfAttackType(EffectType.PHYSICAL)) {
            return;
        }

        int healAmount = (int) (trigger.getAttack().getDamage() * healPercentage);
        // heal the paladin
        getSource().getHero().heal(healAmount);
        trigger.getAttack().combatLog(this, "<s> wurde um " + healPercentage * 100 + "% (" + healAmount + ") des Schadens geheilt.");
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.HIGHEST)
    public void onDamage(DamageTrigger trigger) {

        if (!trigger.getAttack().isOfAttackType(EffectType.PHYSICAL)) {
            return;
        }

        int oldDamage = trigger.getAttack().getDamage();
        int newDamage = (int) (oldDamage - oldDamage * damageReduction);
        trigger.getAttack().setDamage(newDamage);
        trigger.getAttack().combatLog(this, "Schaden von <s> wurde um " + damageReduction * 100 + "% (" + (oldDamage - newDamage) + ") verringert.");
    }
}
