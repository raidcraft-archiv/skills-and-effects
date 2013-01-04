package de.raidcraft.skillsandeffects.skills.physical;

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
import de.raidcraft.skills.effects.disabling.Disarmed;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.AttackTrigger;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Disarm",
        desc = "Entwaffnet den Gegner und verhindert alle physischen Angriffe.",
        types = {EffectType.PHYSICAL, EffectType.HARMFUL, EffectType.DEBUFF, EffectType.DAMAGING},
        triggerCombat = true
)
public class Disarm extends AbstractSkill implements CommandTriggered {

    public Disarm(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(getHero(), QueuedAttack.class).addCallback(new Callback<AttackTrigger>() {
            @Override
            public void run(AttackTrigger trigger) throws CombatException {

                Disarm.this.addEffect(trigger.getAttack().getTarget(), Disarmed.class);
            }
        });
    }

    @Override
    public void apply() {
        //TODO: implement
    }

    @Override
    public void remove() {
        //TODO: implement
    }
}
