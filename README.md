#PluginInject
PluginInject is the fourth in a series of attempts I have made to provide easy dependency injection
into Bukkit plugins.  I consider it to be the most simple and successful so far.  It is built around
HK2, the reference implementation of JSR 330(Dependency Injection for Java).  HK2 was chosen as it
provides scanning for services at compile time, lifecycle support, and full support for providing
extensions to the injection mechanisms.

This project intentionally avoids dealing with injections between plugins.  Interactions between
plugins that may be disabled or enabled at will falls much more within the realm of OSGI and
introduces complexity that is detrimental toward providing dependency injection for an isolated
project.  PluginInject does however provide named injections of all plugins on the server(described below),
and any plugin that extends ```InjectedPlugin``` will expose its ServiceLocator in a consistent
manner.

API docs for HK2 can be found [here](https://hk2.java.net/2.5.0-b36/apidocs/index.html).  Not all functionality
in the API docs is included with PluginInject, as PluginInject only includes hk2-locator. The [main site](https://hk2.java.net/2.5.0-b36/index.html) for HK2
includes tutorials and detailed examples on a wide array of functionality. HK2 functionality generally can be
modified by implementing specific interfaces, meaning a plugin is capable of providing almost any new injection
behavior. Because plugin-provided binders are applied before the configuration is loaded, it's even possible
to completely replace the loading system or change where the descriptors are read from.
## Using PluginInject
Any plugin using PluginInject should list it as a dependency in its plugin.yml such that
PluginInject is loaded before it.

The general lifecycle of a plugin using PluginInject on load is as follows:

1. Server ServiceLocator is instantiated.
1. Plugin specific ServiceLocator is instantiated.
1. Plugin provided bindings and built-in bindings are applied to the ServiceLocator
1. PluginInject pre-load features are applied
1. Plugin specific ServiceLocator loads services using the DynamicConfigurationService.
1. PluginInject post-load features are applied.
1. All Modules are eagerly instantiated.
1. InjectedPlugin instance is injected into.
1. PostConstruct is invoked on the InjectedPlugin instance.

The general lifecycle of a plugin using PluginInject on shutdown is as follows:

1. PreDestroy is called on InjectedPlugin instance.
1. Plugin specific ServiceLocator is shutdown, destroying all loaded services.

### Writing Your JavaPlugin Class
Plugins wishing to utilize PluginInject should extend ```com.natemortensen.plugininject.InjectedPlugin```,
which will automatically handle loading the inhabitants files and the creation of the ServiceLocator.  Plugins
that extend InjectedPlugin will be unable to use ```onEnable``` or ```onDisable``` for initialization and cleanup,
and should instead use methods marked with ```javax.annotation.PostConstruct``` or ```javax.annotation.PreDestroy```.

If a plugin is unable to extend PluginInject, they can also take advantage of injection using ```com.natemortensen.plugininject.Injection```.
  Check out the source of InjectedPlugin for an example of how to do that.
### Structuring Your Plugin
PluginInject is structured around Modules, which are conceptually small pieces of your plugin's logic.  In practice,
however, they can be pretty much anything.  They are simply the demand applied to the dependency injection system
that triggers action.  They are eagerly instantiated after the plugin has been enabled.

Any Service that is initialized and an instance of ```org.bukkit.Listener``` is automatically registered. This behavior
can be disabled for an individual service by annotating it with ```com.natemortensen.plugininject.listener.DoNotRegister```.
To change this behavior on a larger scale, a plugin can provide a binding for ```com.natemortensen.plugininject.listener.ListenerFilter```
and filter out additional Listeners.

### Generating Bindings at Compile Time
Bindings are generated at compile time by depending on PluginInject, as it includes HK2's Metadata generator,
an annotation processor which scans for ```org.jvnet.hk2.annotations``` and creates descriptors of all services.
Anything that is an interface meant to be provided by injection should be annotated with ```@Contract```. Anything
that implements a contract and is meant to be automatically provided should be annotated with ```@Service```. As such,
all modules should be annotated with ```@Service``` and implement ```Module```.

### Providing Bindings at Run time
PluginInject does not support any sort of automatic scanning at run time. Instead, plugins can override ```InjectedPlugin#getBinders```
and provide Binders which explicitly define the bindings.  For convenience it's recommended to extend ```org.glassfish.hk2.utilities.binding.AbstractBinder```
when implementing a Binder. Note that AbstractBinder's methods are in the form ```bind(x).to(y)``` where x is an instance of y, whereas
Guice's methods are in the form ```bind(y).to(x)```. This is so that an individual service can be bound to multiple contracts.
HK2 does not bind a service to its own type either, that must be done explicitly.  In HK2 the default scope for a class-based binding is
PerLookup, and is most likely not what you want.

### Built-in Bindings
Server-wide:
* Server
* PluginManager
* ScoreboardManager
* ItemFactory
* Messenger
* BukkitScheduler
* Every plugin, bound to its own type, JavaPlugin, and Plugin, named the plugin name

Plugin-specific:
* The plugin instance, bound to its own type, JavaPlugin, and Plugin.  Because it is provided
in the plugin-specific locator, it will injected in place of the other plugin bindings unless ```@Named``` is used
to specify a specific plugin.
* Logger

### Example
An example of a plugin using PluginInject can be found in the example directory.
## Future Plans
PluginInject will be developed as I have time and as I find it useful.

* 0.1 Release - Configuration System


