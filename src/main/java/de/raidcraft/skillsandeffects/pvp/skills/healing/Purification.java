package de.raidcraft.skillsandeffects.pvp.skills.healing;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.callback.Callback;
import de.raidcraft.skills.api.effect.common.QueuedAttack;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skillsandeffects.pvp.effects.debuff.PurificationEffect;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Purification",
        description = "Läutert dein Ziel so dass es dich und deine Gruppe um den erlittenen Schaden heilt.",
        types = {EffectType.HEALING, EffectType.HELPFUL, EffectType.MAGICAL, EffectType.PHYSICAL},
        elements = {EffectElement.HOLY},
        queuedAttack = true
)
public class Purification extends AbstractLevelableSkill implements CommandTriggered {

    public Purification(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(QueuedAttack.class).addCallback(new Callback<AttackTrigger>() {
            @Override
            public void run(AttackTrigger trigger) throws CombatException {

                addEffect(trigger.getAttack().getTarget(), PurificationEffect.class);
            }
        });
    }
}
