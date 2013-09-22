package de.raidcraft.skillsandeffects.pvp.effects.buffs.damage;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.Buff;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.effect.Stackable;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Bloodlust",
        description = "Erhöht deinen Schaden mit jedem Stack.",
        types = {EffectType.PHYSICAL, EffectType.BUFF}
)
public class BloodlustEffect extends ExpirableEffect<Skill> implements Stackable, Triggered, Buff {

    private double damageIncreasePerStack;
    private double attackIncreasePerStack;

    public BloodlustEffect(Skill source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        damageIncreasePerStack = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("damage-increase"));
        attackIncreasePerStack = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("attack-increase"));
    }

    @TriggerHandler
    public void onAttack(AttackTrigger trigger) {

        double oldDamage = trigger.getAttack().getDamage();
        double increase = attackIncreasePerStack * getStacks();
        double newDamage = oldDamage + oldDamage * increase;
        if (newDamage - oldDamage < 1) {
            return;
        }
        trigger.getAttack().setDamage(newDamage);
        getSource().getHolder().combatLog(this,"Schaden um " + (int)(increase * 100) + "% (" + (newDamage - oldDamage) + ") erhöht.");
        getSource().getHolder().debug("damaged increased " + oldDamage + "->" + newDamage + " - " + getName());
    }

    @TriggerHandler
    public void onDamage(DamageTrigger trigger) {

        double oldDamage = trigger.getAttack().getDamage();
        double increase = damageIncreasePerStack * getStacks();
        double newDamage = oldDamage + oldDamage * increase;
        if (newDamage - oldDamage < 1) {
            return;
        }
        trigger.getAttack().setDamage(newDamage);
        getSource().getHolder().combatLog(this, "Erlittener Schaden um " + increase * 100 + "% (" + (newDamage - oldDamage) + ") erhöht.");
        getSource().getHolder().debug("taken damaged increased " + oldDamage + "->" + newDamage + " - " + getName());
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        setStacks(1);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        setStacks(0);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        setStacks(getStacks() + 1);
    }

    @Override
    public String displayBuff() {

        if (getStacks() < 1) {
            return null;
        }
        return ChatColor.YELLOW + "[" + ChatColor.GREEN + getFriendlyName() + ChatColor.YELLOW + ":"
                + ChatColor.AQUA + getStacks() + "/" + getMaxStacks() + ChatColor.YELLOW + "]";
    }
}
