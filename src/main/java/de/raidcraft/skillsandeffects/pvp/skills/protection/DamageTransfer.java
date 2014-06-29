package de.raidcraft.skillsandeffects.pvp.skills.protection;

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
import de.raidcraft.skillsandeffects.pvp.effects.buffs.protection.DamageTransferEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Damage Transfer",
        description = "Transferiert genommenen Schaden vom Ziel auf den Zaubernden.",
        types = {EffectType.PROTECTION, EffectType.BUFF, EffectType.MAGICAL, EffectType.HELPFUL}
)
public class DamageTransfer extends AbstractSkill implements CommandTriggered {

    private CharacterTemplate affectedTarget = null;
    private ConfigurationSection transferedDamage;

    public DamageTransfer(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        transferedDamage = data.getConfigurationSection("transfered-damage");
    }

    public double getTransferedDamage() {

        return ConfigUtil.getTotalValue(this, transferedDamage);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (affectedTarget != null) {
            removeEffect(affectedTarget, DamageTransferEffect.class);
            getHolder().combatLog(this, "Verbindung mit " + affectedTarget.getName() + " aufgehoben.");
            affectedTarget = null;
            return;
        }
        CharacterTemplate target = getTarget(args, true, false);
        addEffect(target, DamageTransferEffect.class);
        affectedTarget = target;
    }
}
