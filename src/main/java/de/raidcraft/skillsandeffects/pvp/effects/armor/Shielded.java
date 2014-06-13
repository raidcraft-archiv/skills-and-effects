package de.raidcraft.skillsandeffects.pvp.effects.armor;

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
import de.raidcraft.skills.effects.Slow;
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
        types = {EffectType.HELPFUL, EffectType.ABSORBING, EffectType.REDUCING, EffectType.REFLECTING},
        configUsage = {
            "reflect[bool]: should the blocked damage reflect?",
            "reduction-in-percent[bool]",
            "reduction[baseSection]: how much damage is reduced",
            "max-abosorption[baseSection]: when to remove the effect"
        }
)
public class Shielded extends AbstractEffect<Skill> implements Triggered {

    private double damageReduction = 0.0;
    private double blockedDamage = 0;
    private int maxAbsorption;
    private int absorbed = 0;
    private boolean reflect = false;
    private boolean reductionInPercent = true;
    private boolean slow = false;
    private Callback<DamageTrigger> callback;

    public Shielded(Skill source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        reflect = data.getBoolean("reflect", false);
        reductionInPercent = data.getBoolean("reduction-in-percent", true);
        damageReduction = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("reduction"));
        maxAbsorption = (int) ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("max-absorption"));
    }

    public void addCallback(Callback<DamageTrigger> callback) {

        this.callback = callback;
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        double oldDamage = trigger.getAttack().getDamage();
        double newDamage;
        if (isReductionInPercent()) {
            newDamage = (int) (oldDamage - oldDamage * damageReduction);
        } else {
            newDamage = (int) (oldDamage - damageReduction);
        }
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
            getSource().getHolder().sendMessage(ChatColor.RED + e.getMessage());
        }
        // if the attack was cancelled that means the callback dont want the damage blocked
        // but the attack needs to be uncancelled for the damage to go thru
        if (trigger.getAttack().isCancelled()) {
            trigger.getAttack().setCancelled(false);
            return;
        }

        // respect the max damage that can be absorbed
        if (maxAbsorption > 0 && absorbed + getBlockedDamage() > maxAbsorption) {
            setBlockedDamage(maxAbsorption);
        }
        // lets reset the blocked damage maybe the callback changed it
        newDamage = oldDamage - getBlockedDamage();

        try {
            if (reflect && trigger.getAttack().getSource() instanceof CharacterTemplate) {
                new EffectDamage(this, getBlockedDamage()).run();
            }
        } catch (CombatException e) {
            getSource().getHolder().sendMessage("Schaden konnte nicht reflektiert werden: " + e.getMessage());
        }

        trigger.getAttack().setDamage(newDamage);
        getSource().getHolder().combatLog(this, getBlockedDamage() + " Schaden absorbiert.");
        absorbed += getBlockedDamage();

        // remove the effect when the limit is reached
        if (maxAbsorption > 0 && absorbed >= maxAbsorption) {
            remove();
        }
    }

    public double getBlockedDamage() {

        return blockedDamage;
    }

    public void setBlockedDamage(int blockedDamage) {

        this.blockedDamage = blockedDamage;
    }

    public void setDamageReduction(double damageReduction) {

        this.damageReduction = damageReduction;
    }

    public boolean isReductionInPercent() {

        return reductionInPercent;
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        renew(target);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        if (slow) {
            target.removeEffect(Slow.class, getSource());
        }
        absorbed = 0;
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        if (slow) {
            target.addEffect(getSource(), getSource(), Slow.class);
        }
        absorbed = 0;
    }
}