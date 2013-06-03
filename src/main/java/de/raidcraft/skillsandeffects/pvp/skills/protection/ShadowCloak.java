package de.raidcraft.skillsandeffects.pvp.skills.protection;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.protection.MagicImmunity;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Shadow Cloak",
        description = "Entfernt alle schädlichen Effekte und macht dich immun gegen Zauber.",
        types = {EffectType.BUFF, EffectType.HELPFUL, EffectType.PROTECTION}
)
public class ShadowCloak extends AbstractSkill implements CommandTriggered {

    private Set<EffectType> effectsToRemove = new HashSet<>();

    public ShadowCloak(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        for (String str : data.getStringList("effects-to-remove")) {
            EffectType type = EffectType.fromString(str);
            if (type == null) {
                RaidCraft.LOGGER.warning("Wrong removeable effect type " + str + " defined in config " + getName());
                continue;
            }
            effectsToRemove.add(type);
        }
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        // remove all effect types
        for (EffectType type : effectsToRemove) {
            getHolder().removeEffectTypes(type);
        }
        // add magic immunity
        addEffect(getHolder(), MagicImmunity.class);
    }
}
