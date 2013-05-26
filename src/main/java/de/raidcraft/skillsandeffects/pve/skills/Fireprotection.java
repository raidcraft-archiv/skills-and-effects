package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.EnvironmentAttack;
import de.raidcraft.skills.api.combat.action.SkillAction;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skillsandeffects.pve.effects.protection.FireProtectionEffect;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Fire Protection",
        description = "Wirft alle 2 Sekunden ein Rüstungsteil ab und erlischt somit Flammen.",
        types = {EffectType.PROTECTION}
)
public class FireProtection extends AbstractSkill implements Triggered {

    public FireProtection(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (isOnCooldown()) {
            return;
        }
        if (getHolder().hasEffect(FireProtectionEffect.class)) {
            return;
        }
        if (getHolder().getEntity().getEquipment().getArmorContents().length < 4) {
            return;
        }
        if (trigger.getAttack() instanceof EnvironmentAttack) {
            EntityDamageEvent.DamageCause damageCause = ((EnvironmentAttack) trigger.getAttack()).getDamageCause();
            if (damageCause == EntityDamageEvent.DamageCause.FIRE_TICK) {
                getHolder().addEffect(this, FireProtectionEffect.class);
                substractUsageCost(new SkillAction(this));
            }
        }
    }
}
