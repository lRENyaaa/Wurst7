package net.wurstclient.commands;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.MathUtils;

public class ClickCmd extends Command {

    public ClickCmd()
    {
        super("click",
                "Click a block.",
                ".click <x> <y> <z>");
    }

    @Override
    public void call(String[] args) throws CmdException
    {
        if(args.length != 3)
            throw new CmdSyntaxError();

        BlockPos pos = argsToXyzPos(args[0], args[1], args[2]);

        ClientPlayNetworkHandler conn = MC.getNetworkHandler();
        if (conn == null)
            return;
        PlayerActionC2SPacket packet = new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                pos, Direction.UP);
        conn.sendPacket(packet);
    }

    private BlockPos argsToXyzPos(String... xyz) throws CmdSyntaxError
    {
        BlockPos playerPos = BlockPos.ofFloored(MC.player.getPos());
        int[] player = {playerPos.getX(), playerPos.getY(), playerPos.getZ()};
        int[] pos = new int[3];

        for(int i = 0; i < 3; i++)
            if(MathUtils.isInteger(xyz[i]))
                pos[i] = Integer.parseInt(xyz[i]);
            else if(xyz[i].equals("~"))
                pos[i] = player[i];
            else if(xyz[i].startsWith("~")
                    && MathUtils.isInteger(xyz[i].substring(1)))
                pos[i] = player[i] + Integer.parseInt(xyz[i].substring(1));
            else
                throw new CmdSyntaxError("Invalid coordinates.");

        return new BlockPos(pos[0], pos[1], pos[2]);
    }
}
