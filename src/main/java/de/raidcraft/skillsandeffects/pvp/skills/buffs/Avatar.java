package de.raidcraft.skillsandeffects.pvp.skills.buffs;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.avatar.AbstractAvatar;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.avatar.BerserkerAvatar;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.avatar.PaladinAvatar;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Avatar",
        description = "Verwandelt dich in einen Halbgott mit übermenschlichen Fähigkeiten.",
        types = {EffectType.BUFF, EffectType.MAGICAL, EffectType.AVATAR, EffectType.HELPFUL}
)
public class Avatar extends AbstractSkill implements CommandTriggered {

    public enum Type {

        BERSERKER(BerserkerAvatar.class, "berserker"),
        PALADIN(PaladinAvatar.class, "paladin");

        private final Class<? extends AbstractAvatar> effectClass;

        private final String[] aliases;
        private Type(Class<? extends AbstractAvatar> effectClass, String... aliases) {

            this.effectClass = effectClass;
            this.aliases = aliases;
        }

        public Class<? extends AbstractAvatar> getEffectClass() {

            return effectClass;
        }

        public String[] getAliases() {

            return aliases;
        }

        public boolean isAlias(String alias) {

            for (String name : aliases) {
                if (name.equalsIgnoreCase(alias)) {
                    return true;
                }
            }
            return false;
        }

        public static Type fromAlias(String alias) {

            for (Type type : values()) {
                if (type.isAlias(alias)) {
                    return type;
                }
            }
            return null;
        }
    }

    private Type avatarType;

    public Avatar(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        avatarType = Type.fromAlias(data.getString("type"));
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (avatarType == null) return;

        if (getHolder().hasEffect(avatarType.getEffectClass())) {
            getHolder().getEffect(avatarType.getEffectClass()).renew();
        } else {
            getHolder().removeEffectTypes(EffectType.AVATAR);
            addEffect(this, getHolder(), avatarType.getEffectClass());
        }
    }
}
