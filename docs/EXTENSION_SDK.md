# Extension SDK Design

GatotAuth provides a dedicated SDK for writing extensions (e.g., custom web dashboards, MFA modules, social media registration steps).

## 1. Extension Lifecycle
An extension is a separate JAR file loaded from the `/extensions` folder. It implements the `GatotAuthExtension` interface.

```java
public interface GatotAuthExtension {
    void onEnable(ExtensionContext context);
    void onDisable();
}
```

The `ExtensionContext` provides access to configuration options, logging streams, and the `GatotAuthApi` instance.

## 2. Dynamic Configurations
Extensions can load custom configuration files (TOML format recommended for readability) using a typed binder interface:

```java
public interface ExtensionContext {
    <T> T getConfig(Class<T> configClass);
}
```

## 3. Alternative Extensibility Mechanisms
- *Alternative 1: Bukkit Plugin Dependencies (`depend` in plugin.yml)*: Custom additions are separate Bukkit/Velocity plugins that require GatotAuth load-order constraints.
- *Trade-off*: Complex setup, breaks if the server architecture moves from Spigot to Folia/Velocity.
- *Recommended: Unified Native Extension Folder*: Extensions are loaded directly by GatotAuth's core class loader. They remain platform-agnostic, meaning the same MFA extension works unmodified across Velocity proxy and Paper single-server platforms.
