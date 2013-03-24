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
import de.raidcraft.skillsandeffects.pvp.effects.potion.Invisibility;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Shadow Strike",
        description = "Verursacht extra Schaden wenn du Unsichtbar angreifst.",
        types = {EffectType.PHYSICAL, EffectType.HARMFUL, EffectType.DAMAGING},
        queuedAttack = true
)
public class ShadowStrike extends AbstractSkill implements CommandTriggered {

    private ConfigurationSection bonusDamage;

    public ShadowStrike(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        bonusDamage = data.getConfigurationSection("bonus-damage");
    }

    private int getBonusDamage() {

        return (int) ConfigUtil.getTotalValue(this, bonusDamage);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(getHero(), QueuedAttack.class).addCallback(new Callback<AttackTrigger>() {
            @Override
            public void run(AttackTrigger trigger) throws CombatException {

                if (getHero().hasEffect(Invisibility.class)) {
                    trigger.getAttack().setDamage(trigger.getAttack().getDamage() + getBonusDamage());
                }
            }
        });
    }
}
