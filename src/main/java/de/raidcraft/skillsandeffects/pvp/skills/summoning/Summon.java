package de.raidcraft.skillsandeffects.pvp.skills.summoning;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.util.StringUtil;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.requirement.Requirement;
import de.raidcraft.api.requirement.RequirementManager;
import de.raidcraft.skills.CharacterManager;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.creature.Creature;
import de.raidcraft.skills.requirement.SkillRequirementResolver;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skills.util.StringUtils;
import de.raidcraft.skillsandeffects.pvp.effects.misc.Summoned;
import de.raidcraft.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Summon",
        description = "Beschwört eine Kreatur die für den Beschwörer kämpft.",
        types = {EffectType.MAGICAL, EffectType.SILENCABLE, EffectType.SUMMON}
)
public class Summon extends AbstractLevelableSkill implements CommandTriggered {

    private static CharacterManager CHARACTER_MANAGER;

    private final Map<String, SummonedCreatureConfig> creatureConfigs = new HashMap<>();
    private String resource;

    public Summon(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }


    @Override
    public void load(ConfigurationSection data) {

        resource = data.getString("resource", "souls");
        ConfigurationSection creatures = data.getConfigurationSection("creatures");
        if (creatures == null) return;
        for (String key : creatures.getKeys(false)) {
            try {
                SummonedCreatureConfig config = new SummonedCreatureConfig(key, data.getConfigurationSection("creatures." + key), this);
                creatureConfigs.put(key, config);
            } catch (InvalidConfigurationException e) {
                RaidCraft.LOGGER.warning(e.getMessage());
            }
        }
    }

    @Override
    public void onLevelGain() {

        boolean match = true;
        for (SummonedCreatureConfig config : creatureConfigs.values()) {
            for (Requirement requirement : config.requirements) {
                if (!requirement.isMet()) {
                    match = false;
                    break;
                }
            }
            if (match) {
                getHolder().sendMessage(ChatColor.GREEN + "Du kannst eine neue Kreatur beschwören: " + config.getFriendlyName());
            }
        }
    }

    private SummonedCreatureConfig findMatchingCreature(String name) throws CombatException {

        name = StringUtils.formatName(name);

        List<String> foundConfigs = new ArrayList<>();
        for (SummonedCreatureConfig config : creatureConfigs.values()) {
            if (config.name.startsWith(name) || StringUtils.formatName(config.getFriendlyName()).startsWith(name)) {
                foundConfigs.add(config.name);
            }
        }

        if (foundConfigs.size() > 1) {
            throw new CombatException("Es gibt mehrere beschwörbare Kreaturen mit dem Namen " + name + ":\n" +
                    StringUtil.joinString(foundConfigs, ", ", 0));
        }
        if (foundConfigs.size() < 1) {
            throw new CombatException("Du kennst keine beschwörbaren Kreaturen mit dem Namen: " + name);
        }
        return creatureConfigs.get(foundConfigs.get(0));
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (args.argsLength() < 1) {
            throw new CombatException(
                    "Du musst mindestens eine Kreatur zum beschwören angeben: /cast " + getFriendlyName() + " <kreatur> [anzahl]\n" +
                            StringUtil.joinString(creatureConfigs.keySet(), ", ", 0));
        }

        SummonedCreatureConfig config = findMatchingCreature(args.getString(0));

        if (!config.isMeetingAllRequirements()) {
            throw new CombatException(config.getResolveReason());
        }

        int amount = args.getInteger(1, 1);
        int maxAmount = config.getAmount();
        if (amount > maxAmount) {
            amount = maxAmount;
            getHolder().sendMessage(ChatColor.RED + "Du kannst maximal " + maxAmount + " " + config.getFriendlyName() + " beschwören.");
        }

        summonCreatures(config, amount);
    }

