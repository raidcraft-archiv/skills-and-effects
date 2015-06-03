package de.raidcraft.skillsandeffects.pvp.skills.physical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.common.QueuedAttack;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.damage.BloodlustEffect;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Blood Strike",
        description = "Zerfetzt deinen Gegner mit all deiner Wut.",
        types = {EffectType.PHYSICAL, EffectType.HARMFUL, EffectType.DAMAGING},
        queuedAttack = true
)
public class BloodStrike extends AbstractLevelableSkill implements CommandTriggered {

    public BloodStrike(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        final BloodlustEffect effect = getEffect(BloodlustEffect.class);
        if (!hasEffect(BloodlustEffect.class)) {
            throw new CombatException("Du benötigst mindestens einen Stack " + effect.getFriendlyName());
        }
        addEffect(QueuedAttack.class).addCallback(trigger -> {

            trigger.getAttack().setDamage(getTotalDamage() * effect.getStacks());
            removeEffect(BloodlustEffect.class);
        });
    }
}
