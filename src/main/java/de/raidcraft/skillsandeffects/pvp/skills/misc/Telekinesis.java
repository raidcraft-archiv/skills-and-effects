package de.raidcraft.skillsandeffects.pvp.skills.misc;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.blocks.BlockID;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import net.minecraft.server.v1_5_R3.Block;
import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Telekinesis",
        description = "Ermöglicht es von der Ferne mit Objekten zu interagieren.",
        types = {EffectType.MAGICAL, EffectType.HELPFUL}
)
public class Telekinesis extends AbstractSkill implements CommandTriggered {

    public Telekinesis(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        org.bukkit.block.Block block = getBlockTarget().getBlock();
        int typeId = block.getTypeId();
        if (BlockID.LEVER != typeId && BlockID.STONE_BUTTON != typeId && BlockID.WOODEN_BUTTON != typeId) {
            throw new CombatException("Ziel muss ein Hebel oder Knopf sein!");
        }
        Block nmsBlock = Block.byId[typeId];
        nmsBlock.interact(((CraftWorld) block.getWorld()).getHandle(), block.getX(), block.getY(), block.getZ(), null, 0, 0F, 0F, 0F);
    }
}
