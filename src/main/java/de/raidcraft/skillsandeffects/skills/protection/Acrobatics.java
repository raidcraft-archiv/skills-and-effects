package de.raidcraft.skillsandeffects.skills.protection;

import de.raidcraft.skills.api.combat.AttackSource;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skillsandeffects.skills.tools.ToolLevel;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

/**
 * @author Philip
 */
@SkillInformation(
        name = "Acrobatics",
        desc = "Minimiert Fallschaden",
        types = {EffectType.UNBINDABLE},
        triggerCombat = false
)
public class Acrobatics extends AbstractLevelableSkill implements Triggered {

    private Map<String, Object> data;

    public Acrobatics(Hero hero, SkillProperties skillData, Profession profession, THeroSkill database) {

        super(hero, skillData, profession, database);
        attachLevel(new ToolLevel(this, database));
    }

    @Override
    public void load(ConfigurationSection data) {
        this.data = data.getValues(false);
    }

    /*
     * Level increase
     */
    @TriggerHandler(checkUsage = false)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if(!trigger.getAttack().hasSource(AttackSource.ENVIRONMENT)) {
            return;
        }



    }

    @Override
    public void apply() {
    }

    @Override
    public void remove() {
    }
}
