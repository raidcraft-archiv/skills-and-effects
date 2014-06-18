package de.raidcraft.skillsandeffects.pvp.effects.buffs.generic;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.SkillAction;
import de.raidcraft.skills.api.effect.AbstractEffect;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.PlayerCastSkillTrigger;
import de.raidcraft.skillsandeffects.pvp.skills.buffs.ResourceTransfer;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Resource Transfer",
        description = "Transferiert Kosten der einen Resource zur anderen.",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.HELPFUL}
)
public class ResourceTransferEffect extends AbstractEffect<ResourceTransfer> implements Triggered {

    public ResourceTransferEffect(ResourceTransfer source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.LOWEST)
    public void onSkillCast(PlayerCastSkillTrigger trigger) {

        SkillAction action = trigger.getAction();
        double sourceCost = action.getResourceCost(getSource().getSource().getName());
        if (sourceCost == 0.0) {
            return;
        }
        // lets reduce the source resource by the transfered amount
        double transferAmount = sourceCost * getSource().getTransferAmount();
        transferAmount = ((int) (100 * transferAmount)) / 100.0;
        action.setResourceCost(getSource().getSource().getName(), sourceCost - transferAmount);

        // then lets calculate the ratio and add the new resource cost
        double newResourceCost = action.getResourceCost(getSource().getDestination().getName());
        newResourceCost += transferAmount * getSource().getTransferRatio();
        newResourceCost = ((int) (100 * newResourceCost)) / 100.0;
        combatLog(transferAmount + " " + getSource().getSource().getFriendlyName()
                + " wurde in " + newResourceCost + getSource().getDestination().getFriendlyName() + " umgewandelt.");
        action.setResourceCost(getSource().getDestination().getName(), newResourceCost);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {


    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {


    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {


    }
}