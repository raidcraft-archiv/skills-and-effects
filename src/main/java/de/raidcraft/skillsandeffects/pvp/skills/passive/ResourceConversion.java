package de.raidcraft.skillsandeffects.pvp.skills.passive;

import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.resource.Resource;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.ResourceChangeTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Resource Conversion",
        description = "Beim Verlust von einer Resource wird eine andere erneuert.",
        types = {EffectType.BUFF, EffectType.HELPFUL}
)
public class ResourceConversion extends AbstractSkill implements Triggered {

    private String resourceToListen;
    private String resourceToRestore;
    private ConfigurationSection chancePerResource;
    private ConfigurationSection amount;

    public ResourceConversion(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        resourceToListen = data.getString("using-resource");
        resourceToRestore = data.getString("gaining-resource");
        chancePerResource = data.getConfigurationSection("chance-per-resource");
        amount = data.getConfigurationSection("amount");
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onResourceChange(ResourceChangeTrigger trigger) {

        Resource resourceToListen = getHolder().getResource(this.resourceToListen);
        Resource resourceToRestore = getHolder().getResource(this.resourceToRestore);
        if (trigger.getAction() != ResourceChangeTrigger.Action.LOSS
                || !trigger.getResource().equals(resourceToListen)) {
            return;
        }
        double change = resourceToListen.getCurrent() - trigger.getNewValue();
        double chance = getChance() * change;
        if (Math.random() < chance) {
            resourceToRestore.setCurrent(resourceToRestore.getCurrent() + getAmount());
        }
    }

    public double getChance() {

        return ConfigUtil.getTotalValue(this, chancePerResource);
    }

    public int getAmount() {

        return (int) ConfigUtil.getTotalValue(this, amount);
    }
}
