package de.raidcraft.skillsandeffects.pvp.effects.buffs.protection;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.skills.protection.ShieldWall;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Shieldwall",
        description = "Verhindert eingehenden Schaden."
)
public class ShieldWallEffect extends ExpirableEffect<ShieldWall> implements Triggered {

    private double damageReduction = 0.60;

    public ShieldWallEffect(ShieldWall source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onDamage(DamageTrigger trigger) {

        double oldDamage = trigger.getAttack().getDamage();
        double newDamage = oldDamage - oldDamage * damageReduction;
        trigger.getAttack().setDamage(newDamage);
        trigger.getAttack().combatLog(this, damageReduction * 100 + "% (" + (oldDamage - newDamage) + ") Schaden verhindert.");
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        info(getFriendlyName() + " aktiv!");
    }

    @Override
    public void load(ConfigurationSection data) {

        damageReduction = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("damage-reduction"));
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        info(getFriendlyName() + " vorbei.");
    }
}
