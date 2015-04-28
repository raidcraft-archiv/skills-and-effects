package de.raidcraft.skillsandeffects.pvp.skills.buffs;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.damage.BloodlustEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Rampage",
        description = "Versetzt dich in einen Wutanfall und generiert sofort die max. Anzahl an Blutrausch Stacks."
)
public class Rampage extends AbstractSkill implements CommandTriggered {

    private double healthCostPercent = 0.25;

    public Rampage(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        healthCostPercent = data.getDouble("health-cost", 0.25);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        int healthCost = (int) (getHolder().getMaxHealth() * healthCostPercent);
        if (getHolder().getHealth() - healthCost < 1) {
            throw new CombatException(CombatException.Type.LOW_HEALTH);
        }
        BloodlustEffect effect = addEffect(BloodlustEffect.class);
        effect.setStacks(effect.getMaxStacks());
        getHolder().setHealth(getHolder().getHealth() - healthCost);
    }
}
