package de.raidcraft.skillsandeffects.pvp.skills.protection;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.protection.FadeEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Fade",
        description = "Verblasst und erhöht die Ausweichenchance.",
        types = {EffectType.BUFF, EffectType.HELPFUL, EffectType.PROTECTION}
)
public class Fade extends AbstractSkill implements CommandTriggered {

    private ConfigurationSection evadeChance;

    public Fade(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        evadeChance = data.getConfigurationSection("evade-chance");
    }

    public double getEvadeChance() {

        return ConfigUtil.getTotalValue(this, evadeChance);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(getHolder(), FadeEffect.class);
    }
}
