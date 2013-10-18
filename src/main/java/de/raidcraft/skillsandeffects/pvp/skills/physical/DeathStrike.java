package de.raidcraft.skillsandeffects.pvp.skills.physical;

import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.Attack;
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
import de.raidcraft.skills.trigger.EntityDeathTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.effects.misc.DeathStrikeEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Death Strike",
        description = "Heilt den Benutzter um einen Teil der Leben. Kann nur nach einem Todestoß genutzt werden.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.HEALING},
        queuedAttack = true
)
public class DeathStrike extends AbstractSkill implements Triggered {

    private ConfigurationSection healAmount;

    public DeathStrike(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        healAmount = data.getConfigurationSection("heal-amount");
    }

    public int getHealAmount() {

        return (int) ConfigUtil.getTotalValue(this, healAmount);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR, filterTargets = false)
    public void onEntityDeath(EntityDeathTrigger trigger) {

        Attack attack = trigger.getEvent().getCharacter().getLastDamageCause();
        if (attack != null && attack.isAttacker(getHolder()) && canUseAbility()) {
            try {
                addEffect(getHolder(), DeathStrikeEffect.class);
            } catch (CombatException ignored) {
                // ignored
            }
        }
    }
}
