package vectorientation.simpleLibs.simpleRay;
/*
 * Copyright (c) 2021 magistermaks
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class SimpleRay {

    private Vec3d origin;
    private final Vec3d reciDir;
    private final int signX;
    private final int signY;
    private final int signZ;

    /**
     * Creates new ray. example usage:
     * Ray ray = new Ray( player.getCameraPosVec(1.0F), player.getRotationVec(1.0F) );
     *
     * @param origin - Camera position
     * @param dir - Camera facing direction
     */
    public SimpleRay(Vec3d origin, Vec3d dir ) {
        this.origin = origin;

        this.reciDir = new Vec3d( 1.0 / dir.x, 1.0 / dir.y, 1.0 / dir.z );
        this.signX = (reciDir.x < 0.0) ? 1 : 0;
        this.signY = (reciDir.y < 0.0) ? 1 : 0;
        this.signZ = (reciDir.z < 0.0) ? 1 : 0;
    }

    /**
     * Offsets the ray in 3D space by the inverse of the given position (of the box),
     * so that the Ray can intersect with a Box (which is always at word origin). Example usage:
     * ray.offset( blockPosOfTheBox );
     *
     * @param pos - Offset position
     */
    public void offset( BlockPos pos ) {
        offset( new Vec3d( pos.getX(), pos.getY(), pos.getZ() ) );
    }

    /**
     * Same as <code>Ray#offset(BlockPos)</code> but uses a Vec3d not BlockPos
     *
     * @param vector - Offset vector
     * @see SimpleRay#offset(BlockPos)
     */
    public void offset( Vec3d vector ) {
        origin = origin.subtract( vector );
    }

    /**
     * Returns current offset position. Example usage:
     * BlockPos pos = ray.getOffset();
     */
    public Vec3d getOffset() {
        return origin;
    }

    /**
     * Checks is the ray intersects with a given Box,
     * remember to specify box's position in the world using `offset()`. Example usage:
     * boolean flag = ray.intersects( BOX );
     *
     * @param box - box to check intersection with
     * @see SimpleRay#offset(BlockPos)
     */
    public boolean intersects( Box box ) {
        Vec3d[] bounds = new Vec3d[2];
        bounds[0] = new Vec3d( box.minX, box.minY, box.minZ );
        bounds[1] = new Vec3d( box.maxX, box.maxY, box.maxZ );

        double txMin, txMax, tyMin, tyMax, tzMin, tzMax;

        txMin = ( bounds[    signX].x - origin.x ) * reciDir.x;
        txMax = ( bounds[1 - signX].x - origin.x ) * reciDir.x;
        tyMin = ( bounds[    signY].y - origin.y ) * reciDir.y;
        tyMax = ( bounds[1 - signY].y - origin.y ) * reciDir.y;

        if( (txMin > tyMax) || (tyMin > txMax) ) return false;

        txMin = Math.max( txMin, tyMin );
        txMax = Math.min( txMax, tyMax );

        tzMin = ( bounds[    signZ].z - origin.z ) * reciDir.z;
        tzMax = ( bounds[1 - signZ].z - origin.z ) * reciDir.z;

        return (txMin <= tzMax) && (tzMin <= txMax);
    }

}
