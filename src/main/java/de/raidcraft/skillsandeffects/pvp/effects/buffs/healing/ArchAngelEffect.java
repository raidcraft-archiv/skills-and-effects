package de.raidcraft.skillsandeffects.pvp.effects.buffs.healing;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.HealAction;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.trigger.HealTrigger;
import de.raidcraft.skillsandeffects.pvp.skills.protection.ArchAngel;
import de.raidcraft.util.MathUtil;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Arch Angel",
        description = "Schütz das Ziel vor dem sicheren Tod.",
        types = {EffectType.BUFF, EffectType.HEALING, EffectType.PROTECTION, EffectType.MAGICAL, EffectType.HELPFUL},
        elements = {EffectElement.HOLY}
)
public class ArchAngelEffect extends ExpirableEffect<ArchAngel> implements Triggered {

    private boolean died = false;

    public ArchAngelEffect(ArchAngel source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.HIGHEST)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        CharacterTemplate target = trigger.getAttack().getTarget();
        if (target.getHealth() - trigger.getAttack().getDamage() > 0) {
            return;
        }
        died = true;
        // cancel the attack and save the target
        remove();
        new HealAction<>(this, target, (int) (getSource().getDeathHealAmount() * target.getMaxHealth())).run();
        info("Du wurdest von " + getFriendlyName() + " vor dem Tod bewahrt!");
        throw new CombatException("Der tödliche Schaden wurde von " + getFriendlyName() + " verhindert.");
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onHeal(HealTrigger trigger) {

        int oldAmount = trigger.getAmount();
        double bonusHealAmount = getSource().getBonusHealAmount();
        int newAmount = (int) (oldAmount + oldAmount * bonusHealAmount);
        combatLog("Erhaltene Heilung um " + (newAmount - oldAmount) + "(" + MathUtil.toPercent(bonusHealAmount) + "%) erhöht.");
        trigger.setAmount(newAmount);

        // also give back some mana
        if (trigger.getSource().equals(getSource().getHolder())) {
            getSource().giveResourceBonus(trigger.getAction());
        }
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {


    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        if (!died) {
            // reduce the cooldown of the archangel spell to the defined amount
            getSource().setRemainingCooldown(getSource().getCooldownReduction());
        }
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {


    }
}
