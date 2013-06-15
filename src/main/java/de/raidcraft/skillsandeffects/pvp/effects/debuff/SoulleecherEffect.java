package de.raidcraft.skillsandeffects.pvp.effects.debuff;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.callback.Callback;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.EntityDeathTrigger;
import de.raidcraft.skillsandeffects.pvp.skills.magical.Soulleecher;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Soul Leecher",
        description = "Can call a deathcallback on entity death",
        types = {EffectType.MAGICAL, EffectType.DEBUFF, EffectType.HARMFUL, EffectType.DAMAGING, EffectType.PURGEABLE},
        elements = {EffectElement.DARK}
)
public class SoulleecherEffect extends ExpirableEffect<Soulleecher> implements Triggered {

    private Callback<CharacterTemplate> deathCallback;
    private PotionEffect witherEffect;

    public SoulleecherEffect(Soulleecher source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        witherEffect = new PotionEffect(PotionEffectType.WITHER, (int) getDuration(), 1, true);
    }

    public void setDeathCallback(Callback<CharacterTemplate> deathCallback) {

        this.deathCallback = deathCallback;
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onDeath(EntityDeathTrigger trigger) throws CombatException {

        if (deathCallback != null) {
            deathCallback.run(trigger.getEvent().getCharacter());
        }
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        renew(target);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        getTarget().getEntity().removePotionEffect(PotionEffectType.WITHER);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        getTarget().getEntity().addPotionEffect(witherEffect);
    }
}
