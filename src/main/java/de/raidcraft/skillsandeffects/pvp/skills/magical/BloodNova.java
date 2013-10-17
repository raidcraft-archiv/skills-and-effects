package de.raidcraft.skillsandeffects.pvp.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
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
import de.raidcraft.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Blood Nova",
        description = "Verursacht Schaden basierend auf der Entfernung von der Mitte der Nova.",
        types = {EffectType.MAGICAL, EffectType.DAMAGING, EffectType.SILENCABLE},
        elements = {EffectElement.DARK}
)
public class BloodNova extends AbstractSkill implements CommandTriggered {

    private ConfigurationSection damagePerBlock;

    public BloodNova(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        damagePerBlock = data.getConfigurationSection("damage-per-block");
    }

    public double getDamagePerBlock() {

        return ConfigUtil.getTotalValue(this, damagePerBlock);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        Location center = getHolder().getEntity().getLocation();
        List<CharacterTemplate> targets = getSafeNearbyTargets(false);
        for (CharacterTemplate target : targets) {
            int damage = (int) (getTotalDamage() + LocationUtil.getBlockDistance(center, target.getEntity().getLocation()) * getDamagePerBlock());
            magicalAttack(target, damage);
        }
    }
}
