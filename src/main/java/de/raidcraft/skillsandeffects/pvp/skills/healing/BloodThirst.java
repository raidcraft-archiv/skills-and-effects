package de.raidcraft.skillsandeffects.pvp.skills.healing;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.resource.Resource;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Bloodthirst",
        description = "Heilt dich basierend auf der Menge deiner Wut.",
        types = {EffectType.HEALING, EffectType.HELPFUL, EffectType.MAGICAL}
)
public class BloodThirst extends AbstractSkill implements CommandTriggered {

    private String resourceName = "rage";
    private double healFactor = 1.0;
    private int minCost = 20;

    public BloodThirst(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        resourceName = data.getString("resource");
        healFactor = ConfigUtil.getTotalValue(this, data.getConfigurationSection("heal"));
        minCost = (int) ConfigUtil.getTotalValue(this, data.getConfigurationSection("min"));
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        Resource resource = getHero().getResource(resourceName);
        if (resource.getCurrent() >= minCost) {
            getHero().heal((int) (resource.getCurrent() * healFactor));
            resource.setCurrent(resource.getDefault());
        } else {
            throw new CombatException("Nicht genug " + resource.getFriendlyName() + ".");
        }
    }
}
