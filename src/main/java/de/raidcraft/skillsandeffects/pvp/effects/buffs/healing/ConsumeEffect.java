package de.raidcraft.skillsandeffects.pvp.effects.buffs.healing;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.PeriodicExpirableEffect;
import de.raidcraft.skills.api.events.RCCombatEvent;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.resource.Resource;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.CombatTrigger;
import de.raidcraft.skillsandeffects.pvp.skills.healing.Consume;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Consume",
        description = "Regeneriert Leben durch Essen.",
        types = {EffectType.BUFF, EffectType.HEALING},
        priority = -1.0
)
public class ConsumeEffect extends PeriodicExpirableEffect<Consume> implements Triggered {

    private Consume.Consumeable consumeable;
    private int resourceGain;

    public ConsumeEffect(Consume source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onCombat(CombatTrigger trigger) throws CombatException {

        if (trigger.getEvent().getType() == RCCombatEvent.Type.ENTER) {
            remove();
        }
    }

    public void setConsumeable(Consume.Consumeable consumeable) {

        this.consumeable = consumeable;
    }

    @Override
    protected void tick(CharacterTemplate target) throws CombatException {

        if (target.isInCombat()) {
            remove();
        }
        if (consumeable == null) {
            return;
        }
        if (consumeable.getType() == Consume.ConsumeableType.HEALTH) {
            target.heal(resourceGain);
        } else if (consumeable.getType() == Consume.ConsumeableType.RESOURCE) {
            Resource resource = consumeable.getResource();
            resource.setCurrent(resource.getCurrent() + resourceGain);
        }
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        renew(target);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        this.resourceGain = 0;
        info(consumeable.getType() == Consume.ConsumeableType.HEALTH ? "Lebens" : consumeable.getResource().getFriendlyName()
        + " Regeneration beendet.");
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        if (target.isInCombat()) {
            remove();
        }
        this.resourceGain = (int) consumeable.getResourceGain() / getTickCount();
        info("Du regenerierst nun langsam " +
                (consumeable.getType() == Consume.ConsumeableType.HEALTH ? "Leben" : consumeable.getResource().getFriendlyName()) + "."
        );
    }
}
