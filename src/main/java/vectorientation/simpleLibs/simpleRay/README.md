Simple to use, single file, ray utility.

```Java
  // Create new ray tracer from 'entity''s eyes
  SimpleRay ray = new SimpleRay( entity.getCameraPosVec(1.0F), entity.getRotationVec(1.0F) );

  // Check if ray intersects a 'box', at 'blockPos'
  ray.offset( blockPos );
  if( ray.intersects( box ) ) {
    // Ray intersected the given box
  }
```
If you have any more questions see JavaDoc comments in the source code.

