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
import de.raidcraft.skills.trigger.RegainHealthTrigger;
import de.raidcraft.skillsandeffects.pvp.skills.healing.Consume;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

    private final PotionEffect regainEffect;
    private Consume.Consumeable consumeable;
    private int resourceGain;
    private boolean breakCombat = false;

    public ConsumeEffect(Consume source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        regainEffect = new PotionEffect(PotionEffectType.HEAL, (int) (getDuration() + getDelay()), 1, true);
    }

    @Override
    public void load(ConfigurationSection data) {

        breakCombat = data.getBoolean("break-in-combat", false);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onCombat(CombatTrigger trigger) throws CombatException {

        if (breakCombat && trigger.getEvent().getType() == RCCombatEvent.Type.ENTER) {
            remove();
        }
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.LOWEST)
    public void onHealthGain(RegainHealthTrigger trigger) {

        if (trigger.getEvent().getRegainReason() == EntityRegainHealthEvent.RegainReason.MAGIC_REGEN) {
            trigger.getEvent().setCancelled(true);
            trigger.setCancelled(true);
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
        target.getEntity().removePotionEffect(PotionEffectType.HEAL);
        info((consumeable.getType() == Consume.ConsumeableType.HEALTH ? "Lebens" : consumeable.getResource().getFriendlyName())
                + " Regeneration beendet.");
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        if (breakCombat && target.isInCombat()) {
            remove();
        }
        if (consumeable.getType() == Consume.ConsumeableType.HEALTH) {
            if (consumeable.isPercentage()) {
                this.resourceGain = (int) ((target.getMaxHealth() * consumeable.getResourceGain()) / getTickCount());
            } else {
                this.resourceGain = (int) (consumeable.getResourceGain() / getTickCount());
            }
        } else if (consumeable.getType() == Consume.ConsumeableType.RESOURCE) {
            if (consumeable.isPercentage()) {
                this.resourceGain = (int) ((consumeable.getResource().getMax() * consumeable.getResourceGain()) / getTickCount());
            } else {
                this.resourceGain = (int) (consumeable.getResourceGain() / getTickCount());
            }
        }
        target.getEntity().addPotionEffect(regainEffect);
        info("Du regenerierst nun langsam " +
                (consumeable.getType() == Consume.ConsumeableType.HEALTH ? "Leben" : consumeable.getResource().getFriendlyName()) + "."
        );
    }
}
