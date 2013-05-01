package de.raidcraft.skillsandeffects.pvp.skills.bow;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.ProjectileType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.effects.misc.MultishotEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Multi Shot",
        description = "Schiesst mehrere Pfeile auf einmal ab."
)
public class Multishot extends AbstractLevelableSkill implements CommandTriggered, Triggered {

    private ProjectileType type = ProjectileType.ARROW;
    private ConfigurationSection amount;

    public Multishot(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        this.type = ProjectileType.fromName(data.getString("type", "arrow"));
        this.amount = data.getConfigurationSection("amount");
    }

    public ProjectileType getType() {

        return type;
    }

    public int getAmount() {

        return (int) ConfigUtil.getTotalValue(this, amount);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(getHolder(), MultishotEffect.class);
    }
}
