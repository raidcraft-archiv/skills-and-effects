package de.raidcraft.skillsandeffects.pvp.skills.armor;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
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
import de.raidcraft.skillsandeffects.pvp.effects.armor.BuffingArmorEffect;
import de.raidcraft.util.EnumUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Buffing Armor",
        description = "Eine Schützende Rüstung die verschiedene Effekte hat.",
        configUsage = {
                "types[list]: HEAL_INCREASE, HEALTH_INCREASE, RESOURCE_REGAIN",
                "health-increase[baseSection]",
                "heal-increase[baseSection]",
                "resource[string]: For resource regain type",
                "resource-regain[baseSection]"
        },
        effects = {BuffingArmorEffect.class}
)
public class BuffingArmor extends AbstractSkill implements CommandTriggered {

    private Set<Type> types = new HashSet<>();
    private ConfigurationSection healthIncrease;
    private ConfigurationSection healIncrease;
    private String resource;
    private ConfigurationSection resourceRegain;
    public BuffingArmor(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        for (String str : data.getStringList("types")) {
            Type type = Type.fromString(str);
            if (type == null) {
                RaidCraft.LOGGER.warning("Unknown type definded in the " + getName() + " config: " + str);
                continue;
            }
            types.add(type);
        }
        healthIncrease = data.getConfigurationSection("health-increase");
        healIncrease = data.getConfigurationSection("heal-increase");
        // resource regain
        resource = data.getString("resource");
        resourceRegain = data.getConfigurationSection("resource-regain");
    }

    public boolean hasType(Type type) {

        return types.contains(type);
    }

    public double getHealthIncrease() {

        return ConfigUtil.getTotalValue(this, healthIncrease);
    }

    public double getHealIncrease() {

        return ConfigUtil.getTotalValue(this, healIncrease);
    }

    public double getResourceRegain() {

        return ConfigUtil.getTotalValue(this, resourceRegain);
    }

    public Resource getResource() {

        return getHolder().getResource(resource);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (types.isEmpty()) {
            throw new CombatException("No armor types defined in the config! Please fix your config...");
        }
        if (types.contains(Type.RESOURCE_REGAIN) && resource == null) {
            throw new CombatException("Wrong resource defined in the config! Please fix it...");
        }
        if (hasEffect(BuffingArmorEffect.class)) {
            removeEffect(BuffingArmorEffect.class);
        }
        addEffect(BuffingArmorEffect.class);
    }

    public enum Type {

        HEAL_INCREASE,
        HEALTH_INCREASE,
        RESOURCE_REGAIN;

        public static Type fromString(String str) {

            return EnumUtils.getEnumFromString(BuffingArmor.Type.class, str);
        }

    }
}