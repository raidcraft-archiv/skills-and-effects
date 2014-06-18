package de.raidcraft.skillsandeffects.pvp.skills.resources;

import de.raidcraft.skills.api.events.RCCombatEvent;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.resource.Resource;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.CombatTrigger;
import de.raidcraft.skillsandeffects.pvp.effects.resources.RageEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Rage",
        description = "Regeneriert Wut wenn im Kampf."
)
public class Rage extends AbstractSkill implements Triggered {

    private String resource;

    public Rage(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        resource = data.getString("resource", "rage");
    }

    public Resource getResource() {

        return getHolder().getResource(this.resource);
    }

    @TriggerHandler
    public void onCombat(CombatTrigger trigger) throws CombatException {

        if (getResource() == null) {
            throw new CombatException("Unknown resource defined in the config! Please report this as a bug...");
        }

        if (trigger.getEvent().getType() == RCCombatEvent.Type.ENTER) {
            addEffect(RageEffect.class);
        } else {
            removeEffect(RageEffect.class);
        }
    }
}
