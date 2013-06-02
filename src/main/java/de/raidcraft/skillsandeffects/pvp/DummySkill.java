package de.raidcraft.skillsandeffects.pvp;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Dummy Skill",
        description = "Allows to substract and add resources."
)
public class DummySkill extends AbstractSkill implements CommandTriggered {

    public DummySkill(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {


    }
}
