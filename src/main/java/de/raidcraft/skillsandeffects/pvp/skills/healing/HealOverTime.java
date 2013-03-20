package de.raidcraft.skillsandeffects.pvp.skills.healing;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.player.UnknownPlayerException;
import de.raidcraft.skills.SkillsPlugin;
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
import de.raidcraft.skillsandeffects.pvp.effects.buffs.healing.HealOverTimeEffect;
import de.raidcraft.util.LocationUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Heal over Time",
        desc = "Heilt das Ziel über eine bestimmte Zeit.",
        types = {EffectType.HEALING, EffectType.HELPFUL, EffectType.MAGICAL, EffectType.SILENCABLE},
        elements = {EffectElement.LIGHT}
)
public class HealOverTime extends AbstractSkill implements CommandTriggered {

    private boolean selfHeal = false;

    public HealOverTime(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        selfHeal = data.getBoolean("self-heal", false);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        CharacterTemplate target;
        if (args.argsLength() > 0) {
            try {
                target = RaidCraft.getComponent(SkillsPlugin.class).getCharacterManager().getHero(args.getString(0));
                if (!LocationUtil.isWithinRadius(getHero().getPlayer().getLocation(), target.getEntity().getLocation(), getTotalRange())) {
                    throw new CombatException(CombatException.Type.OUT_OF_RANGE);
                }
            } catch (UnknownPlayerException e) {
                throw new CombatException(e.getMessage());
            }
        } else if (selfHeal || getHero().getPlayer().isSneaking()) {
            // self holy
            target = getHero();
        } else {
            target = getTarget();
        }
        if (!target.isFriendly(getHero())) {
            throw new CombatException("Ziel kann nicht geheilt werden da es nicht in deiner Gruppe ist.");
        }
        // lets add the actual heal effect
        addEffect(target, HealOverTimeEffect.class);
    }
}
