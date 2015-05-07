package de.raidcraft.skillsandeffects.pvp.skills.bow;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.ProjectileType;
import de.raidcraft.skills.api.effect.common.QueuedProjectile;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.IgnoredSkill;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@IgnoredSkill
public abstract class AbstractBowTrap extends AbstractSkill implements CommandTriggered {

    protected int width = 1;
    protected int length = 1;
    protected int height = 1;

    public AbstractBowTrap(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        width = (int) ConfigUtil.getTotalValue(this, data.getConfigurationSection("width"));
        length = (int) ConfigUtil.getTotalValue(this, data.getConfigurationSection("length"));
        height = (int) ConfigUtil.getTotalValue(this, data.getConfigurationSection("height"));
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(QueuedProjectile.class).addCallback(location -> {

            if (LocationUtil.isSafeZone(getHolder().getPlayer(), location)) {
                throw new CombatException(CombatException.Type.PVP);
            }
            runTrap(location);
        }, ProjectileType.ARROW);
    }

    protected abstract void runTrap(Location target) throws CombatException;
}
