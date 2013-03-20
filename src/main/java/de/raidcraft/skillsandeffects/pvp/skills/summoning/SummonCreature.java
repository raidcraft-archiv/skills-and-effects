package de.raidcraft.skillsandeffects.pvp.skills.summoning;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.skills.CharacterManager;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.effects.misc.Summoned;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Random;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Summon Creature",
        desc = "Beschwört eine Kreatur die für den Beschwörer kämpft.",
        types = {EffectType.MAGICAL, EffectType.SILENCABLE, EffectType.SUMMON}
)
public class SummonCreature extends AbstractSkill implements CommandTriggered {

    private static final CharacterManager CHARACTER_MANAGER = RaidCraft.getComponent(SkillsPlugin.class).getCharacterManager();
    private static final Random RANDOM = new Random();

    private EntityType entityType;
    private int minDamage;
    private int maxDamage;
    private int minHealth;
    private int maxHealth;
    private ConfigurationSection amount;

    public SummonCreature(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        entityType = EntityType.fromName(data.getString("type"));
        minDamage = data.getInt("min-damage", 5);
        maxDamage = data.getInt("max-damage", 20);
        minHealth = data.getInt("min-health", 50);
        maxHealth = data.getInt("max-health", 100);
        amount = data.getConfigurationSection("amount");
    }

    public int getAmount() {

        return (int) ConfigUtil.getTotalValue(this, amount);
    }

    public int getHealth() {

        return RANDOM.nextInt(maxHealth - minHealth + 1) + minHealth;
    }

    public int getDamage() {

        return RANDOM.nextInt(maxDamage - minDamage + 1) + minDamage;
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (entityType == null) return;

        Location target = getBlockTarget();
        for (int i = 0; i < getAmount(); i++) {
            Entity entity = target.getWorld().spawnEntity(target, entityType);
            if (entity instanceof LivingEntity) {
                CharacterTemplate summoned = CHARACTER_MANAGER.getCharacter((LivingEntity) entity);
                // lets apply an effect to the summoned creature that will kill it when it ends
                addEffect(summoned, Summoned.class);
            } else {
                entity.remove();
                throw new CombatException("Bitte überprüfe die Skill Konfiguration! Du musst eine lebende Kreatur beschwören...");
            }
        }
    }
}
