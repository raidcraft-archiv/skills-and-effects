package de.raidcraft.skillsandeffects.skills.buffs;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.effects.buffs.damage.BloodlustEffect;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Wutanfall",
        desc = "Versetzt dich in einen Wutanfall und generiert sofort die max. Anzahl an Blutrausch Stacks."
)
public class Rampage extends AbstractSkill implements CommandTriggered {

    public Rampage(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (getHero().hasEffect(BloodlustEffect.class)) {
            BloodlustEffect effect = getHero().getEffect(BloodlustEffect.class);
            effect.setStacks(effect.getMaxStacks());
        } else {
            BloodlustEffect effect = addEffect(getHero(), BloodlustEffect.class);
            effect.setStacks(effect.getMaxStacks());
        }
    }
}
