package de.raidcraft.skillsandeffects.pvp.skills.protection;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.HealAction;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.resource.Resource;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Arch Angel",
        description = "Beschützt das Ziel vor dem sicheren Tod.",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.HELPFUL, EffectType.HEALING},
        elements = {EffectElement.HOLY}
)
public class ArchAngel extends AbstractSkill implements CommandTriggered {

    private ConfigurationSection deathHealAmount;
    private ConfigurationSection bonusHealAmount;
    private ConfigurationSection resourceBonus;
    private ConfigurationSection cooldownReduction;
    private Resource resource;

    public ArchAngel(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        deathHealAmount = data.getConfigurationSection("death-heal-amount");
        bonusHealAmount = data.getConfigurationSection("bonus-heal-amount");
        resourceBonus = data.getConfigurationSection("resource-bonus");
        cooldownReduction = data.getConfigurationSection("cooldown-reduction");
        resource = getHolder().getResource(data.getString("resource"));
    }

    public void giveResourceBonus(HealAction<?> action) {

        if (resource == null) {
            return;
        }
        if (action.getSource() instanceof Skill) {
            double resourceCost = ((Skill) action.getSource()).getTotalResourceCost(resource.getName());
            resource.setCurrent((int) (resource.getCurrent() + resourceCost * ConfigUtil.getTotalValue(this, resourceBonus)));
        }
    }

    public double getCooldownReduction() {

        return ConfigUtil.getTotalValue(this, cooldownReduction);
    }

    public double getBonusHealAmount() {

        return ConfigUtil.getTotalValue(this, bonusHealAmount);
    }

    public double getDeathHealAmount() {

        return ConfigUtil.getTotalValue(this, deathHealAmount);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {


    }
}
