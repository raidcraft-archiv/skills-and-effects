package de.raidcraft.skillsandeffects.pvp.skills.passive;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.HealAction;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.resource.HealthResource;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.trigger.ResourceChangeTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Holy Blood",
        description = "Heilt die Gruppe bei eingehendem Schaden.",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.HEALING}
)
public class HolyBlood extends AbstractSkill implements Triggered {

    private ConfigurationSection healAmount;
    private boolean resourceDamage = true;

    public HolyBlood(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        healAmount = data.getConfigurationSection("heal-amount");
        resourceDamage = data.getBoolean("resource-damage", true);
    }

    public double getHealAmount() {

        return ConfigUtil.getTotalValue(this, healAmount);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        healParty((int) (trigger.getAttack().getDamage() * getHealAmount()));
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onResourceChange(ResourceChangeTrigger trigger) throws CombatException {

        if (!resourceDamage || trigger.getAction() != ResourceChangeTrigger.Action.LOSS) {
            return;
        }
        if (trigger.getResource() instanceof HealthResource) {
            healParty((int) (trigger.getNewValue() * getHealAmount()));
        }
    }

    private void healParty(int amount) throws CombatException {

        for (CharacterTemplate target : getSafeNearbyTargets(true)) {
            if (!target.equals(getHolder())) {
                new HealAction<>(this, target, amount).run();
            }
        }
    }
}
