package de.raidcraft.skillsandeffects.pvp.effects.potion;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.trigger.EntityTargetTrigger;
import de.raidcraft.skills.trigger.PlayerLoginTrigger;
import de.raidcraft.skills.trigger.PlayerQuitTrigger;
import de.raidcraft.util.EffectUtil;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Author: Philip
 * Date: 27.12.12 - 21:45
 * Description:
 */
@EffectInformation(
        name = "Invisibility",
        description = "Lässt das Ziel unsichtbar werden",
        types = {EffectType.BUFF},
        global = true
)
public class Invisibility<S> extends ExpirableEffect<S> implements Triggered {

    private final PotionEffect invisibility;
    private boolean slow = false;
    private boolean removeOnDamage = true;
    private boolean removeOnAttack = true;
    private PotionEffect slowEffect;

    public Invisibility(S source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        invisibility = new PotionEffect(PotionEffectType.INVISIBILITY, (int) getDuration(), 1);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void playerLogin(PlayerLoginTrigger trigger) {

        if (getTarget() instanceof Hero) {
            trigger.getEvent().getPlayer().hidePlayer(((Hero) getTarget()).getPlayer());
        }
    }

    @TriggerHandler(ignoreCancelled = true)
    public void playerQuit(PlayerQuitTrigger trigger) {

        if (getTarget() instanceof Hero) {
            trigger.getEvent().getPlayer().showPlayer(((Hero) getTarget()).getPlayer());
        }
    }

    @TriggerHandler(ignoreCancelled = true, filterTargets = false)
    public void onTarget(EntityTargetTrigger trigger) {

        if (trigger.getEvent().getTarget() == null || getTarget() == null) {
            return;
        }
        if (trigger.getEvent().getTarget().equals(getTarget().getEntity())) {
            trigger.getEvent().setCancelled(true);
        }
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (removeOnDamage) {
            remove();
        }
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onAttack(AttackTrigger trigger) throws CombatException {

        if (removeOnAttack) {
            remove();
        }
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        info("Du bist nun unsichtbar.");
        EffectUtil.playEffect(target.getEntity().getLocation(), Effect.SMOKE);
        renew(target);
    }

    @Override
    public void load(ConfigurationSection data) {

        slow = data.getBoolean("slow", false);
        // slows by 15% for each strength point
        slowEffect = new PotionEffect(PotionEffectType.SLOW, (int) getDuration(), data.getInt("slow-modifier", 2));
        removeOnDamage = data.getBoolean("remove-on-damage", true);
        removeOnAttack = data.getBoolean("remove-on-attack", true);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        target.getEntity().addPotionEffect(invisibility);
        if (slow) target.getEntity().addPotionEffect(slowEffect);
        if (target instanceof Hero) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.hidePlayer((Player) target.getEntity());
            }
        }
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        info("Unsichtbarkeit wurde aufgehoben.");
        target.getEntity().removePotionEffect(PotionEffectType.INVISIBILITY);
        target.getEntity().removePotionEffect(PotionEffectType.SLOW);
        if (target instanceof Hero) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.showPlayer((Player) target.getEntity());
            }
        }
    }
}
