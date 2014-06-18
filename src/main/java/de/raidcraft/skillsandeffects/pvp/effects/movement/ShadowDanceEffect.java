package de.raidcraft.skillsandeffects.pvp.effects.movement;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.PeriodicExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.PlayerCastSkillTrigger;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.generic.DamageBuff;
import de.raidcraft.skillsandeffects.pvp.skills.movement.ShadowDance;
import de.raidcraft.skillsandeffects.pvp.skills.movement.ShadowStep;
import de.raidcraft.util.MathUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Shadow Dance",
        description = "Springt von Ziel zu ziel und fügt Schaden zu.",
        types = {EffectType.HELPFUL, EffectType.BUFF, EffectType.MOVEMENT}
)
public class ShadowDanceEffect extends PeriodicExpirableEffect<ShadowDance> implements Triggered {

    private String resource;
    private boolean bonusDamage = false;

    public ShadowDanceEffect(ShadowDance source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.LOWEST)
    public void onSkillCast(PlayerCastSkillTrigger trigger) {

        if (resource == null) {
            return;
        }
        trigger.getAction().setResourceCost(resource, 0);
    }

    @Override
    protected void tick(CharacterTemplate target) throws CombatException {

        apply(target);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        List<CharacterTemplate> targets = target.getNearbyTargets(getSource().getTotalRange(), false);
        if (targets.size() < 1) {
            return;
        }
        shadowStep(targets);

        if (bonusDamage) {
            target.addEffect((Skill) getSource(), DamageBuff.class);
        }
    }

    @Override
    public void load(ConfigurationSection data) {

        resource = data.getString("resource");
        bonusDamage = data.getBoolean("bonus-damage", false);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        apply(target);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        if (bonusDamage) {
            target.removeEffect(DamageBuff.class, getSource());
        }
    }

    private void shadowStep(List<CharacterTemplate> targets) throws CombatException {

        ShadowStep.shadowStep(getTarget(), targets.get(MathUtil.RANDOM.nextInt(targets.size())));
    }
}
