package de.raidcraft.skillsandeffects.pvp.skills.debuff;

import com.sk89q.minecraft.util.commands.CommandContext;
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
import de.raidcraft.skillsandeffects.pvp.effects.debuff.CurseEffect;
import de.raidcraft.util.EnumUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Curse",
        description = "Verflucht alle Gegner im Umkreis.",
        types = {EffectType.MAGICAL, EffectType.DEBUFF, EffectType.HARMFUL, EffectType.AREA}
)
public class Curse extends AbstractSkill implements CommandTriggered {

    public enum Type {

        WEAKNESS,
        BLINDNESS,
        CASTTIME,
        MAGIC_DAMAGE;

        public static Type fromString(String str) {

            return EnumUtils.getEnumFromString(Curse.Type.class, str);
        }
    }

    private Type type;
    private ConfigurationSection weakness;
    private ConfigurationSection castTime;
    private ConfigurationSection magicDamage;
    private boolean singleTarget = false;

    public Curse(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        type = Type.fromString(data.getString("type"));
        singleTarget = data.getBoolean("single-target", false);
        this.weakness = data.getConfigurationSection("damage-reduction");
        this.castTime = data.getConfigurationSection("cast-time");
        this.magicDamage = data.getConfigurationSection("magic-damage");
    }

    public Type getType() {

        return type;
    }

    public double getWeakness() {

        return ConfigUtil.getTotalValue(this, weakness);
    }

    public double getCastTime() {

        return ConfigUtil.getTotalValue(this, castTime);
    }

    public double getMagicDamage() {

        return ConfigUtil.getTotalValue(this, magicDamage);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (type == null) {
            throw new CombatException("Unknown curse type defined! Please check your config...");
        }

        if (!singleTarget) {
            List<CharacterTemplate> targets = getSafeNearbyTargets(false);
            for (CharacterTemplate target : targets) {
                addEffect(target, CurseEffect.class);
            }
        } else {
            addEffect(getTarget(), CurseEffect.class);
        }
    }
}
