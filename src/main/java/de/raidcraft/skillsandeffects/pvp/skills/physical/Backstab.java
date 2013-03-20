package de.raidcraft.skillsandeffects.pvp.skills.physical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.callback.Callback;
import de.raidcraft.skills.api.effect.common.QueuedAttack;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.AttackTrigger;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Backstab",
        desc = "Verursacht sehr hohen Schaden wenn von hinten angegriffen wird.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.HARMFUL}
)
public class Backstab extends AbstractSkill implements CommandTriggered {

    public Backstab(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(getHero(), QueuedAttack.class).addCallback(new Callback<AttackTrigger>() {
            @Override
            public void run(AttackTrigger trigger) throws CombatException {

                if (!trigger.getSource().isBehind(trigger.getAttack().getTarget())) {
                    trigger.getAttack().setCancelled(true);
                    throw new CombatException("Du kannst mit dem Skill " + getFriendlyName() + " nur von hinten angreifen.");
                }
            }
        });
    }
}
