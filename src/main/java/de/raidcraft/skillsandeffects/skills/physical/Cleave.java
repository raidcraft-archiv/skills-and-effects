package de.raidcraft.skillsandeffects.skills.physical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
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
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Cleave",
        desc = "Fügt allen Gegenern neben dem getroffenen Ziel Schaden zu.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.HARMFUL, EffectType.AREA},
        triggerCombat = true
)
public class Cleave extends AbstractSkill implements CommandTriggered {

    private int maxTargets = 3;

    public Cleave(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        maxTargets = data.getInt("max-targets", 3);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(getHero(), QueuedAttack.class).addCallback(new Callback<AttackTrigger>() {
            @Override
            public void run(AttackTrigger trigger) throws CombatException {

                int i = 0;
                for (CharacterTemplate target : trigger.getAttack().getTarget().getNearbyTargets(getTotalRange())) {

                    if (target.equals(getHero())) {
                        continue;
                    }
                    if (!(i < maxTargets)) {
                        break;
                    }
                    attack(target, trigger.getAttack().getDamage());
                    i++;
                }
            }
        });
    }
}