    public List<CharacterTemplate> summonCreatures(SummonedCreatureConfig config, int amount) throws CombatException {

        if (CHARACTER_MANAGER == null) {
            CHARACTER_MANAGER = RaidCraft.getComponent(SkillsPlugin.class).getCharacterManager();
        }
        List<CharacterTemplate> summonedCreatures = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            SummonedCreature creature = CHARACTER_MANAGER.spawnCharacter(
                    config.entityType,
                    getBlockTarget().add(0, 1, 0),
                    SummonedCreature.class,
                    config
            );
            // add the summoned effect that will kill the creature when the summon ends
            addEffect(creature, Summoned.class);
            // also add some exp to the skill and display the combatlog message
            getAttachedLevel().addExp(config.expForSummon);
            getHolder().combatLog(this, config.getFriendlyName() + " mit " + creature.getMaxHealth() + " Leben " +
                    "und " + creature.getDamage() + " Schaden beschworen.");
            summonedCreatures.add(creature);
        }
        return summonedCreatures;
    }

    public static class SummonedCreatureConfig implements SkillRequirementResolver {

        private final String name;
        private final Summon skill;
        private String friendlyName;
        private EntityType entityType;
        private int expForSummon;
        private List<Requirement> requirements = new ArrayList<>();
        private ConfigurationSection amount;
        private ConfigurationSection minDamage;
        private ConfigurationSection maxDamage;
        private ConfigurationSection minHealth;
        private ConfigurationSection maxHealth;

        public SummonedCreatureConfig(String name, ConfigurationSection config, Summon skill) throws InvalidConfigurationException {

            this.name = name;
            this.skill = skill;
            load(config);
        }

        private void load(final ConfigurationSection config) throws InvalidConfigurationException {

            this.friendlyName = config.getString("name", name);

            this.entityType = EntityType.fromName(config.getString("type"));
            if (entityType == null)
                throw new InvalidConfigurationException("No Entity with the type " + config.getString("type") + " found!");

            expForSummon = config.getInt("exp", 0);

            Bukkit.getScheduler().runTaskLater(RaidCraft.getComponent(SkillsPlugin.class), new Runnable() {
                @Override
                public void run() {

                    requirements.addAll(RequirementManager.createRequirements(
                            SummonedCreatureConfig.this,
                            config.getConfigurationSection("requirements")
                    ));
                }
            }, 1L);

            amount = config.getConfigurationSection("amount");

            minDamage = config.getConfigurationSection("min-damage");
            maxDamage = config.getConfigurationSection("max-damage");
            if (maxDamage == null) maxDamage = minDamage;

            minHealth = config.getConfigurationSection("min-health");
            maxHealth = config.getConfigurationSection("max-health");
            if (maxHealth == null) maxHealth = minHealth;
        }

        public String getFriendlyName() {

            return friendlyName;
        }

        public int getAmount() {

            return (int) ConfigUtil.getTotalValue(skill, amount);
        }

        public int getDamage() {

            int minDamage = (int) ConfigUtil.getTotalValue(skill, this.minDamage);
            int maxDamage = (int) ConfigUtil.getTotalValue(skill, this.maxDamage);
            return MathUtil.RANDOM.nextInt(maxDamage - minDamage + 1) + minDamage;
        }

        public int getMaxHealth() {

            int minHealth = (int) ConfigUtil.getTotalValue(skill, this.minHealth);
            int maxHealth = (int) ConfigUtil.getTotalValue(skill, this.maxHealth);
            return MathUtil.RANDOM.nextInt(maxHealth - minHealth + 1) + minHealth;
        }

        @Override
        public Hero getObject() {

            return skill.getHolder();
        }

        @Override
        public List<Requirement> getRequirements() {

            return requirements;
        }

        @Override
        public boolean isMeetingAllRequirements() {

            for (Requirement requirement : requirements) {
                if (!requirement.isMet()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String getResolveReason() {

            for (Requirement requirement : requirements) {
                if (!requirement.isMet()) {
                    return requirement.getLongReason();
                }
            }
            return getFriendlyName() + " kann freigeschaltet werden.";
        }
    }

    public static class SummonedCreature extends Creature {

        public SummonedCreature(LivingEntity entity, SummonedCreatureConfig config) {

            super(entity);
            setMaxHealth(config.getMaxHealth());
            setHealth(getMaxHealth());
            setDamage(config.getDamage());
            getEntity().setCustomName(ChatColor.RED + "Kreatur von " + config.skill.getHolder().getName());
            getEntity().setCustomNameVisible(true);
        }
    }
}
