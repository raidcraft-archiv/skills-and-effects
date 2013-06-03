package de.raidcraft.skillsandeffects.pvp.skills.protection;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
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
import de.raidcraft.skillsandeffects.pvp.effects.buffs.protection.PainSupressionEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Pain Supression",
        description = "Verringert den erlittenen Schaden des Ziels enorm.",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.ABSORBING, EffectType.HELPFUL, EffectType.PROTECTION}
)
public class PainSupression extends AbstractSkill implements CommandTriggered {

    private ConfigurationSection reduction;

    public PainSupression(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        reduction = data.getConfigurationSection("reduction");
    }

    public double getDamageReduction() {

        return ConfigUtil.getTotalValue(this, reduction);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        CharacterTemplate target = getTarget(args, true, false);
        addEffect(target, PainSupressionEffect.class);
    }
}
