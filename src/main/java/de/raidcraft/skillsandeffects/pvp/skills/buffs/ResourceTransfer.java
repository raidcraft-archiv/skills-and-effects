package de.raidcraft.skillsandeffects.pvp.skills.buffs;

import com.sk89q.minecraft.util.commands.CommandContext;
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
import de.raidcraft.skillsandeffects.pvp.effects.buffs.generic.ResourceTransferEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Resource Transfer",
        description = "Transferiert die Kosten von einer Resource zur nächsten.",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.HELPFUL}
)
public class ResourceTransfer extends AbstractSkill implements CommandTriggered {

    private String sourceResource;
    private String destinationResource;
    private ConfigurationSection transferAmount;
    private ConfigurationSection transferRatio;

    public ResourceTransfer(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        sourceResource = data.getString("source-resource");
        destinationResource = data.getString("destination-resource");
        transferAmount = data.getConfigurationSection("transfer-amount");
        transferRatio = data.getConfigurationSection("transfer-ratio");
    }

    public Resource getSource() {

        return getHolder().getResource(sourceResource);
    }

    public Resource getDestination() {

        return getHolder().getResource(destinationResource);
    }

    public double getTransferAmount() {

        return ConfigUtil.getTotalValue(this, transferAmount);
    }

    public double getTransferRatio() {

        return ConfigUtil.getTotalValue(this, transferRatio);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (getSource() == null || getDestination() == null) {
            throw new CombatException("Skill is wrong configured! Could not find defined resources...");
        }
        if (getHolder().hasEffect(ResourceTransferEffect.class)) {
            removeEffect(ResourceTransferEffect.class);
        } else {
            addEffect(ResourceTransferEffect.class);
        }
    }
}
