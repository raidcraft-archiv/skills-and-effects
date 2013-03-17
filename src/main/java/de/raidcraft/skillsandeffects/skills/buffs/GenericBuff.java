package de.raidcraft.skillsandeffects.skills.buffs;

import com.sk89q.minecraft.util.commands.CommandContext;
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
import de.raidcraft.skillsandeffects.effects.buffs.generic.HealthBuff;
import de.raidcraft.util.EnumUtils;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Buff",
        desc = "Verbessert einen oder mehrere Werte der Gruppe.",
        types = {EffectType.MAGICAL, EffectType.HELPFUL, EffectType.BUFF}
)
public class GenericBuff extends AbstractSkill implements CommandTriggered {

    public enum BuffType {

        HEALTH(HealthBuff.class);

        private final Class<? extends Effect<GenericBuff>> aClass;

        private BuffType(Class<? extends Effect<GenericBuff>> aClass) {

            this.aClass = aClass;
        }

        @SuppressWarnings("unchecked")
        public <E extends Effect<GenericBuff>> Class<E> clazz() {

            return (Class<E>) aClass;
        }

        public static BuffType fromString(String name) {

            return EnumUtils.getEnumFromString(BuffType.class, name);
        }
    }

    private BuffType type;

    public GenericBuff(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        type = BuffType.fromString(data.getString("type"));
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (type == null) {
            return;
        }

        for (Hero hero : getHero().getGroup().getMembers()) {
            addEffect(hero, type.clazz());
        }
    }
}
