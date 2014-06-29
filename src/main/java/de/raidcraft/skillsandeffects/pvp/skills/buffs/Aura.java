package de.raidcraft.skillsandeffects.pvp.skills.buffs;

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
import de.raidcraft.skillsandeffects.pvp.effects.buffs.aura.AbstractAura;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.aura.DamageAura;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.aura.ManaRegenAura;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.aura.ProtectionAura;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.aura.ReflectionAura;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Aura",
        description = "Schützt dich und deine Gruppe mit einer Aura.",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.HELPFUL, EffectType.AURA}
)
public class Aura extends AbstractSkill implements CommandTriggered {

    private Type auraType;

    public Aura(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        this.auraType = Type.fromAlias(data.getString("type"));
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (auraType == null) return;

        // add the effect to ourselves and our nearby party
        if (!getHolder().hasEffect(auraType.getEffectClass())) {
            getHolder().removeEffectTypes(EffectType.AURA);
        }
        addEffect(this, getHolder(), auraType.getEffectClass());

        for (CharacterTemplate member : getNearbyPartyMembers()) {
            if (!member.hasEffect(auraType.getEffectClass())) {
                member.removeEffectTypes(EffectType.AURA);
            }
            addEffect(this, getHolder(), auraType.getEffectClass());
        }
    }

    public enum Type {

        PROTECTION(ProtectionAura.class, "protection"),
        REFLECTION(ReflectionAura.class, "reflection"),
        MANA_REGAIN(ManaRegenAura.class, "mana-regain"),
        DAMAGE(DamageAura.class, "damage");

        private final Class<? extends AbstractAura> effectClass;

        private final String[] aliases;

        private Type(Class<? extends AbstractAura> effectClass, String... aliases) {

            this.effectClass = effectClass;
            this.aliases = aliases;
        }

        public static Type fromAlias(String alias) {

            for (Type type : values()) {
                if (type.isAlias(alias)) {
                    return type;
                }
            }
            return null;
        }

        public boolean isAlias(String alias) {

            for (String name : aliases) {
                if (name.equalsIgnoreCase(alias)) {
                    return true;
                }
            }
            return false;
        }

        public Class<? extends AbstractAura> getEffectClass() {

            return effectClass;
        }

        public String[] getAliases() {

            return aliases;
        }
    }
}