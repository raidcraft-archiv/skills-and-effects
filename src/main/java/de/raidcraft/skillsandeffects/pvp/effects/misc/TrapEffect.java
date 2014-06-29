package de.raidcraft.skillsandeffects.pvp.effects.misc;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Trap",
        description = "Entfernt die Falle nach Ablauf.",
        priority = -1.0
)
public class TrapEffect extends ExpirableEffect<Skill> {

    private Material materialBefore = Material.AIR;
    private Set<Block> changedBlocks = new HashSet<>();

    public TrapEffect(Skill source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        setPriority(-1.0);
    }

    public void setChangedBlocks(Set<Block> changedBlocks) {

        this.changedBlocks = changedBlocks;
    }

    public void setMaterialBefore(Material material) {

        this.materialBefore = material;
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        for (Block block : changedBlocks) {
            block.setType(materialBefore);
        }
    }
}
