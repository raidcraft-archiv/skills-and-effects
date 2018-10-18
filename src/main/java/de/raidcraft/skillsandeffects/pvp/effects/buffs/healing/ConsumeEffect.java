package de.raidcraft.skillsandeffects.pvp.effects.buffs.healing;

import de.raidcraft.api.items.attachments.Consumeable;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.HealAction;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.PeriodicExpirableEffect;
import de.raidcraft.skills.api.events.RCCombatEvent;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.resource.Resource;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.CombatTrigger;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skillsandeffects.pvp.skills.healing.Consume;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Consume",
        description = "Regeneriert Leben durch Essen.",
        types = {EffectType.BUFF, EffectType.HEALING},
        priority = 1.0
)
public class ConsumeEffect extends PeriodicExpirableEffect<Consume> implements Triggered {

    private final PotionEffect regainEffect;
    private Consume.ConsumeableWrapper consumeable;
    private int resourceGain;
    private boolean breakCombat = false;
    private boolean breakOnDamage = true;

    public ConsumeEffect(Consume source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        regainEffect = new PotionEffect(PotionEffectType.REGENERATION, (int) (getDuration() + getDelay()), 0, true);
    }

    @Override
    public double getPriority() {

        return super.getPriority() + resourceGain / getInterval();
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onCombat(CombatTrigger trigger) throws CombatException {

        if (breakCombat && trigger.getEvent().getType() == RCCombatEvent.Type.ENTER) {
            remove();
        }
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (breakOnDamage && trigger.getAttack().getDamage() > 0) {
            remove();
        }
    }

    public void setConsumeable(Consume.ConsumeableWrapper consumeable) {

        this.consumeable = consumeable;
    }

    @Override
    protected void tick(CharacterTemplate target) throws CombatException {

        if (target.isInCombat()) {
            remove();
        }
        if (consumeable == null || !(target instanceof Hero)) {
            return;
        }
        consumeable.applyRessourceGain(resourceGain);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        renew(target);
    }

    @Override
    public void load(ConfigurationSection data) {

        breakCombat = data.getBoolean("break-in-combat", false);
        breakOnDamage = data.getBoolean("break-on-damage", true);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        if (breakCombat && target.isInCombat()) {
            remove();
        }
        switch (consumeable.getType()) {
            case HEALTH:
                if (consumeable.isPercentage()) {
                    this.resourceGain = (int) ((target.getMaxHealth() * consumeable.getResourceGain()) / getTickCount());
                } else {
                    this.resourceGain = (int) (consumeable.getResourceGain() / getTickCount());
                }
                info("Du regenerierst nun langsam Leben.");
                break;
            case RESOURCE:
                if (consumeable.isPercentage()) {
                    this.resourceGain = (int) ((consumeable.getResource().getMax() * consumeable.getResourceGain()) / getTickCount());
                } else {
                    this.resourceGain = (int) (consumeable.getResourceGain() / getTickCount());
                }
                info("Du regenerierst nun langsam " + consumeable.getResource().getFriendlyName());
                break;
            case ATTRIBUTE:
                if (consumeable.isPercentage()) {
                    this.resourceGain = (int) ((consumeable.getAttribute().getBaseValue() * consumeable.getResourceGain()) / getTickCount());
                } else {
                    this.resourceGain = (int) (consumeable.getResourceGain() / getTickCount());
                }
                info("Du regenerierst nun langsam " + consumeable.getAttribute().getFriendlyName());
                break;

        }
        target.getEntity().addPotionEffect(regainEffect);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        this.resourceGain = 0;
        target.getEntity().removePotionEffect(PotionEffectType.REGENERATION);
        info((consumeable.getType() == Consumeable.Type.HEALTH ? "Lebens" : consumeable.getResource().getFriendlyName())
                + " Regeneration beendet.");
    }
}
