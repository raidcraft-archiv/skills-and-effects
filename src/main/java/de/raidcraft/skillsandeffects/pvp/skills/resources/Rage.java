package de.raidcraft.skillsandeffects.pvp.skills.resources;

import de.raidcraft.skills.api.events.RCCombatEvent;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.CombatTrigger;
import de.raidcraft.skillsandeffects.pvp.effects.resources.RageEffect;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Rage",
        description = "Regeneriert Wut wenn im Kampf."
)
public class Rage extends AbstractSkill implements Triggered {

    public static final String RESOURCE_NAME = "rage";

    public Rage(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @TriggerHandler
    public void onCombat(CombatTrigger trigger) throws CombatException {

        if (getHolder().getResource(RESOURCE_NAME) == null) {
            return;
        }

        if (trigger.getEvent().getType() == RCCombatEvent.Type.ENTER) {
            addEffect(getHolder(), RageEffect.class);
        } else {
            getHolder().removeEffect(RageEffect.class);
        }
    }
}
