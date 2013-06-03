package de.raidcraft.skillsandeffects.pvp.skills.bow;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.ProjectileType;
import de.raidcraft.skills.api.combat.action.RangedAttack;
import de.raidcraft.skills.api.combat.callback.BowFireCallback;
import de.raidcraft.skills.api.combat.callback.ProjectileCallback;
import de.raidcraft.skills.api.effect.common.QueuedBowFire;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.BowFireTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.MathUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Volley",
        description = "Schießt einen Pfeilreigen ab der alles im Umkreis mit Pfeilen eindeckt.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING}
)
public class Volley extends AbstractLevelableSkill implements CommandTriggered {

    private ConfigurationSection amount;

    public Volley(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        amount = data.getConfigurationSection("amount");
    }

    public int getAmount() {

        return (int) ConfigUtil.getTotalValue(this, amount);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(getHolder(), QueuedBowFire.class).addCallback(new BowFireCallback() {
            @Override
            public void run(BowFireTrigger trigger) throws CombatException {

                Vector velocity = trigger.getEvent().getProjectile().getVelocity();
                for (int i = 0; i < getAmount(); i++) {
                    RangedAttack<ProjectileCallback> attack = rangedAttack(ProjectileType.ARROW, getTotalDamage());
                    attack.setVelocity(velocity.clone().add(
                            new Vector(MathUtil.RANDOM.nextInt(i), MathUtil.RANDOM.nextInt(i), MathUtil.RANDOM.nextInt(i))
                    ));
                    attack.run();
                }
            }
        });
    }
}
