package de.raidcraft.skillsandeffects.pvp.skills.buffs;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.Effect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.generic.HasteBuff;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.generic.HealthBuff;
import de.raidcraft.util.EnumUtils;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Buff",
        description = "Verbessert einen oder mehrere Werte der Gruppe.",
        types = {EffectType.MAGICAL, EffectType.HELPFUL, EffectType.BUFF}
)
public class GenericBuff extends AbstractSkill implements CommandTriggered {

    public enum BuffType {

        HEALTH(HealthBuff.class),
        HASTE(HasteBuff.class);

        private final Class<? extends Effect<? extends Skill>> aClass;

        private BuffType(Class<? extends Effect<? extends Skill>> aClass) {

            this.aClass = aClass;
        }

        @SuppressWarnings("unchecked")
        public <E extends Effect<S>, S> Class<E> clazz() {

            return (Class<E>) aClass;
        }

        public static BuffType fromString(String name) {

            return EnumUtils.getEnumFromString(BuffType.class, name);
        }
    }

    private BuffType type;
    private boolean self;
    private boolean group;

    public GenericBuff(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        type = BuffType.fromString(data.getString("type"));
        self = data.getBoolean("self-only", false);
        group = data.getBoolean("group", false);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (type == null) {
            return;
        }

        if (group) {
            for (CharacterTemplate target : getNearbyPartyMembers()) {
                addEffect(target, type.clazz());
            }
        } else {
            CharacterTemplate target = getTarget(args, true, self);
            addEffect(target, type.clazz());
        }
    }
}
