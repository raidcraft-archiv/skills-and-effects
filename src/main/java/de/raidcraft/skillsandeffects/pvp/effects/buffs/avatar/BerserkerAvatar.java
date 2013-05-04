package de.raidcraft.skillsandeffects.pvp.effects.buffs.avatar;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.effects.Bleed;
import de.raidcraft.skillsandeffects.pvp.effects.resources.RageEffect;
import de.raidcraft.skillsandeffects.pvp.skills.buffs.Avatar;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Berserker Avatar",
        description = "Verwandelt dich in einen Berserker mit erhöhtem Schadensoutput.",
        types = {EffectType.AVATAR, EffectType.BUFF, EffectType.HELPFUL}
)
public class BerserkerAvatar extends AbstractAvatar implements Triggered {

    private double attackIncrease = 0.25;
    private double damageIncrease = 0.10;
    private double deepWoundChance = 0.05;
    private double rageRegenIncrease = 1.0;

    private double oldRagePerAttack;
    private double oldRagePerDamage;

    public BerserkerAvatar(Avatar source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        attackIncrease = data.getDouble("attack-increase", 0.25);
        damageIncrease = data.getDouble("damage-increase", 0.10);
        deepWoundChance = data.getDouble("deep-wound-chance", 0.05);
        rageRegenIncrease = data.getDouble("rage-increase", 1.0);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        // set the rage regen
        RageEffect effect = target.getEffect(RageEffect.class);
        oldRagePerAttack = effect.getRagePerAttackDamage();
        oldRagePerDamage = effect.getRagePerDamage();
        effect.setRagePerAttackDamage(oldRagePerAttack + rageRegenIncrease * oldRagePerAttack);
        effect.setRagePerDamage(oldRagePerDamage + rageRegenIncrease * oldRagePerDamage);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        // reset the rage regen
        RageEffect effect = target.getEffect(RageEffect.class);
        effect.setRagePerAttackDamage(oldRagePerAttack);
        effect.setRagePerDamage(oldRagePerDamage);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.HIGHEST)
    public void onAttack(AttackTrigger trigger) throws CombatException {

        if (!trigger.getAttack().isOfAttackType(EffectType.PHYSICAL)) {
            return;
        }
        int oldDamage = trigger.getAttack().getDamage();
        trigger.getAttack().setDamage((int) (oldDamage + oldDamage * attackIncrease));

        if (Math.random() < deepWoundChance) {
            trigger.getAttack().getTarget().addEffect(getSource(), (Skill) getSource(), Bleed.class);
        }
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.HIGHEST)
    public void onDamage(DamageTrigger trigger) {

        if (!trigger.getAttack().isOfAttackType(EffectType.PHYSICAL)) {
            return;
        }
        int oldDamage = trigger.getAttack().getDamage();
        trigger.getAttack().setDamage((int) (oldDamage + oldDamage * damageIncrease));
    }
}
