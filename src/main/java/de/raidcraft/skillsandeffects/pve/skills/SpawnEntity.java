package de.raidcraft.skillsandeffects.pve.skills;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.api.action.requirement.RequirementResolver;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.TreeMap;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Spawn Entity",
        description = "Lässt zufällig beliebige Entities spawnen."
)
public class SpawnEntity extends AbstractSkill implements CommandTriggered {

    private final TreeMap<Integer, EntitySpawner> spawnChance = new TreeMap<>();

    public SpawnEntity(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        ConfigurationSection entities = data.getConfigurationSection("entities");
        if (entities == null) return;
        for (String key : entities.getKeys(false)) {
            try {
                int priority = Integer.parseInt(key);
                ConfigurationSection section = data.getConfigurationSection("entities" + key);
                EntityType type = EntityType.fromName(section.getString("entity"));
                if (type != null) {
                    int amount = section.getInt("amount", 1);
                    EntitySpawner spawner = new EntitySpawner(type, amount, section.getConfigurationSection("chance"), getHolder());
                    spawner.setRequirements(section.getConfigurationSection("requirements"));
                    spawnChance.put(priority, spawner);
                } else {
                    RaidCraft.LOGGER.warning("Entity Type " + section.getString("entity") + " unknown in " + getName() + ".yml");
                }
            } catch (NumberFormatException e) {
                RaidCraft.LOGGER.warning("Wrong entity config in " + getName() + ".yml priority needs to be a number!");
            }
        }
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        Location location = getHolder().getEntity().getLocation();
        for (EntitySpawner spawner : spawnChance.descendingMap().values()) {

            if (!spawner.isMeetingAllRequirements(getHolder().getPlayer())) {
                continue;
            }

            if (Math.random() < spawner.getChance(this)) {
                for (int i = 0; i < spawner.getAmount(); i++) {
                    location.getWorld().spawnEntity(location, spawner.type);
                }
                break;
            }
        }
    }

    public static class EntitySpawner implements RequirementResolver<Player> {

        private final Hero hero;
        private final EntityType type;
        private final int amount;
        private final ConfigurationSection chance;
        private List<Requirement<Player>> requirements;

        public EntitySpawner(EntityType type, int amount, ConfigurationSection chance, Hero hero) {

            this.hero = hero;
            this.type = type;
            this.amount = amount;
            this.chance = chance;
        }

        public EntityType getType() {

            return type;
        }

        public int getAmount() {

            return amount;
        }

        public double getChance(Skill skill) {

            return ConfigUtil.getTotalValue(skill, chance);
        }

        @Override
        public List<Requirement<Player>> getRequirements() {

            return requirements;
        }

        public void setRequirements(ConfigurationSection config) {

            this.requirements = ActionAPI.createRequirements(type.name(), config, Player.class);
        }
    }
}
