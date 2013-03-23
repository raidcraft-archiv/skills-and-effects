package de.raidcraft.skillsandeffects.pvp.skills.summoning;

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
import de.raidcraft.skillsandeffects.pvp.effects.misc.Summoned;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Sacrifice",
        description = "Opfert beschworene Kreaturen und stellt Leben wieder her.",
        types = {EffectType.MAGICAL, EffectType.SILENCABLE, EffectType.HELPFUL, EffectType.HEALING}
)
public class Sacrifice extends AbstractSkill implements CommandTriggered {

    private int maxSacrifice;
    private ConfigurationSection healthPerSacrifice;

    public Sacrifice(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        maxSacrifice = data.getInt("max-sacrifice", 5);
        healthPerSacrifice = data.getConfigurationSection("health-per-sacrifice");
    }

    private int getHealthPerSacrifice() {

        return (int) ConfigUtil.getTotalValue(this, healthPerSacrifice);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        int heal = 0;
        int healthPerSacrifice = getHealthPerSacrifice();
        int i = 0;
        for (CharacterTemplate partyMember : getHero().getParty().getMembers()) {

            if (i >= maxSacrifice) {
                break;
            }
            if (partyMember.hasEffect(Summoned.class)) {
                // this will kill the creature
                partyMember.removeEffect(Summoned.class);
                heal += healthPerSacrifice;
                i++;
            }
        }
        // issue the heal
        if (heal > 0) {
            getHero().heal(heal);
        }
    }
}
