package de.raidcraft.skillsandeffects.skills.physical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.callback.Callback;
import de.raidcraft.skills.api.effect.common.QueuedAttack;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.AttackTrigger;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
public class Execute extends AbstractLevelableSkill implements CommandTriggered {

    private double healthTreshold = 0.20;
    private double damagePerResource = 0.5;

    public Execute(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        healthTreshold = data.getDouble("health-threshhold", 0.20);
        damagePerResource = data.getDouble("damage-per-resource", 0.5);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(getHero(), QueuedAttack.class).addCallback(new Callback<AttackTrigger>() {
            @Override
            public void run(AttackTrigger trigger) throws CombatException {

                CharacterTemplate target = trigger.getAttack().getTarget();
                if (target.getHealth() / target.getMaxHealth() > healthTreshold) {
                    throw new CombatException("Dein Ziel muss unter " + healthTreshold * 100 + "% Leben haben.");
                }
                trigger.getAttack().setDamage((int) (getTotalDamage() + damagePerResource * getHero().getResource()));
            }
        });
    }
}
