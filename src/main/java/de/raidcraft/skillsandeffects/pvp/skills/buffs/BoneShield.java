package de.raidcraft.skillsandeffects.pvp.skills.buffs;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
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
import de.raidcraft.skillsandeffects.pvp.effects.buffs.protection.BoneShieldEffect;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Bone Shield",
        description = "Schütz das Ziel mit Knochen",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.HELPFUL, EffectType.ABSORBING}
)
public class BoneShield extends AbstractSkill implements CommandTriggered {

    private boolean current;
    private String resource;
    private ConfigurationSection absorbtion;
    private ConfigurationSection maxTargets;

    private List<CharacterTemplate> affectedTargets = new ArrayList<>();

    public BoneShield(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        current = data.getBoolean("current", true);
        resource = data.getString("resource");
        absorbtion = data.getConfigurationSection("absorbtion");
        maxTargets = data.getConfigurationSection("max-targets");
    }

    public int getMaxTargets() {

        double value = ConfigUtil.getTotalValue(this, maxTargets);
        return value > 0 ? (int) value : 1;
    }

    public double getAbsorbtion() {

        Resource resource = getHolder().getResource(this.resource);
        if (resource == null) {
            return 0.0;
        }
        double value;
        if (current) {
            value = resource.getCurrent();
        } else {
            value = resource.getMax();
        }
        return value * ConfigUtil.getTotalValue(this, absorbtion);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (resource == null) {
            throw new CombatException("Wrong resource defined! Please fix your config...");
        }
        if (affectedTargets.size() >= getMaxTargets() && affectedTargets.size() > 0) {
            affectedTargets.remove(0).removeEffect(BoneShieldEffect.class);
        }
        CharacterTemplate target = getTarget(args, true, false);
        addEffect(target, BoneShieldEffect.class);
        affectedTargets.add(target);
    }
}
