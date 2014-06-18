package de.raidcraft.skillsandeffects.pvp.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.Effect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Remove Effects",
        description = "Removes harmfull effects from the target."
)
public class RemoveEffects extends AbstractSkill implements CommandTriggered {

    private boolean harmfull;
    private boolean helpfull;
    private boolean self;
    private ConfigurationSection amount;

    public RemoveEffects(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        harmfull = data.getBoolean("harmfull", true);
        helpfull = data.getBoolean("helpfull", false);
        self = data.getBoolean("self", false);
        amount = data.getConfigurationSection("amount");
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        CharacterTemplate target = getTarget(args, !helpfull && harmfull, self);
        int amount = getAmount();
        int removedEffects = 0;
        if (helpfull) {
            for (Effect effect : target.getEffects(EffectType.HELPFUL, EffectType.PURGEABLE)) {
                if (removedEffects >= amount) {
                    break;
                }
                target.removeEffect(effect);
                removedEffects++;
            }
        }
        if (harmfull) {
            for (Effect effect : target.getEffects(EffectType.HARMFUL, EffectType.PURGEABLE)) {
                if (removedEffects >= amount) {
                    break;
                }
                target.removeEffect(effect);
                removedEffects++;
            }
        }
    }

    public int getAmount() {

        return (int) ConfigUtil.getTotalValue(this, amount);
    }
}
