package de.raidcraft.skillsandeffects.pve.skills;

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
import de.raidcraft.skillsandeffects.pve.effects.NightVisionEffect;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Night Vision",
        description = "Verleit dem Zaubernden einen Nachtsichttrank.",
        types = {EffectType.BUFF, EffectType.HELPFUL}
)
public class NightVision extends AbstractSkill implements CommandTriggered {

    public NightVision(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(NightVisionEffect.class);
    }
}
