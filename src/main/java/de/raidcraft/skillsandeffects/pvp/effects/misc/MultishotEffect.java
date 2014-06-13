package de.raidcraft.skillsandeffects.pvp.effects.misc;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.ProjectileType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.ProjectileLaunchTrigger;
import de.raidcraft.skillsandeffects.pvp.skills.bow.Multishot;
import org.bukkit.Bukkit;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Multi Shot",
        description = "Verschiesst mehrere Pfeile auf einmal.",
        types = {EffectType.PHYSICAL, EffectType.HARMFUL, EffectType.DAMAGING}
)
public class MultishotEffect extends ExpirableEffect<Multishot> implements Triggered {

    public MultishotEffect(Multishot source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @TriggerHandler
    public void onProjectileLaunch(ProjectileLaunchTrigger trigger) throws CombatException {


        if (ProjectileType.valueOf(trigger.getEvent().getEntity()) != getSource().getType()) {
            return;
        }
        int amount = getSource().getAmount();
        for (int i = 1; i <= amount; i++) {
            Bukkit.getScheduler().runTaskLater(RaidCraft.getComponent(SkillsPlugin.class), new Runnable() {
                @Override
                public void run() {

                    try {
                        getSource().rangedAttack(getSource().getType()).run();
                    } catch (CombatException e) {
                        warn(e.getMessage());
                    }
                }
            }, 2 * i);
        }
        remove();
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        info("Du legst mehrere Pfeile in deinen Bogen ein.");
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }
}
