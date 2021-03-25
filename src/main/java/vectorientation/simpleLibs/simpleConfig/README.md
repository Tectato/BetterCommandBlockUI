Simple to use, single file, gloabal configuration lib for Fabric mods.

```Java
  // Load config 'config.properties', if it isn't present create one 
  // using the lambda specified as the provider.
  SimpleConfig CONFIG = SimpleConfig.of( "config" ).provider( this::provider ).request();

  // Custom config provider, returnes the default config content
  // if the custom provider is not specified SimpleConfig will create an empty file instead
  private String provider( String filename ) {
    return "#My default config content\n";
  }

  // And that's it! Now you can request values from the config:
  public final String SOME_STRING = CONFIG.getOrDefault( "key.of.the.value1", "default value" );
  public final int SOME_INTEGER = CONFIG.getOrDefault( "key.of.the.value2", 42 );
  public final bool SOME_BOOL = CONFIG.getOrDefault( "key.of.the.value3", false );
```
The config consists of key-value pairs separated with `=`, if `#` is used as the first char in line, that line will be considered a comment.
If you have any more questions see JavaDoc comments in the source code.
