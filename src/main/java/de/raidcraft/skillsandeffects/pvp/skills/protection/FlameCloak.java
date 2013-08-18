package de.raidcraft.skillsandeffects.pvp.skills.protection;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.resource.Resource;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.effects.damaging.FlameCloakEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Flame Cloak",
        description = "Verursacht an Gegnern in Reichweite Schaden.",
        types = {EffectType.DAMAGING, EffectType.BUFF, EffectType.MAGICAL},
        elements = {EffectElement.FIRE}
)
public class FlameCloak extends AbstractSkill implements CommandTriggered {

    private String resource;
    private ConfigurationSection resourceBurnTick;

    public FlameCloak(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        this.resource = data.getString("resource");
        this.resourceBurnTick = data.getConfigurationSection("resource-cost");
    }

    public void substractResourceTick() throws CombatException {

        Resource resource = getHolder().getResource(this.resource);
        if (resource == null) {
            return;
        }
        int current = (int) (resource.getCurrent() - ConfigUtil.getTotalValue(this, resourceBurnTick));
        if (current < 0) {
            getHolder().removeEffect(FlameCloakEffect.class);
            return;
        }
        resource.setCurrent(current);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (resource == null) {
            throw new CombatException("Unknown resource defined! Please report this as a bug...");
        }
        if (getHolder().hasEffect(FlameCloakEffect.class)) {
            // remove
            getHolder().removeEffect(FlameCloakEffect.class);
        } else {
            addEffect(getHolder(), FlameCloakEffect.class);
        }
    }
}
