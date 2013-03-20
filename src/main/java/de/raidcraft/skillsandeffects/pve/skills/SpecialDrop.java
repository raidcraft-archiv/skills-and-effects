package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.BlockBreakTrigger;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Special Drop",
        desc = "Ermöglicht es spezielle Items in abgebauten Blöcken zu finden."
)
public class SpecialDrop extends AbstractSkill implements Triggered {

    public SpecialDrop(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onBlockBreak(BlockBreakTrigger trigger) {


    }
}
