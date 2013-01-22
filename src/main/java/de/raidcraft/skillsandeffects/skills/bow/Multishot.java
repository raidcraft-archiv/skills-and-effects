package de.raidcraft.skillsandeffects.skills.bow;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.combat.ProjectileType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.ProjectileLaunchTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.effects.misc.MultishotEffect;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Multi-Shot",
        desc = "Schiesst mehrere Pfeile auf einmal ab."
)
public class Multishot extends AbstractLevelableSkill implements CommandTriggered, Triggered {

    private ProjectileType type = ProjectileType.ARROW;
    private ConfigurationSection amount;

    public Multishot(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        this.type = ProjectileType.fromName(data.getString("type", "arrow"));
        this.amount = data.getConfigurationSection("amount");
    }

    private int getAmount() {

        return (int) ConfigUtil.getTotalValue(this, amount);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(getHero(), MultishotEffect.class);
    }

    @TriggerHandler
    public void onProjectileLaunch(ProjectileLaunchTrigger trigger) throws CombatException {

        if (ProjectileType.valueOf(trigger.getEvent().getEntity()) != type || !getHero().hasEffect(MultishotEffect.class)) {
            return;
        }
        for (int i = 1; i <= getAmount(); i++) {
            Bukkit.getScheduler().runTaskLater(RaidCraft.getComponent(SkillsPlugin.class), new Runnable() {
                @Override
                public void run() {

                    try {
                        rangedAttack(type);
                    } catch (CombatException e) {
                        RaidCraft.LOGGER.warning(e.getMessage());
                    }
                }
            }, 2 * i);
        }
        getHero().removeEffect(MultishotEffect.class);
    }

}