package de.raidcraft.skillsandeffects.skills.resources;

import de.raidcraft.skills.api.events.RCCombatEvent;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.hero.ResourceType;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.CombatTrigger;
import de.raidcraft.skillsandeffects.effects.resources.RageEffect;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Rage",
        desc = "Regeneriert Wut wenn im Kampf."
)
public class Rage extends AbstractSkill implements Triggered {

    public Rage(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @TriggerHandler
    public void onCombat(CombatTrigger trigger) throws CombatException {

        if (getHero().getResourceBar().getType() != ResourceType.RAGE) {
            return;
        }

        if (trigger.getEvent().getType() == RCCombatEvent.Type.ENTER) {
            addEffect(getHero(), RageEffect.class);
        } else {
            getHero().removeEffect(RageEffect.class);
        }
    }
}
