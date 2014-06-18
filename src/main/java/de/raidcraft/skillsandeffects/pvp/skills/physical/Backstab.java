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
import de.raidcraft.skillsandeffects.pvp.effects.potion.Invisibility;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Backstab",
        description = "Verursacht sehr hohen Schaden wenn man unsichtbar angreift.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.HARMFUL},
        queuedAttack = true
)
public class Backstab extends AbstractSkill implements CommandTriggered {
    
    private boolean needsInvisibility = true;

    public Backstab(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }
    
    @Override
    public void load(ConfigurationSection data) {

        this.needsInvisibility = data.getBoolean("needs-invisibility", true);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(QueuedAttack.class).addCallback(new Callback<AttackTrigger>() {
            @Override
            public void run(AttackTrigger trigger) throws CombatException {

                if (needsInvisibility && !getHolder().hasEffect(Invisibility.class)) {
                    throw new CombatException("Du kannst mit dem Skill " + getFriendlyName() + " nur aus der Unsichtbarkeit heraus angreifen.");
                }
            }
        });
    }
}