package de.raidcraft.skillsandeffects.skills.bow;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.effects.misc.AimedShotEffect;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Aimed Shot",
        desc = "Verursacht extra Schaden wenn der Bogen ganz gespannt wurde."
)
public class AimedShot extends AbstractLevelableSkill implements CommandTriggered {

    public AimedShot(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(getHero(), AimedShotEffect.class);
    }
}