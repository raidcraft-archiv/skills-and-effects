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
import de.raidcraft.skillsandeffects.pvp.effects.buffs.combatstance.AbstractCombatStance;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.combatstance.AttackStance;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.combatstance.BerserkerStance;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.combatstance.DefenseStance;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Combat Stance",
        description = "Ermöglicht den Wechsel von verschiedenen Kampfhaltungen.",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.HELPFUL, EffectType.COMBAT_STANCE}
)
public class CombatStance extends AbstractSkill implements CommandTriggered {

    private Type auraType;

    public CombatStance(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

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
            getHolder().removeEffectTypes(EffectType.COMBAT_STANCE);
        }
        addEffect(this, getHolder(), auraType.getEffectClass());
    }

    public enum Type {

        ATTACK(AttackStance.class, "attack"),
        DEFENSE(DefenseStance.class, "defense"),
        BERSERKER(BerserkerStance.class, "berserker");

        private final Class<? extends AbstractCombatStance> effectClass;

        private final String[] aliases;

        private Type(Class<? extends AbstractCombatStance> effectClass, String... aliases) {

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

        public Class<? extends AbstractCombatStance> getEffectClass() {

            return effectClass;
        }

        public String[] getAliases() {

            return aliases;
        }
    }
}