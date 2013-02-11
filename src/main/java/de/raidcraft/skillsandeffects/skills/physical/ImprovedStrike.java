package de.raidcraft.skillsandeffects.skills.physical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Improved Strike",
        desc = "Verursacht mehr Schaden beim nächsten normalen Angriff.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.HARMFUL}
)
public class ImprovedStrike extends AbstractSkill implements CommandTriggered, Triggered {

    private boolean triggered = false;
    private double damageIncrease = 1.0;

    public ImprovedStrike(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        this.damageIncrease = ConfigUtil.getTotalValue(this, data.getConfigurationSection("damage"));
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        triggered = true;
        getHero().sendMessage("" + ChatColor.GRAY + ChatColor.ITALIC + "Der nächste normale Angriff verursacht mehr Schaden!");
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.HIGHEST)
    public void onAttack(AttackTrigger trigger) {

        if (triggered && trigger.getAttack().isOfAttackType(EffectType.DEFAULT_ATTACK)) {
            triggered = false;
            trigger.getAttack().setDamage((int) (trigger.getAttack().getDamage() * damageIncrease));
        }
    }
}
