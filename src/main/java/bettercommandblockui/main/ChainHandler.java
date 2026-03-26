package bettercommandblockui.main;

import bettercommandblockui.main.ui.screen.BetterCommandBlockScreen;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ChainHandler {
    private BetterCommandBlockScreen screen;
    private BlockState current, next;
    private BlockPos currentPos;
    private List<Tuple<BlockState, Direction>> prior;
    private Level world;

    public ChainHandler(BetterCommandBlockScreen screen, BlockPos pos){
        this.screen = screen;
        assert Minecraft.getInstance().player != null;
        this.world = Minecraft.getInstance().player.level();
        this.currentPos = pos;//BlockPos.ofFloored(executor.getPos());
        this.current = world.getBlockState(currentPos);
        prior = new LinkedList<>();
        scanChain();
    }

    public void scanChain(){
        Direction currentDir = current.getValue(CommandBlock.FACING);
        BlockState up = getCommandBlock(currentPos.offset(new Vec3i(0,1,0)));
        BlockState down = getCommandBlock(currentPos.offset(new Vec3i(0,-1,0)));
        BlockState north = getCommandBlock(currentPos.offset(new Vec3i(0,0,-1)));
        BlockState south = getCommandBlock(currentPos.offset(new Vec3i(0,0,1)));
        BlockState east = getCommandBlock(currentPos.offset(new Vec3i(1,0,0)));
        BlockState west = getCommandBlock(currentPos.offset(new Vec3i(-1,0,0)));

        switch(currentDir){
            case UP -> {
                if(up != null) next = up;
            }
            case DOWN -> {
                if(down != null) next = down;
            }
            case NORTH -> {
                if(north != null) next = north;
            }
            case SOUTH -> {
                if(south != null) next = south;
            }
            case EAST -> {
                if(east != null) next = east;
            }
            case WEST -> {
                if(west != null) next = west;
            }
        }

        if((up != null) && up.getValue(CommandBlock.FACING) == Direction.DOWN) prior.add(new Tuple<>(up, Direction.UP));
        if((down != null) && down.getValue(CommandBlock.FACING) == Direction.UP) prior.add(new Tuple<>(down, Direction.DOWN));
        if((north != null) && north.getValue(CommandBlock.FACING) == Direction.SOUTH) prior.add(new Tuple<>(north, Direction.NORTH));
        if((south != null) && south.getValue(CommandBlock.FACING) == Direction.NORTH) prior.add(new Tuple<>(south, Direction.SOUTH));
        if((east != null) && east.getValue(CommandBlock.FACING) == Direction.WEST) prior.add(new Tuple<>(east, Direction.EAST));
        if((west != null) && west.getValue(CommandBlock.FACING) == Direction.EAST) prior.add(new Tuple<>(west, Direction.WEST));

        if(!prior.isEmpty()){
            prior.sort(new Comparator<Tuple<BlockState, Direction>>() {
                @Override
                public int compare(Tuple<BlockState, Direction> o1, Tuple<BlockState, Direction> o2) {
                    if(o1.getA().getValue(CommandBlock.FACING) == currentDir) return -1;
                    if(o2.getA().getValue(CommandBlock.FACING) == currentDir) return 1;
                    return 0;
                }
            });
        }

        /*System.out.println("Next: (" + currentDir + ") " + (next == null ? "none" : next));
        System.out.println("Prior:");
        for(Pair<BlockState,Direction> state : prior){
            System.out.println(state.getRight() + ": " + state.getLeft());
        }*/
    }

    public boolean isInChain(){
        return (next != null && next.getBlock().equals(BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("minecraft","chain_command_block"))))
                || (!prior.isEmpty() && current.getBlock().equals(BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("minecraft","chain_command_block"))));
    }

    public BlockState getNext(){
        return next;
    }

    public List<Tuple<BlockState, Direction>> getPrior(){
        return prior;
    }

    private BlockState getCommandBlock(BlockPos pos){
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CommandBlockEntity) {
            return world.getBlockState(pos);
        }
        return null;
    }
}
