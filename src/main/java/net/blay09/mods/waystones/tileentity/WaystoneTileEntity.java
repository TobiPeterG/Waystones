package net.blay09.mods.waystones.tileentity;

import net.blay09.mods.waystones.core.IWaystone;
import net.blay09.mods.waystones.worldgen.NameGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class WaystoneTileEntity extends TileEntity implements IWaystone {

    private boolean isDummy;
    private UUID waystoneUid = UUID.randomUUID();
    private String waystoneName = "";
    private UUID owner;
    private boolean isGlobal;
    private boolean wasGenerated = true;
    private boolean isMossy;

    public WaystoneTileEntity() {
        super(ModTileEntities.waystone);
    }

    public WaystoneTileEntity(boolean isDummy) {
        super(ModTileEntities.waystone);
        this.isDummy = isDummy;
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.put("UUID", NBTUtil.writeUniqueId(waystoneUid));
        tagCompound.putBoolean("IsDummy", isDummy);
        if (!isDummy) {
            if (!waystoneName.equals("%RANDOM%")) {
                tagCompound.putString("WaystoneName", waystoneName);
                tagCompound.putBoolean("WasGenerated", wasGenerated);
            } else {
                tagCompound.putBoolean("WasGenerated", true);
            }

            if (owner != null) {
                tagCompound.put("Owner", NBTUtil.writeUniqueId(owner));
            }

            tagCompound.putBoolean("IsGlobal", isGlobal);
            tagCompound.putBoolean("IsMossy", isMossy);
        }
        return tagCompound;
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        waystoneUid = NBTUtil.readUniqueId(tagCompound.getCompound("UUId"));
        isDummy = tagCompound.getBoolean("IsDummy");
        if (!isDummy) {
            waystoneName = tagCompound.getString("WaystoneName");
            wasGenerated = tagCompound.getBoolean("WasGenerated");
            if (tagCompound.contains("Owner")) {
                owner = NBTUtil.readUniqueId(tagCompound.getCompound("Owner"));
            }

            isGlobal = tagCompound.getBoolean("IsGlobal");

            isMossy = tagCompound.getBoolean("IsMossy");
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        super.onDataPacket(net, pkt);
        generateNameIfNecessary();
        read(pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        generateNameIfNecessary();
        return write(new CompoundNBT());
    }

    private void generateNameIfNecessary() {
        if (waystoneName.isEmpty()) {
            waystoneName = NameGenerator.get(world).getName(world.getBiome(pos), world.rand);
        }
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
    }

    public String getWaystoneName() {
        return waystoneName;
    }

    @Override
    public boolean isOwner(PlayerEntity player) {
        return owner == null || player.getGameProfile().getId().equals(owner) || player.abilities.isCreativeMode;
    }

    @Override
    public Direction getFacing() {
        return null;
    }

    @Override
    public boolean isValid() {
        return !removed;
    }

    public boolean isMossy() {
        return isMossy;
    }

    public void setMossy(boolean mossy) {
        isMossy = mossy;
    }

    @Override
    public UUID getWaystoneUid() {
        return waystoneUid;
    }

    @Override
    public String getName() {
        return waystoneName;
    }

    @Override
    public DimensionType getDimensionType() {
        return Objects.requireNonNull(world).dimension.getType();
    }

    public boolean wasGenerated() {
        return wasGenerated;
    }

    public void setWaystoneName(String waystoneName) {
        this.waystoneName = waystoneName;

        BlockState state = world.getBlockState(pos);
        world.markAndNotifyBlock(pos, world.getChunkAt(pos), state, state, 3);
        markDirty();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
    }

    public void setOwner(PlayerEntity owner) {
        this.owner = owner.getGameProfile().getId();
        markDirty();
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean isGlobal) {
        this.isGlobal = isGlobal;
        markDirty();
    }

    public WaystoneTileEntity getParent() {
        if (isDummy) {
            TileEntity tileBelow = world.getTileEntity(pos.down());
            if (tileBelow instanceof WaystoneTileEntity) {
                return (WaystoneTileEntity) tileBelow;
            }
        }
        return this;
    }

    public void initializePlacedBy(LivingEntity placer) {
        setOwner((PlayerEntity) placer);
        wasGenerated = false;
    }

    public IWaystone getWaystone() {
        if (isDummy) {
            TileEntity tileBelow = world.getTileEntity(pos.down());
            if (tileBelow instanceof WaystoneTileEntity) {
                return (WaystoneTileEntity) tileBelow;
            }
        }

        return this;
    }


}
