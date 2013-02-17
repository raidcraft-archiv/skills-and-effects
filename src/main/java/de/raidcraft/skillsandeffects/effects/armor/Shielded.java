package de.raidcraft.skillsandeffects.effects.armor;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.EffectDamage;
import de.raidcraft.skills.api.combat.callback.Callback;
import de.raidcraft.skills.api.effect.AbstractEffect;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skillsandeffects.effects.potion.Slow;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Shielded",
        description = "Schützt dich vor Angriffen",
        types = {EffectType.HELPFUL, EffectType.ABSORBING, EffectType.REDUCING, EffectType.REFLECTING}
)
public class Shielded extends AbstractEffect<Skill> implements Triggered {

    private double damageReduction = 0.0;
    private int blockedDamage = 0;
    private boolean reflect = false;
    private boolean slow = false;
    private Callback<DamageTrigger> callback;

    public Shielded(Skill source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        reflect = data.getBoolean("reflect", false);
        damageReduction = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("reduction"));
    }

    public void addCallback(Callback<DamageTrigger> callback) {

        this.callback = callback;
    }

    @TriggerHandler
    public void onDamage(DamageTrigger trigger) {

        int oldDamage = trigger.getAttack().getDamage();
        int newDamage = (int) (oldDamage - oldDamage * damageReduction);
        blockedDamage = oldDamage - newDamage;

        try {
            // run the callback
            if (callback != null) {
                callback.run(trigger);
            }
        } catch (CombatException e) {
            // we want to display the message to the player and return
            // from this effect silently to allow the damage to go thru
            trigger.getAttack().setCancelled(true);
            getSource().getHero().sendMessage(ChatColor.RED + e.getMessage());
        }
        // if the attack was cancelled that means the callback dont want the damage blocked
        // but the attack needs to be uncancelled for the damage to go thru
        if (trigger.getAttack().isCancelled()) {
            trigger.getAttack().setCancelled(false);
            return;
        }

        // lets rese the blocked damage maybe the callback changed it
        newDamage = oldDamage - getBlockedDamage();

        try {
            if (reflect && trigger.getAttack().getSource() instanceof CharacterTemplate) {
                new EffectDamage(this, getBlockedDamage()).run();
            }
        } catch (CombatException e) {
            getSource().getHero().sendMessage("Schaden konnte nicht reflektiert werden: " + e.getMessage());
        }
        trigger.getAttack().setDamage(newDamage);
        getSource().getHero().debug("reduced damage " + oldDamage + "->" + newDamage + " - skill: " + getSource());
        getSource().getHero().combatLog(this, getBlockedDamage() + " Schaden absorbiert.");
    }

    public int getBlockedDamage() {

        return blockedDamage;
    }

    public void setBlockedDamage(int blockedDamage) {

        this.blockedDamage = blockedDamage;
    }

    public void setDamageReduction(double damageReduction) {

        this.damageReduction = damageReduction;
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        if (slow) {
            target.addEffect(getSource(), getSource(), Slow.class);
        }
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        if (slow) {
            target.removeEffect(Slow.class);
        }
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        if (slow) {
            target.addEffect(getSource(), getSource(), Slow.class);
        }
    }
}
