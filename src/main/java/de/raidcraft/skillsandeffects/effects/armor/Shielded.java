package de.raidcraft.skillsandeffects.effects.armor;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.AttackType;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.EffectDamage;
import de.raidcraft.skills.api.combat.callback.Callback;
import de.raidcraft.skills.api.effect.AbstractEffect;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.LevelableSkill;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.effects.potion.Slowness;
import de.raidcraft.skills.trigger.DamageTrigger;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Shielded",
        description = "Schützt dich vor Angriffen",
        types = {EffectType.HELPFUL, EffectType.HARMFUL, EffectType.DAMAGING}
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

        damageReduction = 0.0;
        damageReduction += data.getDouble("reduction.level-modifier") * getSource().getHero().getLevel().getLevel();
        damageReduction += data.getDouble("reduction.prof-level-modifier") * getSource().getProfession().getLevel().getLevel();

        if (getSource() instanceof LevelableSkill) {
            damageReduction += data.getDouble("reduction.skill-level-modifier") * ((LevelableSkill) getSource()).getLevel().getLevel();
        }
        // cap reduction default is 60%
        if (data.getDouble("reduction.cap", 0.60) < damageReduction) {
            damageReduction = data.getDouble("reduction.cap", 0.60);
        }
    }

    public void addCallback(Callback<DamageTrigger> callback) {

        this.callback = callback;
    }

    @TriggerHandler
    public void onDamage(DamageTrigger trigger) throws CombatException {

        int oldDamage = trigger.getAttack().getDamage();
        int newDamage = (int) (oldDamage - oldDamage * damageReduction);
        blockedDamage = oldDamage - newDamage;

        // run the callback
        if (callback != null) {
            callback.run(trigger);
        }
        // if the attack was cancelled that means the callback dont want the damage blocked
        // but the attack needs to be uncancelled for the damage to go thru
        if (trigger.getAttack().isCancelled()) {
            trigger.getAttack().setCancelled(false);
            return;
        }

        if (reflect && trigger.getAttack().getSource() instanceof CharacterTemplate) {
            new EffectDamage(this, blockedDamage, AttackType.MAGICAL).run();
        }
        trigger.getAttack().setDamage(newDamage);
        trigger.getHero().debug("reduced damage " + oldDamage + "->" + newDamage + " - skill: " + getSource());
        trigger.getHero().combatLog(blockedDamage + "dmg wurden vom Schild absorbiert.");
    }

    public int getBlockedDamage() {

        return blockedDamage;
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        if (slow) {
            target.addEffect(getSource(), getSource(), Slowness.class);
        }
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        if (slow) {
            target.removeEffect(Slowness.class);
        }
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        if (slow) {
            target.addEffect(getSource(), getSource(), Slowness.class);
        }
    }
}
